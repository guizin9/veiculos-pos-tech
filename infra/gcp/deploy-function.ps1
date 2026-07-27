#Requires -Version 5.1
<#
.SYNOPSIS
  Faz deploy da Cloud Function Gen2 gerar-codigo-pagamento (trigger Pub/Sub).

.PARAMETER ProjectId
  ID do projeto GCP (obrigatório).

.PARAMETER Region
  Região GCP (padrão: us-central1).

.PARAMETER Topic
  Tópico Pub/Sub de entrada (padrão: veiculos-eventos).
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [string]$Region = "us-central1",

    [string]$Topic = "veiculos-eventos"
)

$ErrorActionPreference = "Stop"
$FunctionName = "gerar-codigo-pagamento"
$SourceDir = Join-Path $PSScriptRoot "functions\gerar-codigo-pagamento"

if (-not (Test-Path $SourceDir)) {
    throw "Diretório da função não encontrado: $SourceDir"
}

gcloud config set project $ProjectId | Out-Null

Write-Host "Verificando tópico '$Topic'..."
try {
    gcloud pubsub topics describe $Topic 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Tópico não encontrado. Execute primeiro: .\setup-pubsub.ps1 -ProjectId $ProjectId"
        exit 1
    }
} catch {
    Write-Host "Tópico não encontrado. Execute primeiro: .\setup-pubsub.ps1 -ProjectId $ProjectId"
    exit 1
}

Write-Host "Deploy Cloud Function Gen2 '$FunctionName'..."
Push-Location $SourceDir
try {
    gcloud functions deploy $FunctionName `
        --gen2 `
        --runtime=nodejs20 `
        --region=$Region `
        --source=. `
        --entry-point=gerarCodigoPagamento `
        --trigger-topic=$Topic `
        --memory=256Mi `
        --timeout=60s `
        --max-instances=5 `
        --min-instances=0 `
        --quiet

    if ($LASTEXITCODE -ne 0) {
        throw "Falha no deploy da Cloud Function"
    }
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Cloud Function implantada com sucesso."
Write-Host "  Nome:   $FunctionName"
Write-Host "  Região: $Region"
Write-Host "  Trigger: Pub/Sub tópico '$Topic'"
Write-Host ""
Write-Host "Ver logs:"
Write-Host "  gcloud functions logs read $FunctionName --gen2 --region=$Region --limit=20"
Write-Host ""
Write-Host "Teste: crie uma reserva na API (Cloud Run) e confira os logs da função."
