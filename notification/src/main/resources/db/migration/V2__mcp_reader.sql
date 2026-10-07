DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mcp_reader') THEN
        CREATE ROLE mcp_reader LOGIN PASSWORD '${mcp_password}';
        ALTER ROLE mcp_reader SET default_transaction_read_only = on;
    END IF;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END
$$;

GRANT CONNECT ON DATABASE notification TO mcp_reader;
GRANT USAGE ON SCHEMA public TO mcp_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO mcp_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO mcp_reader;