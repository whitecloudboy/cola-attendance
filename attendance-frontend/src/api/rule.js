/**
 * 考勤规则 DSL 配置相关 API
 */
import request from './request'

/** 获取当前 DSL 规则配置（启用状态、上班/下班规则等） */
export function getDslConfig() {
  return request.get('/attendance/rule/dsl')
}

/** 保存 DSL 规则配置 */
export function saveDslConfig(config) {
  return request.put('/attendance/rule/dsl', config)
}
