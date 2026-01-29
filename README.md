# Product Search Service 🚀

[![CI](https://github.com/abalart/product-search/actions/workflows/ci.yml/badge.svg)](https://github.com/abalart/product-search/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0+-green.svg)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Arquitectura de Software de Alto Rendimiento

Sistema de búsqueda ultra eficiente de productos diseñado para procesar **millones de búsquedas** con MongoDB y Redis.

---

## 📋 Tabla de Contenidos

- [Características Principales](#características-principales)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [API Endpoints](#api-endpoints)
- [Optimizaciones de Performance](#optimizaciones-de-performance)
- [Tests](#tests)
- [Métricas y Monitoreo](#métricas-y-monitoreo)

---

## 🎯 Características Principales

### Performance y Escalabilidad

- **Búsqueda ultra rápida** con índices optimizados en MongoDB
- **Caché distribuido** con Redis (TTL configurable)
- **Connection pooling** optimizado para alta concurrencia
- **Paginación eficiente** con cursor-based pagination
- **Circuit Breaker** pattern para resiliencia
- **Proyecciones de campos** para carga eficiente en memoria

### Arquitectura

- **Clean Architecture** (Hexagonal)
- **Domain-Driven Design** (DDD)
- **SOLID Principles**
- **MapStruct** para mapeo eficiente sin reflexión
- **Async processing** para operaciones pesadas

### Funcionalidades

- Búsqueda full-text con relevancia
- Filtrado por múltiples criterios
- Autocomplete para sugerencias rápidas
- Productos similares (recomendaciones)
- Ordenamiento flexible
- Paginación optimizada

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                     REST API Layer                       │
│              (Controllers + DTOs + Mappers)              │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                  Application Layer                       │
│          (Use Cases + Business Logic + Cache)            │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                    Domain Layer                          │
│        (Entities + Value Objects + Interfaces)           │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                Infrastructure Layer                      │
│         (MongoDB + Redis + Persistence Adapters)         │
└──────────────────────────────────────────────────────────┘
```

### Capas de la Arquitectura

#### 1. **Domain Layer** (Núcleo del negocio)

- `Product`: Entidad principal
- `SearchCriteria`: Value Object para búsquedas
- `PageResult`: Value Object para resultados paginados
- `ProductRepository`: Puerto (interface) para persistencia
- `CacheRepository`: Puerto para caché

#### 2. **Application Layer** (Casos de uso)

- `ProductSearchService`: Orquesta la lógica de búsqueda
- Implementa caching strategy
- Circuit breaker para resiliencia

#### 3. **Infrastructure Layer** (Adaptadores)

- `MongoProductRepositoryAdapter`: Implementación MongoDB
- `RedisCacheRepositoryAdapter`: Implementación Redis
- `ProductDocument`: Entidad MongoDB con índices
- `RestMapper` y `ProductMapper`: Conversiones eficientes

#### 4. **REST Layer** (API)

- `ProductSearchController`: Endpoints REST
- DTOs optimizados para respuestas
- OpenAPI/Swagger documentation

---

## 💻 Tecnologías

| Tecnología        | Versión | Propósito                |
| ----------------- | ------- | ------------------------ |
| Java              | 17      | Lenguaje base            |
| Spring Boot       | 3.2.2   | Framework principal      |
| MongoDB           | 6.0+    | Base de datos principal  |
| Redis             | 7.0+    | Caché distribuido        |
| MapStruct         | 1.5.5   | Mapeo eficiente          |
| Resilience4j      | 2.1.0   | Circuit breaker          |
| Lombok            | 1.18.30 | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.3.0   | Documentación API        |
| JUnit 5           | 5.10.x  | Testing                  |
| Testcontainers    | 1.19.3  | Integration tests        |

---

## 📦 Requisitos

- **JDK 17** o superior
- **Maven 3.8+**
- **MongoDB 6.0+** (local o Docker)
- **Redis 7.0+** (local o Docker)
- **8GB RAM** mínimo recomendado
- **Docker** (opcional, para contenedores)

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/abalart/product-search.git
cd product-search
```

### 2. Iniciar MongoDB y Redis con Docker

```bash
# MongoDB
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_DATABASE=product_search \
  mongo:6.0

# Redis
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 3. Crear índices en MongoDB

```javascript
use product_search

// Índice de texto completo
db.products.createIndex(
  { name: "text", description: "text" },
  { weights: { name: 10, description: 5 } }
)

// Índices compuestos (ya definidos en @CompoundIndex)
db.products.createIndex({ category: 1, active: 1 })
db.products.createIndex({ brand: 1, active: 1 })
db.products.createIndex({ price: 1, rating: -1 })
db.products.createIndex({ active: 1, stock: 1 })
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

---

## ⚙️ Configuración

### application.properties

Ubicación: `src/main/resources/application.properties`

#### MongoDB

```properties
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=product_search
```

#### Redis

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.lettuce.pool.max-active=50
```

#### Cache TTL

```properties
# Product cache: 1 hour
# Search cache: 5 minutes
```

---

## 🏃 Ejecución

### Modo desarrollo

```bash
mvn spring-boot:run
```

### Modo producción

```bash
java -jar target/product-search-1.0.0.jar
```

### Con perfil específico

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

La aplicación estará disponible en: `http://localhost:8080`

---

## 📚 API Endpoints

### Swagger UI

Accede a la documentación interactiva:

```
http://localhost:8080/swagger-ui.html
```

### Principales Endpoints

#### 1. **Búsqueda de productos**

```http
POST /api/v1/products/search
Content-Type: application/json

{
  "query": "laptop gaming",
  "category": "Electronics",
  "brand": "Dell",
  "minPrice": 500.00,
  "maxPrice": 2000.00,
  "minRating": 4.0,
  "inStockOnly": true,
  "activeOnly": true,
  "sortBy": "price",
  "sortDirection": "ASC",
  "page": 0,
  "size": 20
}
```

#### 2. **Obtener producto por ID**

```http
GET /api/v1/products/{id}
```

#### 3. **Autocomplete**

```http
GET /api/v1/products/autocomplete?query=lap&limit=10
```

#### 4. **Productos similares**

```http
GET /api/v1/products/{id}/similar?limit=10
```

#### 5. **Health Check**

```http
GET /api/v1/products/health
```

---

## ⚡ Optimizaciones de Performance

### 1. **Índices MongoDB**

- Índice de texto completo en `name` y `description`
- Índices compuestos para queries comunes
- Índices en campos filtrados frecuentemente

### 2. **Caché Distribuido (Redis)**

- **L2 Cache** para productos individuales (TTL: 1h)
- Cache de búsquedas comunes (TTL: 5min)
- Cache-aside pattern con lazy loading

### 3. **Connection Pooling**

```
MongoDB:
- Max connections: 100
- Min connections: 10
- Connection timeout: 5s

Redis:
- Max active: 50
- Max idle: 20
- Min idle: 5
```

### 4. **Proyecciones de Campos**

Solo se cargan los campos necesarios en cada query

### 5. **Paginación Eficiente**

- Offset-based para primeras páginas
- Cursor-based para datasets grandes

### 6. **MapStruct (Zero-reflection)**

Mapeo compilado, sin overhead de reflexión

### 7. **Circuit Breaker**

Previene cascading failures en alta carga

---

## 🧪 Tests

### Ejecutar todos los tests

```bash
mvn test
```

### Ejecutar solo tests unitarios

```bash
mvn test -Dtest=*ServiceTest
```

### Coverage report

```bash
mvn jacoco:report
# Ver: target/site/jacoco/index.html
```

**📚 Documentación completa:** Ver [TESTING.md](TESTING.md)

---

## 📊 Métricas y Monitoreo

### Actuator Endpoints

```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/prometheus
```

### Métricas disponibles

- Latencia de queries
- Cache hit rate
- Circuit breaker status
- Connection pool stats
- JVM metrics

### Integración con Prometheus

```yaml
# prometheus.yml
scrape_configs:
  - job_name: "product-search"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["localhost:8080"]
```

---

## 🎯 Capacidad de Procesamiento

Con la arquitectura implementada, el sistema puede manejar:

- **10,000+ búsquedas/segundo** (con cache warm)
- **Latencia p99 < 100ms** para búsquedas cacheadas
- **Latencia p99 < 500ms** para búsquedas en MongoDB
- **Millones de productos** en base de datos
- **Escalado horizontal** con múltiples instancias

---

## 📝 Buenas Prácticas Implementadas

✅ **Clean Code**: Nombres descriptivos, métodos pequeños, SRP  
✅ **SOLID**: Principios aplicados en toda la arquitectura  
✅ **DDD**: Modelo de dominio rico, value objects  
✅ **Hexagonal Architecture**: Desacoplamiento de capas  
✅ **Circuit Breaker**: Resiliencia ante fallos  
✅ **Observability**: Logging estructurado, métricas  
✅ **Testing**: Unit tests, integration tests  
✅ **Documentation**: OpenAPI/Swagger, JavaDoc

---

## 🔮 Mejoras Futuras (Roadmap)

- [ ] Elasticsearch para búsqueda avanzada
- [ ] GraphQL API
- [ ] Event-driven architecture con Kafka
- [ ] Multi-tenancy support
- [ ] Machine Learning para recomendaciones
- [ ] Distributed tracing con Jaeger
- [ ] Kubernetes deployment configs
- [ ] CI/CD pipelines

---

## �‍💻 Autor

**Agustin Balart**  
Desarrollador Full Stack | Especialista en Arquitecturas Escalables

[![GitHub](https://img.shields.io/badge/GitHub-abalart-181717?style=flat&logo=github)](https://github.com/abalart)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=flat&logo=linkedin)](https://www.linkedin.com/in/agustin-balart)

### 💼 Sobre este proyecto

Proyecto demostrativo de arquitectura escalable implementando:

- Clean Architecture y DDD
- Microservicios de alto rendimiento
- Optimización de bases de datos
- Buenas prácticas de desarrollo

---

## 📬 Contacto

¿Preguntas o sugerencias? Abre un [issue](https://github.com/abalart/product-search/issues) o contáctame directamente.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.
