# E2E 全链路编排脚本
# 流程：清理 -> 排班 -> 生成空考勤 -> 模拟打卡 -> 日终补录 -> 正确性校验
# 使用：先启动 attendance-backend，再执行本脚本
Param(
  [int]$Cycles = 1,
  [string]$Date = "",
  [int]$SampleCount = 5,
  [string]$ReportDir = "logs"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$repo = Split-Path $MyInvocation.MyCommand.Path -Parent
$scriptDir = Join-Path $repo "scripts"
$reportPath = if ([System.IO.Path]::IsPathRooted($ReportDir)) { $ReportDir } else { Join-Path $repo $ReportDir }
if (-not (Test-Path $reportPath)) { New-Item -ItemType Directory -Path $reportPath | Out-Null }

if ([string]::IsNullOrWhiteSpace($Date)) {
  $Date = (Get-Date).ToString("yyyy-MM-dd")
}

function Run-Step($label, $scriptBlock) {
  Write-Output "[STEP] $label"
  & $scriptBlock
  if ($LASTEXITCODE -ne 0) {
    Write-Error "Step failed: $label"
    exit 1
  }
}

for ($i = 1; $i -le $Cycles; $i++) {
  Write-Output "==== Cycle $i/$Cycles date=$Date ===="

  Run-Step "Cleanup" { python "$scriptDir\cleanup_attendance.py" --date $Date }
  Run-Step "Auto schedule" { python "$scriptDir\auto_schedule.py" --date $Date }
  Run-Step "Simulate punch + triggers" {
    python "$scriptDir\simulate_punch.py" --date $Date --trigger-generate --trigger-end
  }
  $reportFile = Join-Path $reportPath ("verify-$Date-cycle$i.json")
  Run-Step "Verify" { python "$scriptDir\verify_result.py" --date $Date --samples $SampleCount --output $reportFile }
}

Write-Output "[DONE] full link cycles completed"
