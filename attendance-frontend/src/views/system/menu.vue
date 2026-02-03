<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>菜单管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
      </template>
      <el-table :data="tableData" row-key="id" default-expand-all border>
        <el-table-column prop="name" label="菜单名称" min-width="140" />
        <el-table-column prop="path" label="路由路径" width="140" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'info'">{{ row.type === 1 ? '菜单' : '按钮' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="visible" label="可见" width="80">
          <template #default="{ row }">{{ row.visible === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
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
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="/system/xxx" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="可见">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
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
import { getMenuTree, saveMenu, updateMenu, deleteMenu } from '../../api/menu'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null, parentId: null, name: '', path: '', type: 1, permission: '', icon: '', sort: 0, visible: 1
})
const rules = { name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }

const treeSelectData = ref([])

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

async function load() {
  const list = await getMenuTree()
  tableData.value = list
  treeSelectData.value = toTreeSelect(list)
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.parentId = row.parentId ?? null
    form.name = row.name
    form.path = row.path ?? ''
    form.type = row.type ?? 1
    form.permission = row.permission ?? ''
    form.icon = row.icon ?? ''
    form.sort = row.sort ?? 0
    form.visible = row.visible ?? 1
  } else {
    form.id = null
    form.parentId = null
    form.name = ''
    form.path = ''
    form.type = 1
    form.permission = ''
    form.icon = ''
    form.sort = 0
    form.visible = 1
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await updateMenu(form)
    else await saveMenu(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该菜单？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      load()
    }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
</style>
