<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>{{ t('schedulePage.title') }}</span>
      </template>
      <el-form :inline="true" class="toolbar">
        <el-form-item :label="t('schedulePage.startDate')">
          <el-date-picker v-model="dateRange[0]" type="date" value-format="YYYY-MM-DD" :placeholder="t('schedulePage.startDate')" />
        </el-form-item>
        <el-form-item :label="t('schedulePage.endDate')">
          <el-date-picker v-model="dateRange[1]" type="date" value-format="YYYY-MM-DD" :placeholder="t('schedulePage.endDate')" />
        </el-form-item>
        <el-form-item :label="t('schedulePage.dept')">
          <el-tree-select
            v-model="filterDeptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            :placeholder="t('user.choose')"
            clearable
            check-strictly
            style="width:160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">{{ t('action.query') }}</el-button>
        </el-form-item>
      </el-form>

      <div class="schedule-wrap">
        <el-table :data="gridRows" border size="small" class="schedule-table">
          <el-table-column prop="shiftName" :label="t('schedulePage.shift')" width="140" fixed>
            <template #default="{ row }">
              <span class="shift-cell" :style="row.color ? { borderLeft: '3px solid ' + row.color } : {}">{{ row.shiftName }}</span>
            </template>
          </el-table-column>
          <el-table-column v-for="d in dateColumns" :key="d" :label="d" width="100" align="center">
            <template #default="{ row }">
              <div class="cell-person" @click="openSetPerson(row.shiftId, d)">
                <template v-if="getSlot(row.shiftId, d).userName">
                  {{ getSlot(row.shiftId, d).userName }}
                  <el-button type="danger" link size="small" class="cell-clear" @click.stop="clearSlot(getSlot(row.shiftId, d).id)">×</el-button>
                </template>
                <span v-else class="cell-empty">+</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="t('schedulePage.setPerson')" width="400" @close="dialogVisible = false">
      <el-form label-width="80px">
        <el-form-item :label="t('schedulePage.person')">
          <el-select v-model="setPersonUserId" filterable :placeholder="t('user.choose')" style="width:100%">
            <el-option v-for="u in userList" :key="u.id" :label="u.displayName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('action.cancel') }}</el-button>
        <el-button type="primary" @click="confirmSetPerson">{{ t('action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getDeptTree } from '../../api/dept'
import { getShiftList } from '../../api/shift'
import { getScheduleList, setSchedule, deleteSchedule } from '../../api/schedule'
import { getUserPage } from '../../api/user'

const { t } = useI18n()
const dateRange = ref([getDefaultStart(), getDefaultEnd()])
const filterDeptId = ref(null)
const deptTree = ref([])
const gridRows = ref([])
const dateColumns = ref([])
const slotMap = ref({})
const userList = ref([])
const dialogVisible = ref(false)
const setPersonShiftId = ref(null)
const setPersonDate = ref(null)
const setPersonUserId = ref(null)

function getDefaultStart() {
  const d = new Date()
  d.setDate(1)
  return d.toISOString().slice(0, 10)
}
function getDefaultEnd() {
  const d = new Date()
  d.setMonth(d.getMonth() + 1)
  d.setDate(0)
  return d.toISOString().slice(0, 10)
}

function toTreeSelect(nodes) {
  return (nodes || []).map(n => ({
    id: n.id,
    name: n.name,
    children: n.children?.length ? toTreeSelect(n.children) : undefined
  }))
}

function getSlot(shiftId, date) {
  return slotMap.value[`${date}_${shiftId}`] || {}
}

function openSetPerson(shiftId, date) {
  setPersonShiftId.value = shiftId
  setPersonDate.value = date
  setPersonUserId.value = null
  dialogVisible.value = true
}

async function confirmSetPerson() {
  if (!setPersonUserId.value) {
    ElMessage.warning(t('user.choose'))
    return
  }
  try {
    await setSchedule({ workDate: setPersonDate.value, shiftId: setPersonShiftId.value, userId: setPersonUserId.value })
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // message by request interceptor
  }
}

async function clearSlot(scheduleId) {
  if (!scheduleId) return
  try {
    await deleteSchedule(scheduleId)
    ElMessage.success(t('common.deleteSuccess'))
    loadData()
  } catch (e) {}
}

async function loadDept() {
  const list = await getDeptTree()
  deptTree.value = toTreeSelect(list)
}

async function loadShifts() {
  const list = await getShiftList({ deptId: filterDeptId.value ?? undefined })
  gridRows.value = (list || []).map(s => ({ shiftId: s.id, shiftName: s.name, color: s.color, sort: s.sort })).sort((a, b) => (a.sort || 0) - (b.sort || 0))
}

async function loadScheduleList() {
  const start = dateRange.value[0]
  const end = dateRange.value[1]
  if (!start || !end) return
  const list = await getScheduleList({ startDate: start, endDate: end, deptId: filterDeptId.value ?? undefined })
  const map = {}
  ;(list || []).forEach(s => {
    const key = `${s.workDate}_${s.shiftId}`
    map[key] = { id: s.id, userId: s.userId, userName: s.userName }
  })
  slotMap.value = map
}

function buildDateColumns() {
  const start = dateRange.value[0]
  const end = dateRange.value[1]
  if (!start || !end) { dateColumns.value = []; return }
  const arr = []
  const s = new Date(start)
  const e = new Date(end)
  while (s <= e) {
    arr.push(s.toISOString().slice(0, 10))
    s.setDate(s.getDate() + 1)
  }
  dateColumns.value = arr
}

async function loadUserList() {
  const res = await getUserPage({ current: 1, size: 500 })
  userList.value = res.records || []
}

async function loadData() {
  buildDateColumns()
  await loadShifts()
  await loadScheduleList()
}

onMounted(async () => {
  await loadDept()
  await loadUserList()
  loadData()
})
</script>

<style scoped>
.page { padding: 0; }
.toolbar { margin-bottom: 12px; }
.schedule-wrap { overflow-x: auto; }
.schedule-table { min-width: 100%; }
.shift-cell { padding-left: 6px; display: block; }
.cell-person { min-height: 36px; cursor: pointer; padding: 4px; }
.cell-empty { color: #999; font-size: 16px; }
.cell-clear { padding: 0 4px; vertical-align: middle; }
</style>
