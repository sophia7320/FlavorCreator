// 全局状态管理工具
const requestConfig = require('../config/request.json')

/**
 * 获取当前使用的请求方式
 * @returns {boolean} true - 使用 callContainer, false - 使用直连请求
 */
function getUseCallContainer() {
  return requestConfig.useCallContainer
}

module.exports = {
  getUseCallContainer
}
