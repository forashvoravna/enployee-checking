<#
  Backup external PostgreSQL DB -> restore into Docker Postgres container.

  REQUIREMENTS:
    - docker (Desktop) running, Linux engine
    - pg_dump on host PATH (PostgreSQL client tools)
#>

[CmdletBinding()]
param(
  # ---- EXTERNAL (SOURCE) DB ----
  [string]$SrcHost = "192.168.202.17",
  [int]   $SrcPort = 5432,
  [string]$SrcDb   = "employee_checking",
  [string]$SrcUser = "postgres",

  # ---- DOCKER (TARGET) ----
  [string]$PgContainer  = "employee-db",          # docker container name of Postgres
  [string]$TgtDb        = "employee_checking",    # target DB name inside container
  [string]$TgtUser      = "postgres",             # psql -U user inside container
  [switch]$RecreateDb,                             # drops and recreates DB before restore
  [int]   $ParallelJobs = 4,                       # pg_restore -j

  # ---- MODE ----
  [ValidateSet("file","pipe")]
  [string]$Mode = "file"                           # file = dump to .dump then restore; pipe = stream directly
)

function Fail($msg){ Write-Error $msg; exit 1 }

Write-Host "==== Checking prerequisites ====" -ForegroundColor Cyan

# 1) docker available?
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Fail "Docker is not on PATH. Install/Start Docker Desktop." }
# engine up?
try {
  docker version | Out-Null
} catch {
  Fail "Docker daemon not reachable. Start Docker Desktop (Linux engine)."
}

# 2) pg_dump available?
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) {
  Fail "pg_dump not found. Install PostgreSQL client tools and add to PATH."
}

# 3) container exists?
$cont = docker ps -a --format "{{.Names}}" | Where-Object { $_ -eq $PgContainer }
if (-not $cont) { Fail "Container '$PgContainer' not found. Check docker-compose service name." }

# 4) ensure container is running
$running = docker ps --format "{{.Names}}" | Where-Object { $_ -eq $PgContainer }
if (-not $running) {
  Write-Host "Starting container '$PgContainer'..." -ForegroundColor Yellow
  docker start $PgContainer | Out-Null
}

# 5) ask for external DB password securely
$sec = Read-Host "Enter password for external user '$SrcUser'@$SrcHost" -AsSecureString
$SrcPass = (New-Object System.Net.NetworkCredential("", $sec)).Password

# 6) prepare paths
$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$dumpFile = Join-Path $env:TEMP "pg_${SrcDb}_$ts.dump"

Write-Host "==== Mode: $Mode ====" -ForegroundColor Cyan

if ($Mode -eq "file") {
  Write-Host ">> Dumping external DB to: $dumpFile" -ForegroundColor Green
  $env:PGPASSWORD = $SrcPass
  $dumpArgs = @(
    "-h", $SrcHost,
    "-p", $SrcPort,
    "-U", $SrcUser,
    "-d", $SrcDb,
    "-Fc",              # custom format
    "-f", $dumpFile
  )
  & pg_dump @dumpArgs
  if ($LASTEXITCODE -ne 0) { Fail "pg_dump failed." }
  Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue

  $containerPath = "${PgContainer}:/tmp/restore.dump"
  Write-Host ">> Copy dump into container: $containerPath" -ForegroundColor Green
  docker cp $dumpFile $containerPath | Out-Null

  if ($RecreateDb) {
    Write-Host ">> Dropping & creating DB '$TgtDb' in container..." -ForegroundColor Yellow
    docker exec -i $PgContainer psql -U $TgtUser -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$TgtDb';" | Out-Null
    docker exec -i $PgContainer psql -U $TgtUser -c "DROP DATABASE IF EXISTS $TgtDb;" | Out-Null
    docker exec -i $PgContainer psql -U $TgtUser -c "CREATE DATABASE $TgtDb;" | Out-Null
  }

  Write-Host ">> Restoring into '$TgtDb' ..." -ForegroundColor Green
  $restoreCmd = "pg_restore -U $TgtUser -d $TgtDb --clean --no-owner --no-acl -j $ParallelJobs /tmp/restore.dump"
  docker exec -i $PgContainer bash -lc $restoreCmd
  if ($LASTEXITCODE -ne 0) { Fail "pg_restore failed." }

} elseif ($Mode -eq "pipe") {
  Write-Host ">> Streaming dump directly into container (no temp file)..." -ForegroundColor Green
  $env:PGPASSWORD = $SrcPass
  $dumpArgs = @(
    "-h", $SrcHost,
    "-p", $SrcPort,
    "-U", $SrcUser,
    "-d", $SrcDb,
    "-Fc"              # custom format
  )
  $restoreCmd = "pg_restore -U $TgtUser -d $TgtDb --clean --no-owner --no-acl -j $ParallelJobs"
  if ($RecreateDb) {
    Write-Host ">> Dropping & creating DB '$TgtDb' in container..." -ForegroundColor Yellow
    docker exec -i $PgContainer psql -U $TgtUser -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$TgtDb';" | Out-Null
    docker exec -i $PgContainer psql -U $TgtUser -c "DROP DATABASE IF EXISTS $TgtDb;" | Out-Null
    docker exec -i $PgContainer psql -U $TgtUser -c "CREATE DATABASE $TgtDb;" | Out-Null
  }
  # pipe: pg_dump | docker exec pg_restore
  (& pg_dump @dumpArgs) | docker exec -i $PgContainer bash -lc $restoreCmd
  if ($LASTEXITCODE -ne 0) { Fail "pipe pg_dump|pg_restore failed." }
  Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

# 7) quick verification
Write-Host ">> Verifying tables in '$TgtDb' ..." -ForegroundColor Cyan
docker exec -i $PgContainer psql -U $TgtUser -d $TgtDb -c "\dt" | Out-Null

Write-Host "✅ Done. Restore completed successfully." -ForegroundColor Green
Write-Host "Tip: To see sample counts: docker exec -it $PgContainer psql -U $TgtUser -d $TgtDb -c `"select count(*) from your_table;`""
