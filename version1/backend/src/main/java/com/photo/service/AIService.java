package com.photo.service;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * AI服务类，使用Deep Java Library (DJL) 进行图像内容识别和智能标签生成
 * 集成了ResNet50模型进行图像内容分析
 */
@Slf4j
@Service
public class AIService {

    @Value("${ai.model.max-retries:3}")
    private int maxRetries;

    @Value("${ai.model.retry-delay-seconds:10}")
    private int retryDelaySeconds;

    private ZooModel<Image, Classifications> model;
    private Predictor<Image, Classifications> predictor;
    private boolean modelInitialized = false;

    /**
     * 应用启动后尝试初始化模型，但只记录状态，不阻塞启动
     * 改进版本：在新线程中尝试初始化，带有进度跟踪和自动重试
     */
    @PostConstruct
    public void initModelOnStartup() {
        log.info("🚀 AIService启动，开始异步初始化AI模型...");
        // 在新线程中尝试初始化，避免阻塞应用启动
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 等待5秒让应用完全启动
                log.info("🔧 开始尝试初始化AI模型 (ResNet50)...");

                // 尝试初始化模型
                boolean success = ensureModelInitialized();

                if (success) {
                    log.info("✅ AI模型成功初始化，ResNet50已加载就绪");
                    log.info("📊 模型信息: {}", model != null ? model.getModelPath() : "Unknown");
                    log.info("🎯 预测器状态: {}", predictor != null ? "Available" : "Not available");
                } else {
                    log.warn("⚠️ AI模型初始化失败");
                    log.info("🔄 AI服务将使用模拟模式进行图像分析");
                    log.info("💡 可能的解决方案:");
                    log.info("   1. 检查网络连接 - 模型需要从DJL仓库下载");
                    log.info("   2. 验证DJL依赖 (pom.xml)");
                    log.info("   3. 检查磁盘空间，模型缓存路径: C:/Users/Simo/.djl/cache");
                    log.info("   4. 确保PyTorch本地库可用");
                    log.info("   5. 重启应用或手动调用API /api/ai/status 触发重试");
                }
            } catch (Exception e) {
                log.error("💥 AI模型初始化过程中出现错误: {}", e.getMessage());
                log.error("🔍 详细错误:", e);
            }
        }).start();
    }

    /**
     * 确保AI模型已初始化
     * @return true如果模型可用，false如果模型初始化失败
     */
    private boolean ensureModelInitialized() {
        if (modelInitialized && predictor != null) {
            log.debug("AI model already initialized");
            return true;
        }

        int attempts = 0;
        while (attempts < maxRetries) {
            attempts++;
            try {
                log.info("Initializing AI model (attempt {}/{})...", attempts, maxRetries);
                initializeModel();
                modelInitialized = true;
                log.info("✅ AI model initialized successfully on attempt {}/{}", attempts, maxRetries);
                return true;
            } catch (Exception e) {
                log.error("❌ Failed to initialize AI model on attempt {}/{}: {}", attempts, maxRetries, e.getMessage());

                if (attempts < maxRetries) {
                    log.info("⏳ Waiting {} seconds before retry...", retryDelaySeconds);
                    try {
                        Thread.sleep(retryDelaySeconds * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Model initialization retry interrupted");
                        break;
                    }
                } else {
                    log.error("💥 All {} attempts to initialize AI model failed", maxRetries);
                    log.info("AI service will use simulation mode for image analysis");
                    modelInitialized = false;
                }
            }
        }
        return false;
    }

    /**
     * 使用ResNet模型分析图像内容并返回智能标签
     * 现在只返回1-2个最相关的标签，避免过多无关标签
     *
     * @param imageFile 要分析的图像文件
     * @return 图像内容的智能标签列表（通常1-2个标签）
     */
    public List<String> analyzeImageContent(File imageFile) {
        log.info("🚀 Starting AI analysis for file: {} (size: {} bytes)",
                 imageFile.getName(), imageFile.length());

        // 确保模型已初始化
        if (!ensureModelInitialized()) {
            log.warn("⚠️ AI model not available, using SIMULATION mode");
            List<String> simulatedTags = simulateAIAnalysis(imageFile.getName());
            log.info("🎯 Simulation mode returned {} tags: {}", simulatedTags.size(), simulatedTags);
            return simulatedTags;
        }

        try {
            log.info("🔍 Analyzing image content for file: {}", imageFile.getName());

            // 加载图像
            Image image = ImageFactory.getInstance().fromFile(imageFile.toPath());
            log.debug("📐 Image loaded successfully, dimensions: {}x{}",
                      image.getWidth(), image.getHeight());

            // 使用模型进行预测
            log.debug("🤖 Running model prediction...");
            Classifications classifications = predictor.predict(image);

            // 获取前5个预测结果以便完整记录
            List<Classifications.Classification> topPredictions = classifications.topK(5);
            log.info("📊 Model returned {} total predictions", topPredictions.size());

            // 记录所有预测结果，即使置信度低
            for (int i = 0; i < topPredictions.size(); i++) {
                Classifications.Classification prediction = topPredictions.get(i);
                double probability = prediction.getProbability();
                String className = prediction.getClassName();
                int classId = getClassIdFromClassName(className);
                log.info("  {}. {} (ID: {}) - {:.2f}% confidence",
                         i + 1, className, classId, probability * 100);
            }

            // 使用新的标签选择逻辑 - 基于类名直接匹配
            List<String> selectedLabels = new ArrayList<>();

            for (Classifications.Classification prediction : topPredictions) {
                double probability = prediction.getProbability();
                String className = prediction.getClassName().toLowerCase();

                log.debug("Processing prediction: {} with confidence {:.2f}%", className, probability * 100);

                // 只考虑置信度高于30%的预测（降低阈值以获得更多匹配）
                if (probability > 0.3) {
                    String matchedTag = matchClassNameToTag(className);
                    if (matchedTag != null && !selectedLabels.contains(matchedTag)) {
                        selectedLabels.add(matchedTag);
                        log.debug("✅ Matched '{}' to tag: {}", className, matchedTag);
                    }
                }

                // 限制最多2个标签
                if (selectedLabels.size() >= 2) {
                    log.debug("🎯 Reached maximum of 2 labels, stopping analysis");
                    break;
                }
            }

            // 如果未找到合适的标签，使用智能回退
            if (selectedLabels.isEmpty()) {
                log.info("🤔 No suitable predictions found (confidence > 30%), using smart fallback");
                for (Classifications.Classification prediction : topPredictions.subList(0, Math.min(2, topPredictions.size()))) {
                    String className = prediction.getClassName().toLowerCase();
                    // 检查是否为风景/自然相关
                    if (className.contains("mountain") || className.contains("valley") ||
                        className.contains("lake") || className.contains("sea") ||
                        className.contains("river") || className.contains("forest") ||
                        className.contains("sky") || className.contains("sunset") ||
                        className.contains("geyser") || className.contains("cliff") ||
                        className.contains("shore") || className.contains("coast")) {
                        selectedLabels.add("风景");
                        break;
                    } else if (className.contains("tree") || className.contains("plant") ||
                               className.contains("flower") || className.contains("nature")) {
                        selectedLabels.add("自然");
                        break;
                    }
                }
                // 默认使用风景
                if (selectedLabels.isEmpty()) {
                    selectedLabels.add("风景");
                }
            }

            log.info("✅ AI analysis completed: {} tags generated: {}", selectedLabels.size(), selectedLabels);
            return selectedLabels;

        } catch (Exception e) {
            log.error("💥 Error analyzing image with AI model: {}", e.getMessage(), e);
            log.info("🔄 Falling back to simulation mode");
            List<String> simulatedTags = simulateAIAnalysis(imageFile.getName());
            log.info("🎯 Fallback simulation returned {} tags: {}", simulatedTags.size(), simulatedTags);
            return simulatedTags;
        }
    }

    // ImageNet WordNet ID到类别索引(0-999)的映射
    private static final Map<String, Integer> WORDNET_TO_INDEX = new HashMap<>();
    static {
        // 动物类 (0-11)
        WORDNET_TO_INDEX.put("01440764", 0);   // tench
        WORDNET_TO_INDEX.put("01443537", 1);   // goldfish
        WORDNET_TO_INDEX.put("01484850", 2);   // great white shark
        WORDNET_TO_INDEX.put("01491361", 3);   // tiger shark
        WORDNET_TO_INDEX.put("01494475", 4);   // hammerhead
        WORDNET_TO_INDEX.put("01496331", 5);   // electric ray
        WORDNET_TO_INDEX.put("01498041", 6);   // stingray
        // ... (这里应该包含完整的1000个映射)
        // 为了简化，我们使用另一种方式：直接使用synset.txt中的索引
    }

    /**
     * 从类名中提取类别索引(0-999)
     * 使用ResNet模型的synset.txt索引，而不是WordNet ID
     */
    private int getClassIdFromClassName(String className) {
        try {
            log.debug("Parsing class name: {}", className);

            // 方式1: 直接解析数字索引 (如果类名就是数字)
            try {
                int directIndex = Integer.parseInt(className.trim());
                if (directIndex >= 0 && directIndex < 1000) {
                    log.debug("Direct index parsed: {}", directIndex);
                    return directIndex;
                }
            } catch (NumberFormatException ignored) {
            }

            // 方式2: 如果类名格式为 "n01440764" 或 "n01440764_tench"
            // 我们需要使用类名的顺序位置（在synset.txt中的行号）
            if (className.startsWith("n")) {
                // 提取WordNet ID
                StringBuilder numericPart = new StringBuilder();
                for (int i = 1; i < className.length(); i++) {
                    char c = className.charAt(i);
                    if (Character.isDigit(c)) {
                        numericPart.append(c);
                    } else if (numericPart.length() > 0) {
                        break;
                    }
                }

                if (numericPart.length() > 0) {
                    // 使用WordNet ID的哈希映射到0-999范围
                    // 这是一个简化的方法，实际应该使用完整的synset映射
                    int wordnetId = Integer.parseInt(numericPart.toString());

                    // 使用预定义的映射表查找
                    Integer mappedIndex = WORDNET_TO_INDEX.get(numericPart.toString());
                    if (mappedIndex != null) {
                        log.debug("Mapped WordNet ID {} to index {}", wordnetId, mappedIndex);
                        return mappedIndex;
                    }

                    // 如果没有精确映射，使用哈希映射
                    // 注意：这只是一个近似，可能不准确
                    int hashIndex = Math.abs(wordnetId) % 1000;
                    log.debug("Using hash mapping for WordNet ID {} -> index {}", wordnetId, hashIndex);
                    return hashIndex;
                }
            }

            // 方式3: 通过类名中的关键词匹配
            String lowerName = className.toLowerCase();
            for (Map.Entry<String, Integer> entry : getKeywordToIndexMap().entrySet()) {
                if (lowerName.contains(entry.getKey())) {
                    log.debug("Keyword match: '{}' found in '{}', using index {}",
                              entry.getKey(), className, entry.getValue());
                    return entry.getValue();
                }
            }

            // 最后的fallback
            int fallbackId = Math.abs(className.hashCode()) % 1000;
            log.debug("Using fallback class ID: {} for class name: {}", fallbackId, className);
            return fallbackId;

        } catch (Exception e) {
            log.warn("Failed to parse class ID from class name: {}, using fallback", className);
            return Math.abs(className.hashCode()) % 1000;
        }
    }

    /**
     * 直接将类名匹配到中文标签
     * 基于类名中的关键词进行匹配
     */
    private String matchClassNameToTag(String className) {
        String lowerName = className.toLowerCase();

        // 动物类 - 狗
        if (lowerName.contains("chihuahua") || lowerName.contains("spaniel") ||
            lowerName.contains("dog") || lowerName.contains("retriever") ||
            lowerName.contains("terrier") || lowerName.contains("shepherd") ||
            lowerName.contains("poodle") || lowerName.contains("husky") ||
            lowerName.contains("beagle") || lowerName.contains("pug") ||
            lowerName.contains("pomeranian") || lowerName.contains("collie") ||
            lowerName.contains("malamute") || lowerName.contains("pinscher") ||
            lowerName.contains("schnauzer") || lowerName.contains("setter") ||
            lowerName.contains("hound") || lowerName.contains("mastiff") ||
            lowerName.contains("shiba") || lowerName.contains("akita")) {
            return "动物";
        }

        // 动物类 - 猫科
        if (lowerName.contains("tabby") || lowerName.contains("cat") ||
            lowerName.contains("persian") || lowerName.contains("siamese") ||
            lowerName.contains("leopard") || lowerName.contains("lion") ||
            lowerName.contains("tiger") || lowerName.contains("cheetah") ||
            lowerName.contains("lynx") || lowerName.contains("jaguar") ||
            lowerName.contains("cougar")) {
            return "动物";
        }

        // 鸟类
        if (lowerName.contains("bird") || lowerName.contains("cock") ||
            lowerName.contains("hen") || lowerName.contains("ostrich") ||
            lowerName.contains("finch") || lowerName.contains("magpie") ||
            lowerName.contains("chickadee") || lowerName.contains("eagle") ||
            lowerName.contains("owl") || lowerName.contains("sparrow") ||
            lowerName.contains("parrot") || lowerName.contains("penguin") ||
            lowerName.contains("stork") || lowerName.contains("flamingo") ||
            lowerName.contains("heron") || lowerName.contains("crane") ||
            lowerName.contains("pelican") || lowerName.contains("albatross")) {
            return "动物";
        }

        // 海洋生物
        if (lowerName.contains("fish") || lowerName.contains("shark") ||
            lowerName.contains("whale") || lowerName.contains("dolphin") ||
            lowerName.contains("seal") || lowerName.contains("sea lion") ||
            lowerName.contains("starfish") || lowerName.contains("lobster") ||
            lowerName.contains("crab") || lowerName.contains("ray") ||
            lowerName.contains("goldfish")) {
            return "动物";
        }

        // 其他动物
        if (lowerName.contains("bear") || lowerName.contains("wolf") ||
            lowerName.contains("fox") || lowerName.contains("deer") ||
            lowerName.contains("rabbit") || lowerName.contains("squirrel") ||
            lowerName.contains("monkey") || lowerName.contains("ape") ||
            lowerName.contains("gorilla") || lowerName.contains("elephant") ||
            lowerName.contains("zebra") || lowerName.contains("giraffe") ||
            lowerName.contains("horse") || lowerName.contains("cow") ||
            lowerName.contains("pig") || lowerName.contains("sheep") ||
            lowerName.contains("goat") || lowerName.contains("mouse") ||
            lowerName.contains("hamster") || lowerName.contains("turtle") ||
            lowerName.contains("snake") || lowerName.contains("lizard") ||
            lowerName.contains("frog") || lowerName.contains("butterfly") ||
            lowerName.contains("bee") || lowerName.contains("ant") ||
            lowerName.contains("spider") || lowerName.contains("snail")) {
            return "动物";
        }

        // 风景类
        if (lowerName.contains("mountain") || lowerName.contains("valley") ||
            lowerName.contains("alp") || lowerName.contains("cliff") ||
            lowerName.contains("geyser") || lowerName.contains("lakeside") ||
            lowerName.contains("seashore") || lowerName.contains("volcano") ||
            lowerName.contains("coral reef") || lowerName.contains("sandbar") ||
            lowerName.contains("promontory") || lowerName.contains("beach") ||
            lowerName.contains("shore") || lowerName.contains("coast") ||
            lowerName.contains("lake") || lowerName.contains("river") ||
            lowerName.contains("ocean") || lowerName.contains("waterfall") ||
            lowerName.contains("canyon") || lowerName.contains("desert") ||
            lowerName.contains("glacier") || lowerName.contains("island")) {
            return "风景";
        }

        // 自然类
        if (lowerName.contains("tree") || lowerName.contains("flower") ||
            lowerName.contains("plant") || lowerName.contains("forest") ||
            lowerName.contains("garden") || lowerName.contains("park") ||
            lowerName.contains("grass") || lowerName.contains("leaf") ||
            lowerName.contains("mushroom") || lowerName.contains("fern") ||
            lowerName.contains("bamboo") || lowerName.contains("cactus")) {
            return "自然";
        }

        // 美食类
        if (lowerName.contains("food") || lowerName.contains("meal") ||
            lowerName.contains("dish") || lowerName.contains("pizza") ||
            lowerName.contains("burger") || lowerName.contains("sandwich") ||
            lowerName.contains("pasta") || lowerName.contains("salad") ||
            lowerName.contains("soup") || lowerName.contains("cake") ||
            lowerName.contains("bread") || lowerName.contains("bagel") ||
            lowerName.contains("pretzel") || lowerName.contains("hotdog") ||
            lowerName.contains("cheese") || lowerName.contains("fruit") ||
            lowerName.contains("vegetable") || lowerName.contains("broccoli") ||
            lowerName.contains("cabbage") || lowerName.contains("carrot") ||
            lowerName.contains("potato") || lowerName.contains("tomato") ||
            lowerName.contains("apple") || lowerName.contains("orange") ||
            lowerName.contains("banana") || lowerName.contains("strawberry") ||
            lowerName.contains("chocolate") || lowerName.contains("coffee") ||
            lowerName.contains("wine") || lowerName.contains("ice cream")) {
            return "美食";
        }

        // 人像类
        if (lowerName.contains("person") || lowerName.contains("people") ||
            lowerName.contains("portrait") || lowerName.contains("face") ||
            lowerName.contains("man") || lowerName.contains("woman") ||
            lowerName.contains("child") || lowerName.contains("baby") ||
            lowerName.contains("suit") || lowerName.contains("dress") ||
            lowerName.contains("uniform") || lowerName.contains("gown") ||
            lowerName.contains("academic gown") || lowerName.contains("apron") ||
            lowerName.contains("swimsuit") || lowerName.contains("miniskirt") ||
            lowerName.contains("cloak") || lowerName.contains("poncho")) {
            return "人像";
        }

        // 交通工具类
        if (lowerName.contains("car") || lowerName.contains("vehicle") ||
            lowerName.contains("automobile") || lowerName.contains("jeep") ||
            lowerName.contains("limousine") || lowerName.contains("minivan") ||
            lowerName.contains("sports car") || lowerName.contains("convertible") ||
            lowerName.contains("cab") || lowerName.contains("taxi") ||
            lowerName.contains("bus") || lowerName.contains("truck") ||
            lowerName.contains("ambulance") || lowerName.contains("fire engine") ||
            lowerName.contains("police") || lowerName.contains("school bus") ||
            lowerName.contains("bicycle") || lowerName.contains("bike") ||
            lowerName.contains("motorcycle") || lowerName.contains("scooter") ||
            lowerName.contains("airplane") || lowerName.contains("aircraft") ||
            lowerName.contains("airliner") || lowerName.contains("plane") ||
            lowerName.contains("helicopter") || lowerName.contains("train") ||
            lowerName.contains("locomotive") || lowerName.contains("boat") ||
            lowerName.contains("ship") || lowerName.contains("submarine") ||
            lowerName.contains("yacht") || lowerName.contains("sailboat") ||
            lowerName.contains("canoe") || lowerName.contains("speedboat")) {
            return "交通工具";
        }

        // 建筑类
        if (lowerName.contains("building") || lowerName.contains("architecture") ||
            lowerName.contains("house") || lowerName.contains("church") ||
            lowerName.contains("mosque") || lowerName.contains("palace") ||
            lowerName.contains("library") || lowerName.contains("castle") ||
            lowerName.contains("bridge") || lowerName.contains("tower") ||
            lowerName.contains("dam") || lowerName.contains("barn") ||
            lowerName.contains("greenhouse") || lowerName.contains("monastery") ||
            lowerName.contains("hut") || lowerName.contains("tent") ||
            lowerName.contains("patio") || lowerName.contains("porch") ||
            lowerName.contains("restaurant") || lowerName.contains("shop") ||
            lowerName.contains("store") || lowerName.contains("market") ||
            lowerName.contains("stadium") || lowerName.contains("theater") ||
            lowerName.contains("cinema")) {
            return "建筑";
        }

        // 没有匹配到
        return null;
    }

    /**
     * 关键词到ImageNet索引的映射表
     * 这些索引对应ImageNetToCustomLabelMapper中定义的类别
     */
    private Map<String, Integer> getKeywordToIndexMap() {
        Map<String, Integer> map = new HashMap<>();
        // 动物 - 狗 (151-268)
        map.put("chihuahua", 151);
        map.put("japanese spaniel", 152);
        map.put("maltese dog", 153);
        map.put("pekinese", 154);
        map.put("shih-tzu", 155);
        map.put("papillon", 157);
        map.put("toy terrier", 158);
        map.put("rhodesian ridgeback", 159);
        map.put("afghan hound", 160);
        map.put("golden retriever", 207);
        map.put("labrador", 208);
        map.put("german shepherd", 235);
        map.put("poodle", 265);

        // 动物 - 猫 (281-293)
        map.put("tabby", 281);
        map.put("tiger cat", 282);
        map.put("persian cat", 283);
        map.put("siamese cat", 284);
        map.put("egyptian cat", 285);
        map.put("cougar", 286);
        map.put("lynx", 287);
        map.put("leopard", 288);
        map.put("snow leopard", 289);
        map.put("jaguar", 290);
        map.put("lion", 291);
        map.put("tiger", 292);
        map.put("cheetah", 293);

        // 鸟类 (8-24)
        map.put("cock", 8);
        map.put("hen", 9);
        map.put("ostrich", 10);
        map.put("brambling", 11);
        map.put("goldfinch", 12);
        map.put("magpie", 18);
        map.put("chickadee", 19);

        // 海洋生物 (0-7)
        map.put("tench", 0);
        map.put("goldfish", 1);
        map.put("great white shark", 2);

        // 风景 (970-989)
        map.put("mountain", 970);
        map.put("valley", 978);
        map.put("alp", 979);
        map.put("cliff", 980);
        map.put("coral reef", 981);
        map.put("geyser", 982);
        map.put("lakeside", 983);
        map.put("seashore", 984);
        map.put("volcano", 987);

        // 美食 (907-950)
        map.put("carbonara", 907);
        map.put("bagel", 924);
        map.put("pretzel", 925);
        map.put("cheeseburger", 926);
        map.put("hotdog", 927);
        map.put("broccoli", 930);
        map.put("pizza", 956);

        // 人像 (440-442, 850-852)
        map.put("academic gown", 440);
        map.put("suit", 850);
        map.put("t-shirt", 852);

        // 建筑 (483-495)
        map.put("church", 483);
        map.put("mosque", 484);
        map.put("palace", 493);
        map.put("library", 495);

        // 交通工具 (609-645)
        map.put("jeep", 609);
        map.put("limousine", 610);
        map.put("sports car", 614);
        map.put("bicycle", 629);
        map.put("airliner", 640);

        return map;
    }

    /**
     * 模拟AI分析结果（回退模式）
     * 基于文件名关键词智能匹配标签
     */
    private List<String> simulateAIAnalysis(String fileName) {
        List<String> simulatedLabels = new ArrayList<>();
        String lowerCaseName = fileName.toLowerCase();

        // 动物类检测
        if (lowerCaseName.contains("dog") || lowerCaseName.contains("dog") ||
            lowerCaseName.contains("puppy") || lowerCaseName.contains("犬")) {
            simulatedLabels.add("动物");
        } else if (lowerCaseName.contains("cat") || lowerCaseName.contains("kitty") ||
                   lowerCaseName.contains("kitten") || lowerCaseName.contains("猫") ||
                   lowerCaseName.contains("咪")) {
            simulatedLabels.add("动物");
        } else if (lowerCaseName.contains("bird") || lowerCaseName.contains("鸟") ||
                   lowerCaseName.contains("chicken") || lowerCaseName.contains("duck")) {
            simulatedLabels.add("动物");
        } else if (lowerCaseName.contains("fish") || lowerCaseName.contains("鱼") ||
                   lowerCaseName.contains("shark") || lowerCaseName.contains("海豚")) {
            simulatedLabels.add("动物");
        }

        // 风景类检测
        else if (lowerCaseName.contains("landscape") || lowerCaseName.contains("scenery") ||
                 lowerCaseName.contains("mountain") || lowerCaseName.contains("山") ||
                 lowerCaseName.contains("sunset") || lowerCaseName.contains("日出") ||
                 lowerCaseName.contains("sunrise") || lowerCaseName.contains("日落") ||
                 lowerCaseName.contains("lake") || lowerCaseName.contains("海洋") ||
                 lowerCaseName.contains("sea") || lowerCaseName.contains("river") ||
                 lowerCaseName.contains("水") || lowerCaseName.contains("sky") ||
                 lowerCaseName.contains("cloud")) {
            simulatedLabels.add("风景");
        }

        // 美食类检测
        else if (lowerCaseName.contains("food") || lowerCaseName.contains("meal") ||
                 lowerCaseName.contains("dish") || lowerCaseName.contains("美食") ||
                 lowerCaseName.contains("吃") || lowerCaseName.contains("水果") ||
                 lowerCaseName.contains("fruit") || lowerCaseName.contains("蔬菜") ||
                 lowerCaseName.contains("蛋糕") || lowerCaseName.contains("cake") ||
                 lowerCaseName.contains("pizza") || lowerCaseName.contains("汉堡")) {
            simulatedLabels.add("美食");
        }

        // 人像类检测
        else if (lowerCaseName.contains("portrait") || lowerCaseName.contains("people") ||
                 lowerCaseName.contains("face") || lowerCaseName.contains("person") ||
                 lowerCaseName.contains("人像") || lowerCaseName.contains("人") ||
                 lowerCaseName.contains("selfie") || lowerCaseName.contains("照片")) {
            simulatedLabels.add("人像");
        }

        // 车辆类检测
        else if (lowerCaseName.contains("car") || lowerCaseName.contains("vehicle") ||
                 lowerCaseName.contains("bike") || lowerCaseName.contains("bicycle") ||
                 lowerCaseName.contains("车") || lowerCaseName.contains("bus") ||
                 lowerCaseName.contains("train") || lowerCaseName.contains("飞机") ||
                 lowerCaseName.contains("plane")) {
            simulatedLabels.add("交通工具");
        }

        // 建筑类检测
        else if (lowerCaseName.contains("building") || lowerCaseName.contains("architecture") ||
                 lowerCaseName.contains("house") || lowerCaseName.contains("建筑") ||
                 lowerCaseName.contains("房") || lowerCaseName.contains("桥") ||
                 lowerCaseName.contains("bridge") || lowerCaseName.contains("tower")) {
            simulatedLabels.add("建筑");
        }

        // 自然类检测
        else if (lowerCaseName.contains("nature") || lowerCaseName.contains("tree") ||
                 lowerCaseName.contains("forest") || lowerCaseName.contains("flower") ||
                 lowerCaseName.contains("自然") || lowerCaseName.contains("花") ||
                 lowerCaseName.contains("树") || lowerCaseName.contains("草")) {
            simulatedLabels.add("自然");
        }

        // 默认标签 - 如果无法确定，使用风景（最常见）
        if (simulatedLabels.isEmpty()) {
            simulatedLabels.add("风景");
        }

        log.info("Simulated AI analysis resulted in {} tags: {}", simulatedLabels.size(), simulatedLabels);
        return simulatedLabels;
    }

    /**
     * 执行更详细的AI分析
     */
    public AIAnalysisResult detailedAnalyze(File imageFile) {
        List<String> tags = analyzeImageContent(imageFile);

        // 计算平均置信度（模拟值）
        double confidence = calculateConfidenceFromTags(tags);

        return new AIAnalysisResult(tags, confidence);
    }

    /**
     * 根据标签计算置信度
     * 新的逻辑基于标签的具体性
     */
    private double calculateConfidenceFromTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0.3;
        }

        // 检查是否有具体的标签（除了"AI识别"和"图像"）
        boolean hasSpecificLabel = false;
        for (String tag : tags) {
            if (!tag.equals("AI识别") && !tag.equals("图像")) {
                hasSpecificLabel = true;
                break;
            }
        }

        if (hasSpecificLabel) {
            return 0.75; // 有具体标签，置信度较高
        } else if (tags.contains("图像") && tags.contains("AI识别") && tags.size() == 2) {
            return 0.4; // 只有通用标签，置信度较低
        } else {
            return 0.6; // 默认置信度
        }
    }

    /**
     * 初始化ResNet50模型
     */
    private void initializeModel() throws ModelException, IOException {
        log.info("🚀 Initializing ResNet50 model...");

        try {
            // 创建图像分类转换器
            Pipeline pipeline = new Pipeline();
            pipeline.add(new Resize(224, 224))
                    .add(new ToTensor());

            Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                    .setPipeline(pipeline)
                    .optSynsetArtifactName("synset.txt")
                    .build();

            log.info("📝 Creating model criteria...");

            // 创建模型标准 - 尝试不同的模型URL
            Criteria<Image, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                    .setTypes(Image.class, Classifications.class)
                    .optModelUrls("djl://ai.djl.pytorch/resnet")
                    .optTranslator(translator)
                    .optProgress(new ProgressBar())
                    .optOption("mapLocation", "true")  // 允许CPU运行
                    .build();

            log.info("📥 Loading model from DJL repository...");
            log.info("⚠️ 首次运行会自动下载ResNet50模型（约100MB），请确保网络连接正常");

            // 加载模型（可能会下载模型文件）
            long startTime = System.currentTimeMillis();
            model = ModelZoo.loadModel(criteria);
            long loadTime = System.currentTimeMillis() - startTime;

            log.info("✅ Model loaded successfully in {} ms", loadTime);
            log.info("📊 Model info: {}", model.getModelPath());

            // 创建预测器
            predictor = model.newPredictor();

            // 测试预测器是否可用（使用一个小测试）
            try {
                log.info("🧪 Testing model with simple prediction...");
                // 创建一个小的测试图像
                BufferedImage testBufferedImage = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = testBufferedImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, 224, 224);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Test", 100, 100);
                g2d.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(testBufferedImage, "PNG", baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Image testImage = ImageFactory.getInstance().fromInputStream(bais);
                Classifications testResult = predictor.predict(testImage);
                log.info("✅ Model test passed, can predict classifications");
            } catch (Exception testEx) {
                log.warn("⚠️ Model test failed, but predictor created: {}", testEx.getMessage());
            }

            log.info("🎉 ResNet50 model fully initialized and ready");

        } catch (Exception e) {
            log.error("💥 Failed to initialize ResNet50 model: {}", e.getMessage(), e);
            log.info("📋 Troubleshooting steps:");
            log.info("  1. Check internet connection - model needs to be downloaded");
            log.info("  2. Verify DJL dependencies in pom.xml");
            log.info("  3. Check available disk space for model cache");
            log.info("  4. Ensure PyTorch native libraries are available");
            throw e;
        }
    }

    /**
     * 关闭模型资源
     */
    public void close() {
        try {
            if (predictor != null) {
                predictor.close();
            }
            if (model != null) {
                model.close();
            }
            log.info("AI model resources closed successfully");
        } catch (Exception e) {
            log.error("Error closing AI model resources", e);
        }
    }

    /**
     * 手动重新初始化AI模型
     * @return 初始化结果消息
     */
    public String reinitializeModel() {
        log.info("🔄 手动触发AI模型重新初始化...");

        // 关闭现有资源
        close();
        modelInitialized = false;

        // 尝试重新初始化
        boolean success = ensureModelInitialized();

        if (success) {
            return "✅ AI模型重新初始化成功";
        } else {
            return "❌ AI模型重新初始化失败，将使用模拟模式";
        }
    }
    /**
     * 检查AI模型是否可用
     * @return true如果模型已初始化并可用
     */
    public boolean isModelAvailable() {
        return modelInitialized && predictor != null;
    }

    /**
     * 获取模型初始化状态详情
     * @return 包含状态信息的Map
     */
    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", isModelAvailable());
        status.put("modelInitialized", modelInitialized);
        status.put("predictorAvailable", predictor != null);
        status.put("modelAvailable", model != null);
        status.put("maxRetries", maxRetries);
        status.put("retryDelaySeconds", retryDelaySeconds);
        status.put("timestamp", System.currentTimeMillis());

        if (model != null) {
            status.put("modelPath", model.getModelPath());
        }

        return status;
    }
    public static class AIAnalysisResult {
        private final List<String> tags;
        private final double confidence;

        public AIAnalysisResult(List<String> tags, double confidence) {
            this.tags = tags;
            this.confidence = confidence;
        }

        public List<String> getTags() {
            return tags;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}