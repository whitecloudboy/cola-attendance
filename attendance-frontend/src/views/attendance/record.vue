<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('record.title') }}</span>
        <el-button type="primary" style="float:right" @click="showImport = true">{{ t('record.importExcel') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('record.startDate')">
          <el-date-picker v-model="query.startDate" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item :label="t('record.endDate')">
          <el-date-picker v-model="query.endDate" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item :label="t('record.userName')">
          <el-input v-model="query.userName" placeholder="" clearable style="width:120px" />
        </el-form-item>
        <el-form-item :label="t('record.device')">
          <el-select v-model="query.deviceId" clearable placeholder="" style="width:160px">
            <el-option v-for="d in deviceList" :key="d.id" :label="d.deviceName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ t('action.query') }}</el-button>
          <el-button @click="resetQuery(); load()">{{ t('action.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="userName" :label="t('record.userName')" width="100" />
        <el-table-column prop="deptName" :label="t('record.dept')" width="120" />
        <el-table-column prop="eventTime" :label="t('record.eventTime')" width="170">
          <template #default="{ row }">{{ formatTime(row.eventTime) }}</template>
        </el-table-column>
        <el-table-column prop="deviceName" :label="t('record.device')" width="120" />
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

    <el-dialog v-model="showImport" :title="t('record.importExcel')" width="480">
      <p class="import-tip">{{ t('record.importTip') }}</p>
      <p class="import-actions">
        <el-button type="primary" link :loading="downloadLoading" @click="doDownloadTemplate">{{ t('record.downloadTemplate') }}</el-button>
      </p>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx"
        :on-change="onFileChange"
      >
        <el-button type="primary">选择文件</el-button>
      </el-upload>
      <template #footer>
        <el-button @click="showImport = false">{{ t('action.cancel') }}</el-button>
        <el-button type="primary" :loading="importLoading" @click="doImport">{{ t('action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getDeviceList } from '../../api/device'
import { getRecordPage, importRecordExcel, downloadRecordTemplate } from '../../api/record'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 20, total: 0 })
const query = reactive({ startDate: '', endDate: '', userName: '', deviceId: null })
const deviceList = ref([])
const showImport = ref(false)
const importLoading = ref(false)
const downloadLoading = ref(false)
const uploadRef = ref(null)
let selectedFile = null

function onFileChange(_file, fileList) {
  selectedFile = fileList.length ? fileList[0].raw : null
}

function formatTime(v) {
  if (!v) return '-'
  const s = typeof v === 'string' ? v : String(v)
  return s.replace('T', ' ').slice(0, 19)
}

async function loadDeviceList() {
  const list = await getDeviceList({})
  deviceList.value = list || []
}

async function load() {
  const res = await getRecordPage({
    current: page.current,
    size: page.size,
    deviceId: query.deviceId ?? undefined,
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
  query.deviceId = null
}

async function doDownloadTemplate() {
  downloadLoading.value = true
  try {
    const blob = await downloadRecordTemplate()
    if (blob.type === 'application/json') {
      const text = await blob.text()
      const json = JSON.parse(text)
      ElMessage.error(json.msg || '下载失败')
      return
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '打卡记录导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板已下载')
  } catch (e) {
    ElMessage.error(e.message || '下载失败')
  } finally {
    downloadLoading.value = false
  }
}

async function doImport() {
  if (!selectedFile) {
    ElMessage.warning('请先选择文件')
    return
  }
  importLoading.value = true
  try {
    const count = await importRecordExcel(selectedFile)
    ElMessage.success('成功导入 ' + count + ' 条')
    showImport.value = false
    selectedFile = null
    if (uploadRef.value) uploadRef.value.clearFiles()
    load()
  } catch (e) {
    // message by interceptor
  } finally {
    importLoading.value = false
  }
}

onMounted(async () => {
  await loadDeviceList()
  load()
})
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
.import-tip { color: #666; font-size: 12px; margin-bottom: 8px; }
.import-actions { margin-bottom: 12px; }
</style>
