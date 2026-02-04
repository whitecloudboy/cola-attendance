<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('menuMgmt.title') }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">{{ t('action.add') }}</el-button>
      </template>
      <el-table :data="tableData" row-key="id" default-expand-all border>
        <el-table-column prop="name" :label="t('menuMgmt.menuName')" min-width="140" />
        <el-table-column prop="path" :label="t('menuMgmt.routePath')" width="140" />
        <el-table-column prop="type" :label="t('menuMgmt.type')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'info'">{{ row.type === 1 ? t('menuMgmt.typeMenu') : t('menuMgmt.typeButton') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="t('menuMgmt.sort')" width="80" />
        <el-table-column prop="visible" :label="t('menuMgmt.visible')" width="80">
          <template #default="{ row }">{{ row.visible === 1 ? t('menuMgmt.yes') : t('menuMgmt.no') }}</template>
        </el-table-column>
        <el-table-column :label="t('action.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openForm(row)">{{ t('action.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ t('action.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('menuMgmt.dialogEdit') : t('menuMgmt.dialogAdd')" width="520" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('menuMgmt.parent')">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'name', value: 'id' }"
            :placeholder="t('menuMgmt.parentPlaceholder')"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.menuName')" prop="name">
          <el-input v-model="form.name" :placeholder="t('menuMgmt.menuName')" />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.routePath')">
          <el-input v-model="form.path" :placeholder="t('menuMgmt.pathPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.type')" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">{{ t('menuMgmt.typeMenu') }}</el-radio>
            <el-radio :label="2">{{ t('menuMgmt.typeButton') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('menuMgmt.permission')">
          <el-input v-model="form.permission" :placeholder="t('menuMgmt.permissionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.icon')">
          <el-input v-model="form.icon" :placeholder="t('menuMgmt.iconPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="t('menuMgmt.visible')">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">{{ t('menuMgmt.yes') }}</el-radio>
            <el-radio :label="0">{{ t('menuMgmt.no') }}</el-radio>
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
import { getMenuTree, saveMenu, updateMenu, deleteMenu } from '../../api/menu'

const { t } = useI18n()
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null, parentId: null, name: '', path: '', type: 1, permission: '', icon: '', sort: 0, visible: 1
})
const rules = { name: [{ required: true, message: () => t('menuMgmt.menuNameRequired'), trigger: 'blur' }] }

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
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    load()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(t('menuMgmt.deleteConfirm'), t('common.tip'), { type: 'warning' })
    .then(async () => {
      await deleteMenu(row.id)
      ElMessage.success(t('common.deleteSuccess'))
      load()
    }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page { padding: 0; }
</style>
