-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_balise_device_id ON balises(device_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_latitude ON balise_events(latitude);
CREATE INDEX IF NOT EXISTS idx_balise_events_longitude ON balise_events(longitude);
CREATE INDEX IF NOT EXISTS idx_containers_container_number ON containers(container_number);
