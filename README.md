# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**


# CuentaClara API - Clean Architecture

## Arquitectura

Este backend implementa **Clean Architecture** con el plugin de Bancolombia. La regla fundamental es que las dependencias siempre apuntan hacia adentro: la infraestructura depende del dominio, pero el dominio **nunca** depende de la infraestructura ni de frameworks externos.

```
┌─────────────────────────────────────────────────┐
│                  Application                     │
│  (ensambla módulos, inyecta dependencias)        │
├─────────────────────────────────────────────────┤
│              Infrastructure                      │
│  ┌──────────────────┐  ┌─────────────────────┐  │
│  │  Entry Points    │  │  Driven Adapters     │  │
│  │  (api-rest)      │  │  (r2dbc-postgresql)  │  │
│  └──────────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────┤
│                   Domain                         │
│  ┌──────────────────┐  ┌─────────────────────┐  │
│  │  Use Cases       │  │  Model              │  │
│  │  (lógica app)    │  │  (entidades puras)  │  │
│  └──────────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

## Reglas de Arquitectura

### 1. El dominio NO depende de nada externo

- El módulo `model` no tiene dependencias de Spring, JPA, R2DBC ni ningún framework.
- Solo contiene clases Java puras (POJOs), enums y excepciones de negocio.
- Las interfaces (ports/gateways) se definen aquí para que la infraestructura las implemente.

### 2. Los casos de uso orquestan la lógica

- El módulo `usecase` depende únicamente de `model`.
- Nunca importa clases de infraestructura.
- Define la lógica de aplicación y llama a los ports definidos en el modelo.

### 3. La infraestructura implementa los ports

- Los **Driven Adapters** (`r2dbc-postgresql`) implementan las interfaces definidas en `model`.
- Los **Entry Points** (`api-rest`) exponen endpoints que invocan los casos de uso.
- Aquí sí se permiten dependencias de frameworks (Spring, R2DBC, Flyway, etc.).

### 4. Application ensambla todo

- El módulo `app-service` inicia la app y resuelve dependencias via `@ComponentScan`.
- Es el único lugar con `public static void main`.

---

## Principios SOLID Obligatorios

| Principio | Aplicación en este proyecto |
|---|---|
| **S** - Single Responsibility | Cada clase tiene una sola razón de cambio. Un UseCase = una operación de negocio. |
| **O** - Open/Closed | Nuevas funcionalidades se agregan creando nuevos UseCases/Adapters, no modificando los existentes. |
| **L** - Liskov Substitution | Las implementaciones de los ports (gateways) son intercambiables sin romper el contrato. |
| **I** - Interface Segregation | Los ports se definen pequeños y específicos. No crear interfaces "god" con muchos métodos. |
| **D** - Dependency Inversion | El dominio define interfaces (ports); la infraestructura las implementa. Nunca al revés. |

---

## Comandos para Generar Código

### Crear un modelo (entidad de dominio)

```bash
gradle generateModel --name=NombreModelo
```

Genera la clase en `domain/model/src/main/java/co/com/cuentaclara/model/`.

### Crear un caso de uso

```bash
gradle generateUseCase --name=NombreCasoDeUso
```

Genera la clase en `domain/usecase/src/main/java/co/com/cuentaclara/usecase/`.

### Crear un entry point (API REST)

```bash
gradle generateEntryPoint --type=apirest
```

### Crear un driven adapter

```bash
gradle generateDrivenAdapter --type=r2dbc
```

---

## Flujo de Implementación

Al agregar una nueva funcionalidad, seguir **siempre** este orden:

```
1. Modelo      → Definir entidad y port/gateway en domain/model
2. Caso de uso → Implementar lógica de negocio en domain/usecase
3. Adapter     → Implementar el port en infrastructure/driven-adapters
4. Entry Point → Exponer endpoint en infrastructure/entry-points
```

### Ejemplo: Crear un nuevo módulo "Contacts"

```bash
# 1. Crear el modelo
gradle generateModel --name=Contact

# 2. Definir el gateway (port) en model
#    → Crear interfaz ContactGateway en model/src/.../model/contact/gateways/

# 3. Crear el caso de uso
gradle generateUseCase --name=ContactUseCase

# 4. Implementar el gateway en el driven adapter (r2dbc-postgresql)

# 5. Exponer en el entry point (api-rest)
```

---

## Buenas Prácticas del Proyecto

### Domain (model + usecase)

- **Cero dependencias de frameworks.** Solo `reactor-core` para tipos reactivos.
- Usar `UUID` como identificador.
- Usar `BigDecimal` para valores monetarios.
- Definir excepciones de negocio propias (no reutilizar excepciones de Spring).
- Los gateways (ports) son interfaces en el modelo, no en usecase.
- Soft delete: usar campo `deletedAt` en las entidades.
- Validaciones de negocio van en el caso de uso, no en el controller.

### Infrastructure

- Los controllers (entry points) solo mapean DTOs y delegan al caso de uso.
- Los adapters implementan los gateways del dominio.
- Las migraciones de base de datos van en `r2dbc-postgresql/src/main/resources/db/migration/`.
- Usar `@Table` de Spring Data R2DBC en los modelos del adapter, nunca en el dominio.
- Tenant isolation: siempre filtrar por `tenant_id` en queries.

### Testing

- Tests unitarios para casos de uso (mockear gateways).
- Tests de integración para adapters.
- Ejecutar: `gradle test`

### Configuración

- Properties de conexión en `adapters.r2dbc.*` dentro de `application.yaml`.
- Flyway corre automáticamente al iniciar usando JDBC (R2DBC no soporta DDL migrations).
- No hardcodear secretos; usar variables de entorno en producción.

---

## Estructura del Proyecto

```
cuentaclara-api/
├── applications/
│   └── app-service/          ← Ensamblador, main(), application.yaml
├── domain/
│   ├── model/                ← Entidades, enums, gateways (ports)
│   └── usecase/              ← Casos de uso (lógica de negocio)
├── infrastructure/
│   ├── driven-adapters/
│   │   └── r2dbc-postgresql/ ← Implementación de gateways + Flyway migrations
│   ├── entry-points/
│   │   └── api-rest/         ← Controllers REST
│   └── helpers/              ← Utilidades compartidas
├── build.gradle              ← Config raíz
├── main.gradle               ← Config compartida de subprojects
└── settings.gradle
```

---

## Stack Tecnológico

- **Java 21**
- **Spring Boot 4.0.5** (WebMVC + R2DBC)
- **Spring Data R2DBC** (acceso reactivo a PostgreSQL)
- **Flyway** (migraciones de base de datos via JDBC)
- **PostgreSQL** (base de datos)
- **Lombok** (reducir boilerplate)
- **Reactor** (programación reactiva)
- **Gradle** con plugin Clean Architecture de Bancolombia
