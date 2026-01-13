# Unified Hotel Search API (Backend Challenge)

## Resumen
Construir un microservicio **solo backend** en **Java 17+ / Spring Boot 3 (WebFlux)** que consume **2 APIs publicas** (proveedores) para buscar "hoteles/alojamientos" cerca de una ciudad y devuelve un resultado **normalizado** en un **formato estandar**.

- **Sin frontend**
- **Sin base de datos** (permitido: cache en memoria con TTL)
- **Entrada**: ciudad + radio + limite
- **Salida**: lista de hoteles en formato `StandardHotel`

---

## Objetivos principales
1. Integrar **multiples proveedores** (APIs publicas) y aislar sus DTOs.
2. Mapear y unificar resultados a un **modelo comun**.
3. Manejar fallos de proveedores (timeouts, respuestas parciales, warnings).
4. Exponer endpoints REST propios y documentarlos con OpenAPI.

---

## Stack requerido
- Java 17+
- Spring Boot 3.x
- Spring WebFlux (Reactive)
- WebClient
- Testing: JUnit 5 + Mockito + WireMock (o MockWebServer)
- Opcional: Resilience4j (timeout/retry/circuit breaker), Caffeine (cache)

---

## Proveedores (APIs publicas)
### Provider A: Geocoding (ciudad -> lat/lon)
Elegir 1:
- **Nominatim (OpenStreetMap)** (sin API key; respetar rate limit)
- **Geoapify Geocoding** (requiere API key)

### Provider B y C: Places/Accommodations
- **OpenTripMap** (places/POIs; permite filtrar por kinds como "accomodations" segun disponibilidad)
- Segundo proveedor de places: **Geoapify Places** (API key) o alternativa publica equivalente

> Nota: el challenge evalua la normalizacion multi-fuente. Si algun provider no entrega rating/amenities, se devuelve vacio o null segun el contrato.

---

## Endpoints requeridos
### 1) Buscar hoteles por ciudad
`GET /api/v1/hotels/search?city=Barcelona&radiusKm=5&limit=20`

Respuesta:
```json
{
  "query": { "city": "Barcelona", "radiusKm": 5, "limit": 20 },
  "results": [ { "id": "std_...", "name": "...", "...": "..." } ],
  "warnings": [
    { "provider": "OPENTRIPMAP", "message": "timeout after 2000ms" }
  ]
}
```

Reglas:
- Si un proveedor falla y el otro responde, devolver **resultado parcial** + warning.
- Orden sugerido: por `score` desc, luego `distanceKm` asc.
- Deduplicacion: por coordenadas cercanas + nombre parecido (heuristica simple aceptada).

### 2) Detalle por ID estandar
`GET /api/v1/hotels/{standardHotelId}`

Debe devolver un `StandardHotel` completo (o lo maximo posible segun fuentes) incluyendo `sourceReferences`.

### 3) Estado de proveedores
`GET /api/v1/providers/status`

Ejemplo:
```json
{
  "providers": [
    { "name": "NOMINATIM", "status": "UP", "latencyMs": 120 },
    { "name": "OPENTRIPMAP", "status": "DEGRADED", "lastError": "timeout" }
  ]
}
```

---

## Modelo estandar (contrato)
### StandardHotel
```json
{
  "id": "std_6f2a9c...",
  "name": "Hotel Example",
  "type": "HOTEL",
  "address": {
    "country": "ES",
    "city": "Barcelona",
    "street": "Carrer X 123",
    "postalCode": "08001"
  },
  "geo": { "lat": 41.3874, "lon": 2.1686 },
  "rating": { "value": 4.3, "reviewsCount": 120 },
  "amenities": ["WIFI", "PARKING", "POOL"],
  "distanceKm": 1.2,
  "score": 0.86,
  "sourceReferences": [
    { "provider": "OPENTRIPMAP", "externalId": "X123" },
    { "provider": "GEOAPIFY", "externalId": "Y999" }
  ]
}
```

### Reglas de normalizacion
- `id`: estable. Sugerencia:
  - `std_{sha256(provider + ":" + externalId).substr(0,12)}`
  - Si no hay externalId confiable, usar `sha256(name + ":" + lat + ":" + lon)`
- `amenities`: normalizar a un enum interno (`WIFI`, `PARKING`, `POOL`, `GYM`, `SPA`, `AIR_CONDITIONING`, etc.)
- `distanceKm`: distancia entre el centro (lat/lon de la ciudad) y el hotel
- `score` (ejemplo simple):
  - `score = 0.6 * ratingNormalized + 0.3 * completeness + 0.1 * (1 - distanceNormalized)`

---

## Arquitectura sugerida (paquetes)
```
com.example.unifiedhotels
  api
    HotelController
    ProvidersController
    dto (request/response propios)
  domain
    StandardHotel, Address, GeoPoint, Rating
    enums (Amenity, ProviderName, PlaceType)
  service
    HotelSearchService
    HotelDetailsService
    DeduplicationService
    ScoringService
  providers
    geocoding
      NominatimClient (WebClient)
      dto (NominatimResponse...)
      NominatimMapper
    places
      OpenTripMapClient
      dto (...)
      OpenTripMapMapper
      GeoapifyPlacesClient (opcional)
  config
    WebClientConfig
    ProviderProperties
  error
    GlobalExceptionHandler
    ErrorResponse
  util
    DistanceUtils (Haversine)
    HashIdUtils
```

---

## Reglas reactivas (WebFlux)
- No bloquear (no `.block()` en servicios).
- Usar `Mono`/`Flux`:
  - Geocoding: `Mono<GeoPoint>`
  - Providers: `Flux<ProviderPlace>`
  - Merge: `Flux.merge(p1, p2)`
  - Dedup/score: `collectList()` y procesar en memoria (aceptable por limite)

---

## Manejo de errores
- Timeouts por proveedor (por ejemplo 2000-3000 ms).
- Para cada proveedor capturar error y transformarlo a warning sin tumbar toda la request.
- Errores propios:
  - 400 si falta `city` o es invalida
  - 404 si no se puede geocodificar la ciudad
  - 502 si fallan **todos** los proveedores de places

Formato de error:
```json
{
  "timestamp": "2026-01-12T12:34:56Z",
  "status": 404,
  "error": "CITY_NOT_FOUND",
  "message": "No se pudo resolver la ciudad 'X'"
}
```

---

## Testing requerido
- Unit tests:
  - mappers por proveedor -> StandardHotel
  - scoring + dedup
- Integration tests:
  - WireMock stubs para simular providers
  - test del endpoint `/api/v1/hotels/search` con respuesta parcial y warnings

---

## Entregables
1. Repo con codigo
2. README (este documento) con:
   - como correr
   - variables de entorno (API keys si aplica)
   - ejemplos curl
3. Coleccion de Postman (opcional)
4. Tests pasando

---

## Como correr (ejemplo)
### Variables de entorno
- `OPENTRIPMAP_API_KEY` (si aplica)
- `GEOAPIFY_API_KEY` (si aplica)
- `PROVIDER_TIMEOUT_MS=2500`

### Run
```bash
./mvnw spring-boot:run
```

### Ejemplo curl
```bash
curl "http://localhost:8080/api/v1/hotels/search?city=Barcelona&radiusKm=5&limit=10"
```

---

## Bonus (para destacar)
- Cache TTL por query (Caffeine)
- Resilience4j: circuit breaker por proveedor
- Observabilidad: Micrometer metrics por proveedor (latencia, error rate)
- OpenAPI/Swagger bien documentado
- Dedup mejorada: fuzzy matching con threshold

---

## Criterios de evaluacion
- Correcta normalizacion multi-proveedor
- Buen diseno (capas, DTOs aislados, cohesion)
- Resiliencia y manejo de errores
- Tests (calidad y cobertura de casos)
- Claridad del README y decisiones tecnicas
