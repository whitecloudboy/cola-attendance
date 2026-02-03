<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>部门管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
      </template>
      <el-table :data="tableData" row-key="id" default-expand-all border>
        <el-table-column prop="name" label="部门名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="480" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'name', value: 'id' }"
            placeholder="不选为顶级"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
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
import { getDeptTree, saveDept, updateDept, deleteDept } from '../../api/dept'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({ id: null, parentId: null, name: '', sort: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

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
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该部门？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
</style>
