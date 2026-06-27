import sqlite3
import json

conn = sqlite3.connect('app-backend/vuavuive_v2.db')
cursor = conn.cursor()
cursor.execute("SELECT id, full_name, email, phone, role, password_hash, is_active FROM users")
rows = cursor.fetchall()
users = []
for r in rows:
    users.append({
        "id": r[0],
        "full_name": r[1],
        "email": r[2],
        "phone": r[3],
        "role": r[4],
        "password_hash": r[5],
        "is_active": bool(r[6])
    })

print(json.dumps(users, indent=2, ensure_ascii=False))
conn.close()
