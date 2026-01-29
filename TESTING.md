# 🧪 Testing Guide - Product Search Service

Guía completa para ejecutar tests en el proyecto Product Search.

## 📋 Tabla de Contenidos

- [Tests Unitarios](#tests-unitarios)
- [Tests de Integración](#tests-de-integración)
- [Cobertura de Código](#cobertura-de-código)
- [CI/CD en GitHub Actions](#cicd-en-github-actions)

---

## 🔬 Tests Unitarios

Tests que **no requieren dependencias externas** (MongoDB, Redis).

### Ejecutar todos los tests unitarios

```bash
mvn test -Dtest=*ServiceTest
```

### Ejecutar un test específico

```bash
mvn test -Dtest=ProductSearchServiceTest
```

### Tests disponibles

| Test | Descripción |
|------|-------------|
| `ProductSearchServiceTest` | Lógica de búsqueda y caché (5 casos) |
| `ProductSearchControllerTest` | Endpoints REST (7 casos) |

### Ejemplo de ejecución

```bash
$ mvn test -Dtest=ProductSearchServiceTest
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.search.product.application.service.ProductSearchServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.245 s
[INFO] BUILD SUCCESS
```

---

## 🔌 Tests de Integración

Tests que **requieren MongoDB y Redis** funcionando localmente.

### Prerequisitos

Asegúrate de que MongoDB y Redis estén corriendo:

```bash
# Opción 1: Docker Compose (recomendado)
docker-compose up -d

# Opción 2: Servicios locales instalados
# MongoDB debe escuchar en localhost:27017
# Redis debe escuchar en localhost:6379
```

### Verificar servicios

```bash
# MongoDB
mongosh --eval "db.adminCommand('ping')"

# Redis
redis-cli ping
```

### Ejecutar tests con dependencias

```bash
# Todos los tests (incluyendo integración)
mvn test

# Solo tests de integración
mvn test -Dtest=*ControllerTest
```

### Configuración de variables de entorno

Si usas puertos diferentes, configura las variables de entorno:

```bash
# Windows PowerShell
$env:SPRING_DATA_MONGODB_URI = "mongodb://localhost:27017/product_search_test"
$env:SPRING_DATA_REDIS_HOST = "localhost"
$env:SPRING_DATA_REDIS_PORT = "6379"
mvn test

# Linux/Mac
export SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/product_search_test"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_DATA_REDIS_PORT="6379"
mvn test
```

---

## 📊 Cobertura de Código

### Generar reporte de cobertura

```bash
mvn clean test jacoco:report
```

### Ver reporte (Windows)

```bash
# Se genera en: target/site/jacoco/index.html
start target/site/jacoco/index.html
```

### Ver reporte (Linux/Mac)

```bash
open target/site/jacoco/index.html
# o
firefox target/site/jacoco/index.html
```

### Reporte de cobertura en CI

El reporte se sube automáticamente a [Codecov](https://codecov.io) en cada push.

**Objetivo de cobertura:** 60%+ por paquete

---

## 🚀 CI/CD en GitHub Actions

### Cómo funciona el workflow

**Archivo:** `.github/workflows/ci.yml`

#### Pasos ejecutados en cada push:

1. **Checkout** → Descargar código
2. **Setup JDK 17** → Preparar ambiente Java
3. **Build** → `mvn clean package -DskipTests`
4. **Unit Tests** → `mvn test -Dtest=ProductSearchServiceTest` (opcional)
5. **Coverage** → `mvn jacoco:report`
6. **Upload a Codecov** → Enviar reporte

### Por qué solo se ejecutan tests unitarios en CI

- ✅ **Unitarios**: No requieren dependencias → Rápidos y confiables
- ❌ **Integración**: Requieren MongoDB/Redis → Inestables en CI

### Ejecutar localmente como CI

```bash
mvn clean package -DskipTests
mvn test -Dtest=ProductSearchServiceTest
mvn jacoco:report
```

---

## 🎯 Flujo de Trabajo Recomendado

### Durante desarrollo

```bash
# 1. Iniciar servicios
docker-compose up -d

# 2. Ejecutar tests unitarios (rápido)
mvn test -Dtest=*ServiceTest

# 3. Si necesitas tests de integración
mvn test

# 4. Verificar cobertura
mvn jacoco:report
```

### Antes de hacer commit

```bash
# 1. Compilar
mvn clean compile

# 2. Tests completos
mvn test

# 3. Empaquetar
mvn package

# 4. Commit solo si todo pasa
git add .
git commit -m "feat: description"
git push origin main
```

### En CI/CD (GitHub Actions)

```
push → Compile → Unit Tests → Coverage Upload → Badge Update ✅
```

---

## 📈 Interpretando Resultados

### Success (Verde)

```
[INFO] BUILD SUCCESS
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
```

### Fallos comunes

#### 1. **MongoDB no disponible**

```
ERROR: Connection refused: localhost:27017
```

**Solución:**
```bash
docker-compose up -d
# o verificar que MongoDB está corriendo
mongosh --eval "db.adminCommand('ping')"
```

#### 2. **Redis no disponible**

```
ERROR: Could not connect to Redis at localhost:6379
```

**Solución:**
```bash
docker-compose up -d
# o verificar que Redis está corriendo
redis-cli ping
```

#### 3. **Tests lentos**

Si los tests tardan más de 30 segundos:
- Ejecuta solo unitarios: `mvn test -Dtest=*ServiceTest`
- Aumenta timeout: `mvn test -DtestTimeout=60`

---

## 🔧 Troubleshooting

### ¿Los tests pasan localmente pero fallan en CI?

**Causas comunes:**
- Diferentes versiones de Java
- Variables de entorno no configuradas
- Paths absolutos en el código

**Solución:**
- Usa `mvn clean` antes de cada test
- Verifica `application-test.properties`
- Usa paths relativos

### ¿Necesito todos los tests para desarrollar?

**Recomendación:**
- Unitarios: SÍ, siempre (son rápidos)
- Integración: Cuando modifiques MongoDB/Redis
- Coverage: Antes de hacer push

### ¿Cómo deshabilitar tests?

```bash
# Sin ejecutar tests
mvn clean package -DskipTests

# Saltear solo cierta clase
mvn test -Dtest=!ProductSearchControllerTest
```

---

## 📚 Recursos

- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [GitHub Actions Java](https://github.com/actions/setup-java)
- [Codecov Integration](https://codecov.io)

---

**¿Preguntas sobre testing?** Abre un [issue](https://github.com/abalart/product-search/issues) en el repositorio.
