#Requires -Version 5.1
<#
.SYNOPSIS
  Deploy do serviço veiculos-api no Cloud Run (profile gcp).

.PARAMETER CloudSqlInstance
  Connection name Cloud SQL (projeto:regiao:instancia).

.PARAMETER AllowUnauthenticated
  Expõe a API publicamente (útil para demo FASE 5).

.PARAMETER DisablePubSub
  Desabilita Pub/Sub na 1ª subida (quando tópico ainda não existe).
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [Parameter(Mandatory = $true)]
    [string]$CloudSqlInstance,

    [string]$Region = "us-central1",

    [string]$ServiceName = "veiculos-api",

    [string]$Repository = "veiculos",

    [string]$ImageName = "api",

    [string]$Tag = "latest",

    [string]$DbUsername = "veiculos_user",

    [string]$DbName = "veiculos",

    [string]$PubSubTopic = "veiculos-eventos",

    [switch]$AllowUnauthenticated,

    [switch]$DisablePubSub
)

$ErrorActionPreference = "Stop"
$Image = "${Region}-docker.pkg.dev/${ProjectId}/${Repository}/${ImageName}:${Tag}"

gcloud config set project $ProjectId | Out-Null

$pubsubEnabled = if ($DisablePubSub) { "false" } else { "true" }

$envVars = @(
    "SPRING_PROFILES_ACTIVE=gcp",
    "GCP_PROJECT_ID=$ProjectId",
    "GCP_CLOUDSQL_INSTANCE=$CloudSqlInstance",
    "GCP_CLOUDSQL_DATABASE=$DbName",
    "DB_USERNAME=$DbUsername",
    "GCP_SECRET_MANAGER_ENABLED=false",
    "GCP_PUBSUB_ENABLED=$pubsubEnabled",
    "GCP_PUBSUB_TOPIC=$PubSubTopic"
) -join ","

$secrets = "DB_PASSWORD=veiculos-db-password:latest,JWT_SECRET=veiculos-jwt-secret:latest"

Write-Host "Deploy Cloud Run: $ServiceName"
Write-Host "  Imagem: $Image"
Write-Host "  Cloud SQL: $CloudSqlInstance"

$gcloudArgs = @(
    "run", "deploy", $ServiceName,
    "--image", $Image,
    "--region", $Region,
    "--platform", "managed",
    "--port", "8080",
    "--memory", "512Mi",
    "--cpu", "1",
    "--min-instances", "0",
    "--max-instances", "3",
    "--timeout", "300",
    "--concurrency", "80",
    "--add-cloudsql-instances", $CloudSqlInstance,
    "--set-env-vars", $envVars,
    "--set-secrets", $secrets,
    "--quiet"
)

if ($AllowUnauthenticated) {
    $gcloudArgs += "--allow-unauthenticated"
} else {
    $gcloudArgs += "--no-allow-unauthenticated"
}

& gcloud @gcloudArgs
if ($LASTEXITCODE -ne 0) { throw "Deploy Cloud Run falhou" }

$Url = gcloud run services describe $ServiceName --region $Region --format="value(status.url)"

Write-Host ""
Write-Host "Deploy concluído."
Write-Host "  URL:    $Url"
Write-Host "  Health: $Url/actuator/health"
Write-Host "  Swagger: $Url/swagger-ui.html"
Write-Host ""
Write-Host "Teste rápido:"
Write-Host "  curl $Url/actuator/health"
