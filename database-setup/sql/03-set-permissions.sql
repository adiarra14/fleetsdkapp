-- Set proper ownership for TCP server database user
ALTER TABLE balises OWNER TO adminbdb;
ALTER TABLE balise_events OWNER TO adminbdb;
ALTER TABLE containers OWNER TO adminbdb;
ALTER TABLE assets OWNER TO adminbdb;

-- Grant necessary permissions for TCP server data transmission
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adminbdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adminbdb;
