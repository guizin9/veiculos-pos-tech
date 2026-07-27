#Requires -Version 5.1
<#
.SYNOPSIS
  Cria repositório Docker no Artifact Registry e configura autenticação local.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [string]$Region = "us-central1",

    [string]$Repository = "veiculos"
)

$ErrorActionPreference = "Stop"
gcloud config set project $ProjectId | Out-Null

Write-Host "Verificando repositório Artifact Registry '$Repository' em $Region..."
$exists = $false
try {
    gcloud artifacts repositories describe $Repository --location=$Region 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $exists = $true }
} catch {}

if (-not $exists) {
    Write-Host "Criando repositório..."
    gcloud artifacts repositories create $Repository `
        --repository-format=docker `
        --location=$Region `
        --description="Imagens Docker Veiculos API FASE 5"
} else {
    Write-Host "Repositório já existe."
}

Write-Host "Configurando Docker para Artifact Registry..."
gcloud auth configure-docker "${Region}-docker.pkg.dev" --quiet

$registry = "${Region}-docker.pkg.dev/${ProjectId}/${Repository}"
Write-Host ""
Write-Host "Artifact Registry pronto."
Write-Host "  Registry: $registry"
Write-Host ""
Write-Host "Próximo passo:"
Write-Host "  .\build-and-push.ps1 -ProjectId $ProjectId -Region $Region"
