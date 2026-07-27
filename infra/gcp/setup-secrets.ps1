#Requires -Version 5.1
<#
.SYNOPSIS
  Cria segredos veiculos-db-password e veiculos-jwt-secret no Secret Manager.
  Senhas são lidas de forma segura (não ficam no histórico do terminal).

.PARAMETER ProjectId
  ID do projeto GCP.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId
)

$ErrorActionPreference = "Stop"
gcloud config set project $ProjectId | Out-Null

Write-Host "=== Secret Manager — veiculos-pos-tech ===" -ForegroundColor Cyan
Write-Host "As senhas NAO serao exibidas na tela." -ForegroundColor Yellow
Write-Host ""

# DB password
Write-Host "Digite a senha do usuario PostgreSQL (veiculos_user):" -ForegroundColor White
$DbPassword = Read-Host -AsSecureString
$DbPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPassword))

# JWT secret (min 32 chars)
Write-Host "Digite o JWT_SECRET (minimo 32 caracteres):" -ForegroundColor White
$JwtSecret = Read-Host -AsSecureString
$JwtPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($JwtSecret))

    if ($JwtPlain.Length -lt 32) {
        throw "JWT_SECRET deve ter pelo menos 32 caracteres."
    }

    function Set-Secret($Name, $Value) {
        $exists = $false
        try {
            gcloud secrets describe $Name 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { $exists = $true }
        } catch {}

        $tmp = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($tmp, $Value)
        try {
            if ($exists) {
                Write-Host "Atualizando segredo existente: $Name"
                gcloud secrets versions add $Name --data-file=$tmp
            } else {
                Write-Host "Criando segredo: $Name"
                gcloud secrets create $Name --data-file=$tmp
            }
        } finally {
            Remove-Item $tmp -Force
        }
    }

    Set-Secret "veiculos-db-password" $DbPlain
    Set-Secret "veiculos-jwt-secret" $JwtPlain

    # Limpar variaveis da memoria
    $DbPlain = $null
    $JwtPlain = $null

# IAM para service account do Cloud Run
$projectNumber = gcloud projects describe $ProjectId --format="value(projectNumber)"
$sa = "$projectNumber-compute@developer.gserviceaccount.com"

Write-Host ""
Write-Host "Concedendo secretAccessor para $sa ..." -ForegroundColor Cyan
foreach ($secret in @("veiculos-db-password", "veiculos-jwt-secret")) {
    gcloud secrets add-iam-policy-binding $secret `
        --member="serviceAccount:$sa" `
        --role="roles/secretmanager.secretAccessor" `
        --quiet 2>$null
}

Write-Host ""
Write-Host "Segredos prontos." -ForegroundColor Green
Write-Host "  veiculos-db-password"
Write-Host "  veiculos-jwt-secret"
