<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('dept.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-table :data="tableData" row-key="id" default-expand-all border>
        <el-table-column prop="name" :label="t('dept.name')" min-width="160" />
        <el-table-column prop="sort" :label="t('dept.sort')" width="80" />
        <el-table-column prop="status" :label="t('dept.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? t('dept.statusOn') : t('dept.statusOff') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('action.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">{{ t('action.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ t('action.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('dept.dialogEdit') : t('dept.dialogAdd')" width="480" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('dept.parent')">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'name', value: 'id' }"
            :placeholder="t('dept.parentPlaceholder')"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item :label="t('dept.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('dept.name')" />
        </el-form-item>
        <el-form-item :label="t('dept.sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="t('dept.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ t('dept.statusOn') }}</el-radio>
            <el-radio :label="0">{{ t('dept.statusOff') }}</el-radio>
          </el-radio-group>
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
import { getDeptTree, saveDept, updateDept, deleteDept } from '../../api/dept'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({ id: null, parentId: null, name: '', sort: 0, status: 1 })
const { t } = useI18n()
const rules = { name: [{ required: true, message: () => t('dept.nameRequired'), trigger: 'blur' }] }

const treeSelectData = ref([])

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

async function load() {
  const list = await getDeptTree()
  tableData.value = list
  treeSelectData.value = toTreeSelect(list)
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.parentId = row.parentId ?? null
    form.name = row.name
    form.sort = row.sort ?? 0
    form.status = row.status ?? 1
  } else {
    form.id = null
    form.parentId = null
    form.name = ''
    form.sort = 0
    form.status = 1
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateDept(form)
    else await saveDept(form)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('dept.deleteConfirm'), t('common.tip'), {
    type: 'warning'
  }).then(async () => {
    await deleteDept(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
</style>
