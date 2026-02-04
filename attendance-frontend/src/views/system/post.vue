<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('post.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('post.name')">
          <el-input v-model="query.name" placeholder="" clearable />
        </el-form-item>
        <el-form-item :label="t('post.dept')">
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
        <el-table-column prop="name" :label="t('post.name')" width="140" />
        <el-table-column prop="code" :label="t('post.code')" width="120" />
        <el-table-column prop="deptName" :label="t('post.dept')" width="140" />
        <el-table-column prop="sort" :label="t('post.sort')" width="80" />
        <el-table-column prop="status" :label="t('post.status')" width="80">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('post.dialogEdit') : t('post.dialogAdd')" width="480" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('post.name')" prop="name">
          <el-input v-model="form.name" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('post.code')">
          <el-input v-model="form.code" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('post.dept')">
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
        <el-form-item :label="t('post.sort')">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="t('post.status')">
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
import { getDeptTree } from '../../api/dept'
import { getPostPage, getPost, savePost, updatePost, deletePost } from '../../api/post'

const { t } = useI18n()
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ name: '', deptId: null })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const deptTree = ref([])
const form = reactive({ id: null, name: '', code: '', deptId: null, sort: 0, status: 1 })
const rules = { name: [{ required: true, message: () => t('post.name') + '?', trigger: 'blur' }] }

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
  const res = await getPostPage({
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
    form.status = row.status ?? 1
  } else {
    form.id = null
    form.name = ''
    form.code = ''
    form.deptId = null
    form.sort = 0
    form.status = 1
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updatePost(form)
    else await savePost(form)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('post.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deletePost(row.id)
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
