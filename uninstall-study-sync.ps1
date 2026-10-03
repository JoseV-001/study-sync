$ErrorActionPreference = 'Stop'
$install = [IO.Path]::GetFullPath((Join-Path $env:LOCALAPPDATA 'Programs\Study Sync'))
if ([IO.Path]::GetFullPath($PSScriptRoot) -ne $install) { throw 'Este desinstalador nao esta na pasta oficial do Study Sync.' }
for ($port = 3001; $port -le 3031; $port++) {
    try {
        $diag = Invoke-RestMethod -Uri "http://localhost:$port/sync/diagnostics" -TimeoutSec 1
        if ($diag.javaHome -and $diag.javaHome.StartsWith($install, [StringComparison]::OrdinalIgnoreCase)) {
            Invoke-WebRequest -Method Post -Uri "http://localhost:$port/sync/shutdown" -TimeoutSec 2 | Out-Null
            Start-Sleep -Seconds 2
        }
    } catch { }
}
Get-Process -Name StudySync -ErrorAction SilentlyContinue | ForEach-Object {
    try { if ($_.Path -and [IO.Path]::GetFullPath($_.Path).StartsWith($install, [StringComparison]::OrdinalIgnoreCase)) { Stop-Process -Id $_.Id -Force } } catch { }
}
$menu = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs\Study Sync'
if (Test-Path -LiteralPath $menu) { Remove-Item -LiteralPath $menu -Recurse -Force }
Remove-Item -LiteralPath 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\StudySync' -Recurse -Force -ErrorAction SilentlyContinue
$deleteScript = Join-Path $env:TEMP ("study-sync-uninstall-" + [guid]::NewGuid().ToString('N') + '.cmd')
$quotedInstall = '"' + $install + '"'
Set-Content -LiteralPath $deleteScript -Encoding ASCII -Value @('@echo off', 'ping 127.0.0.1 -n 3 > nul', "rmdir /s /q $quotedInstall", 'del "%~f0"')
Start-Process -FilePath $env:ComSpec -ArgumentList @('/c', "`"$deleteScript`"") -WindowStyle Hidden
Write-Host 'Study Sync foi removido. Seus dados locais em .study-sync foram mantidos.'
