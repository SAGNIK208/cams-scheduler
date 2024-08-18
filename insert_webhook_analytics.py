import psycopg2
import random
from datetime import datetime

# Database connection parameters
conn_params = {
    'dbname': 'cams',
    'user': 'postgres',
    'password': 'postgres',
    'host': 'localhost',
    'port': '5432'
}

def insert_webhook_analytics(conn):
    cur = conn.cursor()
    webhook_ids = ['webhook_1', 'webhook_2', 'webhook_3']  # Add more IDs as needed

    # Fixed old timestamp
    last_aggregation_time = datetime(2020, 1, 1, 0, 0, 0)  # Example old timestamp

    for webhook_id in webhook_ids:
        success_rate = round(random.uniform(70, 100), 2)  # Success rate in percentage
        avg_latency = round(random.uniform(10, 500), 3)  # Average latency in milliseconds
        retry_rate = round(random.uniform(0, 10), 2)  # Retry rate in percentage
        total_events = random.randint(100, 1000)
        health = round(random.uniform(0, 999.999), 3)  # Health score

        cur.execute("""
            INSERT INTO webhook_analytics (
                webhook_id, success_rate, avg_latency, retry_rate, total_events, health, last_aggregation_time
            ) VALUES (%s, %s, %s, %s, %s, %s, %s)
        """, (webhook_id, success_rate, avg_latency, retry_rate, total_events, health, last_aggregation_time))

    conn.commit()
    cur.close()

if __name__ == "__main__":
    conn = psycopg2.connect(**conn_params)
    insert_webhook_analytics(conn)
    conn.close()

