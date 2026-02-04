<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('shift.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('shift.name')">
          <el-input v-model="query.name" placeholder="" clearable />
        </el-form-item>
        <el-form-item :label="t('shift.dept')">
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
        <el-table-column prop="name" :label="t('shift.name')" min-width="120" />
        <el-table-column prop="code" :label="t('shift.code')" width="100" />
        <el-table-column prop="deptName" :label="t('shift.dept')" width="120" />
        <el-table-column prop="sort" :label="t('shift.sort')" width="70" />
        <el-table-column prop="groupNo" :label="t('shift.groupNo')" width="80" />
        <el-table-column :label="t('shift.color')" width="80">
          <template #default="{ row }">
            <span v-if="row.color" class="color-dot" :style="{ background: row.color }" /> {{ row.color || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('shift.startTime')" width="90">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('shift.endTime')" width="90">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="shiftType" :label="t('shift.shiftType')" width="90" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('shift.dialogEdit') : t('shift.dialogAdd')" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('shift.name')" prop="name">
          <el-input v-model="form.name" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('shift.code')">
          <el-input v-model="form.code" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('shift.dept')">
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
        <el-form-item :label="t('shift.sort')">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="t('shift.groupNo')">
          <el-input v-model="form.groupNo" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('shift.color')">
          <el-input v-model="form.color" placeholder="#1890ff" />
        </el-form-item>
        <el-form-item :label="t('shift.startTime')" prop="startTime">
          <el-time-picker v-model="form.startTimeValue" value-format="HH:mm:ss" format="HH:mm" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('shift.endTime')" prop="endTime">
          <el-time-picker v-model="form.endTimeValue" value-format="HH:mm:ss" format="HH:mm" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('shift.isCrossDay')">
          <el-radio-group v-model="form.isCrossDay">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('shift.shiftType')">
          <el-input v-model="form.shiftType" placeholder="行政/值班/夜班" />
        </el-form-item>
        <el-form-item :label="t('shift.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { getShiftPage, getShift, saveShift, updateShift, deleteShift } from '../../api/shift'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ name: '', deptId: null })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const deptTree = ref([])
const form = reactive({
  id: null, name: '', code: '', deptId: null, sort: 0, groupNo: '', color: '',
  startTimeValue: null, endTimeValue: null, startTime: '', endTime: '',
  isCrossDay: 0, shiftType: '', description: ''
})
const rules = { name: [{ required: true, message: () => t('shift.name') + '?', trigger: 'blur' }] }

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

function formatTime(v) {
  if (!v) return '-'
  if (typeof v === 'string') return v.length > 8 ? v.substring(0, 8) : v
  return String(v)
}

async function loadDept() {
  const list = await getDeptTree()
  deptTree.value = toTreeSelect(list)
}

async function load() {
  const res = await getShiftPage({
    current: page.current,
    size: page.size,
    name: query.name || undefined,
    deptId: query.deptId ?? undefined
  })
  tableData.value = res.records || []
  page.total = res.total || 0
}

function resetQuery() {
  query.name = ''
  query.deptId = null
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.name = row.name
    form.code = row.code ?? ''
    form.deptId = row.deptId ?? null
    form.sort = row.sort ?? 0
    form.groupNo = row.groupNo ?? ''
    form.color = row.color ?? ''
    form.startTime = row.startTime ?? ''
    form.endTime = row.endTime ?? ''
    form.startTimeValue = row.startTime ? (row.startTime.length >= 8 ? row.startTime : row.startTime + ':00') : null
    form.endTimeValue = row.endTime ? (row.endTime.length >= 8 ? row.endTime : row.endTime + ':00') : null
    form.isCrossDay = row.isCrossDay ?? 0
    form.shiftType = row.shiftType ?? ''
    form.description = row.description ?? ''
  } else {
    form.id = null
    form.name = ''
    form.code = ''
    form.deptId = null
    form.sort = 0
    form.groupNo = ''
    form.color = ''
    form.startTime = ''
    form.endTime = ''
    form.startTimeValue = null
    form.endTimeValue = null
    form.isCrossDay = 0
    form.shiftType = ''
    form.description = ''
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  form.startTime = form.startTimeValue || ''
  form.endTime = form.endTimeValue || ''
  submitLoading.value = true
  try {
    const payload = {
      id: form.id,
      name: form.name,
      code: form.code || null,
      deptId: form.deptId,
      sort: form.sort,
      groupNo: form.groupNo || null,
      color: form.color || null,
      startTime: form.startTime,
      endTime: form.endTime,
      isCrossDay: form.isCrossDay,
      shiftType: form.shiftType || null,
      description: form.description || null
    }
    if (form.id) await updateShift(payload)
    else await saveShift(payload)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('shift.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deleteShift(row.id)
      ElMessage.success(t('common.deleteSuccess'))
      load()
    }).catch(() => {})
}

onMounted(() => { load(); loadDept() })
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
.color-dot { display: inline-block; width: 14px; height: 14px; border-radius: 2px; margin-right: 6px; vertical-align: middle; }
</style>
