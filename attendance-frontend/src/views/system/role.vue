<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('role.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('role.code')">
          <el-input v-model="query.code" :placeholder="t('role.codePlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="t('role.name')">
          <el-input v-model="query.name" :placeholder="t('role.namePlaceholder')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ t('action.query') }}</el-button>
          <el-button @click="query.code=''; query.name=''; load()">{{ t('action.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="code" :label="t('role.code')" width="140" />
        <el-table-column prop="name" :label="t('role.name')" width="140" />
        <el-table-column prop="description" :label="t('role.desc')" min-width="200" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('role.dialogEdit') : t('role.dialogAdd')" width="480" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('role.code')" prop="code">
          <el-input v-model="form.code" :placeholder="t('role.codeHint')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="t('role.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('role.name')" />
        </el-form-item>
        <el-form-item :label="t('role.desc')">
          <el-input v-model="form.description" type="textarea" :placeholder="t('role.desc')" :rows="2" />
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
import { getRolePage, getRole, saveRole, updateRole, deleteRole } from '../../api/role'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ code: '', name: '' })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({ id: null, code: '', name: '', description: '' })
const rules = {
  code: [{ required: true, message: () => t('role.codeRequired'), trigger: 'blur' }],
  name: [{ required: true, message: () => t('role.nameRequired'), trigger: 'blur' }]
}

async function load() {
  const res = await getRolePage({
    current: page.current,
    size: page.size,
    code: query.code || undefined,
    name: query.name || undefined
  })
  tableData.value = res.records || []
  page.total = res.total || 0
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.code = row.code
    form.name = row.name
    form.description = row.description ?? ''
  } else {
    form.id = null
    form.code = ''
    form.name = ''
    form.description = ''
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateRole(form)
    else await saveRole(form)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('role.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deleteRole(row.id)
      ElMessage.success(t('common.deleteSuccess'))
      load()
    }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
</style>
