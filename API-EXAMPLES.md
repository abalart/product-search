# API Examples - Product Search Service

## Ejemplos de Uso de la API

### 1. Búsqueda Simple

```bash
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "laptop",
    "page": 0,
    "size": 20
  }'
```

### 2. Búsqueda con Filtros Múltiples

```bash
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "gaming laptop",
    "category": "Electronics",
    "brand": "Dell",
    "minPrice": 800.00,
    "maxPrice": 2000.00,
    "minRating": 4.0,
    "inStockOnly": true,
    "activeOnly": true,
    "sortBy": "price",
    "sortDirection": "ASC",
    "page": 0,
    "size": 20
  }'
```

### 3. Búsqueda por Categoría

```bash
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Electronics",
    "sortBy": "rating",
    "sortDirection": "DESC",
    "page": 0,
    "size": 50
  }'
```

### 4. Búsqueda por Rango de Precio

```bash
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 500.00,
    "maxPrice": 1500.00,
    "sortBy": "price",
    "sortDirection": "ASC"
  }'
```

### 5. Obtener Producto por ID

```bash
curl -X GET http://localhost:8080/api/v1/products/60d5ec49f1b2c8b1f8e4e1a1
```

### 6. Autocomplete

```bash
curl -X GET "http://localhost:8080/api/v1/products/autocomplete?query=lap&limit=10"
```

### 7. Productos Similares

```bash
curl -X GET "http://localhost:8080/api/v1/products/60d5ec49f1b2c8b1f8e4e1a1/similar?limit=10"
```

### 8. Health Check

```bash
curl -X GET http://localhost:8080/api/v1/products/health
```

---

## Respuestas Esperadas

### Búsqueda (200 OK)

```json
{
  "content": [
    {
      "id": "60d5ec49f1b2c8b1f8e4e1a1",
      "name": "Laptop Dell XPS 15",
      "description": "High-performance laptop...",
      "sku": "DELL-XPS15-2024",
      "brand": "Dell",
      "category": "Electronics",
      "tags": ["laptop", "gaming", "work"],
      "price": 1299.99,
      "currency": "USD",
      "stock": 45,
      "active": true,
      "rating": 4.7,
      "reviewCount": 328,
      "imageUrls": ["https://example.com/dell-xps-1.jpg"],
      "attributes": {
        "color": "Silver",
        "size": "15.6 inch",
        "weight": 2.0,
        "weightUnit": "kg"
      },
      "createdAt": "2024-01-20T10:30:00",
      "updatedAt": "2024-01-20T10:30:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

### Producto Individual (200 OK)

```json
{
  "id": "60d5ec49f1b2c8b1f8e4e1a1",
  "name": "Laptop Dell XPS 15",
  "sku": "DELL-XPS15-2024",
  "brand": "Dell",
  "category": "Electronics",
  "price": 1299.99,
  "stock": 45,
  "active": true,
  "rating": 4.7
}
```

### Autocomplete (200 OK)

```json
[
  {
    "id": "1",
    "name": "Laptop Dell XPS 15",
    "sku": "DELL-XPS15-2024",
    "price": 1299.99
  },
  {
    "id": "2",
    "name": "Laptop HP Pavilion",
    "sku": "HP-PAV-2024",
    "price": 899.99
  }
]
```

---

## Benchmark de Performance

### Pruebas de Carga con Apache Bench

#### Test 1: Búsqueda simple (sin cache)
```bash
ab -n 10000 -c 100 -p search.json -T application/json \
  http://localhost:8080/api/v1/products/search
```

**Resultados esperados:**
- Requests per second: ~2000-3000
- Mean latency: 30-50ms
- p99 latency: <200ms

#### Test 2: Búsqueda con cache warm
```bash
# Calentar cache primero
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d @search.json

# Ejecutar benchmark
ab -n 50000 -c 200 -p search.json -T application/json \
  http://localhost:8080/api/v1/products/search
```

**Resultados esperados:**
- Requests per second: ~8000-12000
- Mean latency: 15-25ms
- p99 latency: <50ms

#### Test 3: Get by ID (con cache)
```bash
ab -n 100000 -c 500 \
  http://localhost:8080/api/v1/products/60d5ec49f1b2c8b1f8e4e1a1
```

**Resultados esperados:**
- Requests per second: ~15000-20000
- Mean latency: 10-20ms
- p99 latency: <30ms

---

## Monitoreo de Métricas

### Prometheus Queries

```promql
# Latencia promedio de búsquedas
rate(http_server_requests_seconds_sum{uri="/api/v1/products/search"}[5m])
/
rate(http_server_requests_seconds_count{uri="/api/v1/products/search"}[5m])

# Tasa de cache hit
redis_cache_hit_rate

# Throughput de búsquedas
rate(http_server_requests_seconds_count{uri="/api/v1/products/search"}[1m])

# Conexiones MongoDB activas
mongodb_connections_active
```

---

## Optimización de Queries MongoDB

### Analizar Performance de Query

```javascript
db.products.find({
  category: "Electronics",
  active: true,
  price: { $gte: 500, $lte: 2000 }
}).explain("executionStats")
```

### Verificar Uso de Índices

```javascript
db.products.getIndexes()

// Verificar que usa índice compuesto
db.products.find({
  category: "Electronics",
  active: true
}).explain("executionStats").executionStats.executionStages
```

---

## Scripts de Carga de Datos

### Generar 1 millón de productos de prueba

```javascript
// En MongoDB shell
use product_search

const categories = ["Electronics", "Clothing", "Books", "Home", "Sports"];
const brands = ["Brand A", "Brand B", "Brand C", "Brand D", "Brand E"];

let bulkOps = [];
for (let i = 0; i < 1000000; i++) {
  bulkOps.push({
    insertOne: {
      document: {
        name: `Product ${i}`,
        description: `Description for product ${i} with various keywords`,
        sku: `SKU-${i}-${Date.now()}`,
        brand: brands[Math.floor(Math.random() * brands.length)],
        category: categories[Math.floor(Math.random() * categories.length)],
        tags: [`tag${i % 10}`, `tag${i % 20}`],
        price: NumberDecimal((Math.random() * 2000 + 100).toFixed(2)),
        currency: "USD",
        stock: Math.floor(Math.random() * 100),
        active: Math.random() > 0.1,
        rating: Number((Math.random() * 2 + 3).toFixed(1)),
        reviewCount: Math.floor(Math.random() * 500),
        createdAt: new Date(),
        updatedAt: new Date()
      }
    }
  });
  
  // Ejecutar en lotes de 10000
  if (bulkOps.length === 10000) {
    db.products.bulkWrite(bulkOps);
    bulkOps = [];
    print(`Inserted ${i + 1} products...`);
  }
}

if (bulkOps.length > 0) {
  db.products.bulkWrite(bulkOps);
}

print("✅ Data generation completed!");
```

---

## Troubleshooting

### Cache no funciona
```bash
# Verificar conexión Redis
redis-cli ping

# Verificar keys en Redis
redis-cli keys "product:*"
redis-cli keys "search:*"

# Limpiar cache
redis-cli flushall
```

### MongoDB lento
```bash
# Verificar índices
db.products.getIndexes()

# Ver queries lentas
db.setProfilingLevel(1, { slowms: 100 })
db.system.profile.find().limit(10).sort({ ts: -1 }).pretty()

# Estadísticas de colección
db.products.stats()
```

### Alta latencia
```bash
# Verificar métricas
curl http://localhost:8080/actuator/metrics/http.server.requests

# Ver pool de conexiones MongoDB
curl http://localhost:8080/actuator/metrics/mongodb.driver.pool.size

# Ver pool de conexiones Redis
curl http://localhost:8080/actuator/metrics/lettuce.pool.active
```
