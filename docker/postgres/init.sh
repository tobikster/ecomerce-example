#!/bin/bash
set -e

create_database() {
	local db="${1}"
	if [ -n "${db}" ]; then
		echo "Creating database '${db}' if it does not exist already..."
		psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname "postgres" <<-EOSQL
			SELECT 'CREATE DATABASE "${db}"'
			WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${db}')\gexec
		EOSQL
	fi
}

create_database "${POSTGRES_DB_ORDERS}"
create_database "${POSTGRES_DB_PAYMENTS}"
