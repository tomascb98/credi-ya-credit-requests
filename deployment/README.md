# Base de Datos - CrediYa Credit Requests

Este directorio contiene la configuración para usar la base de datos existente del proyecto de autenticación, creando solo el esquema `credit` necesario para el microservicio de solicitudes de crédito.

## Estructura

```
deployment/
├── setup-schema.sh                    # Script para configurar el esquema credit
├── db/
│   └── init/
│       └── docker-entrypoint-initdb.sql  # Script de inicialización del esquema
├── docker-compose.yaml                # Comentado (no necesario)
└── README.md                          # Este archivo
```

## Configuración

### Base de Datos Compartida
- **Host**: `localhost`
- **Puerto**: `5432` (mismo que el proyecto de autenticación)
- **Base de datos**: `crediya`
- **Esquema**: `credit` (nuevo esquema)
- **Usuario**: `appuser`
- **Contraseña**: `appsecret`

### Esquemas en la Base de Datos
```
PostgreSQL (puerto 5432)
├── Base de datos: crediya
    ├── Esquema: auth (para autenticación)
    └── Esquema: credit (para credit requests) ← NUEVO
```

## Uso

### 1. Prerrequisito: Base de datos de autenticación corriendo
```bash
# Navegar al proyecto de autenticación
cd ../credi-ya-authentication/deployment

# Iniciar la base de datos
docker-compose up -d

# Verificar que esté corriendo
docker ps
```

### 2. Configurar el esquema credit
```bash
# Navegar al proyecto de credit requests
cd ../../credi-ya-credit-requests/deployment

# Ejecutar el script de configuración
./setup-schema.sh
```

### 3. Verificar la configuración
```bash
# Conectar a la base de datos
docker exec -it db-portalpagos psql -U appuser -d crediya

# Listar esquemas
\dn

# Listar tablas del esquema credit
\dt credit.*

# Ver datos de ejemplo
SELECT * FROM credit.loan_types;
SELECT * FROM credit.request_states;
SELECT * FROM credit.credit_applications;
```

## Esquema de Base de Datos

### Tablas

1. **`credit.loan_types`** - Tipos de préstamo disponibles
   - `id`: Identificador único
   - `name`: Nombre del tipo de préstamo
   - `minimum_amount`: Monto mínimo permitido
   - `maximum_amount`: Monto máximo permitido
   - `interest_rate`: Tasa de interés mensual
   - `automatic_validation`: Si se valida automáticamente

2. **`credit.request_states`** - Estados de las solicitudes
   - `id`: Identificador único
   - `name`: Nombre del estado
   - `description`: Descripción del estado

3. **`credit.credit_applications`** - Solicitudes de crédito
   - `id`: UUID único de la solicitud
   - `amount`: Monto solicitado
   - `month_term`: Plazo en meses
   - `email`: Email del solicitante
   - `document_number`: Número de documento
   - `loan_type_id`: Referencia al tipo de préstamo
   - `request_state_id`: Referencia al estado actual

## Datos de Ejemplo

El script de inicialización incluye:

- **4 tipos de préstamo**: Personal, Vehicular, Hipotecario, Microcrédito
- **5 estados de solicitud**: Pendiente, Aprobada, Rechazada, En Análisis, Cancelada
- **3 solicitudes de ejemplo** con diferentes configuraciones

## Ventajas de esta Configuración

- ✅ **Eficiencia**: Reutiliza la infraestructura existente
- ✅ **Consistencia**: Misma base de datos, esquemas separados
- ✅ **Simplicidad**: No hay conflictos de puertos
- ✅ **Mantenimiento**: Una sola instancia de PostgreSQL que gestionar
- ✅ **Escalabilidad**: Fácil agregar más esquemas para futuros microservicios

## Notas Importantes

- **IMPORTANTE**: Este proyecto se conecta a la misma instancia de PostgreSQL que el proyecto de autenticación
- El esquema `credit` es completamente independiente del esquema `auth`
- Los volúmenes y redes Docker no son necesarios para esta configuración
- Se incluyen índices para optimizar las consultas más comunes
