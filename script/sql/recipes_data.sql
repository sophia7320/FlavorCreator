-- 菜谱数据（来源：HowToCook 开源项目）
-- 共 30 条菜谱数据，涵盖素菜、荤菜、水产、主食、汤羹、早餐六大类别

-- ============================================
-- 素菜 (6)
-- ============================================

-- 西红柿炒鸡蛋
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'西红柿炒鸡蛋',
NULL, NULL, 1,
'[{"name":"西红柿","quantity":1,"unit":"个"},{"name":"鸡蛋","quantity":2,"unit":"个"},{"name":"食用油","quantity":8,"unit":"ml"},{"name":"盐","quantity":2,"unit":"g"},{"name":"糖","quantity":2,"unit":"g"},{"name":"葱花","quantity":10,"unit":"g"}]',
'[{"order":1,"description":"西红柿洗净，去表皮，去蒂，切成小块"},{"order":2,"description":"鸡蛋打入碗中，加入1g盐搅匀"},{"order":3,"description":"热锅加油，倒入蛋液翻炒至结块微黄，盛出备用"},{"order":4,"description":"锅中加入西红柿块翻炒至软烂"},{"order":5,"description":"加入半熟鸡蛋翻炒均匀"},{"order":6,"description":"加入剩余盐、糖、葱花翻炒均匀后关火盛盘"}]',
'一道酸甜开胃的家常菜肴，嫩滑的鸡蛋裹着软烂多汁的西红柿',
'可根据口味选择甜味或咸味版本，可加番茄酱增加汤汁',
'15', 1, 252,
'["下饭菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 酸辣土豆丝
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'酸辣土豆丝',
NULL, NULL, 1,
'[{"name":"土豆","quantity":240,"unit":"g"},{"name":"大蒜","quantity":4,"unit":"瓣"},{"name":"青椒","quantity":0.5,"unit":"个"},{"name":"红椒","quantity":0.5,"unit":"个"},{"name":"干辣椒","quantity":3,"unit":"个"},{"name":"葱","quantity":1,"unit":"根"},{"name":"生抽","quantity":5,"unit":"ml"},{"name":"陈醋","quantity":10,"unit":"ml"},{"name":"盐","quantity":2,"unit":"g"},{"name":"食用油","quantity":15,"unit":"ml"}]',
'[{"order":1,"description":"土豆去皮切丝，用清水清洗去除多余淀粉，焯水10秒，沥干备用"},{"order":2,"description":"葱切葱花，蒜拍碎切末分两等份，干辣椒切段，青红椒切丝"},{"order":3,"description":"热锅热油下入一半葱花、一半蒜末和干辣椒爆香"},{"order":4,"description":"加入青红椒翻炒几下，加入土豆丝翻炒至变色"},{"order":5,"description":"加生抽、陈醋、剩下的一半蒜末和盐快速翻炒均匀"},{"order":6,"description":"出锅前撒上剩余的葱花，翻匀即可装盘"}]',
'酸辣开胃的经典家常菜，口感脆爽，色泽鲜亮',
'清洗土豆丝淀粉一定要去干净，不然会黏在一起\n加入蒜末、盐后应尽快出锅保留蒜香',
'20', 1, 374,
'["下饭菜","酸辣"]', '家常菜', 1, 0, 0, 0, 0);

-- 地三鲜
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'地三鲜',
NULL, NULL, 1,
'[{"name":"茄子","quantity":200,"unit":"g"},{"name":"土豆","quantity":150,"unit":"g"},{"name":"尖椒","quantity":4,"unit":"个"},{"name":"葱","quantity":5,"unit":"g"},{"name":"蒜","quantity":15,"unit":"g"},{"name":"姜","quantity":5,"unit":"g"},{"name":"豆瓣酱","quantity":15,"unit":"g"},{"name":"生抽","quantity":10,"unit":"ml"},{"name":"糖","quantity":10,"unit":"g"},{"name":"淀粉","quantity":8,"unit":"g"}]',
'[{"order":1,"description":"土豆、茄子洗净去皮，均切成15g不规则小块，尖椒撕成块"},{"order":2,"description":"葱切段，蒜剁碎分两份，姜切沫，调碗汁：生抽、糖、淀粉加清水搅匀"},{"order":3,"description":"热油煎炸土豆3-4分钟至金黄捞出"},{"order":4,"description":"煎炸茄子2-3分钟至变软捞出"},{"order":5,"description":"煎炸尖椒15秒边缘起虎皮后捞出"},{"order":6,"description":"重新热油爆香葱姜和一半蒜，加豆瓣酱炒出红油"},{"order":7,"description":"倒入碗汁大火搅拌至冒泡黏稠"},{"order":8,"description":"下入全部土豆、茄子、尖椒翻炒20秒"},{"order":9,"description":"关火撒入另一半蒜翻拌均匀盛盘"}]',
'东北经典家常菜，茄子软糯、土豆绵香、尖椒清脆，酱香咸鲜',
'糖比淀粉多非常合理，淀粉只作为物理增稠剂\n茄子在煎炸以后体积会缩小4倍',
'40', 2, 264,
'["东北菜","家常菜","下饭菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 麻婆豆腐
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'麻婆豆腐',
NULL, NULL, 1,
'[{"name":"内脂豆腐","quantity":1,"unit":"盒"},{"name":"咸鸭蛋","quantity":1,"unit":"枚"},{"name":"五花肉","quantity":30,"unit":"g"},{"name":"大蒜","quantity":2,"unit":"瓣"},{"name":"生姜","quantity":2,"unit":"片"},{"name":"小米辣","quantity":5,"unit":"根"},{"name":"蒜蓉辣酱","quantity":5,"unit":"g"},{"name":"花椒","quantity":20,"unit":"颗"},{"name":"食盐","quantity":3,"unit":"g"},{"name":"酱油","quantity":10,"unit":"g"}]',
'[{"order":1,"description":"大蒜和生姜切碎，小米辣切辣椒圈，备用"},{"order":2,"description":"五花肉切肉糜，加入一半食盐和酱油搅拌均匀"},{"order":3,"description":"咸鸭蛋对半切开去蛋黄，蛋白捣碎成小块"},{"order":4,"description":"豆腐划成2.5cm乘3cm大小，备用"},{"order":5,"description":"热锅加油，小火放入蒜姜、辣椒圈、花椒、咸鸭蛋碎、辣酱翻炒20秒"},{"order":6,"description":"调中火放入肉糜翻炒约1分钟至变色"},{"order":7,"description":"调小火放入豆腐，将剩余食盐和酱油均匀洒在豆腐上"},{"order":8,"description":"从锅边倒入开水没过豆腐（不然豆腐容易破）"},{"order":9,"description":"大火烧沸后转中火煮约10分钟"},{"order":10,"description":"水剩五分之一且豆腐入色后关火盛盘"}]',
'香辣滑嫩的豆腐菜，咸鲜中带着微麻微辣，格外开胃下饭',
'期间一定要注意观察，防止糊锅',
'40', 2, 476,
'["川菜","下饭菜","麻辣","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 手撕包菜
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'手撕包菜',
NULL, NULL, 1,
'[{"name":"包菜","quantity":1,"unit":"颗"},{"name":"五花肉","quantity":200,"unit":"g"},{"name":"小米辣","quantity":2,"unit":"根"},{"name":"食用油","quantity":60,"unit":"ml"},{"name":"料酒","quantity":5,"unit":"ml"},{"name":"生抽","quantity":5,"unit":"ml"},{"name":"香醋","quantity":5,"unit":"ml"},{"name":"鸡精","quantity":2,"unit":"g"},{"name":"姜","quantity":2,"unit":"片"},{"name":"蒜头","quantity":2,"unit":"粒"},{"name":"蒜苗","quantity":0.5,"unit":"根"},{"name":"盐","quantity":5,"unit":"g"}]',
'[{"order":1,"description":"包菜对半切开去白芯，手撕成片，加盐清洗沥干"},{"order":2,"description":"姜片、蒜头、小米辣、蒜苗处理好备用"},{"order":3,"description":"五花肉切片清水洗净备用"},{"order":4,"description":"锅中热油大火炒包菜1分钟，加盐继续翻炒2分钟取出备用"},{"order":5,"description":"锅中加余油大火炒五花肉1分钟"},{"order":6,"description":"倒入姜片、蒜头等材料翻炒1分钟"},{"order":7,"description":"倒入包菜，加香醋、料酒、鸡精大火翻炒2分钟出锅"}]',
'酸辣脆爽的经典湘菜，包菜脆嫩入味，五花肉干香不腻，开胃下饭',
'包菜炒至七分熟即可，加盐可以锁住水分\n翻炒时间可根据个人口感灵活调整',
'20', 2, 1663,
'["湘菜","下饭菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 蚝油生菜
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'蚝油生菜',
NULL, NULL, 1,
'[{"name":"生菜","quantity":200,"unit":"g"},{"name":"蚝油","quantity":8,"unit":"ml"},{"name":"大蒜","quantity":5,"unit":"瓣"},{"name":"生抽","quantity":10,"unit":"ml"},{"name":"盐","quantity":0.5,"unit":"g"},{"name":"白糖","quantity":1,"unit":"g"},{"name":"食用油","quantity":8,"unit":"ml"}]',
'[{"order":1,"description":"生菜洗净并去掉烂菜叶"},{"order":2,"description":"锅中放清水加油和盐，煮沸后放入生菜焯水10秒"},{"order":3,"description":"捞出生菜控干水分摆盘"},{"order":4,"description":"调汁：生抽、蚝油、盐、白糖加清水搅拌均匀"},{"order":5,"description":"热锅加油爆香蒜泥，倒入调汁煮沸后关火"},{"order":6,"description":"将锅中汤汁均匀浇在生菜上"}]',
'爽口鲜香的家常菜，脆嫩生菜淋上蒜香蚝油汁，咸中带甜',
'这道菜富含维生素，做法简单，爽口又不上火',
'15', 1, 125,
'["快手菜","减脂菜","清淡"]', '家常菜', 1, 0, 0, 0, 0);

-- ============================================
-- 荤菜 (10)
-- ============================================

-- 宫保鸡丁
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'宫保鸡丁',
NULL, NULL, 1,
'[{"name":"鸡腿","quantity":1,"unit":"个"},{"name":"熟花生","quantity":150,"unit":"g"},{"name":"大葱","quantity":1,"unit":"根"},{"name":"干辣椒","quantity":10,"unit":"g"},{"name":"生抽","quantity":10,"unit":"g"},{"name":"香醋","quantity":5,"unit":"g"},{"name":"白糖","quantity":2,"unit":"g"},{"name":"料酒","quantity":15,"unit":"g"},{"name":"盐","quantity":2,"unit":"g"},{"name":"淀粉","quantity":25,"unit":"g"},{"name":"植物油","quantity":20,"unit":"g"},{"name":"花椒","quantity":5,"unit":"g"},{"name":"鸡精","quantity":2,"unit":"g"},{"name":"芝麻油","quantity":10,"unit":"g"}]',
'[{"order":1,"description":"鸡腿去骨，用刀背拍打一遍，切1.5cm见方肉丁，泡水10分钟捞出控干"},{"order":2,"description":"取大葱葱绿与姜片加开水泡葱姜水，葱白切1.5cm圆粒，花生微波焙干"},{"order":3,"description":"鸡丁加盐、老抽、料酒、淀粉拌匀，分次加入葱姜水搅拌至粘手，腌制1小时"},{"order":4,"description":"干辣椒焙干至微糊，花椒焙香，捞出备用"},{"order":5,"description":"大火热油7成热下鸡丁，煎至发白翻面，翻炒均匀"},{"order":6,"description":"下葱粒，加葱姜水及热水盖盖中小火焖2分钟"},{"order":7,"description":"转大火，下花生、干辣椒、花椒，加鸡精、醋、白糖翻炒均匀"},{"order":8,"description":"水淀粉勾芡收汁，淋芝麻油出锅"}]',
'川味名菜以糊辣荔枝味见长，鸡丁嫩滑、花生酥脆',
'辣椒依据个人口味酌量添加，怕辣可去籽',
'90', 3, 1790,
'["川菜","经典","下饭菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 回锅肉
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'回锅肉',
NULL, NULL, 1,
'[{"name":"五花肉","quantity":250,"unit":"g"},{"name":"小葱","quantity":2,"unit":"棵"},{"name":"生姜","quantity":10,"unit":"g"},{"name":"青红椒","quantity":30,"unit":"g"},{"name":"蒜苗","quantity":1,"unit":"把"},{"name":"料酒","quantity":5,"unit":"ml"},{"name":"豆瓣酱","quantity":10,"unit":"ml"},{"name":"生抽","quantity":5,"unit":"ml"},{"name":"味精","quantity":5,"unit":"g"}]',
'[{"order":1,"description":"锅烧热，用手将五花肉紧压锅上炙皮，刷净至黑色碳化部分去除"},{"order":2,"description":"五花肉放冷水加姜片、料酒、葱结，大火煮开撇沫继续煮15分钟"},{"order":3,"description":"青红椒切圈，蒜苗切段，姜切小片"},{"order":4,"description":"煮熟的五花肉捞出过冷水晾凉，擦干切2mm薄片"},{"order":5,"description":"锅烧热加油滑锅，放入五花肉煸炒至肥肉透明肉片微卷"},{"order":6,"description":"倒入豆瓣酱、生抽和味精翻炒15秒"},{"order":7,"description":"放入青红椒圈和姜片翻炒30秒"},{"order":8,"description":"放入蒜苗翻炒60秒后出锅"}]',
'四川传统名菜，色泽红亮，肥而不腻，咸鲜微辣',
'回锅肉过冷水晾凉后肉质会更紧致\n切记不要切厚了，不然很腻',
'40', 3, 997,
'["川菜","经典","下饭菜","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 糖醋排骨
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'糖醋排骨',
NULL, NULL, 1,
'[{"name":"排骨","quantity":300,"unit":"g"},{"name":"白砂糖","quantity":30,"unit":"g"},{"name":"食用油","quantity":350,"unit":"ml"},{"name":"生抽","quantity":5,"unit":"ml"},{"name":"蚝油","quantity":5,"unit":"ml"},{"name":"老抽","quantity":5,"unit":"ml"},{"name":"鸡精","quantity":2,"unit":"g"},{"name":"姜片","quantity":2,"unit":"片"},{"name":"芝麻","quantity":2,"unit":"g"},{"name":"番茄酱","quantity":10,"unit":"g"},{"name":"香醋","quantity":5,"unit":"ml"},{"name":"五香粉","quantity":0.5,"unit":"g"}]',
'[{"order":1,"description":"排骨与姜片放入冷水大火煮沸，转中火再转小火焯水2-3分钟捞出"},{"order":2,"description":"用开水反复清洗排骨2-3遍去血沫"},{"order":3,"description":"油温升至170度下排骨炸3-5分钟至表面金黄捞出控油"},{"order":4,"description":"另取锅小火加热热水加白糖搅拌至完全溶解呈淡黄色"},{"order":5,"description":"倒入排骨翻炒，加醋、生抽、蚝油、鸡精、番茄酱、五香粉翻炒均匀"},{"order":6,"description":"加开水至刚好没过排骨，大火煮沸加老抽上色快速收汁"},{"order":7,"description":"起锅装盘撒上芝麻即可"}]',
'酸甜可口、外酥里嫩的传统名菜，排骨富含优质蛋白和钙质',
'炸排骨时可轻撒干淀粉提升酥脆口感\n收汁时应快速翻炒使排骨均匀裹上调料',
'45', 3, 1022,
'["传统菜","酸甜","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 鱼香肉丝
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'鱼香肉丝',
NULL, NULL, 1,
'[{"name":"里脊肉","quantity":200,"unit":"g"},{"name":"胡萝卜","quantity":100,"unit":"g"},{"name":"青椒","quantity":100,"unit":"g"},{"name":"木耳","quantity":5,"unit":"g"},{"name":"生抽","quantity":10,"unit":"ml"},{"name":"料酒","quantity":5,"unit":"ml"},{"name":"蛋清","quantity":1,"unit":"个"},{"name":"淀粉","quantity":10,"unit":"g"},{"name":"醋","quantity":15,"unit":"ml"},{"name":"白糖","quantity":10,"unit":"g"},{"name":"盐","quantity":5,"unit":"g"},{"name":"姜","quantity":20,"unit":"g"},{"name":"葱","quantity":20,"unit":"g"},{"name":"蒜","quantity":2,"unit":"瓣"},{"name":"豆瓣酱","quantity":15,"unit":"g"}]',
'[{"order":1,"description":"制作腌料：生抽、料酒、淀粉、水、蛋清混合均匀"},{"order":2,"description":"制作香汁：生抽、醋、白糖、盐、淀粉、水混合均匀"},{"order":3,"description":"用腌料腌制里脊肉15-30分钟，抓匀入味"},{"order":4,"description":"干木耳泡发4小时洗净切块，青椒切丝，胡萝卜切丝焯水"},{"order":5,"description":"姜蒜切沫，葱切5mm小段"},{"order":6,"description":"锅烧热加油，倒入腌肉快速滑散至变白盛出"},{"order":7,"description":"锅烧热加油，爆香葱姜蒜和豆瓣酱"},{"order":8,"description":"倒入胡萝卜翻炒后加青椒和木耳翻炒2分钟"},{"order":9,"description":"倒入炒过的肉快速翻炒不超过20秒"},{"order":10,"description":"倒入香汁快速翻炒不超过15秒后关火盛盘"}]',
'经典川菜，口味酸甜微辣，肉丝滑嫩，荤素搭配营养均衡',
'干木耳需提前泡发4小时\n鱼香汁要提前调好，炒制过程动作要快',
'40', 3, 421,
'["川菜","经典","下饭菜","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 简易红烧肉
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'简易红烧肉',
NULL, NULL, 1,
'[{"name":"猪五花肉","quantity":1500,"unit":"g"},{"name":"姜","quantity":6,"unit":"片"},{"name":"冰糖","quantity":15,"unit":"g"},{"name":"生抽","quantity":10,"unit":"ml"},{"name":"老抽","quantity":15,"unit":"ml"},{"name":"料酒","quantity":5,"unit":"ml"},{"name":"香叶","quantity":3,"unit":"片"},{"name":"八角","quantity":2,"unit":"个"},{"name":"鹌鹑蛋","quantity":2,"unit":"个"},{"name":"豆皮","quantity":80,"unit":"g"},{"name":"盐","quantity":3,"unit":"g"},{"name":"葱","quantity":1,"unit":"根"}]',
'[{"order":1,"description":"五花肉切4.5cm大块，豆皮切2cm宽，姜切片，鹌鹑蛋煮熟扎孔"},{"order":2,"description":"冷水锅放入五花肉，加料酒葱姜煮15分钟去血腥"},{"order":3,"description":"中小火煎五花肉至六面微黄出油，倒出煎出的油"},{"order":4,"description":"加冰糖炒至融化，与五花肉炒至上色"},{"order":5,"description":"加生抽、老抽、料酒翻炒上色"},{"order":6,"description":"加开水炖煮，放姜、香叶、八角"},{"order":7,"description":"煮开后加鹌鹑蛋和豆皮，中小火炖40分钟"},{"order":8,"description":"开盖大火收汁，加盐翻炒后出锅"}]',
'色泽红润油亮，口感软糯，肥而不腻，酱香浓郁',
'刀工差的同学切大块请自觉延长炖煮时间\n收汁切记不可收干',
'90', 2, 2775,
'["下饭菜","经典"]', '家常菜', 1, 0, 0, 0, 0);

-- 可乐鸡翅
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'可乐鸡翅',
NULL, NULL, 1,
'[{"name":"鸡翅中","quantity":12,"unit":"只"},{"name":"可乐","quantity":500,"unit":"ml"},{"name":"白糖","quantity":10,"unit":"g"},{"name":"生抽","quantity":15,"unit":"g"},{"name":"老抽","quantity":3,"unit":"g"},{"name":"盐","quantity":2,"unit":"g"},{"name":"生姜","quantity":2,"unit":"片"},{"name":"料酒","quantity":20,"unit":"ml"},{"name":"小葱","quantity":1,"unit":"根"}]',
'[{"order":1,"description":"鸡翅冷水下锅加姜片和料酒，大火煮开后撇去浮沫沥出"},{"order":2,"description":"捞出鸡翅两面划刀，用生抽腌制10分钟"},{"order":3,"description":"小火起油爆香姜片，下鸡翅煎至两面金黄"},{"order":4,"description":"倒入可乐没过鸡翅，大火煮沸撇去浮沫，加葱结"},{"order":5,"description":"加盐、白糖、生抽调味，老抽上色"},{"order":6,"description":"葱结变黄后捞出，转中火慢煮"},{"order":7,"description":"待可乐呈现挂丝状态，关小火收汁装盘"}]',
'色泽红亮、口感嫩滑，味道咸甜适口',
'加入生姜爆香能防止鸡翅粘锅\n最后收汁勿开过大火，防止味道偏苦',
'40', 2, 960,
'["创意菜","下饭菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 黄焖鸡
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'黄焖鸡',
NULL, NULL, 1,
'[{"name":"鸡腿","quantity":2,"unit":"只"},{"name":"香菇","quantity":5,"unit":"朵"},{"name":"青椒","quantity":2,"unit":"个"},{"name":"生姜","quantity":2,"unit":"片"},{"name":"干辣椒","quantity":6,"unit":"个"},{"name":"盐","quantity":10,"unit":"g"},{"name":"料酒","quantity":10,"unit":"ml"},{"name":"白胡椒粉","quantity":5,"unit":"g"},{"name":"白糖","quantity":5,"unit":"g"},{"name":"酱油","quantity":5,"unit":"ml"},{"name":"土豆","quantity":1,"unit":"个"}]',
'[{"order":1,"description":"鸡腿洗净剁成4cm大小的块，姜切片，干辣椒切圈"},{"order":2,"description":"香菇切片，青椒切马蹄状，土豆切滚刀块"},{"order":3,"description":"炒糖色：冷油放白糖小火加热至棕色"},{"order":4,"description":"迅速倒入鸡块大火翻炒，烹入料酒"},{"order":5,"description":"加入姜片、干辣椒、酱油炒匀"},{"order":6,"description":"倒入香菇水或清水以没过鸡肉为准"},{"order":7,"description":"加香菇片、白胡椒粉、盐、土豆"},{"order":8,"description":"盖盖焖煮中小火15-20分钟至鸡肉软烂汤汁浓稠"},{"order":9,"description":"放入青椒加味精兜炒均匀后关火"}]',
'汤汁浓郁，鸡肉滑嫩，咸鲜微甜，十分下饭',
'炒糖色有难度，新手可用老抽替代\n汤汁不要收得太干',
'40', 2, 766,
'["下饭菜","焖菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 辣椒炒肉
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'辣椒炒肉',
NULL, NULL, 1,
'[{"name":"青椒","quantity":3,"unit":"个"},{"name":"猪瘦肉","quantity":200,"unit":"g"},{"name":"盐","quantity":3,"unit":"g"},{"name":"生抽","quantity":3,"unit":"ml"},{"name":"蚝油","quantity":3,"unit":"ml"},{"name":"大蒜","quantity":5,"unit":"g"},{"name":"姜","quantity":5,"unit":"g"},{"name":"酱油","quantity":2,"unit":"ml"},{"name":"豆豉","quantity":3,"unit":"g"}]',
'[{"order":1,"description":"青椒洗净去籽，滚刀切好备用"},{"order":2,"description":"大蒜拍切蒜瓣，姜切末"},{"order":3,"description":"猪瘦肉切片，加生抽、蚝油、盐搅拌均匀腌制10分钟"},{"order":4,"description":"热锅不放油，大火干煸青椒至虎皮状，加盐继续翻炒后捞起"},{"order":5,"description":"热锅加油爆香蒜瓣和姜末"},{"order":6,"description":"加入腌制好的猪肉翻炒2分钟"},{"order":7,"description":"加入干煸过的青椒翻炒1分钟"},{"order":8,"description":"加入豆豉和酱油继续翻炒30秒出锅"}]',
'湘味浓郁的家常菜，辣椒干香微焦，肉片滑嫩入味',
'辣椒只能选择青椒，螺丝椒为最优解，切勿选择其他品种辣椒',
'30', 2, 425,
'["湘菜","下饭菜","快手菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 小炒肉
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'小炒肉',
NULL, NULL, 1,
'[{"name":"五花肉","quantity":500,"unit":"g"},{"name":"朝天椒","quantity":4,"unit":"条"},{"name":"小米椒","quantity":4,"unit":"颗"},{"name":"豆豉","quantity":10,"unit":"g"},{"name":"豆瓣酱","quantity":10,"unit":"g"},{"name":"老抽","quantity":10,"unit":"ml"},{"name":"淀粉","quantity":10,"unit":"g"},{"name":"盐","quantity":2,"unit":"g"},{"name":"葱","quantity":1,"unit":"根"},{"name":"蒜","quantity":2,"unit":"瓣"},{"name":"食用油","quantity":15,"unit":"ml"}]',
'[{"order":1,"description":"五花肉切片，加淀粉、老抽、盐搅拌腌制半小时"},{"order":2,"description":"葱切段，小米椒、朝天椒斜刀切好"},{"order":3,"description":"热锅倒油，油热后加入五花肉煸炒至变色盛出"},{"order":4,"description":"锅中加蒜煸出香味，加入豆豉翻炒均匀"},{"order":5,"description":"加入豆瓣酱翻炒均匀"},{"order":6,"description":"加入炒好的五花肉继续翻炒均匀"},{"order":7,"description":"加入小米椒、朝天椒、葱段翻炒40秒后出锅"}]',
'香辣下饭的经典湘式家常菜，五花肉焦香不腻',
'腌制时间至少半小时入味更佳',
'60', 2, 2873,
'["湘菜","下饭菜","香辣","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 红烧猪蹄
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'红烧猪蹄',
NULL, NULL, 1,
'[{"name":"猪蹄","quantity":2,"unit":"根"},{"name":"香叶","quantity":2,"unit":"片"},{"name":"姜","quantity":5,"unit":"片"},{"name":"葱","quantity":0.5,"unit":"根"},{"name":"老抽","quantity":20,"unit":"ml"},{"name":"桂皮","quantity":1,"unit":"块"},{"name":"冰糖","quantity":8,"unit":"粒"},{"name":"料酒","quantity":30,"unit":"ml"},{"name":"生抽","quantity":20,"unit":"ml"},{"name":"盐","quantity":8,"unit":"g"},{"name":"八角","quantity":4,"unit":"个"},{"name":"食用油","quantity":30,"unit":"ml"}]',
'[{"order":1,"description":"冷水锅中放入剁好的猪蹄，加料酒与葱姜煮15分钟去血腥"},{"order":2,"description":"热锅冷油加冰糖，小火熬成糖色约2分钟"},{"order":3,"description":"放入焯过水的猪蹄小火翻炒至两面微黄"},{"order":4,"description":"加香叶、桂皮、八角、生抽、老抽、料酒、姜、盐中火翻炒1分钟"},{"order":5,"description":"加入开水没过猪蹄，大火烧开后关火"},{"order":6,"description":"倒入高压锅压15分钟"},{"order":7,"description":"倒回炒锅大火收汁30秒即可"}]',
'汤汁浓郁、肉质软糯的经典家常菜，咸香甜美',
'没有高压锅可在大火转小火熬制\n红烧猪蹄汤也很下饭可多留些',
'60', 3, 639,
'["经典","胶原蛋白","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- ============================================
-- 水产 (4)
-- ============================================

-- 清蒸鲈鱼
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'清蒸鲈鱼',
NULL, NULL, 1,
'[{"name":"鲈鱼","quantity":1,"unit":"条"},{"name":"香葱","quantity":3,"unit":"根"},{"name":"姜","quantity":1,"unit":"块"},{"name":"食用油","quantity":15,"unit":"ml"},{"name":"蒸鱼豉油","quantity":15,"unit":"ml"},{"name":"料酒","quantity":15,"unit":"ml"},{"name":"食用盐","quantity":10,"unit":"g"}]',
'[{"order":1,"description":"姜切片切丝，葱白切段，葱绿切丝泡冷水备用"},{"order":2,"description":"鲈鱼处理洗净擦干，两面划刀，用盐抹遍鱼身腌制10分钟"},{"order":3,"description":"鱼肚内塞姜葱，鱼身撒姜葱，用筷子将鱼与碟子隔开"},{"order":4,"description":"水烧热放入鱼，大火清蒸10分钟"},{"order":5,"description":"蒸好的鱼换干净盘子去姜葱"},{"order":6,"description":"鱼身浇蒸鱼豉油，撒姜葱丝，热油淋至鱼身即可"}]',
'粤式经典蒸菜，鱼肉细嫩爽滑，味道清淡鲜美',
'大火蒸鱼一般在10分钟内较佳\n蒸鱼需用筷子隔开盘子使鱼均匀受热且不腥',
'30', 2, 385,
'["粤菜","清蒸","鲜嫩","减脂菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 白灼虾
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'白灼虾',
NULL, NULL, 1,
'[{"name":"活虾","quantity":250,"unit":"g"},{"name":"洋葱","quantity":1,"unit":"头"},{"name":"姜","quantity":1,"unit":"块"},{"name":"蒜","quantity":5,"unit":"瓣"},{"name":"葱","quantity":1,"unit":"根"},{"name":"食用油","quantity":15,"unit":"ml"},{"name":"酱油","quantity":15,"unit":"ml"},{"name":"料酒","quantity":20,"unit":"ml"},{"name":"芝麻","quantity":1,"unit":"把"},{"name":"蚝油","quantity":10,"unit":"ml"},{"name":"香醋","quantity":10,"unit":"ml"}]',
'[{"order":1,"description":"洋葱切块，姜切片，平铺平底锅底"},{"order":2,"description":"活虾冲洗，铺在洋葱姜片上"},{"order":3,"description":"倒料酒盖盖，中火1分钟小火5分钟关火5分钟"},{"order":4,"description":"制作蘸料：葱花蒜末加酱油、芝麻、香醋拌匀，淋入热油"},{"order":5,"description":"虾出锅用干净盘子装好，搭配蘸料食用"}]',
'粤式经典快手菜，虾肉鲜甜弹嫩，原汁原味',
'开始不能大火防止糊底\n蘸料也可以只用纯醋，原味虾口感味道都非常棒',
'15', 1, 519,
'["粤菜","鲜嫩","减脂菜"]', '快手菜', 1, 0, 0, 0, 0);

-- 红烧鱼
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'红烧鱼',
NULL, NULL, 1,
'[{"name":"鲫鱼","quantity":1,"unit":"条"},{"name":"姜","quantity":3,"unit":"片"},{"name":"蒜瓣","quantity":4,"unit":"个"},{"name":"干辣椒","quantity":3,"unit":"个"},{"name":"盐","quantity":10,"unit":"g"},{"name":"醋","quantity":5,"unit":"ml"},{"name":"酱油","quantity":5,"unit":"ml"},{"name":"白砂糖","quantity":10,"unit":"g"},{"name":"葱","quantity":2,"unit":"根"},{"name":"小米椒","quantity":2,"unit":"个"},{"name":"蚝油","quantity":5,"unit":"g"}]',
'[{"order":1,"description":"姜蒜切碎，干辣椒切碎备用"},{"order":2,"description":"热锅加油，放入擦干水分的鱼小火慢煎"},{"order":3,"description":"将鱼翻面重复油煎过程"},{"order":4,"description":"放入姜蒜辣椒翻炒出香味"},{"order":5,"description":"倒入料酒和醋"},{"order":6,"description":"放入白砂糖、酱油"},{"order":7,"description":"加冷水以没过鱼身为宜，中火盖盖炖煮"},{"order":8,"description":"加盐、小米椒、蚝油继续焖煮收汁"},{"order":9,"description":"汤汁不多时加香菜葱花盖盖20秒关火起锅"}]',
'家常风味浓郁的经典菜，咸鲜微甜，鱼肉嫩滑入味',
'新手建议以中等大小的鲫鱼上手，提前划好花刀方便成熟',
'40', 3, 570,
'["下饭菜","经典"]', '家常菜', 1, 0, 0, 0, 0);

-- 油焖大虾
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'油焖大虾',
NULL, NULL, 1,
'[{"name":"黑虎虾","quantity":10,"unit":"只"},{"name":"花椒","quantity":5,"unit":"g"},{"name":"葱","quantity":50,"unit":"g"},{"name":"姜","quantity":20,"unit":"g"},{"name":"黄酒","quantity":30,"unit":"g"},{"name":"盐","quantity":3,"unit":"g"},{"name":"冰糖","quantity":10,"unit":"g"},{"name":"植物油","quantity":30,"unit":"ml"}]',
'[{"order":1,"description":"剪虾枪虾须虾爪，开背挑虾线，洗净备用"},{"order":2,"description":"三成油温放花椒，油热离火放葱姜炸料油"},{"order":3,"description":"下油，虾摆放整齐两面变色后轻按虾头"},{"order":4,"description":"放姜米、黄酒、水、盐、冰糖"},{"order":5,"description":"大火烧开转小火盖盖焖煮至皮亮虾弯"},{"order":6,"description":"虾起锅摆盘，汤汁过滤后回锅收浓"},{"order":7,"description":"汤汁剩余四分之一时加葱油，浇在虾上即可"}]',
'鲁菜经典，咸甜交融的酱汁包裹整虾，红亮油润',
'做法参考B站老饭骨视频\n中途不能再加汤水，不要开盖',
'40', 3, 584,
'["鲁菜","经典","宴客"]', '特色菜', 1, 0, 0, 0, 0);

-- ============================================
-- 主食 (4)
-- ============================================

-- 蛋炒饭
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'蛋炒饭',
NULL, NULL, 1,
'[{"name":"冷饭","quantity":500,"unit":"ml"},{"name":"鸡蛋","quantity":2,"unit":"个"},{"name":"火腿","quantity":2,"unit":"个"},{"name":"黄瓜","quantity":30,"unit":"g"},{"name":"胡萝卜","quantity":30,"unit":"g"},{"name":"油","quantity":12,"unit":"ml"},{"name":"盐","quantity":5,"unit":"g"},{"name":"胡椒粉","quantity":8,"unit":"g"},{"name":"香葱","quantity":1,"unit":"颗"},{"name":"生抽","quantity":10,"unit":"ml"}]',
'[{"order":1,"description":"米饭提前用铲子铲成小块，火腿、胡萝卜、黄瓜切好"},{"order":2,"description":"蛋白蛋黄分开各自搅匀"},{"order":3,"description":"大火热油炒蛋白，凝固后盛出备用"},{"order":4,"description":"炒蛋黄，凝固后调中小火，倒入火腿、胡萝卜、黄瓜等爆香"},{"order":5,"description":"重新倒入蛋白翻炒，迅速倒入米饭大火翻炒"},{"order":6,"description":"捣碎米饭块，翻炒至粒粒分明"},{"order":7,"description":"调小火加盐、胡椒粉、生抽翻炒均匀"},{"order":8,"description":"最后倒入香葱再翻炒10秒关火盛盘"}]',
'金黄蛋丝裹着粒粒分明的米饭，搭配火腿蔬菜口感丰富',
'使用隔夜的冷饭炒饭最佳\n炒饭要做到粒粒分明需将饭炒干',
'25', 2, 853,
'["快手菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 扬州炒饭
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'扬州炒饭',
NULL, NULL, 1,
'[{"name":"冷饭","quantity":500,"unit":"g"},{"name":"鸡蛋","quantity":3,"unit":"个"},{"name":"基围虾","quantity":15,"unit":"只"},{"name":"午餐肉","quantity":150,"unit":"g"},{"name":"青豆","quantity":30,"unit":"g"},{"name":"胡萝卜","quantity":30,"unit":"g"},{"name":"玉米粒","quantity":30,"unit":"g"},{"name":"葱","quantity":1,"unit":"根"},{"name":"油","quantity":40,"unit":"ml"},{"name":"盐","quantity":15,"unit":"g"}]',
'[{"order":1,"description":"胡萝卜和午餐肉切丁，葱白葱绿分别切段备用"},{"order":2,"description":"鸡蛋打散搅匀，胡萝卜、青豆、玉米粒煮熟捞出，虾煮熟捞出"},{"order":3,"description":"热锅热油，缓慢倒入蛋液，凝固后立刻捞出"},{"order":4,"description":"将午餐肉、青豆、胡萝卜、玉米粒、虾翻炒1-2分钟装盘"},{"order":5,"description":"洗净锅，热油爆香葱白"},{"order":6,"description":"调小火放入米饭，砸击翻炒至粒粒分明"},{"order":7,"description":"倒入鸡蛋碎开与米饭充分混合"},{"order":8,"description":"转大火倒入所有配料快速翻炒1-2分钟"},{"order":9,"description":"撒盐翻炒均匀，撒葱绿翻炒1分钟关火装盘"}]',
'配料丰富的升级版炒饭，粒粒分明，属于淮扬菜经典主食',
'如做完后胳膊酸痛为正常现象，需加强上肢锻炼',
'60', 3, 1769,
'["淮扬菜","经典"]', '特色菜', 1, 0, 0, 0, 0);

-- 西红柿鸡蛋挂面
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'西红柿鸡蛋挂面',
NULL, NULL, 1,
'[{"name":"挂面","quantity":100,"unit":"g"},{"name":"西红柿","quantity":1,"unit":"个"},{"name":"鸡蛋","quantity":2,"unit":"个"},{"name":"盐","quantity":5,"unit":"g"},{"name":"蚝油","quantity":5,"unit":"g"},{"name":"白砂糖","quantity":2,"unit":"g"},{"name":"酱油","quantity":8,"unit":"g"},{"name":"食用油","quantity":20,"unit":"g"},{"name":"香油","quantity":5,"unit":"g"},{"name":"小葱","quantity":1,"unit":"根"},{"name":"青椒","quantity":1,"unit":"个"}]',
'[{"order":1,"description":"小葱切葱花，西红柿切块，青椒切菱形块，鸡蛋打散"},{"order":2,"description":"起锅烧热倒油，炒鸡蛋至凝固后盛出备用"},{"order":3,"description":"锅中留底油炒香葱白蒜末"},{"order":4,"description":"加西红柿块和青椒炒出汁水"},{"order":5,"description":"加酱油和白砂糖翻炒，加一碗清水煮沸"},{"order":6,"description":"加入炒好的鸡蛋和蚝油，中小火收汁做成臊子"},{"order":7,"description":"另起锅煮面，面条煮至透明捞出盛入臊子碗中拌匀即可"}]',
'家常快手面食，酸甜咸鲜，汤汁浓郁，面条劲道',
'煮面条多次加冷水可使面条口感劲道不粘黏\n鸡蛋液中可加少许黑胡椒提味',
'20', 1, 654,
'["汤面","快手菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 手工水饺
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'手工水饺',
NULL, NULL, 1,
'[{"name":"面粉","quantity":200,"unit":"g"},{"name":"冷水","quantity":150,"unit":"ml"},{"name":"芝麻香油","quantity":3,"unit":"ml"},{"name":"瘦肉末","quantity":250,"unit":"g"},{"name":"肥肉末","quantity":20,"unit":"g"},{"name":"姜","quantity":3,"unit":"g"},{"name":"葱","quantity":15,"unit":"g"},{"name":"盐","quantity":3,"unit":"g"},{"name":"蚝油","quantity":2,"unit":"ml"},{"name":"生抽","quantity":2,"unit":"ml"},{"name":"鸡蛋","quantity":1,"unit":"个"}]',
'[{"order":1,"description":"面粉加芝麻香油，分次加水搅拌成面团，压实醒发45分钟"},{"order":2,"description":"面团搓条切成20份，搓圆压扁擀成直径8cm厚2mm的饺子皮"},{"order":3,"description":"猪肉去皮剁成肉沫，加葱姜末、蚝油、生抽、蛋清拌匀放置30分钟"},{"order":4,"description":"左手上面皮，放约面皮1/2直径的馅，合拢捏实"},{"order":5,"description":"水烧开放饺子调中火，第一次水冒泡加冷水"},{"order":6,"description":"重复加冷水步骤两次，第三次水开后加冷水小火等60秒出锅"}]',
'中式经典主食，皮薄馅大、鲜美多汁，饱腹又美味',
'煮水饺不需要盖锅盖，加三次水防止饺子破损\n搭配黑醋和姜丝做蘸料味道更丰富',
'180', 3, 1313,
'["经典","面食","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- ============================================
-- 汤羹 (4)
-- ============================================

-- 西红柿鸡蛋汤
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'西红柿鸡蛋汤',
NULL, NULL, 1,
'[{"name":"西红柿","quantity":1,"unit":"个"},{"name":"鸡蛋","quantity":2,"unit":"个"},{"name":"香油","quantity":2,"unit":"滴"},{"name":"味素","quantity":5,"unit":"g"},{"name":"盐","quantity":15,"unit":"g"},{"name":"葱姜蒜","quantity":15,"unit":"g"},{"name":"食用油","quantity":15,"unit":"ml"}]',
'[{"order":1,"description":"西红柿洗净切块，葱姜蒜切碎"},{"order":2,"description":"鸡蛋打碗中搅匀"},{"order":3,"description":"热锅加油，冒烟时放入葱姜蒜翻炒30秒"},{"order":4,"description":"放入西红柿翻炒1分钟"},{"order":5,"description":"倒入水至菜品高度1.2倍，加盐"},{"order":6,"description":"开锅后倒入蛋液用筷子打散，加味素和香油"},{"order":7,"description":"30秒后关火出锅"}]',
'酸甜开胃、口感滑嫩的家常汤品，老少皆宜',
'味素可加可不加',
'15', 1, 258,
'["快手菜"]', '家常菜', 1, 0, 0, 0, 0);

-- 紫菜蛋花汤
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'紫菜蛋花汤',
NULL, NULL, 1,
'[{"name":"干紫菜","quantity":10,"unit":"g"},{"name":"鸡蛋","quantity":2,"unit":"个"},{"name":"盐","quantity":2,"unit":"g"},{"name":"食用油","quantity":5,"unit":"ml"},{"name":"香油","quantity":3,"unit":"滴"},{"name":"葱花","quantity":5,"unit":"g"}]',
'[{"order":1,"description":"干紫菜用清水泡15分钟，捞起沥干水分备用"},{"order":2,"description":"热锅倒入清水、油、盐，水开后放入紫菜"},{"order":3,"description":"紫菜烧开3分钟后将蛋液徐徐倒入锅内"},{"order":4,"description":"30秒后撒上葱花转小火20秒"},{"order":5,"description":"关火出锅前放入几滴香油"}]',
'清淡鲜美的家常汤品，紫菜富含碘钙，鸡蛋提供优质蛋白',
'水开后关小火倒入蛋液可使蛋花更嫩\n喜欢浓稠口感可加入2g淀粉',
'20', 1, 217,
'["清淡","减脂菜"]', '快手菜', 1, 0, 0, 0, 0);

-- 玉米排骨汤
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'玉米排骨汤',
NULL, NULL, 1,
'[{"name":"排骨","quantity":800,"unit":"g"},{"name":"玉米","quantity":1,"unit":"根"},{"name":"胡萝卜","quantity":1,"unit":"根"},{"name":"大葱","quantity":0.5,"unit":"根"},{"name":"生姜","quantity":1,"unit":"块"},{"name":"食用油","quantity":10,"unit":"ml"},{"name":"黑胡椒粉","quantity":4,"unit":"g"},{"name":"料酒","quantity":10,"unit":"ml"},{"name":"醋","quantity":10,"unit":"ml"},{"name":"食用盐","quantity":15,"unit":"g"}]',
'[{"order":1,"description":"大葱切段用刀背拍一下，玉米剁块，胡萝卜切滚刀块，姜切大片"},{"order":2,"description":"排骨凉水下锅放大葱、生姜、料酒焯水，撇去浮沫捞出沥干"},{"order":3,"description":"热锅凉油下姜片和排骨煸炒至表面微焦，加醋继续煸炒一分钟"},{"order":4,"description":"冲入开水一次给足，大火烧开"},{"order":5,"description":"下玉米和胡椒粉，盖盖小火炖20分钟"},{"order":6,"description":"放入胡萝卜继续小火炖40分钟"},{"order":7,"description":"出锅前3分钟加盐，撒上葱花即可"}]',
'鲜美清甜的家常汤品，排骨软烂，玉米和胡萝卜带来自然甜味',
'这道菜制作简单，食材不复杂，对新手友好\n适合降温的时候来上一大碗',
'90', 2, 1724,
'["营养"]', '家常菜', 1, 0, 0, 0, 0);

-- 皮蛋瘦肉粥
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'皮蛋瘦肉粥',
NULL, NULL, 1,
'[{"name":"饮用水","quantity":1000,"unit":"ml"},{"name":"皮蛋","quantity":2,"unit":"颗"},{"name":"瘦肉","quantity":100,"unit":"g"},{"name":"大米","quantity":150,"unit":"ml"},{"name":"小葱","quantity":1,"unit":"棵"},{"name":"香菜","quantity":1,"unit":"棵"},{"name":"生菜","quantity":4,"unit":"叶"},{"name":"生姜","quantity":1,"unit":"块"},{"name":"酱油","quantity":5,"unit":"ml"},{"name":"蚝油","quantity":5,"unit":"ml"},{"name":"盐","quantity":2,"unit":"g"},{"name":"胡椒粉","quantity":1,"unit":"g"},{"name":"食用油","quantity":10,"unit":"ml"}]',
'[{"order":1,"description":"大米洗净放入电饭锅内胆，加入饮用水"},{"order":2,"description":"瘦肉洗净加食用油揉搓均匀，放入内胆"},{"order":3,"description":"皮蛋去壳，蛋白切碎块，蛋黄揉碎，放入内胆"},{"order":4,"description":"姜切丝放入内胆，电饭锅煮粥模式煮熟"},{"order":5,"description":"生菜过热水，与葱花、香菜一同加入粥中搅匀"},{"order":6,"description":"加入酱油、蚝油、盐、胡椒粉搅拌均匀即可"}]',
'广东经典家常粥品，粥底绵密顺滑，皮蛋醇香与瘦肉鲜嫩融合',
'作为早餐可提前一夜准备好主料保温到第二天再加配料',
'90', 2, 776,
'["广东","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- ============================================
-- 早餐 (2)
-- ============================================

-- 茶叶蛋
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'茶叶蛋',
NULL, NULL, 1,
'[{"name":"鸡蛋","quantity":8,"unit":"颗"},{"name":"八角","quantity":2,"unit":"颗"},{"name":"香叶","quantity":2,"unit":"片"},{"name":"桂皮","quantity":1,"unit":"块"},{"name":"茴香","quantity":5,"unit":"g"},{"name":"冰糖","quantity":15,"unit":"g"},{"name":"红茶","quantity":20,"unit":"g"},{"name":"生抽","quantity":15,"unit":"g"},{"name":"老抽","quantity":25,"unit":"g"},{"name":"食盐","quantity":3,"unit":"g"}]',
'[{"order":1,"description":"冷水将鸡蛋煮熟，大火约8分钟"},{"order":2,"description":"鸡蛋捞出过冷水"},{"order":3,"description":"将鸡蛋互相碰撞使每个鸡蛋产生裂缝"},{"order":4,"description":"鸡蛋下锅，放入八角、香叶、桂皮、茴香、冰糖、红茶、生抽、老抽、食盐"},{"order":5,"description":"加水没过鸡蛋，大火煮开之后转中小火煮15分钟"},{"order":6,"description":"捞出料渣，鸡蛋再浸泡一会口感更佳"}]',
'传统小吃茶香浓郁，鲜香可口，富含优质蛋白质',
'鸡蛋捞出过冷水是为了让蛋壳之间产生间隙\n想入味更快可剥壳划刀浸泡2天以上',
'30', 2, 193,
'["小吃","传统","家常菜"]', '特色菜', 1, 0, 0, 0, 0);

-- 牛奶燕麦
INSERT INTO `recipe` (`name`, `cover`, `images`, `author_id`, `ingredients`, `steps`, `desc`, `tips`, `cook_time`, `difficulty`, `calories`, `tags`, `category`, `source`, `like_count`, `collection_count`, `comment_count`, `view_count`)
VALUES (
'牛奶燕麦',
NULL, NULL, 1,
'[{"name":"牛奶","quantity":280,"unit":"ml"},{"name":"燕麦","quantity":40,"unit":"g"},{"name":"鸡蛋","quantity":1,"unit":"个"},{"name":"食用油","quantity":5,"unit":"ml"}]',
'[{"order":1,"description":"将牛奶倒入早餐杯备用"},{"order":2,"description":"水沸后加入燕麦煮2分钟"},{"order":3,"description":"煮好的燕麦捞出倒入牛奶中"},{"order":4,"description":"热锅加油煎鸡蛋，每面煎20秒"},{"order":5,"description":"装盘搭配食用，可佐以水果蔬菜"}]',
'高蛋白高纤维的便捷营养早餐，入口温热柔和',
'不同微波炉火力不同无法精确\n混合物不超过容器容量50%防止溢出\n不建议使用玻璃杯烹饪',
'5', 1, 421,
'["营养","减脂菜"]', '快手菜', 1, 0, 0, 0, 0);
