<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('device.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('device.deviceName')">
          <el-input v-model="query.deviceName" placeholder="" clearable />
        </el-form-item>
        <el-form-item :label="t('device.dept')">
          <el-tree-select
            v-model="query.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder=""
            clearable
            check-strictly
            style="width:180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ t('action.query') }}</el-button>
          <el-button @click="resetQuery(); load()">{{ t('action.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="deviceCode" :label="t('device.deviceCode')" width="120" />
        <el-table-column prop="deviceName" :label="t('device.deviceName')" min-width="120" />
        <el-table-column prop="deviceType" :label="t('device.deviceType')" width="90" />
        <el-table-column prop="ipAddress" :label="t('device.ipAddress')" width="120" />
        <el-table-column prop="location" :label="t('device.location')" width="140" />
        <el-table-column prop="deptName" :label="t('device.dept')" width="100" />
        <el-table-column prop="status" :label="t('device.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? t('dept.statusOn') : t('dept.statusOff') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('action.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">{{ t('action.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ t('action.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:12px"
        @current-change="load"
        @size-change="load"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('device.dialogEdit') : t('device.dialogAdd')" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('device.deviceCode')" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="t('device.deviceName')" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('device.deviceType')">
          <el-input v-model="form.deviceType" placeholder="door/face/card" />
        </el-form-item>
        <el-form-item :label="t('device.ipAddress')">
          <el-input v-model="form.ipAddress" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('device.port')">
          <el-input-number v-model="form.port" :min="0" :max="65535" />
        </el-form-item>
        <el-form-item :label="t('device.location')">
          <el-input v-model="form.location" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('device.dept')">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder=""
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item :label="t('device.isAttendance')">
          <el-radio-group v-model="form.isAttendance">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('device.status')">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ t('dept.statusOn') }}</el-radio>
            <el-radio :label="0">{{ t('dept.statusOff') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('device.remark')">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('action.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ t('action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getDeptTree } from '../../api/dept'
import { getDevicePage, saveDevice, updateDevice, deleteDevice } from '../../api/device'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ deviceName: '', deptId: null })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const deptTree = ref([])
const form = reactive({
  id: null, deviceCode: '', deviceName: '', deviceType: '', ipAddress: '', port: null, location: '',
  deptId: null, isAttendance: 1, status: 1, remark: ''
})
const rules = {
  deviceCode: [{ required: true, message: () => t('device.deviceCode') + '?', trigger: 'blur' }],
  deviceName: [{ required: true, message: () => t('device.deviceName') + '?', trigger: 'blur' }]
}

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

async function loadDept() {
  const list = await getDeptTree()
  deptTree.value = toTreeSelect(list)
}

async function load() {
  const res = await getDevicePage({
    current: page.current,
    size: page.size,
    deviceName: query.deviceName || undefined,
    deptId: query.deptId ?? undefined
  })
  tableData.value = res.records || []
  page.total = res.total || 0
}

function resetQuery() {
  query.deviceName = ''
  query.deptId = null
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.deviceCode = row.deviceCode ?? ''
    form.deviceName = row.deviceName ?? ''
    form.deviceType = row.deviceType ?? ''
    form.ipAddress = row.ipAddress ?? ''
    form.port = row.port ?? null
    form.location = row.location ?? ''
    form.deptId = row.deptId ?? null
    form.isAttendance = row.isAttendance ?? 1
    form.status = row.status ?? 1
    form.remark = row.remark ?? ''
  } else {
    form.id = null
    form.deviceCode = ''
    form.deviceName = ''
    form.deviceType = ''
    form.ipAddress = ''
    form.port = null
    form.location = ''
    form.deptId = null
    form.isAttendance = 1
    form.status = 1
    form.remark = ''
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateDevice(form)
    else await saveDevice(form)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('device.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deleteDevice(row.id)
      ElMessage.success(t('common.deleteSuccess'))
      load()
    }).catch(() => {})
}

onMounted(() => { load(); loadDept() })
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
</style>
