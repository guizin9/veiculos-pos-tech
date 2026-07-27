#Requires -Version 5.1
<#
.SYNOPSIS
  Deploy completo da API no Cloud Run — projeto veiculos-pos-tech.

.EXAMPLE
  .\deploy-primeira-vez.ps1 -AllowUnauthenticated -SkipPubSub
#>
param(
    [string]$ProjectId = "veiculos-pos-tech",

    [string]$CloudSqlInstance = "veiculos-pos-tech:us-central1:veiculos-pos-tech-project",

    [string]$Region = "us-central1",

    [switch]$AllowUnauthenticated,

    [switch]$SkipPubSub,

    [switch]$SkipSecrets,

    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot

Write-Host "=== Deploy veiculos-api -> Cloud Run ===" -ForegroundColor Cyan
Write-Host "Project:   $ProjectId"
Write-Host "Cloud SQL: $CloudSqlInstance"
Write-Host "Region:    $Region"
Write-Host ""

# Verificar gcloud autenticado
$accounts = gcloud auth list --filter=status:ACTIVE --format="value(account)" 2>$null
if (-not $accounts) {
    Write-Host "ERRO: gcloud nao autenticado." -ForegroundColor Red
    Write-Host "Execute primeiro:" -ForegroundColor Yellow
    Write-Host "  gcloud auth login"
    Write-Host "  gcloud auth application-default login"
    Write-Host "  gcloud config set project $ProjectId"
    exit 1
}

Write-Host "[1/6] Habilitando APIs..." -ForegroundColor Cyan
& "$Root\setup-apis.ps1" -ProjectId $ProjectId

if (-not $SkipSecrets) {
    Write-Host ""
    Write-Host "[2/6] Criando segredos (Secret Manager)..." -ForegroundColor Cyan
    & "$Root\setup-secrets.ps1" -ProjectId $ProjectId
} else {
    Write-Host "[2/6] Segredos ignorados (-SkipSecrets)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[3/6] Artifact Registry..." -ForegroundColor Cyan
& "$Root\setup-artifact-registry.ps1" -ProjectId $ProjectId -Region $Region

if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "[4/6] Build e push da imagem Docker..." -ForegroundColor Cyan
    & "$Root\build-and-push.ps1" -ProjectId $ProjectId -Region $Region -Tag latest
} else {
    Write-Host "[4/6] Build ignorado (-SkipBuild)" -ForegroundColor Yellow
}

if (-not $SkipPubSub) {
    Write-Host ""
    Write-Host "[5/6] Pub/Sub + Cloud Function..." -ForegroundColor Cyan
    & "$Root\setup-pubsub.ps1" -ProjectId $ProjectId
    & "$Root\deploy-function.ps1" -ProjectId $ProjectId -Region $Region
} else {
    Write-Host "[5/6] Pub/Sub ignorado (-SkipPubSub) — habilite depois" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[6/6] Deploy Cloud Run..." -ForegroundColor Cyan
$deployArgs = @{
    ProjectId          = $ProjectId
    CloudSqlInstance   = $CloudSqlInstance
    Region             = $Region
}
if ($AllowUnauthenticated) { $deployArgs.AllowUnauthenticated = $true }
if ($SkipPubSub) {
    # Passar env var via gcloud diretamente — deploy script precisa suportar
    & "$Root\deploy-cloud-run.ps1" @deployArgs -DisablePubSub
} else {
    & "$Root\deploy-cloud-run.ps1" @deployArgs
}

Write-Host ""
Write-Host "=== Deploy finalizado ===" -ForegroundColor Green
