#!/bin/bash

echo "🔍 Verificando estado del esquema 'credit'..."

# Verificar si Docker está ejecutándose
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está ejecutándose."
    exit 1
fi

# Verificar si la base de datos está corriendo
if ! docker ps --format "table {{.Names}}" | grep -q "db-portalpagos"; then
    echo "❌ Error: La base de datos no está corriendo."
    echo "📝 Inicia la BD del proyecto de autenticación primero."
    exit 1
fi

echo "✅ Base de datos encontrada: db-portalpagos"

# Verificar si el esquema credit existe
echo "🔍 Verificando esquema 'credit'..."
if docker exec -i db-portalpagos psql -U appuser -d crediya -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'credit';" | grep -q "credit"; then
    echo "✅ Esquema 'credit' existe"
    
    # Contar tablas en el esquema
    table_count=$(docker exec -i db-portalpagos psql -U appuser -d crediya -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'credit';" | tr -d ' ')
    echo "📊 Tablas encontradas: $table_count"
    
    # Listar tablas
    echo "📋 Tablas en el esquema 'credit':"
    docker exec -i db-portalpagos psql -U appuser -d crediya -c "\dt credit.*"
    
    # Ver datos de ejemplo
    echo ""
    echo "📝 Datos de ejemplo en credit.loan_types:"
    docker exec -i db-portalpagos psql -U appuser -d crediya -c "SELECT id, name, minimum_amount, maximum_amount, interest_rate FROM credit.loan_types LIMIT 3;"
    
    echo ""
    echo "📝 Datos de ejemplo en credit.request_states:"
    docker exec -i db-portalpagos psql -U appuser -d crediya -c "SELECT id, name, description FROM credit.request_states;"
    
else
    echo "❌ Esquema 'credit' NO existe"
    echo "📝 Ejecuta './setup-schema.sh' para crearlo"
fi
