$ErrorActionPreference = 'Stop'

$projectPath = $PSScriptRoot
$defaultExecutable = Join-Path $projectPath 'release\StudySync\StudySync.exe'
$executablePath = if ($env:STUDY_SYNC_EXECUTABLE) { $env:STUDY_SYNC_EXECUTABLE } else { $defaultExecutable }
$dashboardTaskName = 'Study Sync - Dashboard'
$weeklyTaskName = 'Study Sync - Weekly Sync'
$currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name

if (-not (Test-Path -LiteralPath $executablePath)) {
    throw "Executavel nao encontrado em: $executablePath. Execute package-study-sync.ps1 primeiro."
}

$resolvedExecutable = (Resolve-Path -LiteralPath $executablePath).Path
$dashboardCommand = '"{0}" --study-sync.open-browser=false --study-sync.sync-on-startup=false --study-sync.schedule.enabled=false' -f $resolvedExecutable
$weeklyCommand = '"{0}" --spring.main.web-application-type=none --study-sync.open-browser=false --study-sync.sync-on-startup=false --study-sync.schedule.enabled=false --study-sync.run-once=true' -f $resolvedExecutable

& schtasks.exe /Create /TN $dashboardTaskName /TR $dashboardCommand /SC ONLOGON /RU $currentUser /RL LIMITED /F
if ($LASTEXITCODE -ne 0) {
    throw 'Nao foi possivel criar a tarefa de inicio da dashboard.'
}

& schtasks.exe /Create /TN $weeklyTaskName /TR $weeklyCommand /SC WEEKLY /D MON /ST 20:00 /RU $currentUser /RL LIMITED /F
if ($LASTEXITCODE -ne 0) {
    throw 'Nao foi possivel criar a tarefa de sincronizacao semanal.'
}

$dashboardSettings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -ExecutionTimeLimit ([TimeSpan]::Zero)
Set-ScheduledTask -TaskName $dashboardTaskName -Settings $dashboardSettings | Out-Null

$weeklySettings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -ExecutionTimeLimit (New-TimeSpan -Hours 2)
Set-ScheduledTask -TaskName $weeklyTaskName -Settings $weeklySettings | Out-Null

Write-Host 'Inicio automatico configurado.' -ForegroundColor Green
Write-Host "- Dashboard: ao entrar no Windows ($dashboardTaskName)"
Write-Host "- Sincronizacao: toda segunda-feira as 20:00 ($weeklyTaskName)"
