$apiBaseUrl = "http://localhost:8080/api/v1"
$results = @()

# Función para medir tiempo de respuesta
function MeasureRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Body,
        [int]$RequestNumber = 1
    )
    
    $uri = "$apiBaseUrl$Endpoint"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $response = Invoke-WebRequest -Uri $uri -Method $Method -ContentType "application/json" -Body $Body -UseBasicParsing
        $stopwatch.Stop()
        
        return @{
            Endpoint      = $Endpoint
            Method        = $Method
            Status        = $response.StatusCode
            TimeMs        = $stopwatch.ElapsedMilliseconds
            ResultCount   = if ($response.Content | ConvertFrom-Json | Select-Object -ExpandProperty content -ErrorAction SilentlyContinue) { ($response.Content | ConvertFrom-Json).content.Count } else { 1 }
            RequestNumber = $RequestNumber
            IsCached      = if ($RequestNumber -gt 1) { "Posiblemente" } else { "No" }
        }
    }
    catch {
        $stopwatch.Stop()
        return @{
            Endpoint      = $Endpoint
            Method        = $Method
            Status        = "Error"
            TimeMs        = $stopwatch.ElapsedMilliseconds
            ResultCount   = 0
            RequestNumber = $RequestNumber
            IsCached      = "Error"
            Error         = $_.Exception.Message
        }
    }
}

Write-Host "╔══════════════════════════════════════════════════════════════╗"
Write-Host "║     PRUEBAS DE PERFORMANCE - PRODUCT SEARCH SERVICE          ║"
Write-Host "╚══════════════════════════════════════════════════════════════╝`n"

# TEST 1: Búsqueda por texto (primera llamada - sin caché)
Write-Host "TEST 1: Búsqueda por texto 'laptop' (SIN CACHÉ)"
$body1 = '{"keyword":"laptop","page":0,"size":10,"activeOnly":true}'
$result1a = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body1 -RequestNumber 1
$results += $result1a
Write-Host "  ✓ Tiempo: $($result1a.TimeMs)ms | Status: $($result1a.Status) | Resultados: $($result1a.ResultCount)`n"

Start-Sleep -Seconds 0.5

# TEST 1b: Misma búsqueda (segunda llamada - con caché Redis)
Write-Host "TEST 1b: Misma búsqueda 'laptop' (CON CACHÉ REDIS)"
$result1b = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body1 -RequestNumber 2
$results += $result1b
$cacheSavings = $result1a.TimeMs - $result1b.TimeMs
$cacheSavingsPercent = if ($result1a.TimeMs -gt 0) { [math]::Round(($cacheSavings / $result1a.TimeMs) * 100, 2) } else { 0 }
Write-Host "  ✓ Tiempo: $($result1b.TimeMs)ms | Ahorro: $cacheSavings ms ($cacheSavingsPercent%) | Status: $($result1b.Status)`n"

# TEST 2: Búsqueda por categoría
Write-Host "TEST 2: Búsqueda por categoría 'Electronics' (SIN CACHÉ)"
$body2 = '{"category":"Electronics","page":0,"size":20,"activeOnly":true}'
$result2a = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body2 -RequestNumber 1
$results += $result2a
Write-Host "  ✓ Tiempo: $($result2a.TimeMs)ms | Status: $($result2a.Status) | Resultados: $($result2a.ResultCount)`n"

Start-Sleep -Seconds 0.5

# TEST 2b: Misma búsqueda por categoría (con caché)
Write-Host "TEST 2b: Misma búsqueda por categoría (CON CACHÉ)"
$result2b = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body2 -RequestNumber 2
$results += $result2b
$cacheSavings2 = $result2a.TimeMs - $result2b.TimeMs
$cacheSavingsPercent2 = if ($result2a.TimeMs -gt 0) { [math]::Round(($cacheSavings2 / $result2a.TimeMs) * 100, 2) } else { 0 }
Write-Host "  ✓ Tiempo: $($result2b.TimeMs)ms | Ahorro: $cacheSavings2 ms ($cacheSavingsPercent2%) | Status: $($result2b.Status)`n"

# TEST 3: Búsqueda por rango de precio
Write-Host "TEST 3: Búsqueda por precio 500-2000 USD (SIN CACHÉ)"
$body3 = '{"minPrice":500,"maxPrice":2000,"page":0,"size":15,"activeOnly":true}'
$result3a = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body3 -RequestNumber 1
$results += $result3a
Write-Host "  ✓ Tiempo: $($result3a.TimeMs)ms | Status: $($result3a.Status) | Resultados: $($result3a.ResultCount)`n"

Start-Sleep -Seconds 0.5

# TEST 4: Búsqueda compleja (múltiples filtros)
Write-Host "TEST 4: Búsqueda compleja (categoría + rango precio + rating) - SIN CACHÉ"
$body4 = '{"category":"Laptops","minPrice":1000,"maxPrice":3000,"minRating":4.0,"page":0,"size":20,"activeOnly":true}'
$result4a = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body4 -RequestNumber 1
$results += $result4a
Write-Host "  ✓ Tiempo: $($result4a.TimeMs)ms | Status: $($result4a.Status) | Resultados: $($result4a.ResultCount)`n"

Start-Sleep -Seconds 0.5

# TEST 4b: Misma búsqueda compleja (con caché)
Write-Host "TEST 4b: Misma búsqueda compleja (CON CACHÉ)"
$result4b = MeasureRequest -Method Post -Endpoint "/products/search" -Body $body4 -RequestNumber 2
$results += $result4b
$cacheSavings4 = $result4a.TimeMs - $result4b.TimeMs
$cacheSavingsPercent4 = if ($result4a.TimeMs -gt 0) { [math]::Round(($cacheSavings4 / $result4a.TimeMs) * 100, 2) } else { 0 }
Write-Host "  ✓ Tiempo: $($result4b.TimeMs)ms | Ahorro: $cacheSavings4 ms ($cacheSavingsPercent4%) | Status: $($result4b.Status)`n"

# TEST 5: Obtener un producto por ID (primero obtenemos un ID válido)
Write-Host "TEST 5: Obtener producto por ID (SIN CACHÉ)"
$searchBody = '{"keyword":"Gaming","page":0,"size":1}'
$searchResponse = Invoke-WebRequest -Uri "$apiBaseUrl/products/search" -Method Post -ContentType "application/json" -Body $searchBody -UseBasicParsing
$products = $searchResponse.Content | ConvertFrom-Json
if ($products.content -and $products.content.Count -gt 0) {
    $productId = $products.content[0].id
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-WebRequest -Uri "$apiBaseUrl/products/$productId" -Method Get -UseBasicParsing
    $stopwatch.Stop()
    Write-Host "  ✓ Tiempo: $($stopwatch.ElapsedMilliseconds)ms | Status: $($response.StatusCode)"
    $results += @{
        Endpoint      = "/products/{id}"
        Method        = "GET"
        Status        = $response.StatusCode
        TimeMs        = $stopwatch.ElapsedMilliseconds
        ResultCount   = 1
        RequestNumber = 1
        IsCached      = "No"
    }
}
Write-Host ""

# TEST 6: Health check (rápido)
Write-Host "TEST 6: Health Check (actuator)"
$result6 = MeasureRequest -Method Get -Endpoint "/actuator/health" -Body ""
$results += $result6
Write-Host "  ✓ Tiempo: $($result6.TimeMs)ms | Status: $($result6.Status)`n"

# Mostrar resumen
Write-Host "╔══════════════════════════════════════════════════════════════╗"
Write-Host "║                    RESUMEN DE RESULTADOS                      ║"
Write-Host "╚══════════════════════════════════════════════════════════════╝`n"

$totalTestTime = ($results | Measure-Object TimeMs -Sum).Sum
$avgTime = [math]::Round(($results | Measure-Object TimeMs -Average).Average, 2)
$maxTime = ($results | Measure-Object TimeMs -Maximum).Maximum
$minTime = ($results | Measure-Object TimeMs -Minimum).Minimum

Write-Host "ESTADÍSTICAS GLOBALES:"
Write-Host "  • Total de tests: $($results.Count)"
Write-Host "  • Tiempo total: $totalTestTime ms"
Write-Host "  • Tiempo promedio: $avgTime ms"
Write-Host "  • Tiempo máximo: $maxTime ms"
Write-Host "  • Tiempo mínimo: $minTime ms"
Write-Host ""

Write-Host "IMPACTO DEL CACHÉ REDIS:"
$cacheTests = @(
    @{name = "Búsqueda Simple"; firstTime = $result1a.TimeMs; secondTime = $result1b.TimeMs },
    @{name = "Búsqueda Categoría"; firstTime = $result2a.TimeMs; secondTime = $result2b.TimeMs },
    @{name = "Búsqueda Compleja"; firstTime = $result4a.TimeMs; secondTime = $result4b.TimeMs }
)

foreach ($test in $cacheTests) {
    $improvement = $test.firstTime - $test.secondTime
    $improvementPercent = if ($test.firstTime -gt 0) { [math]::Round(($improvement / $test.firstTime) * 100, 2) } else { 0 }
    Write-Host "  • $($test.name): $($test.firstTime)ms → $($test.secondTime)ms (Mejora: $improvement ms / $improvementPercent%)"
}

# Guardar resultados en archivo
$results | Export-Csv -Path "performance-results.csv" -NoTypeInformation
Write-Host "`n✅ Resultados guardados en 'performance-results.csv'"
