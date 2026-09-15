$ErrorActionPreference = 'Stop'

$projectPath = Split-Path -Parent $PSScriptRoot
$jarPath = Join-Path $projectPath 'target\study-sync-0.0.1-SNAPSHOT.jar'
$serviceName = 'postgresql-x64-18'

Set-Location $projectPath

$localConfigPath = Join-Path $projectPath '.study-sync.local.ps1'
if (Test-Path -LiteralPath $localConfigPath) {
    . $localConfigPath
}

function Show-Failure($message) {
    Write-Host "`nStudy Sync nao foi iniciado: $message" -ForegroundColor Red
    Read-Host 'Pressione Enter para fechar'
    exit 1
}

try {
    $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
    if (-not $service) {
        Show-Failure "o servico do PostgreSQL '$serviceName' nao foi encontrado"
    }
    if ($service.Status -ne 'Running') {
        Start-Service -Name $serviceName
        $service.WaitForStatus('Running', [TimeSpan]::FromSeconds(20))
    }

    if (-not (Test-Path -LiteralPath $jarPath)) {
        Write-Host 'Primeira execucao: compilando a aplicacao...' -ForegroundColor Cyan
        $maven = (Get-Command mvn -ErrorAction SilentlyContinue).Source
        if (-not $maven -and (Test-Path -LiteralPath 'C:\apache\maven\apache-maven-3.9.11\bin\mvn.cmd')) {
            $maven = 'C:\apache\maven\apache-maven-3.9.11\bin\mvn.cmd'
        }
        if (-not $maven) {
            Show-Failure 'Maven nao foi encontrado no PATH'
        }
        & $maven package -DskipTests -q
        if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $jarPath)) {
            Show-Failure 'a compilacao falhou'
        }
    }

    if (-not $env:DATABASE_PASSWORD) {
        Show-Failure 'DATABASE_PASSWORD nao esta configurada nas variaveis de ambiente do Windows'
    }
    $env:SPRING_APPLICATION_JSON = '{"study-sync":{"sync-on-startup":false}}'

    Write-Host 'Study Sync iniciado. Abrindo http://localhost:8080/' -ForegroundColor Green
    Start-Process 'http://localhost:8080/'
    & (Get-Command java -ErrorAction Stop).Source -jar $jarPath
} catch {
    Show-Failure $_.Exception.Message
}
