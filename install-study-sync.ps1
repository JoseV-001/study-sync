param(
    [string]$InstallDirectory = (Join-Path $env:LOCALAPPDATA 'Programs\Study Sync'),
    [switch]$SkipLaunch
)
$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot 'StudySync'
$install = [IO.Path]::GetFullPath($InstallDirectory)
$defaultInstall = [IO.Path]::GetFullPath((Join-Path $env:LOCALAPPDATA 'Programs\Study Sync'))
if (-not (Test-Path -LiteralPath (Join-Path $source 'StudySync.exe'))) {
    throw 'Nao encontrei a pasta StudySync ao lado do instalador. Extraia o ZIP completo antes de continuar.'
}
if ($install -ne $defaultInstall -and -not $install.StartsWith([IO.Path]::GetFullPath($PSScriptRoot) + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'A instalacao personalizada so e aceita dentro da pasta do pacote (uso de validacao).'
}

function Test-Package([string]$directory) {
    $manifestPath = Join-Path $directory 'SHA256SUMS.txt'
    if (-not (Test-Path -LiteralPath $manifestPath)) { throw 'Manifesto SHA-256 ausente; o pacote pode estar incompleto.' }
    $listed = @{}
    foreach ($line in Get-Content -LiteralPath $manifestPath) {
        if ($line -notmatch '^([0-9A-Fa-f]{64})  (.+)$') { throw 'Manifesto SHA-256 invalido.' }
        $expectedHash = $Matches[1]
        $relative = $Matches[2].Replace('/', '\')
        $file = [IO.Path]::GetFullPath((Join-Path $directory $relative))
        if (-not $file.StartsWith([IO.Path]::GetFullPath($directory) + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase) -or -not (Test-Path -LiteralPath $file -PathType Leaf)) {
            throw "Arquivo ausente ou caminho invalido no pacote: $relative"
        }
        $actual = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash
        if ($actual -ne $expectedHash) { throw "Falha de integridade no arquivo: $relative" }
        $listed[$relative] = $true
    }
    $actualFiles = Get-ChildItem -LiteralPath $directory -File -Recurse | Where-Object { $_.Name -ne 'SHA256SUMS.txt' }
    if ($actualFiles.Count -ne $listed.Count) { throw 'O pacote contem arquivos ausentes ou inesperados; extraia-o novamente.' }
}

Test-Package $source
$parent = Split-Path -Parent $install
New-Item -ItemType Directory -Path $parent -Force | Out-Null
$stage = "$install.new"
$backup = "$install.previous"
foreach ($path in @($stage, $backup)) {
    if (Test-Path -LiteralPath $path) {
        $resolved = [IO.Path]::GetFullPath($path)
        if ($resolved -ne [IO.Path]::GetFullPath("$install$(if ($path -eq $stage) { '.new' } else { '.previous' })")) { throw 'Caminho temporario inesperado.' }
        Remove-Item -LiteralPath $path -Recurse -Force
    }
}
New-Item -ItemType Directory -Path $stage -Force | Out-Null
Get-ChildItem -LiteralPath $source -Force | Copy-Item -Destination $stage -Recurse -Force
Test-Package $stage
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'uninstall-study-sync.ps1') -Destination $stage -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'uninstall-study-sync.bat') -Destination $stage -Force

# Ask only this installation to stop, identified by its diagnostics javaHome.
for ($port = 3001; $port -le 3031; $port++) {
    try {
        $diag = Invoke-RestMethod -Uri "http://localhost:$port/sync/diagnostics" -TimeoutSec 1
        if ($diag.javaHome -and $diag.javaHome.StartsWith($install, [StringComparison]::OrdinalIgnoreCase)) {
            Invoke-WebRequest -Method Post -Uri "http://localhost:$port/sync/shutdown" -TimeoutSec 2 | Out-Null
            Start-Sleep -Seconds 3
        }
    } catch { }
}
Get-Process -Name StudySync -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        if ($_.Path -and [IO.Path]::GetFullPath($_.Path).StartsWith($install, [StringComparison]::OrdinalIgnoreCase)) {
            if (-not $_.CloseMainWindow()) { $_.WaitForExit(5000) | Out-Null }
            if (-not $_.HasExited) { Stop-Process -Id $_.Id -Force }
        }
    } catch { }
}

$movedOld = $false
try {
    if (Test-Path -LiteralPath $install) { Move-Item -LiteralPath $install -Destination $backup; $movedOld = $true }
    Move-Item -LiteralPath $stage -Destination $install
} catch {
    if ($movedOld -and -not (Test-Path -LiteralPath $install)) { Move-Item -LiteralPath $backup -Destination $install }
    throw
}
if (Test-Path -LiteralPath $backup) { Remove-Item -LiteralPath $backup -Recurse -Force }

if ($install -eq $defaultInstall) {
    $shell = New-Object -ComObject WScript.Shell
    $menu = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs\Study Sync'
    New-Item -ItemType Directory -Path $menu -Force | Out-Null
    $shortcut = $shell.CreateShortcut((Join-Path $menu 'Study Sync.lnk'))
    $shortcut.TargetPath = Join-Path $install 'StudySync.exe'
    $shortcut.WorkingDirectory = $install
    $shortcut.Save()
    $uninstallLink = $shell.CreateShortcut((Join-Path $menu 'Desinstalar Study Sync.lnk'))
    $uninstallLink.TargetPath = Join-Path $install 'uninstall-study-sync.bat'
    $uninstallLink.WorkingDirectory = $install
    $uninstallLink.Save()

    $uninstallKey = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\StudySync'
    New-Item -Path $uninstallKey -Force | Out-Null
    Set-ItemProperty -Path $uninstallKey -Name DisplayName -Value 'Study Sync'
    Set-ItemProperty -Path $uninstallKey -Name DisplayVersion -Value '1.0.2'
    Set-ItemProperty -Path $uninstallKey -Name Publisher -Value 'JoseV-001'
    Set-ItemProperty -Path $uninstallKey -Name InstallLocation -Value $install
    Set-ItemProperty -Path $uninstallKey -Name UninstallString -Value ('"' + (Join-Path $install 'uninstall-study-sync.bat') + '"')
}
Write-Host "Study Sync 1.0.2 instalado em $install" -ForegroundColor Green
if ($install -eq $defaultInstall) { Write-Host 'Os dados locais em .study-sync foram preservados. Atalho criado no menu Iniciar.' }
if (-not $SkipLaunch -and $install -eq $defaultInstall) { Start-Process -FilePath (Join-Path $install 'StudySync.exe') -WorkingDirectory $install }
