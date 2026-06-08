import sqlite3

conn = sqlite3.connect('vuavuive_v2.db')
cursor = conn.cursor()

# Get tables
cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
tables = cursor.fetchall()
print("Tables:", [t[0] for t in tables])

# Get triggers
cursor.execute("SELECT name, tbl_name, sql FROM sqlite_master WHERE type='trigger'")
triggers = cursor.fetchall()
print("\nTriggers:")
for trigger in triggers:
    print(f"Trigger Name: {trigger[0]}, Table: {trigger[1]}")
    print(trigger[2])
    print("-" * 50)

# Print products columns
cursor.execute("PRAGMA table_info(products)")
print("\nProducts table info:")
for col in cursor.fetchall():
    print(col)

conn.close()
