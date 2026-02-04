<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('result.title') }}</span>
        <el-button type="primary" style="float:right; margin-left:8px" :loading="generateLoading" @click="doGenerateEmpty">{{ t('result.generateEmpty') }}</el-button>
        <el-button style="float:right" :loading="settleLoading" @click="doSettle">{{ t('result.settle') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('result.startDate')">
          <el-date-picker v-model="query.startDate" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item :label="t('result.endDate')">
          <el-date-picker v-model="query.endDate" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item :label="t('result.userName')">
          <el-input v-model="query.userName" placeholder="" clearable style="width:120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ t('action.query') }}</el-button>
          <el-button @click="resetQuery(); load()">{{ t('action.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="attendanceDate" :label="t('result.attendanceDate')" width="110" />
        <el-table-column prop="userName" :label="t('result.userName')" width="100" />
        <el-table-column prop="deptName" :label="t('result.dept')" width="120" />
        <el-table-column prop="shiftType" :label="t('result.shiftType')" width="90" />
        <el-table-column prop="plannedStartTime" :label="t('result.plannedStart')" width="90">
          <template #default="{ row }">{{ formatTime(row.plannedStartTime) }}</template>
        </el-table-column>
        <el-table-column prop="checkInTime" :label="t('result.checkIn')" width="90">
          <template #default="{ row }">{{ formatTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column prop="startStatus" :label="t('result.startStatus')" width="90">
          <template #default="{ row }">{{ statusText(row.startStatus) }}</template>
        </el-table-column>
        <el-table-column prop="plannedEndTime" :label="t('result.plannedEnd')" width="90">
          <template #default="{ row }">{{ formatTime(row.plannedEndTime) }}</template>
        </el-table-column>
        <el-table-column prop="checkOutTime" :label="t('result.checkOut')" width="90">
          <template #default="{ row }">{{ formatTime(row.checkOutTime) }}</template>
        </el-table-column>
        <el-table-column prop="endStatus" :label="t('result.endStatus')" width="90">
          <template #default="{ row }">{{ statusText(row.endStatus) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:12px"
        @current-change="load"
        @size-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { ElMessageBox } from 'element-plus'
import { getResultPage, generateEmptyResult, triggerEndTask } from '../../api/result'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 20, total: 0 })
const query = reactive({ startDate: '', endDate: '', userName: '' })
const generateLoading = ref(false)
const settleLoading = ref(false)

const statusMap = { 1: 'result.statusNormal', 2: 'result.statusLate', 3: 'result.statusNoCard', 4: 'result.statusAbnormal' }

function formatTime(v) {
  if (!v) return '-'
  const s = typeof v === 'string' ? v : String(v)
  return s.length >= 5 ? s.slice(0, 5) : s
}

function statusText(code) {
  if (code == null) return '-'
  return t(statusMap[code] || 'result.statusUnknown')
}

async function load() {
  const res = await getResultPage({
    current: page.current,
    size: page.size,
    startDate: query.startDate || undefined,
    endDate: query.endDate || undefined,
    userKeyword: query.userName?.trim() || undefined
  })
  tableData.value = res.records || []
  page.total = res.total || 0
}

function resetQuery() {
  query.startDate = ''
  query.endDate = ''
  query.userName = ''
}

async function doGenerateEmpty() {
  const today = new Date().toISOString().slice(0, 10)
  generateLoading.value = true
  try {
    const count = await generateEmptyResult(today)
    ElMessage.success('成功生成 ' + count + ' 条空考勤记录')
    load()
  } catch (e) {
    // message by interceptor
  } finally {
    generateLoading.value = false
  }
}

async function doSettle() {
  const today = new Date().toISOString().slice(0, 10)
  try {
    await ElMessageBox.confirm(
      '将对「' + today + '」执行考勤结算（日终补录），未打卡等状态将更新。是否继续？',
      '考勤结算',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  settleLoading.value = true
  try {
    const processed = await triggerEndTask(today)
    ElMessage.success('考勤结算完成，处理 ' + processed + ' 条')
    load()
  } catch (e) {
    // message by interceptor
  } finally {
    settleLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
</style>
