import sqlite3
import sys

sys.stdout.reconfigure(encoding='utf-8')

def print_schema():
    conn = sqlite3.connect('vuavuive_v2.db')
    cursor = conn.cursor()
    cursor.execute("PRAGMA table_info(orders)")
    cols = cursor.fetchall()
    for col in cols:
        print(col)
    conn.close()

if __name__ == '__main__':
    print_schema()
