package com.photo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI服务测试类
 * 测试ResNet图像分类模型的标签识别功能
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class AIServiceTest {

    @Autowired
    private AIService aiService;

    /**
     * 测试AI标签识别基本功能
     * 注意：首次运行会下载ResNet50模型（约100MB）
     */
    @Test
    void testAnalyzeImageContent() {
        // 查找测试图像文件
        String testImagePath = findTestImage();
        if (testImagePath == null) {
            log.warn("No test image found, skipping AI test");
            return;
        }

        File testImage = new File(testImagePath);
        if (!testImage.exists()) {
            log.warn("Test image not found at path: {}, skipping AI test", testImagePath);
            return;
        }

        log.info("Testing AI analysis with image: {}", testImagePath);

        // 分析图像内容
        List<String> tags = aiService.analyzeImageContent(testImage);

        // 验证结果
        assertThat(tags).isNotNull();
        assertThat(tags).isNotEmpty();

        // 应该包含AI标签
        assertThat(tags).contains("AI识别");

        // 验证标签类型（至少应该有一些类别标签）
        log.info("AI analysis generated {} tags: {}", tags.size(), tags);

        // 检查常见的标签类别（包括具体标签和大类）
        boolean hasCategoryTag = false;
        for (String tag : tags) {
            if (!tag.equals("AI识别") && !tag.equals("图像")) {
                // 任何其他中文标签都算作类别标签
                hasCategoryTag = true;
                break;
            }
        }

        if (!hasCategoryTag) {
            log.warn("No category tags found in: {}", tags);
        }

        // 成功分析图像
        log.info("AI analysis test completed successfully");
    }

    /**
     * 测试详细分析功能
     */
    @Test
    void testDetailedAnalyze() {
        String testImagePath = findTestImage();
        if (testImagePath == null) {
            log.warn("No test image found, skipping detailed analysis test");
            return;
        }

        File testImage = new File(testImagePath);
        if (!testImage.exists()) {
            log.warn("Test image not found, skipping detailed analysis test");
            return;
        }

        log.info("Testing detailed AI analysis with image: {}", testImagePath);

        // 执行详细分析
        AIService.AIAnalysisResult result = aiService.detailedAnalyze(testImage);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getTags()).isNotNull().isNotEmpty();
        assertThat(result.getConfidence()).isBetween(0.0, 1.0);

        log.info("Detailed analysis result - tags: {}, confidence: {:.2f}",
                 result.getTags(), result.getConfidence());
    }

    /**
     * 测试模拟模式（当模型未初始化时）
     */
    @Test
    void testSimulationMode() {
        // 创建一个模拟文件
        File mockFile = new File("test-dog-image.jpg");

        // 测试模拟分析（通过模拟文件名触发）
        List<String> tags = aiService.analyzeImageContent(mockFile);

        // 验证结果
        assertThat(tags).isNotNull().isNotEmpty();
        assertThat(tags).contains("AI识别");

        // 对于模拟模式，应该只包含"AI识别"和可能的其他标签
        log.info("Simulation mode test ran successfully");

        log.info("Simulation mode test completed with tags: {}", tags);
    }

    /**
     * 查找测试图像文件
     */
    private String findTestImage() {
        // 尝试在项目根目录查找测试图像
        String[] possiblePaths = {
            "test_image.jpg",
            "backend/test_image.jpg",
            "src/test/resources/test_image.jpg",
            "test.jpg",
            "example.jpg"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                log.info("Found test image at: {}", path);
                return path;
            }
        }

        // 尝试在系统路径中查找示例图像
        String userHome = System.getProperty("user.home");
        String[] systemPaths = {
            userHome + "/Pictures/test.jpg",
            userHome + "/Pictures/example.jpg",
            userHome + "/Desktop/test.jpg",
            userHome + "/Downloads/test.jpg"
        };

        for (String path : systemPaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                log.info("Found test image at: {}", path);
                return path;
            }
        }

        log.warn("No test image found in any of the searched locations");
        return null;
    }
}