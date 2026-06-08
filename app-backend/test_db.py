import sqlite3
import sys

db_path = 'vuavuive_v2.db'
print(f"Connecting to {db_path}...")
try:
    conn = sqlite3.connect(db_path, timeout=5)
    cursor = conn.cursor()
    cursor.execute('UPDATE products SET stock_quantity = stock_quantity WHERE id = "11111111-1111-1111-1111-111111110001"')
    conn.commit()
    print("SQLite Write SUCCESS")
except Exception as e:
    print("SQLite Write FAILED:", e)
    sys.exit(1)
finally:
    conn.close()
