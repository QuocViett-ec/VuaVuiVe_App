import sqlite3
import sys

sys.stdout.reconfigure(encoding='utf-8')

def check_users():
    conn = sqlite3.connect('vuavuive_v2.db')
    cursor = conn.cursor()
    
    print("=== USERS IN DB ===")
    cursor.execute("SELECT id, phone, email, full_name, role, is_active FROM users")
    users = cursor.fetchall()
    for u in users:
        print(f"ID: {u[0]} | Phone: {u[1]} | Email: {u[2]} | Name: {u[3]} | Role: {u[4]} | Active: {u[5]}")

    print("\n=== SHIPPERS IN DB ===")
    cursor.execute("SELECT id, user_id, full_name, phone, current_status, is_active FROM shippers")
    shippers = cursor.fetchall()
    for s in shippers:
        print(f"ID: {s[0]} | UserID: {s[1]} | Name: {s[2]} | Phone: {s[3]} | Status: {s[4]} | Active: {s[5]}")
        
    conn.close()

if __name__ == '__main__':
    check_users()
