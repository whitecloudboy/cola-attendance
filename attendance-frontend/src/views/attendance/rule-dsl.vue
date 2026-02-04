<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('ruleDsl.title') }}</span>
        <el-button type="primary" style="float:right" :loading="saving" @click="save">保存</el-button>
      </template>
      <el-form label-width="120px" class="rule-form">
        <el-form-item :label="t('ruleDsl.enabled')">
          <el-switch v-model="config.enabled" />
          <span class="form-tip">{{ t('ruleDsl.enabledTip') }}</span>
        </el-form-item>

        <el-divider content-position="left">
          {{ t('ruleDsl.startRules') }}
          <el-button type="primary" link size="small" @click="addStartRule">+ 添加</el-button>
        </el-divider>
        <el-table :data="config.startRules" border size="small" class="rule-table">
          <el-table-column prop="name" :label="t('ruleDsl.ruleName')" width="140" />
          <el-table-column prop="when" :label="t('ruleDsl.whenExpr')" min-width="280">
            <template #default="{ row }">
              <el-input v-model="row.when" type="textarea" :rows="2" placeholder="例：有打卡且迟到≤5分钟 #records != null and !#records.isEmpty() and minutesLate() <= 5" />
            </template>
          </el-table-column>
          <el-table-column prop="startStatus" :label="t('ruleDsl.startStatus')" width="100">
            <template #default="{ row }">
              <el-select v-model="row.startStatus" placeholder="" clearable style="width:90px">
                <el-option :value="1" :label="t('result.statusNormal')" />
                <el-option :value="2" :label="t('result.statusLate')" />
                <el-option :value="3" :label="t('result.statusNoCard')" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="stop" :label="t('ruleDsl.stop')" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.stop" />
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">
          {{ t('ruleDsl.endRules') }}
          <el-button type="primary" link size="small" @click="addEndRule">+ 添加</el-button>
        </el-divider>
        <el-table :data="config.endRules" border size="small" class="rule-table">
          <el-table-column prop="name" :label="t('ruleDsl.ruleName')" width="140" />
          <el-table-column prop="when" :label="t('ruleDsl.whenExpr')" min-width="280">
            <template #default="{ row }">
              <el-input v-model="row.when" type="textarea" :rows="2" placeholder="例：有打卡且早退≤5分钟 #records != null and !#records.isEmpty() and minutesEarlyLeave() <= 5" />
            </template>
          </el-table-column>
          <el-table-column prop="endStatus" :label="t('ruleDsl.endStatus')" width="100">
            <template #default="{ row }">
              <el-select v-model="row.endStatus" placeholder="" clearable style="width:90px">
                <el-option :value="1" :label="t('result.statusNormal')" />
                <el-option :value="2" :label="t('result.statusLate')" />
                <el-option :value="3" :label="t('result.statusNoCard')" />
                <el-option :value="4" :label="t('result.statusAbnormal')" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="stop" :label="t('ruleDsl.stop')" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.stop" />
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">{{ t('ruleDsl.usageTitle') }}</el-divider>
        <div class="usage-guide">
          <p class="usage-intro">{{ t('ruleDsl.usageIntro') }}</p>
          <p class="usage-label">{{ t('ruleDsl.usageVars') }}</p>
          <ul>
            <li>{{ t('ruleDsl.usageVarBan') }}</li>
            <li>{{ t('ruleDsl.usageVarRecords') }}</li>
          </ul>
          <p class="usage-label">{{ t('ruleDsl.usageFuncs') }}</p>
          <ul>
            <li>{{ t('ruleDsl.usageFuncLate') }}</li>
            <li>{{ t('ruleDsl.usageFuncEarly') }}</li>
            <li>{{ t('ruleDsl.usageFuncDept') }}</li>
            <li>{{ t('ruleDsl.usageFuncPerson') }}</li>
          </ul>
          <p class="usage-label">{{ t('ruleDsl.usageExamples') }}</p>
          <ul class="usage-examples">
            <li><code>{{ t('ruleDsl.usageEx1') }}</code></li>
            <li><code>{{ t('ruleDsl.usageEx2') }}</code></li>
            <li><code>{{ t('ruleDsl.usageEx3') }}</code></li>
            <li><code>{{ t('ruleDsl.usageEx4') }}</code></li>
            <li><code>{{ t('ruleDsl.usageEx5') }}</code></li>
          </ul>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getDslConfig, saveDslConfig } from '../../api/rule'

const { t } = useI18n()
const config = ref({ enabled: true, startRules: [], endRules: [] })
const saving = ref(false)

async function load() {
  const data = await getDslConfig()
  config.value = {
    enabled: data.enabled ?? true,
    startRules: Array.isArray(data.startRules) ? data.startRules : [],
    endRules: Array.isArray(data.endRules) ? data.endRules : []
  }
}

function addStartRule() {
  if (!config.value.startRules) config.value.startRules = []
  config.value.startRules.push({ name: 'rule-' + (config.value.startRules.length + 1), when: '', startStatus: 1, stop: true })
}
function addEndRule() {
  if (!config.value.endRules) config.value.endRules = []
  config.value.endRules.push({ name: 'rule-' + (config.value.endRules.length + 1), when: '', endStatus: 1, stop: true })
}
async function save() {
  saving.value = true
  try {
    await saveDslConfig(config.value)
    ElMessage.success(t('common.saveSuccess'))
  } catch (e) {
    // message by interceptor
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
.rule-form { max-width: 900px; }
.rule-table { margin-bottom: 16px; }
.form-tip { margin-left: 12px; color: #909399; font-size: 12px; }
.usage-guide {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #505050;
  line-height: 1.6;
}
.usage-intro { margin: 0 0 12px; }
.usage-label { margin: 12px 0 6px; font-weight: 600; color: #303133; }
.usage-guide ul { margin: 0; padding-left: 20px; }
.usage-guide li { margin: 4px 0; }
.usage-examples li { margin: 8px 0; }
.usage-guide code {
  display: block;
  background: #fff;
  padding: 8px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #c41d7f;
  border: 1px solid #e8e8e8;
  word-break: break-all;
}
</style>
