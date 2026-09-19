$ErrorActionPreference = 'Stop'

$taskNames = @('Study Sync - Dashboard', 'Study Sync - Weekly Sync')

foreach ($taskName in $taskNames) {
    & schtasks.exe /Delete /TN $taskName /F 2>$null
    if ($LASTEXITCODE -notin 0, 1) {
        throw "Nao foi possivel remover a tarefa: $taskName"
    }
}

Write-Host 'Inicio automatico removido.' -ForegroundColor Green
