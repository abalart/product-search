# Quick Start Guide

## 🚀 Inicio Rápido en 5 Minutos

### Opción 1: Con Docker Compose (Recomendado)

```bash
# 1. Clonar repositorio
git clone <repository-url>
cd product-search

# 2. Iniciar todos los servicios
docker-compose up -d

# 3. Esperar que los servicios estén listos (30 segundos)
docker-compose logs -f product-search-app

# 4. Verificar que está funcionando
curl http://localhost:8080/api/v1/products/health

# 5. Ver documentación Swagger
open http://localhost:8080/swagger-ui.html
```

**¡Listo!** La aplicación está corriendo en `http://localhost:8080`

---

### Opción 2: Instalación Local

#### Paso 1: Requisitos
```bash
# Verificar Java 17
java -version

# Verificar Maven
mvn -version
```

#### Paso 2: Iniciar MongoDB
```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -v $(pwd)/mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro \
  mongo:6.0
```

#### Paso 3: Iniciar Redis
```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

#### Paso 4: Compilar y ejecutar
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

#### Paso 5: Verificar
```bash
curl http://localhost:8080/api/v1/products/health
# Respuesta: {"status": "UP"}
```

---

## 📝 Primera Búsqueda

### 1. Búsqueda Simple
```bash
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "laptop",
    "page": 0,
    "size": 10
  }' | jq
```

### 2. Ver en Swagger UI
Abre tu navegador: `http://localhost:8080/swagger-ui.html`

1. Click en **POST /api/v1/products/search**
2. Click en **Try it out**
3. Ingresa el JSON de búsqueda
4. Click en **Execute**

---

## 🗃️ Cargar Datos de Prueba

### Opción A: Datos de muestra (ya incluidos)
Los datos de muestra se cargan automáticamente desde `mongo-init.js`

### Opción B: Generar 1 millón de productos
```bash
# Conectar a MongoDB
docker exec -it mongodb mongosh

# Ejecutar script de generación
use product_search
// Copiar y pegar el script de API-EXAMPLES.md
```

### Opción C: Importar desde archivo JSON
```bash
# Crear archivo products.json con tus productos
# Importar
docker exec -i mongodb mongoimport \
  --db product_search \
  --collection products \
  --file /data/products.json \
  --jsonArray
```

---

## 🔍 Verificar Índices

```bash
# Conectar a MongoDB
docker exec -it mongodb mongosh

# Verificar índices
use product_search
db.products.getIndexes()
```

**Índices esperados:**
- `_id_` (automático)
- `text_search_index` (text index en name y description)
- `sku_unique_idx` (único)
- `category_active_idx` (compuesto)
- `brand_active_idx` (compuesto)
- `price_rating_idx` (compuesto)
- `active_stock_idx` (compuesto)
- `tags_idx`
- `rating_idx`
- `created_at_idx`

---

## 📊 Verificar Cache Redis

```bash
# Conectar a Redis
docker exec -it redis redis-cli

# Ver todas las keys
KEYS *

# Ver un producto cacheado
GET "product:60d5ec49f1b2c8b1f8e4e1a1"

# Ver estadísticas
INFO stats
```

---

## 🎯 Pruebas de Performance Rápidas

### Test 1: Búsqueda simple
```bash
time curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{"query": "laptop", "page": 0, "size": 20}'
```

**Esperado:** < 100ms (primera vez), < 20ms (cacheado)

### Test 2: Autocomplete
```bash
time curl "http://localhost:8080/api/v1/products/autocomplete?query=lap&limit=10"
```

**Esperado:** < 50ms

### Test 3: Get by ID
```bash
time curl http://localhost:8080/api/v1/products/60d5ec49f1b2c8b1f8e4e1a1
```

**Esperado:** < 10ms (cacheado)

---

## 📈 Monitorear la Aplicación

### Health Check
```bash
curl http://localhost:8080/actuator/health | jq
```

### Métricas
```bash
# Ver todas las métricas disponibles
curl http://localhost:8080/actuator/metrics

# Ver métrica específica
curl http://localhost:8080/actuator/metrics/http.server.requests | jq

# Ver conexiones MongoDB
curl http://localhost:8080/actuator/metrics/mongodb.driver.pool.size | jq
```

### Prometheus Endpoint
```bash
curl http://localhost:8080/actuator/prometheus
```

---

## 🧪 Ejecutar Tests

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Tests con coverage
mvn clean test jacoco:report
# Ver reporte: target/site/jacoco/index.html
```

---

## 🐛 Troubleshooting

### Problema: "Connection refused" a MongoDB
```bash
# Verificar que MongoDB está corriendo
docker ps | grep mongodb

# Ver logs
docker logs mongodb

# Reiniciar MongoDB
docker restart mongodb
```

### Problema: "Connection refused" a Redis
```bash
# Verificar que Redis está corriendo
docker ps | grep redis

# Probar conexión
docker exec -it redis redis-cli ping
# Respuesta esperada: PONG

# Reiniciar Redis
docker restart redis
```

### Problema: Aplicación no inicia
```bash
# Ver logs detallados
mvn spring-boot:run -X

# Verificar configuración
cat src/main/resources/application.properties
```

### Problema: Búsquedas lentas
```bash
# Verificar índices en MongoDB
docker exec -it mongodb mongosh

use product_search
db.products.getIndexes()

# Ver query plan
db.products.find({category: "Electronics", active: true}).explain("executionStats")

# Debe mostrar: "stage": "IXSCAN" (usa índice)
```

---

## 🔄 Reiniciar Todo

```bash
# Con Docker Compose
docker-compose down -v
docker-compose up -d

# Manual
docker stop mongodb redis
docker rm mongodb redis
# Volver a ejecutar los comandos de inicio
```

---

## 📚 Recursos Adicionales

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

---

## 🎓 Siguientes Pasos

1. ✅ Explorar la API con Swagger UI
2. ✅ Cargar tus propios datos de productos
3. ✅ Ajustar configuraciones en `application.properties`
4. ✅ Implementar nuevos filtros de búsqueda
5. ✅ Integrar con tu frontend
6. ✅ Configurar monitoreo con Prometheus + Grafana
7. ✅ Desplegar en producción (Kubernetes)

---

## 💡 Tips Profesionales

### Tip 1: Calentar el cache
```bash
# Ejecutar búsquedas comunes al inicio
curl -X POST http://localhost:8080/api/v1/products/search \
  -H "Content-Type: application/json" \
  -d '{"category": "Electronics", "page": 0, "size": 20}'
```

### Tip 2: Monitoring en producción
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'product-search'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Tip 3: Optimizar MongoDB
```javascript
// En producción, usar WiredTiger cache
// Configurar en mongod.conf:
storage:
  wiredTiger:
    engineConfig:
      cacheSizeGB: 4  // 50% de RAM disponible
```

---

**¿Problemas?** Consulta [ARCHITECTURE-DECISIONS.md](ARCHITECTURE-DECISIONS.md) y [API-EXAMPLES.md](API-EXAMPLES.md)
