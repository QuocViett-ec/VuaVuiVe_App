import sqlite3
import json

conn = sqlite3.connect('vuavuive_v2.db')
cur = conn.cursor()

cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
tables = [r[0] for r in cur.fetchall()]
print('TABLES:', tables)
print()

for t in tables:
    cur.execute(f'PRAGMA table_info("{t}")')
    cols = cur.fetchall()
    cur.execute(f'SELECT COUNT(*) FROM "{t}"')
    cnt = cur.fetchone()[0]
    print(f'=== {t} ({cnt} rows) ===')
    for c in cols:
        print(f'  col={c[1]} type={c[2]} notnull={c[3]} dflt={c[4]} pk={c[5]}')
    
    # Also get CREATE statement
    cur.execute(f"SELECT sql FROM sqlite_master WHERE name='{t}'")
    ddl = cur.fetchone()
    if ddl:
        print(f'  DDL: {ddl[0]}')
    print()

conn.close()
