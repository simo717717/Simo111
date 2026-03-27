package com.photo.service;

import java.util.*;

/**
 * 将ImageNet类别映射到自定义标签
 * 支持将ResNet模型预测的ImageNet类别转换为用户友好的自定义标签
 */
public class ImageNetToCustomLabelMapper {

    // ImageNet类别ID到类别名称的映射
    private static final Map<Integer, String> IMAGENET_CLASSES = new HashMap<>();

    // ImageNet类别ID到中文名称的映射
    private static final Map<Integer, String> IMAGENET_CLASSES_ZH = new HashMap<>();

    // 自定义标签分类映射
    private static final Map<String, List<String>> CUSTOM_CATEGORIES = new HashMap<>();

    // 直接的中文标签映射（ImageNet类别ID -> 中文标签）
    private static final Map<Integer, String> DIRECT_CHINESE_LABEL_MAP = new HashMap<>();

    static {
        // 初始化ImageNet类别（部分常见类别）
        // 注：这是简化的类别列表，实际ImageNet有1000个类别
        initImageNetClasses();
        initImageNetClassesZH();

        // 初始化自定义分类映射
        initCustomCategories();

        // 初始化直接中文标签映射
        initDirectChineseLabelMap();
    }

    private static void initImageNetClasses() {
        // 风景类
        IMAGENET_CLASSES.put(970, "mountain"); // 山
        IMAGENET_CLASSES.put(978, "valley"); // 山谷
        IMAGENET_CLASSES.put(979, "alp"); // 高山
        IMAGENET_CLASSES.put(980, "cliff"); // 悬崖
        IMAGENET_CLASSES.put(981, "coral reef"); // 珊瑚礁
        IMAGENET_CLASSES.put(982, "geyser"); // 间歇泉
        IMAGENET_CLASSES.put(983, "lakeside"); // 湖边
        IMAGENET_CLASSES.put(984, "seashore"); // 海岸
        IMAGENET_CLASSES.put(985, "sandbar"); // 沙洲
        IMAGENET_CLASSES.put(987, "volcano"); // 火山

        // 动物类 - 狗
        IMAGENET_CLASSES.put(151, "chihuahua"); // 吉娃娃
        IMAGENET_CLASSES.put(152, "Japanese spaniel"); // 日本犬
        IMAGENET_CLASSES.put(153, "Maltese dog"); // 马尔济斯犬
        IMAGENET_CLASSES.put(154, "Pekinese"); // 北京犬
        IMAGENET_CLASSES.put(155, "Shih-Tzu"); // 西施犬
        IMAGENET_CLASSES.put(156, "Blenheim spaniel"); // 布伦海姆西班牙猎犬
        IMAGENET_CLASSES.put(157, "papillon"); // 蝴蝶犬
        IMAGENET_CLASSES.put(158, "toy terrier"); // 玩具梗
        IMAGENET_CLASSES.put(159, "Rhodesian ridgeback"); // 罗得西亚脊背犬
        IMAGENET_CLASSES.put(160, "Afghan hound"); // 阿富汗猎犬
        IMAGENET_CLASSES.put(161, "basset"); // 巴吉度
        IMAGENET_CLASSES.put(162, "beagle"); // 比格犬
        IMAGENET_CLASSES.put(163, "bloodhound"); // 寻血猎犬
        IMAGENET_CLASSES.put(164, "bluetick"); // 蓝斑猎犬
        IMAGENET_CLASSES.put(165, "black-and-tan coonhound"); // 黑褐浣熊猎犬
        IMAGENET_CLASSES.put(166, "Walker hound"); // 沃克猎犬
        IMAGENET_CLASSES.put(167, "English foxhound"); // 英国猎狐犬
        IMAGENET_CLASSES.put(168, "redbone"); // 红骨猎犬
        IMAGENET_CLASSES.put(169, "borzoi"); // 苏俄牧羊犬
        IMAGENET_CLASSES.put(170, "Irish wolfhound"); // 爱尔兰狼犬
        IMAGENET_CLASSES.put(171, "Italian greyhound"); // 意大利灵缇
        IMAGENET_CLASSES.put(172, "whippet"); // 惠比特犬
        IMAGENET_CLASSES.put(173, "Ibizan hound"); // 依比沙猎犬
        IMAGENET_CLASSES.put(174, "Norwegian elkhound"); // 挪威猎鹿犬
        IMAGENET_CLASSES.put(175, "otterhound"); // 水獭猎犬
        IMAGENET_CLASSES.put(176, "Saluki"); // 萨路基猎犬
        IMAGENET_CLASSES.put(177, "Scottish deerhound"); // 苏格兰猎鹿犬
        IMAGENET_CLASSES.put(178, "Weimaraner"); // 魏玛犬
        IMAGENET_CLASSES.put(179, "Staffordshire bullterrier"); // 斯塔福郡斗牛梗
        IMAGENET_CLASSES.put(180, "American Staffordshire terrier"); // 美国斯塔福郡梗
        IMAGENET_CLASSES.put(181, "Bedlington terrier"); // 贝灵顿梗
        IMAGENET_CLASSES.put(182, "Border terrier"); // 边境梗
        IMAGENET_CLASSES.put(183, "Kerry blue terrier"); // 凯利蓝梗
        IMAGENET_CLASSES.put(184, "Irish terrier"); // 爱尔兰梗
        IMAGENET_CLASSES.put(185, "Norfolk terrier"); // 诺福克梗
        IMAGENET_CLASSES.put(186, "Norwich terrier"); // 诺维奇梗
        IMAGENET_CLASSES.put(187, "Yorkshire terrier"); // 约克夏梗
        IMAGENET_CLASSES.put(188, "wire-haired fox terrier"); // 刚毛猎狐梗
        IMAGENET_CLASSES.put(189, "Lakeland terrier"); // 湖畔梗
        IMAGENET_CLASSES.put(190, "Sealyham terrier"); // 西里汉梗
        IMAGENET_CLASSES.put(191, "Airedale"); // 万能梗
        IMAGENET_CLASSES.put(192, "cairn"); // 凯恩梗
        IMAGENET_CLASSES.put(193, "Australian terrier"); // 澳大利亚梗
        IMAGENET_CLASSES.put(194, "Dandie Dinmont"); // 丹迪丁蒙梗
        IMAGENET_CLASSES.put(195, "Boston bull"); // 波士顿斗牛犬
        IMAGENET_CLASSES.put(196, "miniature schnauzer"); // 迷你雪纳瑞
        IMAGENET_CLASSES.put(197, "giant schnauzer"); // 巨型雪纳瑞
        IMAGENET_CLASSES.put(198, "standard schnauzer"); // 标准雪纳瑞
        IMAGENET_CLASSES.put(199, "Scotch terrier"); // 苏格兰梗
        IMAGENET_CLASSES.put(200, "Tibetan terrier"); // 西藏梗
        IMAGENET_CLASSES.put(201, "silky terrier"); // 丝毛梗
        IMAGENET_CLASSES.put(202, "soft-coated wheaten terrier"); // 软毛麦色梗
        IMAGENET_CLASSES.put(203, "West Highland white terrier"); // 西高地白梗
        IMAGENET_CLASSES.put(204, "Lhasa"); // 拉萨犬
        IMAGENET_CLASSES.put(205, "flat-coated retriever"); // 平毛寻回犬
        IMAGENET_CLASSES.put(206, "curly-coated retriever"); // 卷毛寻回犬
        IMAGENET_CLASSES.put(207, "golden retriever"); // 金毛寻回犬
        IMAGENET_CLASSES.put(208, "Labrador retriever"); // 拉布拉多寻回犬
        IMAGENET_CLASSES.put(209, "Chesapeake Bay retriever"); // 切萨皮克湾寻回犬
        IMAGENET_CLASSES.put(210, "German short-haired pointer"); // 德国短毛指示犬
        IMAGENET_CLASSES.put(211, "vizsla"); // 匈牙利维兹拉犬
        IMAGENET_CLASSES.put(212, "English setter"); // 英国塞特犬
        IMAGENET_CLASSES.put(213, "Irish setter"); // 爱尔兰塞特犬
        IMAGENET_CLASSES.put(214, "Gordon setter"); // 戈登塞特犬
        IMAGENET_CLASSES.put(215, "Brittany spaniel"); // 布列塔尼猎犬
        IMAGENET_CLASSES.put(216, "clumber"); // 克伦伯猎犬
        IMAGENET_CLASSES.put(217, "English springer"); // 英国史宾格犬
        IMAGENET_CLASSES.put(218, "Welsh springer spaniel"); // 威尔士史宾格犬
        IMAGENET_CLASSES.put(219, "cocker spaniel"); // 可卡犬
        IMAGENET_CLASSES.put(220, "Sussex spaniel"); // 萨塞克斯猎犬
        IMAGENET_CLASSES.put(221, "Irish water spaniel"); // 爱尔兰水猎犬
        IMAGENET_CLASSES.put(222, "kuvasz"); // 库瓦兹犬
        IMAGENET_CLASSES.put(223, "schipperke"); // 史奇派克犬
        IMAGENET_CLASSES.put(224, "groenendael"); // 比利时牧羊犬
        IMAGENET_CLASSES.put(225, "malinois"); // 马利诺斯犬
        IMAGENET_CLASSES.put(226, "briard"); // 伯瑞犬
        IMAGENET_CLASSES.put(227, "kelpie"); // 澳洲卡尔比犬
        IMAGENET_CLASSES.put(228, "komondor"); // 可蒙犬
        IMAGENET_CLASSES.put(229, "Old English sheepdog"); // 古代英国牧羊犬
        IMAGENET_CLASSES.put(230, "Shetland sheepdog"); // 喜乐蒂牧羊犬
        IMAGENET_CLASSES.put(231, "collie"); // 柯利牧羊犬
        IMAGENET_CLASSES.put(232, "Border collie"); // 边境牧羊犬
        IMAGENET_CLASSES.put(233, "Bouvier des Flandres"); // 法兰德斯牧牛犬
        IMAGENET_CLASSES.put(234, "Rottweiler"); // 罗威纳犬
        IMAGENET_CLASSES.put(235, "German shepherd"); // 德国牧羊犬
        IMAGENET_CLASSES.put(236, "Doberman"); // 杜宾犬
        IMAGENET_CLASSES.put(237, "miniature pinscher"); // 迷你杜宾犬
        IMAGENET_CLASSES.put(238, "Greater Swiss Mountain dog"); // 大瑞士山地犬
        IMAGENET_CLASSES.put(239, "Bernese mountain dog"); // 伯恩山犬
        IMAGENET_CLASSES.put(240, "Appenzeller"); // 阿彭策尔山犬
        IMAGENET_CLASSES.put(241, "EntleBucher"); // 恩特雷布赫山犬
        IMAGENET_CLASSES.put(242, "boxer"); // 拳师犬
        IMAGENET_CLASSES.put(243, "bull mastiff"); // 斗牛獒
        IMAGENET_CLASSES.put(244, "Tibetan mastiff"); // 藏獒
        IMAGENET_CLASSES.put(245, "French bulldog"); // 法国斗牛犬
        IMAGENET_CLASSES.put(246, "Great Dane"); // 大丹犬
        IMAGENET_CLASSES.put(247, "Saint Bernard"); // 圣伯纳犬
        IMAGENET_CLASSES.put(248, "Eskimo dog"); // 爱斯基摩犬
        IMAGENET_CLASSES.put(249, "malamute"); // 阿拉斯加马拉缪特犬
        IMAGENET_CLASSES.put(250, "Siberian husky"); // 西伯利亚哈士奇
        IMAGENET_CLASSES.put(251, "dalmatian"); // 达尔马提亚犬
        IMAGENET_CLASSES.put(252, "affenpinscher"); // 猴犬
        IMAGENET_CLASSES.put(253, "basenji"); // 巴仙吉犬
        IMAGENET_CLASSES.put(254, "pug"); // 巴哥犬
        IMAGENET_CLASSES.put(255, "Leonberg"); // 兰波格犬
        IMAGENET_CLASSES.put(256, "Newfoundland"); // 纽芬兰犬
        IMAGENET_CLASSES.put(257, "Great Pyrenees"); // 大白熊犬
        IMAGENET_CLASSES.put(258, "Samoyed"); // 萨摩耶犬
        IMAGENET_CLASSES.put(259, "Pomeranian"); // 博美犬
        IMAGENET_CLASSES.put(260, "chow"); // 松狮犬
        IMAGENET_CLASSES.put(261, "keeshond"); // 荷兰毛狮犬
        IMAGENET_CLASSES.put(262, "Brabancon griffon"); // 布鲁塞尔格里芬犬
        IMAGENET_CLASSES.put(263, "Pembroke"); // 彭布罗克威尔士柯基犬
        IMAGENET_CLASSES.put(264, "Cardigan"); // 卡迪根威尔士柯基犬
        IMAGENET_CLASSES.put(265, "toy poodle"); // 玩具贵宾犬
        IMAGENET_CLASSES.put(266, "miniature poodle"); // 迷你贵宾犬
        IMAGENET_CLASSES.put(267, "standard poodle"); // 标准贵宾犬
        IMAGENET_CLASSES.put(268, "Mexican hairless"); // 墨西哥无毛犬

        // 动物类 - 猫科
        IMAGENET_CLASSES.put(281, "tabby"); // 虎斑猫
        IMAGENET_CLASSES.put(282, "tiger cat"); // 虎猫
        IMAGENET_CLASSES.put(283, "Persian cat"); // 波斯猫
        IMAGENET_CLASSES.put(284, "Siamese cat"); // 暹罗猫
        IMAGENET_CLASSES.put(285, "Egyptian cat"); // 埃及猫
        IMAGENET_CLASSES.put(286, "cougar"); // 美洲狮
        IMAGENET_CLASSES.put(287, "lynx"); // 猞猁
        IMAGENET_CLASSES.put(288, "leopard"); // 豹
        IMAGENET_CLASSES.put(289, "snow leopard"); // 雪豹
        IMAGENET_CLASSES.put(290, "jaguar"); // 美洲虎
        IMAGENET_CLASSES.put(291, "lion"); // 狮子
        IMAGENET_CLASSES.put(292, "tiger"); // 老虎
        IMAGENET_CLASSES.put(293, "cheetah"); // 猎豹

        // 动物类 - 海洋生物
        IMAGENET_CLASSES.put(0, "tench"); // 丁鲷
        IMAGENET_CLASSES.put(1, "goldfish"); // 金鱼
        IMAGENET_CLASSES.put(2, "great white shark"); // 大白鲨
        IMAGENET_CLASSES.put(3, "tiger shark"); // 虎鲨
        IMAGENET_CLASSES.put(4, "hammerhead"); // 双髻鲨
        IMAGENET_CLASSES.put(5, "electric ray"); // 电鳐
        IMAGENET_CLASSES.put(6, "stingray"); // 黄貂鱼

        // 动物类 - 鸟类
        IMAGENET_CLASSES.put(8, "cock"); // 公鸡
        IMAGENET_CLASSES.put(9, "hen"); // 母鸡
        IMAGENET_CLASSES.put(10, "ostrich"); // 鸵鸟
        IMAGENET_CLASSES.put(11, "brambling"); // 燕雀
        IMAGENET_CLASSES.put(12, "goldfinch"); // 金翅雀
        IMAGENET_CLASSES.put(13, "house finch"); // 家朱雀
        IMAGENET_CLASSES.put(14, "junco"); // 灯草雀
        IMAGENET_CLASSES.put(15, "indigo bunting"); // 靛蓝彩鹀
        IMAGENET_CLASSES.put(16, "robin"); // 知更鸟
        IMAGENET_CLASSES.put(17, "bulbul"); // 鹎
        IMAGENET_CLASSES.put(18, "magpie"); // 喜鹊
        IMAGENET_CLASSES.put(19, "chickadee"); // 山雀
        IMAGENET_CLASSES.put(20, "water ouzel"); // 河鸟
        IMAGENET_CLASSES.put(21, "kite"); // 鸢
        IMAGENET_CLASSES.put(22, "bald eagle"); // 白头鹰
        IMAGENET_CLASSES.put(23, "vulture"); // 秃鹫
        IMAGENET_CLASSES.put(24, "great grey owl"); // 大灰猫头鹰

        // 动物类 - 其他哺乳动物
        IMAGENET_CLASSES.put(330, "sea lion"); // 海狮
        IMAGENET_CLASSES.put(331, "Chihuahua"); // 吉娃娃
        IMAGENET_CLASSES.put(332, "Weimaraner"); // 魏玛犬
        IMAGENET_CLASSES.put(333, "red fox"); // 红狐
        IMAGENET_CLASSES.put(334, "kit fox"); // 沙狐
        IMAGENET_CLASSES.put(335, "Arctic fox"); // 北极狐
        IMAGENET_CLASSES.put(336, "grey fox"); // 灰狐
        IMAGENET_CLASSES.put(337, "tabby cat"); // 虎斑猫
        IMAGENET_CLASSES.put(338, "tiger cat"); // 虎猫
        IMAGENET_CLASSES.put(339, "Persian cat"); // 波斯猫
        IMAGENET_CLASSES.put(340, "Siamese cat"); // 暹罗猫
        IMAGENET_CLASSES.put(341, "Egyptian cat"); // 埃及猫
        IMAGENET_CLASSES.put(342, "cougar"); // 美洲狮
        IMAGENET_CLASSES.put(343, "lynx"); // 猞猁
        IMAGENET_CLASSES.put(344, "leopard"); // 豹
        IMAGENET_CLASSES.put(345, "snow leopard"); // 雪豹
        IMAGENET_CLASSES.put(346, "jaguar"); // 美洲虎
        IMAGENET_CLASSES.put(347, "lion"); // 狮子
        IMAGENET_CLASSES.put(348, "tiger"); // 老虎
        IMAGENET_CLASSES.put(349, "cheetah"); // 猎豹
        IMAGENET_CLASSES.put(350, "brown bear"); // 棕熊
        IMAGENET_CLASSES.put(351, "American black bear"); // 美洲黑熊
        IMAGENET_CLASSES.put(352, "ice bear"); // 北极熊
        IMAGENET_CLASSES.put(353, "sloth bear"); // 树懒熊
        IMAGENET_CLASSES.put(354, "mongoose"); // 猫鼬
        IMAGENET_CLASSES.put(355, "meerkat"); // 狐獴
        IMAGENET_CLASSES.put(356, "tiger beetle"); // 虎甲虫
        IMAGENET_CLASSES.put(357, "ladybug"); // 瓢虫
        IMAGENET_CLASSES.put(358, "ground beetle"); // 步甲
        IMAGENET_CLASSES.put(359, "long-horned beetle"); // 天牛
        IMAGENET_CLASSES.put(360, "leaf beetle"); // 叶甲
        IMAGENET_CLASSES.put(361, "dung beetle"); // 屎壳郎
        IMAGENET_CLASSES.put(362, "rhinoceros beetle"); // 犀牛甲虫
        IMAGENET_CLASSES.put(363, "weevil"); // 象鼻虫
        IMAGENET_CLASSES.put(364, "fly"); // 苍蝇
        IMAGENET_CLASSES.put(365, "bee"); // 蜜蜂
        IMAGENET_CLASSES.put(366, "ant"); // 蚂蚁
        IMAGENET_CLASSES.put(367, "grasshopper"); // 蚱蜢
        IMAGENET_CLASSES.put(368, "cricket"); // 蟋蟀
        IMAGENET_CLASSES.put(369, "walking stick"); // 竹节虫
        IMAGENET_CLASSES.put(370, "cockroach"); // 蟑螂
        IMAGENET_CLASSES.put(371, "mantis"); // 螳螂
        IMAGENET_CLASSES.put(372, "cicada"); // 蝉
        IMAGENET_CLASSES.put(373, "leafhopper"); // 叶蝉
        IMAGENET_CLASSES.put(374, "lacewing"); // 草蜻蛉
        IMAGENET_CLASSES.put(375, "dragonfly"); // 蜻蜓
        IMAGENET_CLASSES.put(376, "damselfly"); // 豆娘

        // 更多鸟类
        IMAGENET_CLASSES.put(377, "admiral"); // 优红蛱蝶
        IMAGENET_CLASSES.put(378, "ringlet"); // 小环蛱蝶
        IMAGENET_CLASSES.put(379, "monarch"); // 黑脉金斑蝶
        IMAGENET_CLASSES.put(380, "cabbage butterfly"); // 菜粉蝶
        IMAGENET_CLASSES.put(381, "sulphur butterfly"); // 粉蝶
        IMAGENET_CLASSES.put(382, "lycaenid"); // 灰蝶
        IMAGENET_CLASSES.put(383, "starfish"); // 海星
        IMAGENET_CLASSES.put(384, "sea urchin"); // 海胆
        IMAGENET_CLASSES.put(385, "sea cucumber"); // 海参
        IMAGENET_CLASSES.put(386, "wood rabbit"); // 木兔
        IMAGENET_CLASSES.put(387, "hare"); // 野兔
        IMAGENET_CLASSES.put(388, "Angora"); // 安哥拉兔
        IMAGENET_CLASSES.put(389, "hamster"); // 仓鼠
        IMAGENET_CLASSES.put(390, "porcupine"); // 豪猪
        IMAGENET_CLASSES.put(391, "fox squirrel"); // 狐松鼠
        IMAGENET_CLASSES.put(392, "marmot"); // 土拨鼠
        IMAGENET_CLASSES.put(393, "beaver"); // 河狸
        IMAGENET_CLASSES.put(394, "guinea pig"); // 豚鼠
        IMAGENET_CLASSES.put(395, "sorrel"); // 栗色马
        IMAGENET_CLASSES.put(396, "zebra"); // 斑马
        IMAGENET_CLASSES.put(397, "hog"); // 猪
        IMAGENET_CLASSES.put(398, "wild boar"); // 野猪
        IMAGENET_CLASSES.put(399, "warthog"); // 疣猪
        IMAGENET_CLASSES.put(400, "hippopotamus"); // 河马
        IMAGENET_CLASSES.put(401, "ox"); // 牛
        IMAGENET_CLASSES.put(402, "water buffalo"); // 水牛
        IMAGENET_CLASSES.put(403, "bison"); // 野牛
        IMAGENET_CLASSES.put(404, "ram"); // 公羊
        IMAGENET_CLASSES.put(405, "bighorn"); // 大角羊
        IMAGENET_CLASSES.put(406, "ibex"); // 野山羊
        IMAGENET_CLASSES.put(407, "hartebeest"); // 狷羚
        IMAGENET_CLASSES.put(408, "impala"); // 黑斑羚
        IMAGENET_CLASSES.put(409, "gazelle"); // 瞪羚
        IMAGENET_CLASSES.put(410, "Arabian camel"); // 阿拉伯骆驼
        IMAGENET_CLASSES.put(411, "llama"); // 羊驼
        IMAGENET_CLASSES.put(412, "weasel"); // 鼬
        IMAGENET_CLASSES.put(413, "mink"); // 水貂
        IMAGENET_CLASSES.put(414, "polecat"); // 欧洲臭鼬
        IMAGENET_CLASSES.put(415, "black-footed ferret"); // 黑足鼬
        IMAGENET_CLASSES.put(416, "otter"); // 水獭
        IMAGENET_CLASSES.put(417, "skunk"); // 臭鼬
        IMAGENET_CLASSES.put(418, "badger"); // 獾
        IMAGENET_CLASSES.put(419, "armadillo"); // 犰狳
        IMAGENET_CLASSES.put(420, "three-toed sloth"); // 三趾树懒
        IMAGENET_CLASSES.put(421, "orangutan"); // 猩猩
        IMAGENET_CLASSES.put(422, "gorilla"); // 大猩猩
        IMAGENET_CLASSES.put(423, "chimpanzee"); // 黑猩猩
        IMAGENET_CLASSES.put(424, "gibbon"); // 长臂猿
        IMAGENET_CLASSES.put(425, "siamang"); // 合趾猿
        IMAGENET_CLASSES.put(426, "guenon"); // 长尾猴
        IMAGENET_CLASSES.put(427, "patas"); // 赤猴
        IMAGENET_CLASSES.put(428, "baboon"); // 狒狒
        IMAGENET_CLASSES.put(429, "macaque"); // 猕猴
        IMAGENET_CLASSES.put(430, "langur"); // 叶猴
        IMAGENET_CLASSES.put(431, "colobus"); // 疣猴
        IMAGENET_CLASSES.put(432, "proboscis monkey"); // 长鼻猴
        IMAGENET_CLASSES.put(433, "marmoset"); // 狨猴
        IMAGENET_CLASSES.put(434, "capuchin"); // 卷尾猴
        IMAGENET_CLASSES.put(435, "howler monkey"); // 吼猴
        IMAGENET_CLASSES.put(436, "titi"); // 伶猴
        IMAGENET_CLASSES.put(437, "spider monkey"); // 蜘蛛猴
        IMAGENET_CLASSES.put(438, "squirrel monkey"); // 松鼠猴
        IMAGENET_CLASSES.put(439, "Madagascar cat"); // 马达加斯加猫

        // 美食类
        IMAGENET_CLASSES.put(907, "carbonara"); // 卡博拉纳意面
        IMAGENET_CLASSES.put(924, "bagel"); // 贝果
        IMAGENET_CLASSES.put(925, "pretzel"); // 椒盐卷饼
        IMAGENET_CLASSES.put(926, "cheeseburger"); // 芝士汉堡
        IMAGENET_CLASSES.put(927, "hotdog"); // 热狗
        IMAGENET_CLASSES.put(928, "mashed potato"); // 土豆泥
        IMAGENET_CLASSES.put(929, "head cabbage"); // 卷心菜
        IMAGENET_CLASSES.put(930, "broccoli"); // 西兰花
        IMAGENET_CLASSES.put(931, "cauliflower"); // 花椰菜
        IMAGENET_CLASSES.put(932, "zucchini"); // 西葫芦
        IMAGENET_CLASSES.put(933, "spaghetti squash"); // 意大利面南瓜
        IMAGENET_CLASSES.put(934, "acorn squash"); // 橡果南瓜
        IMAGENET_CLASSES.put(935, "butternut squash"); // 冬南瓜
        IMAGENET_CLASSES.put(936, "cucumber"); // 黄瓜
        IMAGENET_CLASSES.put(937, "artichoke"); // 朝鲜蓟
        IMAGENET_CLASSES.put(938, "bell pepper"); // 甜椒
        IMAGENET_CLASSES.put(939, "cardoon"); // 刺菜蓟
        IMAGENET_CLASSES.put(940, "mushroom"); // 蘑菇
        IMAGENET_CLASSES.put(941, "Granny Smith"); // 青苹果
        IMAGENET_CLASSES.put(942, "strawberry"); // 草莓
        IMAGENET_CLASSES.put(943, "orange"); // 橙子
        IMAGENET_CLASSES.put(944, "lemon"); // 柠檬
        IMAGENET_CLASSES.put(945, "fig"); // 无花果
        IMAGENET_CLASSES.put(946, "pineapple"); // 菠萝
        IMAGENET_CLASSES.put(947, "banana"); // 香蕉
        IMAGENET_CLASSES.put(948, "jackfruit"); // 菠萝蜜
        IMAGENET_CLASSES.put(949, "custard apple"); // 番荔枝
        IMAGENET_CLASSES.put(950, "pomegranate"); // 石榴
        IMAGENET_CLASSES.put(951, "hay"); // 干草
        IMAGENET_CLASSES.put(952, "carbonara"); // 卡博拉纳意面
        IMAGENET_CLASSES.put(953, "chocolate sauce"); // 巧克力酱
        IMAGENET_CLASSES.put(954, "dough"); // 面团
        IMAGENET_CLASSES.put(955, "meat loaf"); // 肉饼
        IMAGENET_CLASSES.put(956, "pizza"); // 披萨
        IMAGENET_CLASSES.put(957, "potpie"); // 锅饼
        IMAGENET_CLASSES.put(958, "burrito"); // 墨西哥卷饼
        IMAGENET_CLASSES.put(959, "red wine"); // 红酒
        IMAGENET_CLASSES.put(960, "espresso"); // 浓缩咖啡
        IMAGENET_CLASSES.put(961, "cup"); // 杯子
        IMAGENET_CLASSES.put(962, "eggnog"); // 蛋奶酒
        IMAGENET_CLASSES.put(963, "alp"); // 高山
        IMAGENET_CLASSES.put(964, "bubble"); // 气泡
        IMAGENET_CLASSES.put(965, "cliff"); // 悬崖
        IMAGENET_CLASSES.put(966, "coral reef"); // 珊瑚礁
        IMAGENET_CLASSES.put(967, "geyser"); // 间歇泉
        IMAGENET_CLASSES.put(968, "lakeside"); // 湖边
        IMAGENET_CLASSES.put(969, "promontory"); // 海角

        // 人像类
        IMAGENET_CLASSES.put(440, "academic gown"); // 学士袍（人物相关）
        IMAGENET_CLASSES.put(441, "apron"); // 围裙
        IMAGENET_CLASSES.put(442, "backpack"); // 背包（可能有人）
        IMAGENET_CLASSES.put(850, "suit"); // 西装
        IMAGENET_CLASSES.put(851, "sweatshirt"); // 运动衫
        IMAGENET_CLASSES.put(852, "T-shirt"); // T恤衫

        // 建筑类
        IMAGENET_CLASSES.put(483, "church"); // 教堂
        IMAGENET_CLASSES.put(484, "mosque"); // 清真寺
        IMAGENET_CLASSES.put(485, "stupa"); // 佛塔
        IMAGENET_CLASSES.put(486, "triumphal arch"); // 凯旋门
        IMAGENET_CLASSES.put(487, "patio"); // 庭院
        IMAGENET_CLASSES.put(488, "steel arch bridge"); // 钢拱桥
        IMAGENET_CLASSES.put(489, "suspension bridge"); // 悬索桥
        IMAGENET_CLASSES.put(490, "viaduct"); // 高架桥
        IMAGENET_CLASSES.put(491, "barn"); // 谷仓
        IMAGENET_CLASSES.put(492, "greenhouse"); // 温室
        IMAGENET_CLASSES.put(493, "palace"); // 宫殿
        IMAGENET_CLASSES.put(494, "monastery"); // 修道院
        IMAGENET_CLASSES.put(495, "library"); // 图书馆

        // 交通工具类
        IMAGENET_CLASSES.put(609, "jeep"); // 吉普车
        IMAGENET_CLASSES.put(610, "limousine"); // 豪华轿车
        IMAGENET_CLASSES.put(611, "minivan"); // 小型货车
        IMAGENET_CLASSES.put(612, "Model T"); // T型车
        IMAGENET_CLASSES.put(613, "racer"); // 赛车
        IMAGENET_CLASSES.put(614, "sports car"); // 跑车
        IMAGENET_CLASSES.put(615, "convertible"); // 敞篷车
        IMAGENET_CLASSES.put(616, "cab"); // 出租车
        IMAGENET_CLASSES.put(617, "recreational vehicle"); // 房车
        IMAGENET_CLASSES.put(618, "tow truck"); // 拖车
        IMAGENET_CLASSES.put(619, "garbage truck"); // 垃圾车
        IMAGENET_CLASSES.put(620, "fire engine"); // 消防车
        IMAGENET_CLASSES.put(621, "ambulance"); // 救护车
        IMAGENET_CLASSES.put(622, "school bus"); // 校车
        IMAGENET_CLASSES.put(623, "trolleybus"); // 无轨电车
        IMAGENET_CLASSES.put(624, "trolley"); // 手推车
        IMAGENET_CLASSES.put(625, "snowplow"); // 扫雪车
        IMAGENET_CLASSES.put(626, "tractor"); // 拖拉机
        IMAGENET_CLASSES.put(627, "harvester"); // 收割机
        IMAGENET_CLASSES.put(628, "thresher"); // 脱粒机
        IMAGENET_CLASSES.put(629, "bicycle"); // 自行车
        IMAGENET_CLASSES.put(630, "mountain bike"); // 山地自行车
        IMAGENET_CLASSES.put(631, "motor scooter"); // 小型摩托车
        IMAGENET_CLASSES.put(632, "moped"); // 轻骑车
        IMAGENET_CLASSES.put(633, "freight car"); // 货车车厢
        IMAGENET_CLASSES.put(634, "passenger car"); // 客车车厢
        IMAGENET_CLASSES.put(635, "barrow"); // 独轮车
        IMAGENET_CLASSES.put(636, "shopping cart"); // 购物车
        IMAGENET_CLASSES.put(637, "gondola"); // 贡多拉
        IMAGENET_CLASSES.put(638, "canoe"); // 独木舟
        IMAGENET_CLASSES.put(639, "yawl"); // 小帆船
        IMAGENET_CLASSES.put(640, "airliner"); // 客机
        IMAGENET_CLASSES.put(641, "warplane"); // 战斗机
        IMAGENET_CLASSES.put(642, "airship"); // 飞艇
        IMAGENET_CLASSES.put(643, "balloon"); // 气球
        IMAGENET_CLASSES.put(644, "speedboat"); // 快艇
        IMAGENET_CLASSES.put(645, "ocean liner"); // 远洋轮船

        // 自然类 - 植物
        IMAGENET_CLASSES.put(691, "snail"); // 蜗牛
        IMAGENET_CLASSES.put(692, "slug"); // 蛞蝓
        IMAGENET_CLASSES.put(693, "sea slug"); // 海蛞蝓
        IMAGENET_CLASSES.put(694, "chiton"); // 石鳖
        IMAGENET_CLASSES.put(695, "chambered nautilus"); // 鹦鹉螺
        IMAGENET_CLASSES.put(696, "Dungeness crab"); // 珍宝蟹
        IMAGENET_CLASSES.put(697, "rock crab"); // 石蟹
        IMAGENET_CLASSES.put(698, "fiddler crab"); // 招潮蟹
        IMAGENET_CLASSES.put(699, "king crab"); // 帝王蟹
        IMAGENET_CLASSES.put(700, "American lobster"); // 美洲龙虾
        IMAGENET_CLASSES.put(701, "spiny lobster"); // 棘刺龙虾
        IMAGENET_CLASSES.put(702, "crayfish"); // 小龙虾
        IMAGENET_CLASSES.put(703, "hermit crab"); // 寄居蟹
        IMAGENET_CLASSES.put(704, "isopod"); // 等足目动物
        IMAGENET_CLASSES.put(705, "white stork"); // 白鹳
        IMAGENET_CLASSES.put(706, "black stork"); // 黑鹳
        IMAGENET_CLASSES.put(707, "spoonbill"); // 琵鹭
        IMAGENET_CLASSES.put(708, "flamingo"); // 火烈鸟
        IMAGENET_CLASSES.put(709, "little blue heron"); // 小蓝鹭
        IMAGENET_CLASSES.put(710, "American egret"); // 美洲白鹭
        IMAGENET_CLASSES.put(711, "bittern"); // 麻鳽
        IMAGENET_CLASSES.put(712, "crane"); // 鹤
        IMAGENET_CLASSES.put(713, "limpkin"); // 秧鹤
        IMAGENET_CLASSES.put(714, "European gallinule"); // 欧洲水鸡
        IMAGENET_CLASSES.put(715, "American coot"); // 美洲骨顶鸡
        IMAGENET_CLASSES.put(716, "bustard"); // 鸨
        IMAGENET_CLASSES.put(717, "ruddy turnstone"); // 翻石鹬
        IMAGENET_CLASSES.put(718, "red-backed sandpiper"); // 红背滨鹬
        IMAGENET_CLASSES.put(719, "redshank"); // 红脚鹬
        IMAGENET_CLASSES.put(720, "dowitcher"); // 长嘴鹬
        IMAGENET_CLASSES.put(721, "oystercatcher"); // 蛎鹬
        IMAGENET_CLASSES.put(722, "pelican"); // 鹈鹕
        IMAGENET_CLASSES.put(723, "king penguin"); // 王企鹅
        IMAGENET_CLASSES.put(724, "albatross"); // 信天翁
        IMAGENET_CLASSES.put(725, "grey whale"); // 灰鲸
        IMAGENET_CLASSES.put(726, "killer whale"); // 虎鲸
        IMAGENET_CLASSES.put(727, "dugong"); // 儒艮
        IMAGENET_CLASSES.put(728, "sea lion"); // 海狮
        IMAGENET_CLASSES.put(729, "Chihuahua"); // 吉娃娃

        // 自然风景类
        IMAGENET_CLASSES.put(971, "alp"); // 高山
        IMAGENET_CLASSES.put(972, "cliff"); // 悬崖
        IMAGENET_CLASSES.put(973, "coral reef"); // 珊瑚礁
        IMAGENET_CLASSES.put(974, "geyser"); // 间歇泉
        IMAGENET_CLASSES.put(975, "lakeside"); // 湖边
        IMAGENET_CLASSES.put(976, "promontory"); // 海角
        IMAGENET_CLASSES.put(977, "sandbar"); // 沙洲
        IMAGENET_CLASSES.put(986, "lakeside"); // 湖边

        // 其他常见类别
        IMAGENET_CLASSES.put(988, "ballplayer"); // 球员
        IMAGENET_CLASSES.put(989, "golf ball"); // 高尔夫球
    }

    private static void initImageNetClassesZH() {
        // 风景类中文映射
        IMAGENET_CLASSES_ZH.put(970, "山");
        IMAGENET_CLASSES_ZH.put(978, "山谷");
        IMAGENET_CLASSES_ZH.put(979, "高山");
        IMAGENET_CLASSES_ZH.put(980, "悬崖");
        IMAGENET_CLASSES_ZH.put(981, "珊瑚礁");
        IMAGENET_CLASSES_ZH.put(982, "间歇泉");
        IMAGENET_CLASSES_ZH.put(983, "湖边");
        IMAGENET_CLASSES_ZH.put(984, "海岸");
        IMAGENET_CLASSES_ZH.put(985, "沙洲");
        IMAGENET_CLASSES_ZH.put(987, "火山");

        // 动物类 - 狗 中文映射
        IMAGENET_CLASSES_ZH.put(151, "吉娃娃");
        IMAGENET_CLASSES_ZH.put(152, "日本犬");
        IMAGENET_CLASSES_ZH.put(153, "马尔济斯犬");
        IMAGENET_CLASSES_ZH.put(154, "北京犬");
        IMAGENET_CLASSES_ZH.put(155, "西施犬");
        IMAGENET_CLASSES_ZH.put(156, "布伦海姆西班牙猎犬");
        IMAGENET_CLASSES_ZH.put(157, "蝴蝶犬");
        IMAGENET_CLASSES_ZH.put(158, "玩具梗");
        IMAGENET_CLASSES_ZH.put(159, "罗得西亚脊背犬");
        IMAGENET_CLASSES_ZH.put(160, "阿富汗猎犬");
        IMAGENET_CLASSES_ZH.put(161, "巴吉度");
        IMAGENET_CLASSES_ZH.put(162, "比格犬");
        IMAGENET_CLASSES_ZH.put(163, "寻血猎犬");
        IMAGENET_CLASSES_ZH.put(164, "蓝斑猎犬");
        IMAGENET_CLASSES_ZH.put(165, "黑褐浣熊猎犬");
        IMAGENET_CLASSES_ZH.put(166, "沃克猎犬");
        IMAGENET_CLASSES_ZH.put(167, "英国猎狐犬");
        IMAGENET_CLASSES_ZH.put(168, "红骨猎犬");
        IMAGENET_CLASSES_ZH.put(169, "苏俄牧羊犬");
        IMAGENET_CLASSES_ZH.put(170, "爱尔兰狼犬");
        IMAGENET_CLASSES_ZH.put(171, "意大利灵缇");
        IMAGENET_CLASSES_ZH.put(172, "惠比特犬");
        IMAGENET_CLASSES_ZH.put(173, "依比沙猎犬");
        IMAGENET_CLASSES_ZH.put(174, "挪威猎鹿犬");
        IMAGENET_CLASSES_ZH.put(175, "水獭猎犬");
        IMAGENET_CLASSES_ZH.put(176, "萨路基猎犬");
        IMAGENET_CLASSES_ZH.put(177, "苏格兰猎鹿犬");
        IMAGENET_CLASSES_ZH.put(178, "魏玛犬");
        IMAGENET_CLASSES_ZH.put(179, "斯塔福郡斗牛梗");
        IMAGENET_CLASSES_ZH.put(180, "美国斯塔福郡梗");
        IMAGENET_CLASSES_ZH.put(181, "贝灵顿梗");
        IMAGENET_CLASSES_ZH.put(182, "边境梗");
        IMAGENET_CLASSES_ZH.put(183, "凯利蓝梗");
        IMAGENET_CLASSES_ZH.put(184, "爱尔兰梗");
        IMAGENET_CLASSES_ZH.put(185, "诺福克梗");
        IMAGENET_CLASSES_ZH.put(186, "诺维奇梗");
        IMAGENET_CLASSES_ZH.put(187, "约克夏梗");
        IMAGENET_CLASSES_ZH.put(188, "刚毛猎狐梗");
        IMAGENET_CLASSES_ZH.put(189, "湖畔梗");
        IMAGENET_CLASSES_ZH.put(190, "西里汉梗");
        IMAGENET_CLASSES_ZH.put(191, "万能梗");
        IMAGENET_CLASSES_ZH.put(192, "凯恩梗");
        IMAGENET_CLASSES_ZH.put(193, "澳大利亚梗");
        IMAGENET_CLASSES_ZH.put(194, "丹迪丁蒙梗");
        IMAGENET_CLASSES_ZH.put(195, "波士顿斗牛犬");
        IMAGENET_CLASSES_ZH.put(196, "迷你雪纳瑞");
        IMAGENET_CLASSES_ZH.put(197, "巨型雪纳瑞");
        IMAGENET_CLASSES_ZH.put(198, "标准雪纳瑞");
        IMAGENET_CLASSES_ZH.put(199, "苏格兰梗");
        IMAGENET_CLASSES_ZH.put(200, "西藏梗");
        IMAGENET_CLASSES_ZH.put(201, "丝毛梗");
        IMAGENET_CLASSES_ZH.put(202, "软毛麦色梗");
        IMAGENET_CLASSES_ZH.put(203, "西高地白梗");
        IMAGENET_CLASSES_ZH.put(204, "拉萨犬");
        IMAGENET_CLASSES_ZH.put(205, "平毛寻回犬");
        IMAGENET_CLASSES_ZH.put(206, "卷毛寻回犬");
        IMAGENET_CLASSES_ZH.put(207, "金毛寻回犬");
        IMAGENET_CLASSES_ZH.put(208, "拉布拉多寻回犬");
        IMAGENET_CLASSES_ZH.put(209, "切萨皮克湾寻回犬");
        IMAGENET_CLASSES_ZH.put(210, "德国短毛指示犬");
        IMAGENET_CLASSES_ZH.put(211, "匈牙利维兹拉犬");
        IMAGENET_CLASSES_ZH.put(212, "英国塞特犬");
        IMAGENET_CLASSES_ZH.put(213, "爱尔兰塞特犬");
        IMAGENET_CLASSES_ZH.put(214, "戈登塞特犬");
        IMAGENET_CLASSES_ZH.put(215, "布列塔尼猎犬");
        IMAGENET_CLASSES_ZH.put(216, "克伦伯猎犬");
        IMAGENET_CLASSES_ZH.put(217, "英国史宾格犬");
        IMAGENET_CLASSES_ZH.put(218, "威尔士史宾格犬");
        IMAGENET_CLASSES_ZH.put(219, "可卡犬");
        IMAGENET_CLASSES_ZH.put(220, "萨塞克斯猎犬");
        IMAGENET_CLASSES_ZH.put(221, "爱尔兰水猎犬");
        IMAGENET_CLASSES_ZH.put(222, "库瓦兹犬");
        IMAGENET_CLASSES_ZH.put(223, "史奇派克犬");
        IMAGENET_CLASSES_ZH.put(224, "比利时牧羊犬");
        IMAGENET_CLASSES_ZH.put(225, "马利诺斯犬");
        IMAGENET_CLASSES_ZH.put(226, "伯瑞犬");
        IMAGENET_CLASSES_ZH.put(227, "澳洲卡尔比犬");
        IMAGENET_CLASSES_ZH.put(228, "可蒙犬");
        IMAGENET_CLASSES_ZH.put(229, "古代英国牧羊犬");
        IMAGENET_CLASSES_ZH.put(230, "喜乐蒂牧羊犬");
        IMAGENET_CLASSES_ZH.put(231, "柯利牧羊犬");
        IMAGENET_CLASSES_ZH.put(232, "边境牧羊犬");
        IMAGENET_CLASSES_ZH.put(233, "法兰德斯牧牛犬");
        IMAGENET_CLASSES_ZH.put(234, "罗威纳犬");
        IMAGENET_CLASSES_ZH.put(235, "德国牧羊犬");
        IMAGENET_CLASSES_ZH.put(236, "杜宾犬");
        IMAGENET_CLASSES_ZH.put(237, "迷你杜宾犬");
        IMAGENET_CLASSES_ZH.put(238, "大瑞士山地犬");
        IMAGENET_CLASSES_ZH.put(239, "伯恩山犬");
        IMAGENET_CLASSES_ZH.put(240, "阿彭策尔山犬");
        IMAGENET_CLASSES_ZH.put(241, "恩特雷布赫山犬");
        IMAGENET_CLASSES_ZH.put(242, "拳师犬");
        IMAGENET_CLASSES_ZH.put(243, "斗牛獒");
        IMAGENET_CLASSES_ZH.put(244, "藏獒");
        IMAGENET_CLASSES_ZH.put(245, "法国斗牛犬");
        IMAGENET_CLASSES_ZH.put(246, "大丹犬");
        IMAGENET_CLASSES_ZH.put(247, "圣伯纳犬");
        IMAGENET_CLASSES_ZH.put(248, "爱斯基摩犬");
        IMAGENET_CLASSES_ZH.put(249, "阿拉斯加马拉缪特犬");
        IMAGENET_CLASSES_ZH.put(250, "西伯利亚哈士奇");
        IMAGENET_CLASSES_ZH.put(251, "达尔马提亚犬");
        IMAGENET_CLASSES_ZH.put(252, "猴犬");
        IMAGENET_CLASSES_ZH.put(253, "巴仙吉犬");
        IMAGENET_CLASSES_ZH.put(254, "巴哥犬");
        IMAGENET_CLASSES_ZH.put(255, "兰波格犬");
        IMAGENET_CLASSES_ZH.put(256, "纽芬兰犬");
        IMAGENET_CLASSES_ZH.put(257, "大白熊犬");
        IMAGENET_CLASSES_ZH.put(258, "萨摩耶犬");
        IMAGENET_CLASSES_ZH.put(259, "博美犬");
        IMAGENET_CLASSES_ZH.put(260, "松狮犬");
        IMAGENET_CLASSES_ZH.put(261, "荷兰毛狮犬");
        IMAGENET_CLASSES_ZH.put(262, "布鲁塞尔格里芬犬");
        IMAGENET_CLASSES_ZH.put(263, "彭布罗克威尔士柯基犬");
        IMAGENET_CLASSES_ZH.put(264, "卡迪根威尔士柯基犬");
        IMAGENET_CLASSES_ZH.put(265, "玩具贵宾犬");
        IMAGENET_CLASSES_ZH.put(266, "迷你贵宾犬");
        IMAGENET_CLASSES_ZH.put(267, "标准贵宾犬");
        IMAGENET_CLASSES_ZH.put(268, "墨西哥无毛犬");

        // 动物类 - 猫科 中文映射
        IMAGENET_CLASSES_ZH.put(281, "虎斑猫");
        IMAGENET_CLASSES_ZH.put(282, "虎猫");
        IMAGENET_CLASSES_ZH.put(283, "波斯猫");
        IMAGENET_CLASSES_ZH.put(284, "暹罗猫");
        IMAGENET_CLASSES_ZH.put(285, "埃及猫");
        IMAGENET_CLASSES_ZH.put(286, "美洲狮");
        IMAGENET_CLASSES_ZH.put(287, "猞猁");
        IMAGENET_CLASSES_ZH.put(288, "豹");
        IMAGENET_CLASSES_ZH.put(289, "雪豹");
        IMAGENET_CLASSES_ZH.put(290, "美洲虎");
        IMAGENET_CLASSES_ZH.put(291, "狮子");
        IMAGENET_CLASSES_ZH.put(292, "老虎");
        IMAGENET_CLASSES_ZH.put(293, "猎豹");

        // 动物类 - 海洋生物 中文映射
        IMAGENET_CLASSES_ZH.put(0, "丁鲷");
        IMAGENET_CLASSES_ZH.put(1, "金鱼");
        IMAGENET_CLASSES_ZH.put(2, "大白鲨");
        IMAGENET_CLASSES_ZH.put(3, "虎鲨");
        IMAGENET_CLASSES_ZH.put(4, "双髻鲨");
        IMAGENET_CLASSES_ZH.put(5, "电鳐");
        IMAGENET_CLASSES_ZH.put(6, "黄貂鱼");

        // 动物类 - 鸟类 中文映射
        IMAGENET_CLASSES_ZH.put(8, "公鸡");
        IMAGENET_CLASSES_ZH.put(9, "母鸡");
        IMAGENET_CLASSES_ZH.put(10, "鸵鸟");
        IMAGENET_CLASSES_ZH.put(11, "燕雀");
        IMAGENET_CLASSES_ZH.put(12, "金翅雀");
        IMAGENET_CLASSES_ZH.put(13, "家朱雀");
        IMAGENET_CLASSES_ZH.put(14, "灯草雀");
        IMAGENET_CLASSES_ZH.put(15, "靛蓝彩鹀");
        IMAGENET_CLASSES_ZH.put(16, "知更鸟");
        IMAGENET_CLASSES_ZH.put(17, "鹎");
        IMAGENET_CLASSES_ZH.put(18, "喜鹊");
        IMAGENET_CLASSES_ZH.put(19, "山雀");
        IMAGENET_CLASSES_ZH.put(20, "河鸟");
        IMAGENET_CLASSES_ZH.put(21, "鸢");
        IMAGENET_CLASSES_ZH.put(22, "白头鹰");
        IMAGENET_CLASSES_ZH.put(23, "秃鹫");
        IMAGENET_CLASSES_ZH.put(24, "大灰猫头鹰");

        // 动物类 - 其他哺乳动物 中文映射
        IMAGENET_CLASSES_ZH.put(330, "海狮");
        IMAGENET_CLASSES_ZH.put(331, "吉娃娃");
        IMAGENET_CLASSES_ZH.put(332, "魏玛犬");
        IMAGENET_CLASSES_ZH.put(333, "红狐");
        IMAGENET_CLASSES_ZH.put(334, "沙狐");
        IMAGENET_CLASSES_ZH.put(335, "北极狐");
        IMAGENET_CLASSES_ZH.put(336, "灰狐");
        IMAGENET_CLASSES_ZH.put(337, "虎斑猫");
        IMAGENET_CLASSES_ZH.put(338, "虎猫");
        IMAGENET_CLASSES_ZH.put(339, "波斯猫");
        IMAGENET_CLASSES_ZH.put(340, "暹罗猫");
        IMAGENET_CLASSES_ZH.put(341, "埃及猫");
        IMAGENET_CLASSES_ZH.put(342, "美洲狮");
        IMAGENET_CLASSES_ZH.put(343, "猞猁");
        IMAGENET_CLASSES_ZH.put(344, "豹");
        IMAGENET_CLASSES_ZH.put(345, "雪豹");
        IMAGENET_CLASSES_ZH.put(346, "美洲虎");
        IMAGENET_CLASSES_ZH.put(347, "狮子");
        IMAGENET_CLASSES_ZH.put(348, "老虎");
        IMAGENET_CLASSES_ZH.put(349, "猎豹");
        IMAGENET_CLASSES_ZH.put(350, "棕熊");
        IMAGENET_CLASSES_ZH.put(351, "美洲黑熊");
        IMAGENET_CLASSES_ZH.put(352, "北极熊");
        IMAGENET_CLASSES_ZH.put(353, "树懒熊");
        IMAGENET_CLASSES_ZH.put(354, "猫鼬");
        IMAGENET_CLASSES_ZH.put(355, "狐獴");
        IMAGENET_CLASSES_ZH.put(356, "虎甲虫");
        IMAGENET_CLASSES_ZH.put(357, "瓢虫");
        IMAGENET_CLASSES_ZH.put(358, "步甲");
        IMAGENET_CLASSES_ZH.put(359, "天牛");
        IMAGENET_CLASSES_ZH.put(360, "叶甲");
        IMAGENET_CLASSES_ZH.put(361, "屎壳郎");
        IMAGENET_CLASSES_ZH.put(362, "犀牛甲虫");
        IMAGENET_CLASSES_ZH.put(363, "象鼻虫");
        IMAGENET_CLASSES_ZH.put(364, "苍蝇");
        IMAGENET_CLASSES_ZH.put(365, "蜜蜂");
        IMAGENET_CLASSES_ZH.put(366, "蚂蚁");
        IMAGENET_CLASSES_ZH.put(367, "蚱蜢");
        IMAGENET_CLASSES_ZH.put(368, "蟋蟀");
        IMAGENET_CLASSES_ZH.put(369, "竹节虫");
        IMAGENET_CLASSES_ZH.put(370, "蟑螂");
        IMAGENET_CLASSES_ZH.put(371, "螳螂");
        IMAGENET_CLASSES_ZH.put(372, "蝉");
        IMAGENET_CLASSES_ZH.put(373, "叶蝉");
        IMAGENET_CLASSES_ZH.put(374, "草蜻蛉");
        IMAGENET_CLASSES_ZH.put(375, "蜻蜓");
        IMAGENET_CLASSES_ZH.put(376, "豆娘");

        // 更多鸟类 中文映射
        IMAGENET_CLASSES_ZH.put(377, "优红蛱蝶");
        IMAGENET_CLASSES_ZH.put(378, "小环蛱蝶");
        IMAGENET_CLASSES_ZH.put(379, "黑脉金斑蝶");
        IMAGENET_CLASSES_ZH.put(380, "菜粉蝶");
        IMAGENET_CLASSES_ZH.put(381, "粉蝶");
        IMAGENET_CLASSES_ZH.put(382, "灰蝶");
        IMAGENET_CLASSES_ZH.put(383, "海星");
        IMAGENET_CLASSES_ZH.put(384, "海胆");
        IMAGENET_CLASSES_ZH.put(385, "海参");
        IMAGENET_CLASSES_ZH.put(386, "木兔");
        IMAGENET_CLASSES_ZH.put(387, "野兔");
        IMAGENET_CLASSES_ZH.put(388, "安哥拉兔");
        IMAGENET_CLASSES_ZH.put(389, "仓鼠");
        IMAGENET_CLASSES_ZH.put(390, "豪猪");
        IMAGENET_CLASSES_ZH.put(391, "狐松鼠");
        IMAGENET_CLASSES_ZH.put(392, "土拨鼠");
        IMAGENET_CLASSES_ZH.put(393, "河狸");
        IMAGENET_CLASSES_ZH.put(394, "豚鼠");
        IMAGENET_CLASSES_ZH.put(395, "栗色马");
        IMAGENET_CLASSES_ZH.put(396, "斑马");
        IMAGENET_CLASSES_ZH.put(397, "猪");
        IMAGENET_CLASSES_ZH.put(398, "野猪");
        IMAGENET_CLASSES_ZH.put(399, "疣猪");
        IMAGENET_CLASSES_ZH.put(400, "河马");
        IMAGENET_CLASSES_ZH.put(401, "牛");
        IMAGENET_CLASSES_ZH.put(402, "水牛");
        IMAGENET_CLASSES_ZH.put(403, "野牛");
        IMAGENET_CLASSES_ZH.put(404, "公羊");
        IMAGENET_CLASSES_ZH.put(405, "大角羊");
        IMAGENET_CLASSES_ZH.put(406, "野山羊");
        IMAGENET_CLASSES_ZH.put(407, "狷羚");
        IMAGENET_CLASSES_ZH.put(408, "黑斑羚");
        IMAGENET_CLASSES_ZH.put(409, "瞪羚");
        IMAGENET_CLASSES_ZH.put(410, "阿拉伯骆驼");
        IMAGENET_CLASSES_ZH.put(411, "羊驼");
        IMAGENET_CLASSES_ZH.put(412, "鼬");
        IMAGENET_CLASSES_ZH.put(413, "水貂");
        IMAGENET_CLASSES_ZH.put(414, "欧洲臭鼬");
        IMAGENET_CLASSES_ZH.put(415, "黑足鼬");
        IMAGENET_CLASSES_ZH.put(416, "水獭");
        IMAGENET_CLASSES_ZH.put(417, "臭鼬");
        IMAGENET_CLASSES_ZH.put(418, "獾");
        IMAGENET_CLASSES_ZH.put(419, "犰狳");
        IMAGENET_CLASSES_ZH.put(420, "三趾树懒");
        IMAGENET_CLASSES_ZH.put(421, "猩猩");
        IMAGENET_CLASSES_ZH.put(422, "大猩猩");
        IMAGENET_CLASSES_ZH.put(423, "黑猩猩");
        IMAGENET_CLASSES_ZH.put(424, "长臂猿");
        IMAGENET_CLASSES_ZH.put(425, "合趾猿");
        IMAGENET_CLASSES_ZH.put(426, "长尾猴");
        IMAGENET_CLASSES_ZH.put(427, "赤猴");
        IMAGENET_CLASSES_ZH.put(428, "狒狒");
        IMAGENET_CLASSES_ZH.put(429, "猕猴");
        IMAGENET_CLASSES_ZH.put(430, "叶猴");
        IMAGENET_CLASSES_ZH.put(431, "疣猴");
        IMAGENET_CLASSES_ZH.put(432, "长鼻猴");
        IMAGENET_CLASSES_ZH.put(433, "狨猴");
        IMAGENET_CLASSES_ZH.put(434, "卷尾猴");
        IMAGENET_CLASSES_ZH.put(435, "吼猴");
        IMAGENET_CLASSES_ZH.put(436, "伶猴");
        IMAGENET_CLASSES_ZH.put(437, "蜘蛛猴");
        IMAGENET_CLASSES_ZH.put(438, "松鼠猴");
        IMAGENET_CLASSES_ZH.put(439, "马达加斯加猫");

        // 美食类 中文映射
        IMAGENET_CLASSES_ZH.put(907, "卡博拉纳意面");
        IMAGENET_CLASSES_ZH.put(924, "贝果");
        IMAGENET_CLASSES_ZH.put(925, "椒盐卷饼");
        IMAGENET_CLASSES_ZH.put(926, "芝士汉堡");
        IMAGENET_CLASSES_ZH.put(927, "热狗");
        IMAGENET_CLASSES_ZH.put(928, "土豆泥");
        IMAGENET_CLASSES_ZH.put(929, "卷心菜");
        IMAGENET_CLASSES_ZH.put(930, "西兰花");
        IMAGENET_CLASSES_ZH.put(931, "花椰菜");
        IMAGENET_CLASSES_ZH.put(932, "西葫芦");
        IMAGENET_CLASSES_ZH.put(933, "意大利面南瓜");
        IMAGENET_CLASSES_ZH.put(934, "橡果南瓜");
        IMAGENET_CLASSES_ZH.put(935, "冬南瓜");
        IMAGENET_CLASSES_ZH.put(936, "黄瓜");
        IMAGENET_CLASSES_ZH.put(937, "朝鲜蓟");
        IMAGENET_CLASSES_ZH.put(938, "甜椒");
        IMAGENET_CLASSES_ZH.put(939, "刺菜蓟");
        IMAGENET_CLASSES_ZH.put(940, "蘑菇");
        IMAGENET_CLASSES_ZH.put(941, "青苹果");
        IMAGENET_CLASSES_ZH.put(942, "草莓");
        IMAGENET_CLASSES_ZH.put(943, "橙子");
        IMAGENET_CLASSES_ZH.put(944, "柠檬");
        IMAGENET_CLASSES_ZH.put(945, "无花果");
        IMAGENET_CLASSES_ZH.put(946, "菠萝");
        IMAGENET_CLASSES_ZH.put(947, "香蕉");
        IMAGENET_CLASSES_ZH.put(948, "菠萝蜜");
        IMAGENET_CLASSES_ZH.put(949, "番荔枝");
        IMAGENET_CLASSES_ZH.put(950, "石榴");
        IMAGENET_CLASSES_ZH.put(951, "干草");
        IMAGENET_CLASSES_ZH.put(952, "卡博拉纳意面");
        IMAGENET_CLASSES_ZH.put(953, "巧克力酱");
        IMAGENET_CLASSES_ZH.put(954, "面团");
        IMAGENET_CLASSES_ZH.put(955, "肉饼");
        IMAGENET_CLASSES_ZH.put(956, "披萨");
        IMAGENET_CLASSES_ZH.put(957, "锅饼");
        IMAGENET_CLASSES_ZH.put(958, "墨西哥卷饼");
        IMAGENET_CLASSES_ZH.put(959, "红酒");
        IMAGENET_CLASSES_ZH.put(960, "浓缩咖啡");
        IMAGENET_CLASSES_ZH.put(961, "杯子");
        IMAGENET_CLASSES_ZH.put(962, "蛋奶酒");
        IMAGENET_CLASSES_ZH.put(963, "高山");
        IMAGENET_CLASSES_ZH.put(964, "气泡");
        IMAGENET_CLASSES_ZH.put(965, "悬崖");
        IMAGENET_CLASSES_ZH.put(966, "珊瑚礁");
        IMAGENET_CLASSES_ZH.put(967, "间歇泉");
        IMAGENET_CLASSES_ZH.put(968, "湖边");
        IMAGENET_CLASSES_ZH.put(969, "海角");

        // 人像类 中文映射
        IMAGENET_CLASSES_ZH.put(440, "学士袍");
        IMAGENET_CLASSES_ZH.put(441, "围裙");
        IMAGENET_CLASSES_ZH.put(442, "背包");
        IMAGENET_CLASSES_ZH.put(850, "西装");
        IMAGENET_CLASSES_ZH.put(851, "运动衫");
        IMAGENET_CLASSES_ZH.put(852, "T恤衫");

        // 建筑类 中文映射
        IMAGENET_CLASSES_ZH.put(483, "教堂");
        IMAGENET_CLASSES_ZH.put(484, "清真寺");
        IMAGENET_CLASSES_ZH.put(485, "佛塔");
        IMAGENET_CLASSES_ZH.put(486, "凯旋门");
        IMAGENET_CLASSES_ZH.put(487, "庭院");
        IMAGENET_CLASSES_ZH.put(488, "钢拱桥");
        IMAGENET_CLASSES_ZH.put(489, "悬索桥");
        IMAGENET_CLASSES_ZH.put(490, "高架桥");
        IMAGENET_CLASSES_ZH.put(491, "谷仓");
        IMAGENET_CLASSES_ZH.put(492, "温室");
        IMAGENET_CLASSES_ZH.put(493, "宫殿");
        IMAGENET_CLASSES_ZH.put(494, "修道院");
        IMAGENET_CLASSES_ZH.put(495, "图书馆");

        // 交通工具类 中文映射
        IMAGENET_CLASSES_ZH.put(609, "吉普车");
        IMAGENET_CLASSES_ZH.put(610, "豪华轿车");
        IMAGENET_CLASSES_ZH.put(611, "小型货车");
        IMAGENET_CLASSES_ZH.put(612, "T型车");
        IMAGENET_CLASSES_ZH.put(613, "赛车");
        IMAGENET_CLASSES_ZH.put(614, "跑车");
        IMAGENET_CLASSES_ZH.put(615, "敞篷车");
        IMAGENET_CLASSES_ZH.put(616, "出租车");
        IMAGENET_CLASSES_ZH.put(617, "房车");
        IMAGENET_CLASSES_ZH.put(618, "拖车");
        IMAGENET_CLASSES_ZH.put(619, "垃圾车");
        IMAGENET_CLASSES_ZH.put(620, "消防车");
        IMAGENET_CLASSES_ZH.put(621, "救护车");
        IMAGENET_CLASSES_ZH.put(622, "校车");
        IMAGENET_CLASSES_ZH.put(623, "无轨电车");
        IMAGENET_CLASSES_ZH.put(624, "手推车");
        IMAGENET_CLASSES_ZH.put(625, "扫雪车");
        IMAGENET_CLASSES_ZH.put(626, "拖拉机");
        IMAGENET_CLASSES_ZH.put(627, "收割机");
        IMAGENET_CLASSES_ZH.put(628, "脱粒机");
        IMAGENET_CLASSES_ZH.put(629, "自行车");
        IMAGENET_CLASSES_ZH.put(630, "山地自行车");
        IMAGENET_CLASSES_ZH.put(631, "小型摩托车");
        IMAGENET_CLASSES_ZH.put(632, "轻骑车");
        IMAGENET_CLASSES_ZH.put(633, "货车车厢");
        IMAGENET_CLASSES_ZH.put(634, "客车车厢");
        IMAGENET_CLASSES_ZH.put(635, "独轮车");
        IMAGENET_CLASSES_ZH.put(636, "购物车");
        IMAGENET_CLASSES_ZH.put(637, "贡多拉");
        IMAGENET_CLASSES_ZH.put(638, "独木舟");
        IMAGENET_CLASSES_ZH.put(639, "小帆船");
        IMAGENET_CLASSES_ZH.put(640, "客机");
        IMAGENET_CLASSES_ZH.put(641, "战斗机");
        IMAGENET_CLASSES_ZH.put(642, "飞艇");
        IMAGENET_CLASSES_ZH.put(643, "气球");
        IMAGENET_CLASSES_ZH.put(644, "快艇");
        IMAGENET_CLASSES_ZH.put(645, "远洋轮船");

        // 自然类 - 植物/动物 中文映射
        IMAGENET_CLASSES_ZH.put(691, "蜗牛");
        IMAGENET_CLASSES_ZH.put(692, "蛞蝓");
        IMAGENET_CLASSES_ZH.put(693, "海蛞蝓");
        IMAGENET_CLASSES_ZH.put(694, "石鳖");
        IMAGENET_CLASSES_ZH.put(695, "鹦鹉螺");
        IMAGENET_CLASSES_ZH.put(696, "珍宝蟹");
        IMAGENET_CLASSES_ZH.put(697, "石蟹");
        IMAGENET_CLASSES_ZH.put(698, "招潮蟹");
        IMAGENET_CLASSES_ZH.put(699, "帝王蟹");
        IMAGENET_CLASSES_ZH.put(700, "美洲龙虾");
        IMAGENET_CLASSES_ZH.put(701, "棘刺龙虾");
        IMAGENET_CLASSES_ZH.put(702, "小龙虾");
        IMAGENET_CLASSES_ZH.put(703, "寄居蟹");
        IMAGENET_CLASSES_ZH.put(704, "等足目动物");
        IMAGENET_CLASSES_ZH.put(705, "白鹳");
        IMAGENET_CLASSES_ZH.put(706, "黑鹳");
        IMAGENET_CLASSES_ZH.put(707, "琵鹭");
        IMAGENET_CLASSES_ZH.put(708, "火烈鸟");
        IMAGENET_CLASSES_ZH.put(709, "小蓝鹭");
        IMAGENET_CLASSES_ZH.put(710, "美洲白鹭");
        IMAGENET_CLASSES_ZH.put(711, "麻鳽");
        IMAGENET_CLASSES_ZH.put(712, "鹤");
        IMAGENET_CLASSES_ZH.put(713, "秧鹤");
        IMAGENET_CLASSES_ZH.put(714, "欧洲水鸡");
        IMAGENET_CLASSES_ZH.put(715, "美洲骨顶鸡");
        IMAGENET_CLASSES_ZH.put(716, "鸨");
        IMAGENET_CLASSES_ZH.put(717, "翻石鹬");
        IMAGENET_CLASSES_ZH.put(718, "红背滨鹬");
        IMAGENET_CLASSES_ZH.put(719, "红脚鹬");
        IMAGENET_CLASSES_ZH.put(720, "长嘴鹬");
        IMAGENET_CLASSES_ZH.put(721, "蛎鹬");
        IMAGENET_CLASSES_ZH.put(722, "鹈鹕");
        IMAGENET_CLASSES_ZH.put(723, "王企鹅");
        IMAGENET_CLASSES_ZH.put(724, "信天翁");
        IMAGENET_CLASSES_ZH.put(725, "灰鲸");
        IMAGENET_CLASSES_ZH.put(726, "虎鲸");
        IMAGENET_CLASSES_ZH.put(727, "儒艮");
        IMAGENET_CLASSES_ZH.put(728, "海狮");
        IMAGENET_CLASSES_ZH.put(729, "吉娃娃");

        // 自然风景类 中文映射
        IMAGENET_CLASSES_ZH.put(971, "高山");
        IMAGENET_CLASSES_ZH.put(972, "悬崖");
        IMAGENET_CLASSES_ZH.put(973, "珊瑚礁");
        IMAGENET_CLASSES_ZH.put(974, "间歇泉");
        IMAGENET_CLASSES_ZH.put(975, "湖边");
        IMAGENET_CLASSES_ZH.put(976, "海角");
        IMAGENET_CLASSES_ZH.put(977, "沙洲");
        IMAGENET_CLASSES_ZH.put(986, "湖边");

        // 其他常见类别 中文映射
        IMAGENET_CLASSES_ZH.put(988, "球员");
        IMAGENET_CLASSES_ZH.put(989, "高尔夫球");
    }

    private static void initCustomCategories() {
        // 风景类别 - 使用中文标签
        CUSTOM_CATEGORIES.put("风景", Arrays.asList(
            "mountain", "valley", "alp", "cliff", "coral reef",
            "geyser", "lakeside", "seashore", "sandbar", "volcano",
            "promontory",
            "山", "山谷", "高山", "悬崖", "珊瑚礁", "间歇泉", "湖边", "海岸", "沙洲", "火山",
            "海角"
        ));

        // 动物类别 - 狗
        CUSTOM_CATEGORIES.put("动物", Arrays.asList(
            // 狗品种
            "chihuahua", "Japanese spaniel", "Maltese dog", "Pekinese",
            "Shih-Tzu", "Blenheim spaniel", "papillon", "toy terrier",
            "Rhodesian ridgeback", "Afghan hound", "basset", "beagle",
            "bloodhound", "bluetick", "black-and-tan coonhound", "Walker hound",
            "English foxhound", "redbone", "borzoi", "Irish wolfhound",
            "Italian greyhound", "whippet", "Ibizan hound", "Norwegian elkhound",
            "otterhound", "Saluki", "Scottish deerhound", "Weimaraner",
            "Staffordshire bullterrier", "American Staffordshire terrier",
            "Bedlington terrier", "Border terrier", "Kerry blue terrier",
            "Irish terrier", "Norfolk terrier", "Norwich terrier", "Yorkshire terrier",
            "wire-haired fox terrier", "Lakeland terrier", "Sealyham terrier",
            "Airedale", "cairn", "Australian terrier", "Dandie Dinmont",
            "Boston bull", "miniature schnauzer", "giant schnauzer",
            "standard schnauzer", "Scotch terrier", "Tibetan terrier", "silky terrier",
            "soft-coated wheaten terrier", "West Highland white terrier", "Lhasa",
            "flat-coated retriever", "curly-coated retriever", "golden retriever",
            "Labrador retriever", "Chesapeake Bay retriever", "German short-haired pointer",
            "vizsla", "English setter", "Irish setter", "Gordon setter",
            "Brittany spaniel", "clumber", "English springer", "Welsh springer spaniel",
            "cocker spaniel", "Sussex spaniel", "Irish water spaniel", "kuvasz",
            "schipperke", "groenendael", "malinois", "briard", "kelpie", "komondor",
            "Old English sheepdog", "Shetland sheepdog", "collie", "Border collie",
            "Bouvier des Flandres", "Rottweiler", "German shepherd", "Doberman",
            "miniature pinscher", "Greater Swiss Mountain dog", "Bernese mountain dog",
            "Appenzeller", "EntleBucher", "boxer", "bull mastiff", "Tibetan mastiff",
            "French bulldog", "Great Dane", "Saint Bernard", "Eskimo dog", "malamute",
            "Siberian husky", "dalmatian", "affenpinscher", "basenji", "pug",
            "Leonberg", "Newfoundland", "Great Pyrenees", "Samoyed", "Pomeranian",
            "chow", "keeshond", "Brabancon griffon", "Pembroke", "Cardigan",
            "toy poodle", "miniature poodle", "standard poodle", "Mexican hairless",
            // 猫科
            "tabby", "tiger cat", "Persian cat", "Siamese cat", "Egyptian cat",
            "cougar", "lynx", "leopard", "snow leopard", "jaguar", "lion", "tiger", "cheetah",
            // 海洋生物
            "tench", "goldfish", "great white shark", "tiger shark", "hammerhead",
            "electric ray", "stingray",
            // 鸟类
            "cock", "hen", "ostrich", "brambling", "goldfinch", "house finch",
            "junco", "indigo bunting", "robin", "bulbul", "magpie", "chickadee",
            "water ouzel", "kite", "bald eagle", "vulture", "great grey owl",
            "white stork", "black stork", "spoonbill", "flamingo", "little blue heron",
            "American egret", "bittern", "crane", "limpkin", "European gallinule",
            "American coot", "bustard", "ruddy turnstone", "red-backed sandpiper",
            "redshank", "dowitcher", "oystercatcher", "pelican", "king penguin",
            "albatross",
            // 哺乳动物
            "sea lion", "red fox", "kit fox", "Arctic fox", "grey fox",
            "brown bear", "American black bear", "ice bear", "sloth bear",
            "mongoose", "meerkat", "sea urchin", "sea cucumber",
            "wood rabbit", "hare", "Angora", "hamster", "porcupine",
            "fox squirrel", "marmot", "beaver", "guinea pig",
            "sorrel", "zebra", "hog", "wild boar", "warthog", "hippopotamus",
            "ox", "water buffalo", "bison", "ram", "bighorn", "ibex",
            "hartebeest", "impala", "gazelle", "Arabian camel", "llama",
            "weasel", "mink", "polecat", "black-footed ferret", "otter",
            "skunk", "badger", "armadillo", "three-toed sloth", "sloth",
            "orangutan", "gorilla", "chimpanzee", "gibbon", "siamang",
            "guenon", "patas", "baboon", "macaque", "langur", "colobus",
            "proboscis monkey", "marmoset", "capuchin", "howler monkey",
            "titi", "spider monkey", "squirrel monkey", "Madagascar cat",
            // 昆虫
            "tiger beetle", "ladybug", "ground beetle", "long-horned beetle",
            "leaf beetle", "dung beetle", "rhinoceros beetle", "weevil",
            "fly", "bee", "ant", "grasshopper", "cricket", "walking stick",
            "cockroach", "mantis", "cicada", "leafhopper", "lacewing",
            "dragonfly", "damselfly",
            "admiral", "ringlet", "monarch", "cabbage butterfly", "sulphur butterfly", "lycaenid",
            // 水生动物
            "snail", "slug", "sea slug", "chiton", "chambered nautilus",
            "Dungeness crab", "rock crab", "fiddler crab", "king crab",
            "American lobster", "spiny lobster", "crayfish", "hermit crab",
            "isopod", "starfish", "grey whale", "killer whale", "dugong",
            // 中文名称
            "吉娃娃", "日本犬", "马尔济斯犬", "北京犬", "西施犬",
            "虎斑猫", "虎猫", "波斯猫", "暹罗猫", "埃及猫",
            "丁鲷", "金鱼", "大白鲨", "丁鲷", "金鱼", "大白鲨",
            "公鸡", "母鸡", "鸵鸟", "燕雀", "金翅雀", "喜鹊", "山雀",
            "卡博拉纳意面", "贝果", "椒盐卷饼", "芝士汉堡", "热狗",
            "学士袍", "围裙", "背包", "西装", "运动衫", "T恤衫",
            "教堂", "清真寺", "佛塔", "凯旋门", "庭院", "钢拱桥",
            "悬索桥", "高架桥", "谷仓", "温室", "宫殿", "修道院",
            "图书馆", "海狮", "红狐", "沙狐", "北极狐", "灰狐",
            "棕熊", "美洲黑熊", "北极熊", "树懒熊", "猫鼬", "狐獴",
            "虎甲虫", "瓢虫", "步甲", "天牛", "叶甲", "屎壳郎",
            "犀牛甲虫", "象鼻虫", "苍蝇", "蜜蜂", "蚂蚁", "蚱蜢",
            "蛞蝓", "海蛞蝓", "石鳖", "鹦鹉螺", "珍宝蟹", "石蟹",
            "招潮蟹", "帝王蟹", "美洲龙虾", "棘刺龙虾", "小龙虾",
            "木兔", "野兔", "安哥拉兔", "仓鼠", "豪猪", "狐松鼠",
            "土拨鼠", "河狸", "豚鼠", "栗色马", "斑马", "猪", "野猪",
            "疣猪", "河马", "牛", "水牛", "野牛", "公羊", "大角羊",
            "野山羊", "狷羚", "黑斑羚", "瞪羚", "阿拉伯骆驼", "羊驼",
            "松鼠", "海星", "海胆", "海参"
        ));

        // 美食类别 - 使用中文标签
        CUSTOM_CATEGORIES.put("美食", Arrays.asList(
            "carbonara", "bagel", "pretzel", "cheeseburger", "hotdog",
            "mashed potato", "head cabbage", "broccoli", "cauliflower", "zucchini",
            "spaghetti squash", "acorn squash", "butternut squash", "cucumber",
            "artichoke", "bell pepper", "cardoon", "mushroom", "Granny Smith",
            "strawberry", "orange", "lemon", "fig", "pineapple", "banana",
            "jackfruit", "custard apple", "pomegranate", "hay", "chocolate sauce",
            "dough", "meat loaf", "pizza", "potpie", "burrito", "red wine",
            "espresso", "cup", "eggnog",
            "卡博拉纳意面", "贝果", "椒盐卷饼", "芝士汉堡", "热狗", "土豆泥", "卷心菜",
            "西兰花", "花椰菜", "西葫芦", "意大利面南瓜", "橡果南瓜", "冬南瓜",
            "黄瓜", "朝鲜蓟", "甜椒", "刺菜蓟", "蘑菇", "青苹果", "草莓",
            "橙子", "柠檬", "无花果", "菠萝", "香蕉", "菠萝蜜", "番荔枝",
            "石榴", "干草", "巧克力酱", "面团", "肉饼", "披萨", "锅饼",
            "墨西哥卷饼", "红酒", "浓缩咖啡", "杯子", "蛋奶酒"
        ));

        // 人像类别 - 使用中文标签
        CUSTOM_CATEGORIES.put("人像", Arrays.asList(
            "academic gown", "apron", "backpack", "suit", "sweatshirt", "T-shirt",
            "学士袍", "围裙", "背包", "西装", "运动衫", "T恤衫"
        ));

        // 建筑类别 - 使用中文标签
        CUSTOM_CATEGORIES.put("建筑", Arrays.asList(
            "church", "mosque", "stupa", "triumphal arch", "patio",
            "steel arch bridge", "suspension bridge", "viaduct", "barn",
            "greenhouse", "palace", "monastery", "library",
            "教堂", "清真寺", "佛塔", "凯旋门", "庭院", "钢拱桥", "悬索桥", "高架桥",
            "谷仓", "温室", "宫殿", "修道院", "图书馆"
        ));

        // 其他类别 - 使用中文标签
        CUSTOM_CATEGORIES.put("其他", Arrays.asList(
            "ballplayer", "golf ball", "球员", "高尔夫球"
        ));

        // 交通工具类别
        CUSTOM_CATEGORIES.put("交通工具", Arrays.asList(
            "jeep", "limousine", "minivan", "Model T", "racer", "sports car",
            "convertible", "cab", "recreational vehicle", "tow truck", "garbage truck",
            "fire engine", "ambulance", "school bus", "trolleybus", "trolley",
            "snowplow", "tractor", "harvester", "thresher", "bicycle",
            "mountain bike", "motor scooter", "moped", "freight car", "passenger car",
            "barrow", "shopping cart", "gondola", "canoe", "yawl",
            "airliner", "warplane", "airship", "balloon", "speedboat", "ocean liner",
            "吉普车", "豪华轿车", "小型货车", "T型车", "赛车", "跑车",
            "敞篷车", "出租车", "房车", "拖车", "垃圾车", "消防车",
            "救护车", "校车", "无轨电车", "手推车", "扫雪车", "拖拉机",
            "收割机", "脱粒机", "自行车", "山地自行车", "小型摩托车",
            "轻骑车", "货车车厢", "客车车厢", "独轮车", "购物车",
            "贡多拉", "独木舟", "小帆船", "客机", "战斗机", "飞艇",
            "气球", "快艇", "远洋轮船"
        ));

        // 自然类别 - 植物、海洋生物等
        CUSTOM_CATEGORIES.put("自然", Arrays.asList(
            // 植物相关
            "hay", "干草", "tree", "flower", "plant", "garden", "park",
            "sky", "cloud", "grass", "field", "leaf", "wood", "stone", "rock",
            "自然", "树木", "花卉", "森林", "河流", "花园", "公园", "天空",
            "云", "草地", "田野", "叶子", "木头", "石头", "岩石",
            // 海洋生物
            "coral reef", "shore", "coast", "beach", "珊瑚礁", "海岸", "沙滩",
            // 天体与自然现象
            "sunset", "sunrise", "moon", "star", "galaxy", "nebula",
            "日落", "日出", "月亮", "星星", "星系", "星云"
        ));
    }

    /**
     * 初始化直接中文标签映射
     * 为常用ImageNet类别提供更准确的中文标签
     */
    private static void initDirectChineseLabelMap() {
        // 动物类 - 狗
        DIRECT_CHINESE_LABEL_MAP.put(151, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(152, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(153, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(154, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(155, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(156, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(157, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(158, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(159, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(160, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(161, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(162, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(163, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(164, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(165, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(166, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(167, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(168, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(169, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(170, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(171, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(172, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(173, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(174, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(175, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(176, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(177, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(178, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(179, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(180, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(181, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(182, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(183, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(184, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(185, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(186, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(187, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(188, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(189, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(190, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(191, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(192, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(193, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(194, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(195, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(196, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(197, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(198, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(199, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(200, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(201, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(202, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(203, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(204, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(205, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(206, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(207, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(208, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(209, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(210, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(211, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(212, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(213, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(214, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(215, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(216, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(217, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(218, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(219, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(220, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(221, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(222, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(223, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(224, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(225, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(226, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(227, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(228, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(229, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(230, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(231, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(232, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(233, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(234, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(235, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(236, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(237, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(238, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(239, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(240, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(241, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(242, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(243, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(244, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(245, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(246, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(247, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(248, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(249, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(250, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(251, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(252, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(253, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(254, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(255, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(256, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(257, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(258, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(259, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(260, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(261, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(262, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(263, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(264, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(265, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(266, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(267, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(268, "动物");

        // 动物类 - 猫科
        DIRECT_CHINESE_LABEL_MAP.put(281, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(282, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(283, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(284, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(285, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(286, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(287, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(288, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(289, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(290, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(291, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(292, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(293, "动物");

        // 动物类 - 海洋生物
        DIRECT_CHINESE_LABEL_MAP.put(0, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(1, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(2, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(3, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(4, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(5, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(6, "动物");

        // 动物类 - 鸟类
        DIRECT_CHINESE_LABEL_MAP.put(8, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(9, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(10, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(11, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(12, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(13, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(14, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(15, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(16, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(17, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(18, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(19, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(20, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(21, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(22, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(23, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(24, "动物");

        // 动物类 - 其他哺乳动物
        DIRECT_CHINESE_LABEL_MAP.put(330, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(331, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(332, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(333, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(334, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(335, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(336, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(337, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(338, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(339, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(340, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(341, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(342, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(343, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(344, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(345, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(346, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(347, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(348, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(349, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(350, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(351, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(352, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(353, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(354, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(355, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(356, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(357, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(358, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(359, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(360, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(361, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(362, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(363, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(364, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(365, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(366, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(367, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(368, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(369, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(370, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(371, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(372, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(373, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(374, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(375, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(376, "动物");

        // 更多鸟类
        DIRECT_CHINESE_LABEL_MAP.put(377, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(378, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(379, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(380, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(381, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(382, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(383, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(384, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(385, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(386, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(387, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(388, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(389, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(390, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(391, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(392, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(393, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(394, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(395, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(396, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(397, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(398, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(399, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(400, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(401, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(402, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(403, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(404, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(405, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(406, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(407, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(408, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(409, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(410, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(411, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(412, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(413, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(414, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(415, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(416, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(417, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(418, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(419, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(420, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(421, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(422, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(423, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(424, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(425, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(426, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(427, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(428, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(429, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(430, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(431, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(432, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(433, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(434, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(435, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(436, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(437, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(438, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(439, "动物");

        // 美食类
        DIRECT_CHINESE_LABEL_MAP.put(907, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(924, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(925, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(926, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(927, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(928, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(929, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(930, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(931, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(932, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(933, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(934, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(935, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(936, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(937, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(938, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(939, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(940, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(941, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(942, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(943, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(944, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(945, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(946, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(947, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(948, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(949, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(950, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(952, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(953, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(954, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(955, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(956, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(957, "美食");
        DIRECT_CHINESE_LABEL_MAP.put(958, "美食");

        // 建筑类
        DIRECT_CHINESE_LABEL_MAP.put(483, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(484, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(485, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(486, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(487, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(488, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(489, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(490, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(491, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(492, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(493, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(494, "建筑");
        DIRECT_CHINESE_LABEL_MAP.put(495, "建筑");

        // 风景类
        DIRECT_CHINESE_LABEL_MAP.put(970, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(978, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(979, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(980, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(981, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(982, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(983, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(984, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(985, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(987, "风景");

        // 交通工具类
        DIRECT_CHINESE_LABEL_MAP.put(609, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(610, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(611, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(612, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(613, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(614, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(615, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(616, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(617, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(618, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(619, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(620, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(621, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(622, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(623, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(624, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(625, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(626, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(627, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(628, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(629, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(630, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(631, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(632, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(633, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(634, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(635, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(636, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(637, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(638, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(639, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(640, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(641, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(642, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(643, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(644, "交通工具");
        DIRECT_CHINESE_LABEL_MAP.put(645, "交通工具");

        // 自然类 - 植物/动物
        DIRECT_CHINESE_LABEL_MAP.put(691, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(692, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(693, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(694, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(695, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(696, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(697, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(698, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(699, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(700, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(701, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(702, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(703, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(704, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(705, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(706, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(707, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(708, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(709, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(710, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(711, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(712, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(713, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(714, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(715, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(716, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(717, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(718, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(719, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(720, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(721, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(722, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(723, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(724, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(725, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(726, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(727, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(728, "动物");
        DIRECT_CHINESE_LABEL_MAP.put(729, "动物");

        // 自然风景类
        DIRECT_CHINESE_LABEL_MAP.put(971, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(972, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(973, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(974, "自然");
        DIRECT_CHINESE_LABEL_MAP.put(975, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(976, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(977, "风景");
        DIRECT_CHINESE_LABEL_MAP.put(986, "风景");

        // 人像类
        DIRECT_CHINESE_LABEL_MAP.put(440, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(441, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(442, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(850, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(851, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(852, "人像");

        // 其他类别
        DIRECT_CHINESE_LABEL_MAP.put(988, "人像");
        DIRECT_CHINESE_LABEL_MAP.put(989, "自然");
    }

    /**
     * 根据ImageNet类别ID获取自定义标签（优先中文类别）
     * @param imagenetClassId ImageNet类别ID
     * @return 自定义标签列表（优先中文类别）
     */
    public static List<String> getCustomLabels(int imagenetClassId) {
        // 首先检查是否有直接的中文标签映射
        String directChineseLabel = DIRECT_CHINESE_LABEL_MAP.get(imagenetClassId);
        if (directChineseLabel != null) {
            List<String> labels = new ArrayList<>();
            labels.add(directChineseLabel);
            return labels;
        }

        String imagenetClassName = IMAGENET_CLASSES.get(imagenetClassId);
        String imagenetClassNameZH = IMAGENET_CLASSES_ZH.get(imagenetClassId);

        if (imagenetClassName == null) {
            return Collections.emptyList();
        }

        List<String> customLabels = new ArrayList<>();

        // 优先添加中文类别标签
        for (Map.Entry<String, List<String>> entry : CUSTOM_CATEGORIES.entrySet()) {
            if (entry.getValue().contains(imagenetClassName) ||
                (imagenetClassNameZH != null && entry.getValue().contains(imagenetClassNameZH))) {
                customLabels.add(entry.getKey()); // 添加中文类别标签
                break; // 只添加一个主要类别
            }
        }

        // 如果没有匹配到任何自定义类别，则添加通用标签
        if (customLabels.isEmpty()) {
            customLabels.add("图像");
        }

        return customLabels;
    }

    /**
     * 根据ImageNet类别ID获取纯中文标签
     * @param imagenetClassId ImageNet类别ID
     * @return 中文标签列表
     */
    public static List<String> getChineseLabels(int imagenetClassId) {
        List<String> allLabels = getCustomLabels(imagenetClassId);

        // 过滤出中文标签（排除英文和AI标签）
        List<String> chineseLabels = new ArrayList<>();
        for (String label : allLabels) {
            // 保留中文字符的标签，过滤掉英文和纯AI标签
            if (containsChineseCharacters(label) && !label.equals("AI识别")) {
                chineseLabels.add(label);
            }
        }

        // 如果没有中文标签，使用第一个标签（如果有的话）
        if (chineseLabels.isEmpty() && !allLabels.isEmpty()) {
            String firstLabel = allLabels.get(0);
            if (!firstLabel.equals("AI识别")) {
                chineseLabels.add(firstLabel);
            }
        }

        return chineseLabels;
    }

    /**
     * 检查字符串是否包含中文字符
     */
    private static boolean containsChineseCharacters(String str) {
        if (str == null) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据ImageNet类别名称获取自定义标签（优先中文）
     * @param imagenetClassName ImageNet类别名称
     * @return 自定义标签列表
     */
    public static List<String> getCustomLabelsByClassName(String imagenetClassName) {
        // 首先尝试查找对应的类别ID
        for (Map.Entry<Integer, String> entry : IMAGENET_CLASSES.entrySet()) {
            if (entry.getValue().equals(imagenetClassName)) {
                // 使用类别ID获取标签
                return getCustomLabels(entry.getKey());
            }
        }

        List<String> customLabels = new ArrayList<>();

        // 如果没有找到对应的类别ID，尝试直接匹配
        for (Map.Entry<String, List<String>> entry : CUSTOM_CATEGORIES.entrySet()) {
            if (entry.getValue().contains(imagenetClassName)) {
                customLabels.add(entry.getKey()); // 添加中文类别标签
                break; // 只添加一个主要类别
            }
        }

        // 如果没有匹配到任何自定义类别，则添加通用标签
        if (customLabels.isEmpty()) {
            customLabels.add("图像");
        }

        return customLabels;
    }
}