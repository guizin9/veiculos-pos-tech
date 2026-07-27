#Requires -Version 5.1
<#
.SYNOPSIS
  Cria o tópico Pub/Sub usado pela API e pela Cloud Function.

.PARAMETER ProjectId
  ID do projeto GCP (obrigatório).

.PARAMETER Topic
  Nome do tópico (padrão: veiculos-eventos).

.PARAMETER CloudRunServiceAccount
  Service account do Cloud Run que publicará eventos.
  Padrão: SA padrão do Compute Engine do projeto.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [string]$Topic = "veiculos-eventos",

    [string]$CloudRunServiceAccount = ""
)

$ErrorActionPreference = "Stop"

gcloud config set project $ProjectId | Out-Null

Write-Host "Verificando tópico Pub/Sub '$Topic'..."
$topicExists = $false
try {
    gcloud pubsub topics describe $Topic 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $topicExists = $true }
} catch {}

if (-not $topicExists) {
    Write-Host "Criando tópico $Topic..."
    gcloud pubsub topics create $Topic
} else {
    Write-Host "Tópico $Topic já existe."
}

if ([string]::IsNullOrWhiteSpace($CloudRunServiceAccount)) {
    $projectNumber = gcloud projects describe $ProjectId --format="value(projectNumber)"
    $CloudRunServiceAccount = "$projectNumber-compute@developer.gserviceaccount.com"
}

Write-Host "Concedendo roles/pubsub.publisher para $CloudRunServiceAccount..."
gcloud pubsub topics add-iam-policy-binding $Topic `
    --member="serviceAccount:$CloudRunServiceAccount" `
    --role="roles/pubsub.publisher" `
    --quiet

Write-Host ""
Write-Host "Pub/Sub pronto."
Write-Host "  Tópico: projects/$ProjectId/topics/$Topic"
Write-Host "  Publisher: $CloudRunServiceAccount"
Write-Host ""
Write-Host "Configure no Cloud Run:"
Write-Host "  GCP_PUBSUB_ENABLED=true"
Write-Host "  GCP_PUBSUB_TOPIC=$Topic"
