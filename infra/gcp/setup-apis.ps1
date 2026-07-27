#Requires -Version 5.1
<#
.SYNOPSIS
  Habilita APIs necessárias no projeto GCP (execução única).
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId
)

$ErrorActionPreference = "Stop"
gcloud config set project $ProjectId | Out-Null

$apis = @(
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "sqladmin.googleapis.com",
    "secretmanager.googleapis.com",
    "pubsub.googleapis.com",
    "cloudfunctions.googleapis.com",
    "eventarc.googleapis.com",
    "cloudbuild.googleapis.com",
    "logging.googleapis.com"
)

Write-Host "Habilitando APIs no projeto $ProjectId..."
gcloud services enable $apis --quiet

Write-Host "APIs habilitadas."
