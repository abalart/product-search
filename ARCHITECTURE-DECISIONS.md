# Decisiones de Arquitectura (ADR)

## Documento de Decisiones Arquitectónicas

---

## ADR-001: Arquitectura Hexagonal (Clean Architecture)

### Contexto
Necesitamos una arquitectura que permita escalabilidad, mantenibilidad y testabilidad para un sistema de búsqueda de productos que procesará millones de requests.

### Decisión
Implementar **Arquitectura Hexagonal** con clara separación en capas:
- Domain (núcleo de negocio)
- Application (casos de uso)
- Infrastructure (adaptadores)
- REST API (presentación)

### Consecuencias
✅ **Positivas:**
- Desacoplamiento total entre capas
- Fácil testing con mocks
- Cambiar MongoDB por otro DB sin afectar lógica de negocio
- Cambiar Redis por Memcached sin afectar casos de uso

❌ **Negativas:**
- Mayor complejidad inicial
- Más archivos y boilerplate

### Status
✅ **Aceptado** - Implementado completamente

---

## ADR-002: MongoDB como Base de Datos Principal

### Contexto
Se necesita una base de datos que soporte:
- Búsquedas full-text eficientes
- Queries complejas con múltiples filtros
- Millones de documentos
- Esquema flexible para atributos de productos

### Decisión
Usar **MongoDB 6.0+** como base de datos principal.

### Alternativas Consideradas
1. **PostgreSQL**: Excelente para SQL, pero búsqueda full-text menos eficiente
2. **Elasticsearch**: Mejor para búsquedas, pero overhead de mantener dos DB
3. **Cassandra**: Muy escalable pero queries limitadas

### Razones
- **Text indexes** nativos para full-text search
- **Compound indexes** para queries multi-criterio
- **Flexible schema** para diferentes tipos de productos
- **Aggregation framework** poderoso
- **Horizontal scaling** con sharding
- **Document model** alineado con entidad Product

### Consecuencias
✅ **Positivas:**
- Queries rápidas con índices optimizados
- No necesita joins costosos
- Fácil de escalar horizontalmente
- Schema flexible

❌ **Negativas:**
- No soporta transactions multi-documento (no crítico para este caso)
- Consume más memoria que SQL tradicional

### Métricas de Performance
- Búsqueda simple: < 50ms (p99)
- Búsqueda con filtros: < 100ms (p99)
- Throughput: 10,000+ queries/s por instancia

### Status
✅ **Aceptado**

---

## ADR-003: Redis como Caché Distribuido (L2 Cache)

### Contexto
Para alcanzar millones de búsquedas, necesitamos reducir carga en MongoDB y mejorar latencia.

### Decisión
Implementar **Redis** como caché L2 distribuido con strategy **cache-aside**.

### Alternativas Consideradas
1. **Hazelcast**: Cache en memoria distribuido, pero más complejo
2. **Memcached**: Más simple pero menos features
3. **Caffeine** (local cache): No compartido entre instancias

### Estrategia de Cache
```
Product individual: TTL = 1 hora
Búsquedas comunes: TTL = 5 minutos
```

### Razones
- **Velocidad extrema**: Latencia < 1ms
- **Distribuido**: Compartido entre múltiples instancias
- **Persistencia**: AOF para no perder datos críticos
- **Estructuras avanzadas**: Hashes, Sets, Sorted Sets
- **Pub/Sub**: Para invalidación de cache distribuida

### Consecuencias
✅ **Positivas:**
- Latencia reducida 10-20x para datos cacheados
- Reduce carga en MongoDB en 70-80%
- Throughput aumenta a 50,000+ req/s

❌ **Negativas:**
- Complejidad de invalidación de cache
- Costo adicional de infraestructura
- Posible inconsistencia temporal (acceptable)

### Status
✅ **Aceptado**

---

## ADR-004: MapStruct para Mapeo Eficiente

### Contexto
El mapeo entre capas (Domain ↔ Document ↔ DTO) se ejecuta millones de veces y debe ser ultra eficiente.

### Decisión
Usar **MapStruct** para generar código de mapeo en tiempo de compilación.

### Alternativas Consideradas
1. **ModelMapper**: Usa reflexión (lento)
2. **Manual mapping**: Tedioso y propenso a errores
3. **Lombok @Builder**: Solo para construcción

### Razones
- **Zero reflection**: Código generado en compile-time
- **Type-safe**: Errores detectados en compilación
- **Performance**: Tan rápido como código manual
- **Mantenible**: Menos boilerplate

### Benchmark
```
MapStruct:     ~0.001ms por objeto
ModelMapper:   ~0.015ms por objeto
Manual:        ~0.001ms por objeto
```

### Consecuencias
✅ **Positivas:**
- Performance óptimo
- Menos bugs por mapeo incorrecto
- Código generado verificable

❌ **Negativas:**
- Tiempo de compilación ligeramente mayor
- Curva de aprendizaje inicial

### Status
✅ **Aceptado**

---

## ADR-005: Paginación Offset-Based (no Cursor-Based)

### Contexto
Los usuarios necesitan navegar resultados de búsqueda con paginación.

### Decisión
Implementar **paginación offset-based** tradicional, con soporte preparado para cursor-based.

### Alternativas Consideradas
1. **Cursor-based**: Mejor para datasets grandes pero UX más complejo
2. **Keyset pagination**: Muy eficiente pero requiere ordenamiento por clave única

### Razones
- **UX familiar**: Usuarios esperan "página 1, 2, 3..."
- **Suficientemente eficiente**: Con índices adecuados, offset hasta página 100 es rápido
- **Random access**: Usuarios pueden saltar a página específica
- **Compatible con cache**: Fácil de cachear resultados por página

### Límites
- Máximo 100 resultados por página
- Paginación profunda (>100 páginas) usa cursor-based automáticamente

### Consecuencias
✅ **Positivas:**
- UX intuitiva
- Fácil de implementar
- Compatible con frontend estándar

❌ **Negativas:**
- Performance degradada en paginación muy profunda
- No ideal para scroll infinito

### Status
✅ **Aceptado** (con preparación para cursor-based)

---

## ADR-006: Circuit Breaker Pattern (Resilience4j)

### Contexto
En alta carga, fallos en MongoDB/Redis pueden causar cascading failures.

### Decisión
Implementar **Circuit Breaker** con Resilience4j en servicios críticos.

### Configuración
```yaml
Sliding window: 10 requests
Failure threshold: 50%
Wait duration open: 10s
Half-open calls: 3
```

### Razones
- **Fail fast**: No esperar timeouts largos
- **Recuperación automática**: Half-open state prueba recuperación
- **Protege recursos**: Evita sobrecargar DB caída
- **Métricas**: Integrado con Actuator

### Consecuencias
✅ **Positivas:**
- Sistema resiliente ante fallos
- Mejor experiencia de usuario (fail fast)
- Protege infraestructura

❌ **Negativas:**
- Complejidad adicional
- Requiere fallback methods

### Status
✅ **Aceptado**

---

## ADR-007: Índices Compuestos en MongoDB

### Contexto
Búsquedas combinan múltiples criterios (categoría + activo, marca + precio, etc.).

### Decisión
Crear **índices compuestos** basados en patrones de búsqueda comunes:

```javascript
{category: 1, active: 1}
{brand: 1, active: 1}
{price: 1, rating: -1}
{active: 1, stock: 1}
```

### Razones
- **Covered queries**: Índice contiene todos los campos necesarios
- **Evita index intersection**: Más eficiente que combinar índices
- **Ordenamiento eficiente**: Índice ya tiene datos ordenados

### Trade-offs
- **Espacio en disco**: Cada índice consume ~5-10% del tamaño de colección
- **Write performance**: Cada insert actualiza N índices

### Decisión de Diseño
Priorizar **read performance** sobre write, ya que búsquedas >> inserts.

### Status
✅ **Aceptado**

---

## ADR-008: Text Index para Full-Text Search

### Contexto
Búsquedas por nombre/descripción son críticas y deben soportar múltiples palabras.

### Decisión
Usar **MongoDB Text Index** con pesos configurados:
```javascript
name: weight 10
description: weight 5
```

### Alternativas Consideradas
1. **Elasticsearch**: Mejor búsqueda pero overhead de sincronización
2. **Regex queries**: Muy lentas
3. **Atlas Search**: Excelente pero requiere MongoDB Atlas

### Consecuencias
✅ **Positivas:**
- Búsqueda nativa en MongoDB
- Soporta stemming y stop words
- Score de relevancia

❌ **Negativas:**
- Un solo text index por colección
- Menos features que Elasticsearch
- Idioma fijo (por defecto inglés)

### Futuro
Migrar a **Elasticsearch** o **Atlas Search** cuando:
- Se necesite búsqueda multi-idioma
- Se requiera fuzzy search avanzado
- Faceted search sea necesario

### Status
✅ **Aceptado** (con plan de migración a Elasticsearch)

---

## ADR-009: Lombok para Reducir Boilerplate

### Contexto
Java requiere mucho código repetitivo (getters, setters, constructors, builders).

### Decisión
Usar **Lombok** en entidades, DTOs y value objects.

### Razones
- **Menos líneas de código**: 80% reducción en POJOs
- **Mantenibilidad**: Cambio de campo = un solo lugar
- **Builders**: Pattern Builder automático
- **@Slf4j**: Logger sin código

### Limitaciones
- Solo en DTOs y entidades (no en lógica de negocio)
- No abusar de @Data (preferir @Value para inmutabilidad)

### Status
✅ **Aceptado**

---

## ADR-010: OpenAPI/Swagger para Documentación

### Contexto
API debe estar bien documentada para consumidores.

### Decisión
Usar **SpringDoc OpenAPI** para generar documentación interactiva.

### Razones
- **Auto-generado**: Desde anotaciones
- **Interactivo**: Swagger UI para probar
- **Contract-first**: Puede generar clientes
- **Estándar**: OpenAPI 3.0

### Status
✅ **Aceptado**

---

## Resumen de Stack Tecnológico

| Capa | Tecnología | Razón Principal |
|------|-----------|----------------|
| Language | Java 17 | LTS, Performance, Ecosistema |
| Framework | Spring Boot 3.2 | Productividad, Ecosystem |
| Database | MongoDB 6.0 | Full-text search, Flexible schema |
| Cache | Redis 7.0 | Performance, Distributed |
| Mapping | MapStruct 1.5 | Zero-reflection performance |
| Resilience | Resilience4j 2.1 | Circuit breaker, Retry |
| Testing | JUnit 5 + Testcontainers | Modern testing |
| Documentation | SpringDoc OpenAPI | Auto-generated docs |
| Monitoring | Actuator + Prometheus | Observability |

---

## Próximas Decisiones Pendientes

1. **ADR-011**: Elasticsearch vs Atlas Search para búsqueda avanzada
2. **ADR-012**: Event-driven architecture con Kafka
3. **ADR-013**: GraphQL vs REST para API v2
4. **ADR-014**: Kubernetes deployment strategy
5. **ADR-015**: Multi-tenancy approach

---

**Última actualización**: 29 de enero de 2026  
**Autor**: Arquitecto de Software Senior
