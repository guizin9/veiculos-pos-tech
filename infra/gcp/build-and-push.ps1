#Requires -Version 5.1
<#
.SYNOPSIS
  Build da imagem Docker e push para Artifact Registry.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [string]$Region = "us-central1",

    [string]$Repository = "veiculos",

    [string]$ImageName = "api",

    [string]$Tag = "latest"
)

$ErrorActionPreference = "Stop"
$Image = "${Region}-docker.pkg.dev/${ProjectId}/${Repository}/${ImageName}:${Tag}"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Write-Host "Build: $Image"
Write-Host "Contexto: $Root"
docker build -t $Image $Root
if ($LASTEXITCODE -ne 0) { throw "docker build falhou" }

Write-Host "Push: $Image"
docker push $Image
if ($LASTEXITCODE -ne 0) { throw "docker push falhou" }

Write-Host ""
Write-Host "Imagem publicada:"
Write-Host "  $Image"
Write-Host ""
Write-Host "Próximo passo:"
Write-Host "  .\deploy-cloud-run.ps1 -ProjectId $ProjectId -CloudSqlInstance SEU_PROJETO:us-central1:veiculos-db -Region $Region -AllowUnauthenticated"
