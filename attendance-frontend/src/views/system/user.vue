<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>用户管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
      </template>
      <el-form :inline="true" class="query-form">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="query.username=''; load()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="displayName" label="姓名" width="120" />
        <el-table-column prop="deptName" label="部门" width="140" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="displayName">
          <el-input v-model="form.displayName" placeholder="显示名称" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="不填默认123456" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width:100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTree } from '../../api/dept'
import { getRoleList } from '../../api/role'
import { getUserPage, getUser, saveUser, updateUser, deleteUser } from '../../api/user'

const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const query = reactive({ username: '' })
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const deptTree = ref([])
const roleList = ref([])

const form = reactive({
  id: null, username: '', displayName: '', password: '', deptId: null, phone: '', status: 1, roleIds: []
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
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
  const [deptRes, roleRes] = await Promise.all([getDeptTree(), getRoleList()])
  deptTree.value = toTreeSelect(deptRes)
  roleList.value = roleRes || []
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
    dialogVisible.value = true
  }
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateUser(form)
    else await saveUser(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      load()
    }).catch(() => {})
}

onMounted(() => { load(); loadDeptAndRole() })
</script>

<style scoped>
.page { padding: 0; }
.query-form { margin-bottom: 0; }
</style>
