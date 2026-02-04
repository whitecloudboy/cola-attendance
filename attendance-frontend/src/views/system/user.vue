<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('user.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item :label="t('user.username')">
          <el-input v-model="query.username" :placeholder="t('user.username')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ t('action.query') }}</el-button>
          <el-button @click="query.username=''; load()">{{ t('action.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="username" :label="t('user.username')" width="120" />
        <el-table-column prop="displayName" :label="t('user.displayName')" width="120" />
        <el-table-column prop="deptName" :label="t('user.dept')" width="140" />
        <el-table-column prop="phone" :label="t('user.phone')" width="120" />
        <el-table-column prop="status" :label="t('user.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? t('user.enabled') : t('user.disabled') }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('user.dialogEdit') : t('user.dialogAdd')" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('user.username')" prop="username">
          <el-input v-model="form.username" :placeholder="t('user.loginAccount')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="t('user.displayName')" prop="displayName">
          <el-input v-model="form.displayName" :placeholder="t('user.displayName')" />
        </el-form-item>
        <el-form-item v-if="!form.id" :label="t('user.password')" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="t('user.passwordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.dept')">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            :placeholder="t('user.choose')"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item :label="t('user.phone')">
          <el-input v-model="form.phone" :placeholder="t('user.phone')" />
        </el-form-item>
        <el-form-item :label="t('user.status')">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ t('user.enabled') }}</el-radio>
            <el-radio :label="0">{{ t('user.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('user.role')">
          <el-select v-model="form.roleIds" multiple :placeholder="t('user.rolePlaceholder')" style="width:100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.postLabel')">
          <el-select v-model="form.postIds" multiple :placeholder="t('user.postPlaceholder')" style="width:100%">
            <el-option v-for="p in postList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
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
import { getRoleList } from '../../api/role'
import { getPostList } from '../../api/post'
import { getUserPage, getUser, saveUser, updateUser, deleteUser } from '../../api/user'

const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ username: '' })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const deptTree = ref([])
const roleList = ref([])
const postList = ref([])

const { t } = useI18n()

const form = reactive({
  id: null, username: '', displayName: '', password: '', deptId: null, phone: '', status: 1, roleIds: [], postIds: []
})
const rules = {
  username: [{ required: true, message: () => t('user.usernameRequired'), trigger: 'blur' }],
  displayName: [{ required: true, message: () => t('user.displayNameRequired'), trigger: 'blur' }]
}

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

async function load() {
  const res = await getUserPage({
    current: page.current,
    size: page.size,
    username: query.username || undefined
  })
  tableData.value = res.records || []
  page.total = res.total || 0
}

async function loadDeptAndRole() {
  const [deptRes, roleRes, postRes] = await Promise.all([getDeptTree(), getRoleList(), getPostList({})])
  deptTree.value = toTreeSelect(deptRes)
  roleList.value = roleRes || []
  postList.value = postRes || []
}

function openForm(row) {
  if (row) {
    getUser(row.id).then(data => {
      form.id = data.id
      form.username = data.username
      form.displayName = data.displayName
      form.deptId = data.deptId ?? null
      form.phone = data.phone ?? ''
      form.status = data.status ?? 1
      form.roleIds = data.roleIds ?? []
      form.postIds = data.postIds ?? []
      form.password = ''
      dialogVisible.value = true
    })
  } else {
    form.id = null
    form.username = ''
    form.displayName = ''
    form.password = ''
    form.deptId = null
    form.phone = ''
    form.status = 1
    form.roleIds = []
    form.postIds = []
    dialogVisible.value = true
  }
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateUser(form)
    else await saveUser(form)
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('user.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success(t('common.deleteSuccess'))
      load()
    }).catch(() => {})
}

onMounted(() => { load(); loadDeptAndRole() })
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
</style>
