steps to setup cams-schedular

1. install timescale db 
https://docs.timescale.com/self-hosted/latest/install/

-- Create the webhook_events table with latencyCREATETABLE webhook_events (
    id SERIAL PRIMARY KEY,
    webhook_id TEXT NOTNULL,
    timestamp TIMESTAMPTZ NOTNULL,
    request_time INTERVALNOTNULL,
    response_time INTERVALNOTNULL,
    latency INTERVALNOTNULL,  -- Added latency column
    retry_count INTNOTNULL,
    is_success BOOLEANNOTNULL,
    status_code INT,
    error_msg TEXT,
    payload TEXT
);

-- Convert the table into a TimescaleDB hypertableSELECT create_hypertable('webhook_events', 'timestamp');

-- Create indexes-- 1. Index on timestamp for efficient time-based queriesCREATE INDEX ON webhook_events (timestampDESC);

-- Index on webhook_id for quick lookups by webhookCREATE INDEX ON webhook_events (webhook_id);

-- Composite index on is_success, webhook_id, and timestamp for combined queriesCREATE INDEX ON webhook_events (is_success, webhook_id, timestampDESC);

source myenv/bin/activate(create virtual env myenv for pip)
pip install cassandra-driver
run python script in repo to get dummy data in cassandra
create new db in mysql cams;
CREATE TABLE IF NOT EXISTS webhook_aggregates (
    source VARCHAR(255),
    webhook_id VARCHAR(255),
    success_rate DOUBLE,
    latency BIGINT,
    uptime DOUBLE,
    retry_rate DOUBLE,
    total_events INT,
    health VARCHAR(50),
    PRIMARY KEY (source, webhook_id)
);
CREATE INDEX idx_source ON webhook_aggregates (source);
CREATE INDEX idx_webhook_id ON webhook_aggregates (webhook_id);
CREATE INDEX idx_source_webhook ON webhook_aggregates (source, webhook_id);









CREATE DATABASE cams;
\c cams;
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE TABLE webhook_events (
    id TEXT,
    webhook_id TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    request_time INTERVAL NOT NULL,
    response_time INTERVAL NOT NULL,
    latency INTERVAL NOT NULL,
    retry_count INT NOT NULL,
    is_success BOOLEAN NOT NULL,
    status_code INT,
    error_msg TEXT,
    payload TEXT
);
SELECT create_hypertable('webhook_events', 'timestamp');
ALTER TABLE webhook_events ADD PRIMARY KEY (id, timestamp);
CREATE INDEX ON webhook_events (webhook_id, timestamp);
ALTER TABLE webhook_analytics
ADD COLUMN last_aggregation_time TIMESTAMPTZ;


CREATE TABLE webhook_health (
  webhook_id TEXT NOT NULL,
  date DATE NOT NULL,
  downtime NUMERIC(10,2), -- Store downtime in minutes with two decimal places
  PRIMARY KEY (webhook_id, date)
);

CREATE TABLE webhook_analytics (
  webhook_id TEXT NOT NULL PRIMARY KEY,
  success_rate NUMERIC(5,2), -- Percentage, e.g., 95.23
  avg_latency NUMERIC(10,3), -- Average latency in milliseconds
  retry_rate NUMERIC(5,2), -- Percentage, e.g., 2.50
  total_events INT,
  health NUMERIC(6,3) -- Health score, e.g., 0.000 to 999.999
);
