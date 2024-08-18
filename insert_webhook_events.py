import psycopg2
from datetime import datetime, timedelta
import random
import uuid

# Database connection parameters
conn_params = {
    'dbname': 'cams',
    'user': 'postgres',
    'password': 'postgres',
    'host': 'localhost',
    'port': '5432'
}

def insert_webhook_events(conn):
    cur = conn.cursor()
    webhook_ids = ['webhook_1', 'webhook_2', 'webhook_3']  # Add more IDs as needed

    for _ in range(1000):  # Adjust the number of rows to insert
        webhook_id = random.choice(webhook_ids)
        timestamp = datetime.now() - timedelta(days=random.randint(0, 30))
        
        # Generate request_time and response_time, ensuring response_time >= request_time
        request_time = timedelta(milliseconds=random.randint(10, 500))
        response_time = request_time + timedelta(milliseconds=random.randint(0, 500))
        latency = response_time - request_time
        
        retry_count = random.randint(0, 5)
        is_success = random.choice([True, False])
        status_code = random.choice([200, 400, 500])
        error_msg = None if is_success else 'Error message'
        payload = 'Sample payload'

        cur.execute("""
            INSERT INTO webhook_events (
                id, webhook_id, timestamp, request_time, response_time, latency,
                retry_count, is_success, status_code, error_msg, payload
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (
            str(uuid.uuid4()), webhook_id, timestamp, request_time, response_time, latency,
            retry_count, is_success, status_code, error_msg, payload
        ))

    conn.commit()
    cur.close()

if __name__ == "__main__":
    conn = psycopg2.connect(**conn_params)
    insert_webhook_events(conn)
    conn.close()

