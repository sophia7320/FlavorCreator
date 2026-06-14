// 食材封面图片配置
// 各分类基础路径
const BASE_PATH = {
  fruit:              'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/fruit/',
  grains_and_starches:'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/grains%20and%20starches/',
  meat_and_egg:       'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/meat%20and%20egg/',
  others:             'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/others/',
  seasoner:           'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/seasoner/',
  vegetables:         'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/food/vegetables/',
}

// 通用默认封面图
const DEFAULT_COVER = 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/top-bar-dish-bg.png'

// 食材名称 → 封面 URL 映射
const INGREDIENT_COVER_MAP = {
  // ==================== 水果类 ====================
  '苹果':       BASE_PATH.fruit + 'apple.jpg',
  '牛油果':     BASE_PATH.fruit + 'avocado.jpg',
  '香蕉':       BASE_PATH.fruit + 'banana.jpg',
  '蓝莓':       BASE_PATH.fruit + 'blueberry.jpg',
  '哈密瓜':     BASE_PATH.fruit + 'cantaloupe.jpg',
  '樱桃':       BASE_PATH.fruit + 'cherry.jpg',
  '火龙果':     BASE_PATH.fruit + 'dragon%20fruit.jpg',
  '葡萄':       BASE_PATH.fruit + 'grape.jpg',
  '番石榴':     BASE_PATH.fruit + 'guava.jpg',
  '芭乐':       BASE_PATH.fruit + 'guava.jpg',
  '猕猴桃':     BASE_PATH.fruit + 'kiwi%20fruit.jpg',
  '奇异果':     BASE_PATH.fruit + 'kiwi%20fruit.jpg',
  '柠檬':       BASE_PATH.fruit + 'lemon.jpg',
  '芒果':       BASE_PATH.fruit + 'mango.jpg',
  '桑葚':       BASE_PATH.fruit + 'mulberry.jpg',
  '橙子':       BASE_PATH.fruit + 'orange.jpg',
  '橘子':       BASE_PATH.fruit + 'orange.jpg',
  '木瓜':       BASE_PATH.fruit + 'pawpaw.jpg',
  '桃子':       BASE_PATH.fruit + 'peach.jpg',
  '梨':         BASE_PATH.fruit + 'pear.jpg',
  '菠萝':       BASE_PATH.fruit + 'pineapple.jpg',
  '凤梨':       BASE_PATH.fruit + 'pineapple.jpg',
  '李子':       BASE_PATH.fruit + 'plum.jpg',
  '覆盆子':     BASE_PATH.fruit + 'raspberry.jpg',
  '树莓':       BASE_PATH.fruit + 'raspberry.jpg',
  '草莓':       BASE_PATH.fruit + 'strawberry.jpg',
  '西瓜':       BASE_PATH.fruit + 'watermelon.jpg',

  // ==================== 谷薯类 ====================
  '豆腐':       BASE_PATH.grains_and_starches + 'Doufu.jpg',
  '黑豆':       BASE_PATH.grains_and_starches + 'black%20soybean.jpg',
  '玉米':       BASE_PATH.grains_and_starches + 'corn.jpg',
  '小米':       BASE_PATH.grains_and_starches + 'millet.jpg',
  '绿豆':       BASE_PATH.grains_and_starches + 'mung%20bean.jpg',
  '燕麦':       BASE_PATH.grains_and_starches + 'oat.jpg',
  '红豆':       BASE_PATH.grains_and_starches + 'ormosia.jpg',
  '土豆':       BASE_PATH.grains_and_starches + 'potato.jpg',
  '马铃薯':     BASE_PATH.grains_and_starches + 'potato.jpg',
  '紫米':       BASE_PATH.grains_and_starches + 'purple%20rice.jpg',
  '大米':       BASE_PATH.grains_and_starches + 'rice.jpg',
  '米饭':       BASE_PATH.grains_and_starches + 'rice.jpg',
  '芝麻':       BASE_PATH.grains_and_starches + 'sesame.jpg',
  '黄豆':       BASE_PATH.grains_and_starches + 'soybean.jpg',
  '大豆':       BASE_PATH.grains_and_starches + 'soybean.jpg',
  '糯米':       BASE_PATH.grains_and_starches + 'sticky%20rice.jpg',
  '红薯':       BASE_PATH.grains_and_starches + 'sweet%20potato.jpg',
  '甘薯':       BASE_PATH.grains_and_starches + 'sweet%20potato.jpg',
  '芋头':       BASE_PATH.grains_and_starches + 'taro.jpg',
  '山药':       BASE_PATH.grains_and_starches + 'yam.jpg',

  // ==================== 肉蛋类 ====================
  '牛肉':       BASE_PATH.meat_and_egg + 'beef.jpg',
  '鸡胸肉':     BASE_PATH.meat_and_egg + 'chicken%20breast.jpg',
  '鸡腿':       BASE_PATH.meat_and_egg + 'chicken%20leg.jpg',
  '鸡翅':       BASE_PATH.meat_and_egg + 'chicken%20wing.jpg',
  '鸡肉':       BASE_PATH.meat_and_egg + 'chicken.jpg',
  '蛤蜊':       BASE_PATH.meat_and_egg + 'clam%20meat.jpg',
  '螃蟹':       BASE_PATH.meat_and_egg + 'crab.jpg',
  '腊肉':       BASE_PATH.meat_and_egg + 'cured%20meat.jpg',
  '鸭肉':       BASE_PATH.meat_and_egg + 'duck.jpg',
  '鸡蛋':       BASE_PATH.meat_and_egg + 'egg.jpg',
  '鱼肉':       BASE_PATH.meat_and_egg + 'fish.jpg',
  '鹅肉':       BASE_PATH.meat_and_egg + 'goose.jpg',
  '火腿':       BASE_PATH.meat_and_egg + 'ham.jpg',
  '羊肉':       BASE_PATH.meat_and_egg + 'mutton.jpg',
  '猪肉':       BASE_PATH.meat_and_egg + 'pork.jpg',
  '三文鱼':     BASE_PATH.meat_and_egg + 'salmon.jpg',
  '香肠':       BASE_PATH.meat_and_egg + 'sausage.jpg',
  '扇贝':       BASE_PATH.meat_and_egg + 'scallop.jpg',
  '虾':         BASE_PATH.meat_and_egg + 'shrimp.jpg',
  '虾仁':       BASE_PATH.meat_and_egg + 'shrimp.jpg',
  '牛排':       BASE_PATH.meat_and_egg + 'sirloin.jpg',
  '鱿鱼':       BASE_PATH.meat_and_egg + 'squid.jpg',

  // ==================== 其他类 ====================
  '包子':       BASE_PATH.others + 'baozi.jpg',
  '面包':       BASE_PATH.others + 'bread.jpg',
  '饺子':       BASE_PATH.others + 'dumpling.jpg',
  '生姜':       BASE_PATH.others + 'fresh%20ginger.jpg',
  '姜':         BASE_PATH.others + 'fresh%20ginger.jpg',
  '大蒜':       BASE_PATH.others + 'garlic.jpg',
  '蒜':         BASE_PATH.others + 'garlic.jpg',
  '蜂蜜':       BASE_PATH.others + 'honey.jpg',
  '牛奶':       BASE_PATH.others + 'milk.jpg',
  '蘑菇':       BASE_PATH.others + 'mushroom.jpg',
  '金针菇':     BASE_PATH.others + 'needle%20mushroom.jpg',
  '面条':       BASE_PATH.others + 'noodle.jpg',
  '馒头':       BASE_PATH.others + 'steamed%20bun.jpg',

  // ==================== 调味品类 ====================
  '辣椒酱':     BASE_PATH.seasoner + 'chilli%20sauce.jpg',
  '料酒':       BASE_PATH.seasoner + 'cooking%20wine.jpg',
  '老抽':       BASE_PATH.seasoner + 'dark%20soy%20sauce.jpg',
  '番茄酱':     BASE_PATH.seasoner + 'ketchup.jpg',
  '油':         BASE_PATH.seasoner + 'oil.jpg',
  '蚝油':       BASE_PATH.seasoner + 'oyster.jpg',
  '胡椒粉':     BASE_PATH.seasoner + 'pepper.jpg',
  '胡椒':       BASE_PATH.seasoner + 'pepper.jpg',
  '盐':         BASE_PATH.seasoner + 'salt.jpg',
  '生抽':       BASE_PATH.seasoner + 'soy%20sauce.jpg',
  '酱油':       BASE_PATH.seasoner + 'soy%20sauce.jpg',
  '糖':         BASE_PATH.seasoner + 'sugar.jpg',
  '白糖':       BASE_PATH.seasoner + 'sugar.jpg',
  '醋':         BASE_PATH.seasoner + 'vinegar.jpg',

  // ==================== 蔬菜类 ====================
  '娃娃菜':     BASE_PATH.vegetables + 'baby%20cabbage.jpg',
  '苦瓜':       BASE_PATH.vegetables + 'bitter%20gourd.jpg',
  '西兰花':     BASE_PATH.vegetables + 'broccoli.jpg',
  '白菜':       BASE_PATH.vegetables + 'cabbage.jpg',
  '包菜':       BASE_PATH.vegetables + 'cabbage.jpg',
  '胡萝卜':     BASE_PATH.vegetables + 'carrot.jpg',
  '花菜':       BASE_PATH.vegetables + 'cauliflower.jpg',
  '花椰菜':     BASE_PATH.vegetables + 'cauliflower.jpg',
  '芹菜':       BASE_PATH.vegetables + 'celery.jpg',
  '辣椒':       BASE_PATH.vegetables + 'chilli.jpg',
  '韭菜':       BASE_PATH.vegetables + 'chives.jpg',
  '彩椒':       BASE_PATH.vegetables + 'color%20pepper.jpg',
  '黄瓜':       BASE_PATH.vegetables + 'cucumber.jpg',
  '毛豆':       BASE_PATH.vegetables + 'edamame.jpg',
  '茄子':       BASE_PATH.vegetables + 'eggplant.jpg',
  '青椒':       BASE_PATH.vegetables + 'green%20pepper.jpg',
  '生菜':       BASE_PATH.vegetables + 'lettuce.jpg',
  '莲藕':       BASE_PATH.vegetables + 'lotus%20root.jpg',
  '藕':         BASE_PATH.vegetables + 'lotus%20root.jpg',
  '洋葱':       BASE_PATH.vegetables + 'onion.jpg',
  '豌豆':       BASE_PATH.vegetables + 'pea.jpg',
  '南瓜':       BASE_PATH.vegetables + 'pumpkin.jpg',
  '紫甘蓝':     BASE_PATH.vegetables + 'purple%20cabbage.jpg',
  '菠菜':       BASE_PATH.vegetables + 'spinach.jpg',
  '西红柿':     BASE_PATH.vegetables + 'tomato.jpg',
  '番茄':       BASE_PATH.vegetables + 'tomato.jpg',
}

/**
 * 根据食材名称获取封面图 URL
 * @param {string} name - 食材名称
 * @returns {string} 图片 URL
 */
function getIngredientCover(name) {
  if (!name) return DEFAULT_COVER
  return INGREDIENT_COVER_MAP[name] || DEFAULT_COVER
}

module.exports = {
  BASE_PATH,
  DEFAULT_COVER,
  INGREDIENT_COVER_MAP,
  getIngredientCover
}
