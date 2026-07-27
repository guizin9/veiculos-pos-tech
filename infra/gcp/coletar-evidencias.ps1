#Requires -Version 5.1
<#
.SYNOPSIS
  Coleta evidências dos serviços GCP para entrega FASE 5.

.DESCRIPTION
  Gera pasta evidencias/YYYYMMDD-HHmm com outputs de gcloud, testes HTTP
  e instruções de prints do Console. Anexe ao PDF ou apresentação.

.EXAMPLE
  .\coletar-evidencias.ps1
  .\coletar-evidencias.ps1 -ProjectId veiculos-pos-tech -FazerReserva
#>
param(
    [string]$ProjectId = "veiculos-pos-tech",
    [string]$Region = "us-central1",
    [string]$ApiUrl = "https://veiculos-api-k4f2n37iga-uc.a.run.app",
    [switch]$FazerReserva
)

$ErrorActionPreference = "Continue"
$stamp = Get-Date -Format "yyyyMMdd-HHmm"
$outDir = Join-Path $PSScriptRoot "..\..\evidencias\$stamp"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function Save-Text($name, $content) {
    $path = Join-Path $outDir $name
    $content | Out-File -FilePath $path -Encoding utf8
    Write-Host "  -> $name" -ForegroundColor Green
}

Write-Host "`n=== Coleta de evidencias GCP ===" -ForegroundColor Cyan
Write-Host "Projeto: $ProjectId | Pasta: $outDir`n"

# --- 1. Cloud Run (API) ---
Write-Host "[1/8] Cloud Run..." -ForegroundColor Yellow
Save-Text "01-cloud-run.txt" @"
Servico: veiculos-api
Regiao: $Region
URL: $ApiUrl

$(gcloud run services describe veiculos-api --region=$Region --project=$ProjectId 2>&1)
"@

# --- 2. Health + Swagger ---
Write-Host "[2/8] Health / Swagger..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$ApiUrl/actuator/health" -TimeoutSec 60
    Save-Text "02-health.json" ($health | ConvertTo-Json -Depth 6)
} catch {
    Save-Text "02-health.json" "{ `"erro`": `"$($_.Exception.Message)`" }"
}
try {
    $swagger = Invoke-WebRequest -Uri "$ApiUrl/swagger-ui/index.html" -UseBasicParsing -TimeoutSec 30
    Save-Text "03-swagger.txt" "GET /swagger-ui/index.html -> HTTP $($swagger.StatusCode)"
} catch {
    Save-Text "03-swagger.txt" "Erro: $($_.Exception.Message)"
}

# --- 3. Login JWT ---
Write-Host "[3/8] Login JWT..." -ForegroundColor Yellow
try {
    $body = @{ username = "admin"; senha = "admin123" } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$ApiUrl/auth/login" -Method POST -Body $body -ContentType "application/json"
    $tokenPreview = $login.token.Substring(0, [Math]::Min(40, $login.token.Length)) + "..."
    Save-Text "04-login.json" (@{
        status = "OK"
        tipo = $login.tipo
        expiraEmMinutos = $login.expiraEmMinutos
        tokenPreview = $tokenPreview
    } | ConvertTo-Json)
    $script:Token = $login.token
} catch {
    Save-Text "04-login.json" "{ `"erro`": `"$($_.Exception.Message)`" }"
}

# --- 4. Cloud SQL ---
Write-Host "[4/8] Cloud SQL..." -ForegroundColor Yellow
Save-Text "05-cloud-sql.txt" @"
$(gcloud sql instances describe veiculos-pos-tech-project --project=$ProjectId 2>&1)

--- Bancos ---
$(gcloud sql databases list --instance=veiculos-pos-tech-project --project=$ProjectId 2>&1)

--- Usuarios ---
$(gcloud sql users list --instance=veiculos-pos-tech-project --project=$ProjectId 2>&1)
"@

# --- 5. Pub/Sub ---
Write-Host "[5/8] Pub/Sub..." -ForegroundColor Yellow
Save-Text "06-pubsub.txt" @"
--- Topico ---
$(gcloud pubsub topics describe veiculos-eventos --project=$ProjectId 2>&1)

--- Subscriptions ---
$(gcloud pubsub subscriptions list --project=$ProjectId --filter="topic:veiculos-eventos" 2>&1)
"@

# --- 6. Cloud Function ---
Write-Host "[6/8] Cloud Function..." -ForegroundColor Yellow
Save-Text "07-cloud-function.txt" @"
$(gcloud functions describe gerar-codigo-pagamento --gen2 --region=$Region --project=$ProjectId 2>&1)

--- Ultimos logs ---
$(gcloud functions logs read gerar-codigo-pagamento --gen2 --region=$Region --project=$ProjectId --limit=5 2>&1)
"@

# --- 7. Secret Manager + Artifact Registry ---
Write-Host "[7/8] Secrets + Artifact Registry..." -ForegroundColor Yellow
Save-Text "08-secrets.txt" $(gcloud secrets list --project=$ProjectId 2>&1)
Save-Text "09-artifact-registry.txt" $(gcloud artifacts repositories describe veiculos --location=$Region --project=$ProjectId 2>&1)

# --- 8. Reserva E2E (opcional) ---
if ($FazerReserva -and $script:Token) {
    Write-Host "[8/8] Reserva + Pub/Sub E2E..." -ForegroundColor Yellow
    $headers = @{ Authorization = "Bearer $($script:Token)"; "Content-Type" = "application/json" }
    try {
        $veiculos = Invoke-RestMethod -Uri "$ApiUrl/veiculos/a-venda" -Headers $headers
        $clientes = Invoke-RestMethod -Uri "$ApiUrl/clientes" -Headers $headers
        if ($veiculos.Count -gt 0 -and $clientes.Count -gt 0) {
            $v = $veiculos | Where-Object { $_.status -match "Venda|A" } | Select-Object -First 1
            if (-not $v) { $v = $veiculos[0] }
            $vid = if ($v.veiculoId) { $v.veiculoId } else { $v.id }
            $reservaBody = @{ veiculoId = $vid; clienteId = $clientes[0].id; valor = $v.valor } | ConvertTo-Json
            $resp = Invoke-WebRequest -Uri "$ApiUrl/reserva-venda-veiculos" -Method POST -Headers $headers -Body $reservaBody -UseBasicParsing
            Save-Text "10-reserva-e2e.txt" "HTTP $($resp.StatusCode)`n$($resp.Content)"
            Start-Sleep -Seconds 4
            Save-Text "11-function-log-pos-reserva.txt" $(gcloud logging read "resource.labels.service_name=gerar-codigo-pagamento" --project=$ProjectId --limit=3 --format=json 2>&1)
        }
    } catch {
        Save-Text "10-reserva-e2e.txt" "Erro: $($_.Exception.Message)"
    }
} else {
    Write-Host "[8/8] Reserva E2E pulada (use -FazerReserva para incluir)" -ForegroundColor DarkGray
}

# --- Instrucoes prints Console ---
Save-Text "LEIA-PRINTS-CONSOLE.md" @"
# Prints do Console GCP (anexar ao PDF)

Abra: https://console.cloud.google.com/?project=$ProjectId

| # | Menu | O que capturar |
|---|------|----------------|
| 1 | Cloud Run > veiculos-api | Tela do servico, URL, revisao ativa, metricas |
| 2 | Cloud SQL > veiculos-pos-tech-project | Instancia RUNNABLE, PostgreSQL 16 |
| 3 | Pub/Sub > Topics > veiculos-eventos | Topico + subscription Eventarc |
| 4 | Cloud Functions > gerar-codigo-pagamento | Status ACTIVE, trigger Pub/Sub, Node 20 |
| 5 | Secret Manager | veiculos-db-password, veiculos-jwt-secret |
| 6 | Artifact Registry > veiculos | Imagem api:latest |
| 7 | Cloud Logging > Logs Explorer | Filtro: resource.labels.service_name=veiculos-api |
| 8 | Cloud Logging | Log da Function com codigoAlternativo PAG-GCF-... |
| 9 | IAM > Service Accounts | 184616306282-compute@... roles |

Dica: use Win+Shift+S para capturar cada tela.
"@

Write-Host "`nConcluido! Evidencias em:" -ForegroundColor Cyan
Write-Host $outDir
Write-Host "`nProximo passo: rode com -FazerReserva e faca os prints do LEIA-PRINTS-CONSOLE.md`n"
