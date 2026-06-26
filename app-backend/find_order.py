import sqlite3
import sys

sys.stdout.reconfigure(encoding='utf-8')

def find_order():
    conn = sqlite3.connect('vuavuive_v2.db')
    cursor = conn.cursor()
    
    print("=== SEARCHING FOR THE SPECIFIC ORDER ===")
    cursor.execute("SELECT id, status, payment_status, shipper_id, delivery_phone, delivery_name FROM orders WHERE delivery_phone = '1131231' OR delivery_name LIKE '%quoc viet%'")
    orders = cursor.fetchall()
    if not orders:
        print("No matching order found by phone/name in PostgreSQL (SQLite).")
    for o in orders:
        print(f"ID: {o[0]} | Status: {o[1]} | Payment: {o[2]} | ShipperID: {o[3]} | Phone: {o[4]} | Name: {o[5]}")
        
    conn.close()

if __name__ == '__main__':
    find_order()
