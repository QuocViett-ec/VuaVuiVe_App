import sqlite3
import sys

# Set standard output encoding to UTF-8
sys.stdout.reconfigure(encoding='utf-8')

def inspect():
    conn = sqlite3.connect('vuavuive_v2.db')
    cursor = conn.cursor()
    
    print("=== SHIPPERS IN DB ===")
    try:
        cursor.execute("SELECT id, full_name, phone, current_status, is_active FROM shippers")
        shippers = cursor.fetchall()
        for s in shippers:
            print(f"ID: {s[0]} | Name: {s[1]} | Phone: {s[2]} | Status: {s[3]} | Active: {s[4]}")
    except Exception as e:
        print("Error reading shippers:", e)

    print("\n=== LATEST 10 ORDERS IN DB ===")
    try:
        cursor.execute("SELECT id, status, payment_status, shipper_id, created_at FROM orders ORDER BY created_at DESC LIMIT 10")
        orders = cursor.fetchall()
        for o in orders:
            print(f"ID: {o[0]} | Status: {o[1]} | Payment: {o[2]} | ShipperID: {o[3]} | CreatedAt: {o[4]}")
    except Exception as e:
        print("Error reading orders:", e)

    conn.close()

if __name__ == '__main__':
    inspect()
