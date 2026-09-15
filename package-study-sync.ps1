$ErrorActionPreference = 'Stop'

$projectPath = $PSScriptRoot
$jarName = 'study-sync-0.0.1-SNAPSHOT.jar'
$jarPath = Join-Path $projectPath "target\$jarName"
$outputPath = Join-Path $projectPath 'dist'
$java21Path = 'C:\Program Files\Java\jdk-21\bin'
$jpackagePath = Join-Path $java21Path 'jpackage.exe'

Set-Location $projectPath

if (-not (Test-Path -LiteralPath $jpackagePath)) {
    $jpackagePath = (Get-Command jpackage -ErrorAction SilentlyContinue).Source
}
if (-not $jpackagePath) {
    throw 'JDK 21 com jpackage nao foi encontrado.'
}

$maven = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $maven -and (Test-Path -LiteralPath 'C:\apache\maven\apache-maven-3.9.11\bin\mvn.cmd')) {
    $maven = 'C:\apache\maven\apache-maven-3.9.11\bin\mvn.cmd'
}
if (-not $maven) {
    throw 'Maven nao foi encontrado no PATH.'
}

Write-Host 'Compilando a aplicacao...' -ForegroundColor Cyan
& $maven package -DskipTests -q
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $jarPath)) {
    throw 'A compilacao falhou.'
}

New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
$appImagePath = Join-Path $outputPath 'StudySync'
if (Test-Path -LiteralPath $appImagePath) {
    Remove-Item -LiteralPath $appImagePath -Recurse -Force
}

Write-Host 'Gerando o pacote executavel...' -ForegroundColor Cyan
& $jpackagePath `
    --type app-image `
    --name StudySync `
    --input (Join-Path $projectPath 'target') `
    --main-jar $jarName `
    --dest $outputPath `
    --app-version 1.0.0 `
    --vendor 'JoseV-001' `
    --description 'Study Sync - Clockify para Notion' `
    --java-options '-Dstudy-sync.sync-on-startup=false' `
    --java-options '-Dstudy-sync.open-browser=true'

if ($LASTEXITCODE -ne 0) {
    throw 'A criacao do pacote falhou.'
}

$executablePath = Join-Path $appImagePath 'StudySync.exe'
if (-not (Test-Path -LiteralPath $executablePath)) {
    throw 'O executavel nao foi encontrado apos o empacotamento.'
}

Write-Host "`nExecutavel criado em: $executablePath" -ForegroundColor Green
