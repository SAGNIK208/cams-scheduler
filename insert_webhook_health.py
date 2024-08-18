import psycopg2
from datetime import datetime, timedelta
import random

# Database connection parameters
conn_params = {
    'dbname': 'cams',
    'user': 'postgres',
    'password': 'postgres',
    'host': 'localhost',
    'port': '5432'
}

def insert_webhook_health(conn):
    cur = conn.cursor()
    webhook_ids = ['webhook_1', 'webhook_2', 'webhook_3']  # Add more IDs as needed
    inserted_values = set()  # Track inserted combinations

    for _ in range(30):  # Adjust the number of rows to insert
        while True:
            webhook_id = random.choice(webhook_ids)
            date = datetime.now() - timedelta(days=random.randint(0, 30))
            downtime = random.uniform(0, 120)  # Downtime in minutes

            # Check if combination already exists
            if (webhook_id, date.date()) not in inserted_values:
                break

        inserted_values.add((webhook_id, date.date()))  # Add to set after successful insertion

        cur.execute("""
            INSERT INTO webhook_health (webhook_id, date, downtime)
            VALUES (%s, %s, %s)
        """, (webhook_id, date.date(), downtime))

    conn.commit()
    cur.close()

if __name__ == "__main__":
    conn = psycopg2.connect(**conn_params)
    insert_webhook_health(conn)
    conn.close()
