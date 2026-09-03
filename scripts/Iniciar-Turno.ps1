$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (!(Test-Path '.env')) {
  $bytes = New-Object byte[] 24
  $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
  $rng.GetBytes($bytes)
  $rng.Dispose()
  $password = [Convert]::ToBase64String($bytes)
  [IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), "DB_PASSWORD=$password`nFRONTEND_PORT=3004`n", [Text.UTF8Encoding]::new($false))
}
docker compose up --build -d --wait
if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar Docker Compose.' }
$port = 3004
$line = Get-Content '.env' | Where-Object { $_ -match '^FRONTEND_PORT=' } | Select-Object -Last 1
if ($line) { $port = [int]($line.Split('=')[1]) }
$url = "http://localhost:$port"
node scripts/check-api.mjs $url
if ($LASTEXITCODE -ne 0) { throw 'Falló la comprobación de reservas.' }
$date = (Get-Date).AddDays(30).ToString('yyyy-MM-dd')
$before = @(Invoke-RestMethod "$url/api/bookings?date=$date")
docker compose stop
if ($LASTEXITCODE -ne 0) { throw 'No se pudo detener el proyecto.' }
docker compose up -d --wait
if ($LASTEXITCODE -ne 0) { throw 'No se pudo reiniciar el proyecto.' }
$after = @(Invoke-RestMethod "$url/api/bookings?date=$date")
if ((Compare-Object @($before.id | Sort-Object) @($after.id | Sort-Object))) { throw 'La agenda cambió tras reiniciar.' }
Write-Host "APROBADO: reservas, conflictos, cancelaciones y persistencia. Abre $url"
