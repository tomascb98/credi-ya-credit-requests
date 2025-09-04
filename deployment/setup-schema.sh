#!/bin/bash

echo "🔧 Configurando esquema 'credit' en la base de datos existente..."

# Verificar si Docker está ejecutándose
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está ejecutándose. Por favor inicia Docker primero."
    exit 1
fi

# Verificar si la base de datos del proyecto de autenticación está corriendo
if ! docker ps --format "table {{.Names}}" | grep -q "db-portalpagos"; then
    echo "❌ Error: La base de datos del proyecto de autenticación no está corriendo."
    echo "📝 Por favor inicia primero la BD del proyecto de autenticación:"
    echo "   cd ../credi-ya-authentication/deployment"
    echo "   docker-compose up -d"
    exit 1
fi

echo "✅ Base de datos encontrada: db-portalpagos"

# Verificar si el esquema credit ya existe
echo "🔍 Verificando si el esquema 'credit' ya existe..."
if docker exec -i db-portalpagos psql -U appuser -d crediya -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'credit';" | grep -q "credit"; then
    echo "⚠️  El esquema 'credit' ya existe. ¿Deseas recrearlo? (y/N)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo "🗑️  Eliminando esquema existente..."
        docker exec -i db-portalpagos psql -U appuser -d crediya -c "DROP SCHEMA IF EXISTS credit CASCADE;"
    else
        echo "✅ Esquema mantenido. No se realizaron cambios."
        exit 0
    fi
fi

# Ejecutar el script SQL para crear el esquema
echo "📝 Ejecutando script de inicialización..."
docker exec -i db-portalpagos psql -U appuser -d crediya < db/init/docker-entrypoint-initdb.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Esquema 'credit' configurado exitosamente!"
    echo ""
    echo "📊 Información de conexión:"
    echo "   - PostgreSQL: localhost:5432"
    echo "   - Base de datos: crediya"
    echo "   - Esquema: credit"
    echo "   - Usuario: appuser"
    echo "   - Contraseña: appsecret"
    echo ""
    echo "🔍 Para verificar las tablas creadas:"
    echo "   docker exec -it db-portalpagos psql -U appuser -d crediya -c \"\\dt credit.*\""
    echo ""
    echo "📝 Para ver datos de ejemplo:"
    echo "   docker exec -it db-portalpagos psql -U appuser -d crediya -c \"SELECT * FROM credit.loan_types;\""
else
    echo "❌ Error al ejecutar el script SQL"
    exit 1
fi
