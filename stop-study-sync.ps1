$ErrorActionPreference = 'Stop'

$processes = @(Get-Process -Name 'StudySync' -ErrorAction SilentlyContinue)
if ($processes.Count -eq 0) {
    Write-Host 'Study Sync nao esta em execucao.' -ForegroundColor Yellow
    Read-Host 'Pressione Enter para fechar'
    exit 0
}

$processes | Stop-Process -Force
Write-Host 'Study Sync foi encerrado.' -ForegroundColor Green
Read-Host 'Pressione Enter para fechar'
