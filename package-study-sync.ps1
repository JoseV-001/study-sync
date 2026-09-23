$ErrorActionPreference = 'Stop'

$projectPath = $PSScriptRoot
$appVersion = '1.0.1'
$jarName = "study-sync-$appVersion.jar"
$jarPath = Join-Path $projectPath "target\$jarName"
$outputPath = Join-Path $projectPath 'release'
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
$inputPath = Join-Path $projectPath 'target\desktop-input'
if (Test-Path -LiteralPath $inputPath) {
    $resolvedInput = (Resolve-Path -LiteralPath $inputPath).Path
    $expectedInput = [System.IO.Path]::GetFullPath((Join-Path $projectPath 'target\desktop-input'))
    if ($resolvedInput -ne $expectedInput -or -not $resolvedInput.StartsWith($projectPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw 'Caminho temporario de pacote inesperado.'
    }
    Remove-Item -LiteralPath $inputPath -Recurse -Force
}
New-Item -ItemType Directory -Path $inputPath -Force | Out-Null
Copy-Item -LiteralPath $jarPath -Destination (Join-Path $inputPath $jarName) -Force
$appImagePath = Join-Path $outputPath 'StudySync'
if (Test-Path -LiteralPath $appImagePath) {
    $resolvedImage = (Resolve-Path -LiteralPath $appImagePath).Path
    if ($resolvedImage -ne [System.IO.Path]::GetFullPath((Join-Path $projectPath 'release\StudySync'))) {
        throw 'Caminho de pacote inesperado.'
    }
    Remove-Item -LiteralPath $appImagePath -Recurse -Force
}

Write-Host 'Gerando o pacote executavel...' -ForegroundColor Cyan
& $jpackagePath `
    --type app-image `
    --name StudySync `
    --input $inputPath `
    --main-jar $jarName `
    --dest $outputPath `
    --app-version $appVersion `
    --vendor 'JoseV-001' `
    --description 'Study Sync - local study dashboard' `
    --java-options '-Dstudy-sync.desktop=true' `
    --java-options '-Dstudy-sync.sync-on-startup=false' `
    --java-options '-Dstudy-sync.schedule.enabled=false' `
    --java-options '-Dstudy-sync.open-browser=true'

if ($LASTEXITCODE -ne 0) {
    throw 'A criacao do pacote falhou.'
}

$executablePath = Join-Path $appImagePath 'StudySync.exe'
if (-not (Test-Path -LiteralPath $executablePath)) {
    throw 'O executavel nao foi encontrado apos o empacotamento.'
}

$archivePath = Join-Path $outputPath "StudySync-$appVersion-windows-x64.zip"
Compress-Archive -LiteralPath $appImagePath -DestinationPath $archivePath -CompressionLevel Optimal -Force

Write-Host "`nExecutavel criado em: $executablePath" -ForegroundColor Green
Write-Host "Pacote para distribuicao criado em: $archivePath" -ForegroundColor Green
