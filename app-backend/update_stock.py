import sqlite3

conn = sqlite3.connect('vuavuive_v2.db')
cursor = conn.cursor()

# Set stock of 'Rau mồng tơi (500g)' (id: 90000000-0000-0000-0000-000000000102) to 0
cursor.execute("UPDATE products SET stock_quantity = 0 WHERE id = '90000000-0000-0000-0000-000000000102'")

# Set stock of 'Cải bẹ xanh (500g)' (id: 90000000-0000-0000-0000-000000000101) to 2
cursor.execute("UPDATE products SET stock_quantity = 2 WHERE id = '90000000-0000-0000-0000-000000000101'")

# Set stock of 'Dua' (id: 6780f04e-5855-4032-99a8-6cd649da3057) to 0
cursor.execute("UPDATE products SET stock_quantity = 0 WHERE id = '6780f04e-5855-4032-99a8-6cd649da3057'")

conn.commit()
print("Updated stock quantities successfully.")

cursor.execute("SELECT id, name, stock_quantity FROM products WHERE id IN ('90000000-0000-0000-0000-000000000102', '90000000-0000-0000-0000-000000000101', '6780f04e-5855-4032-99a8-6cd649da3057')")
for row in cursor.fetchall():
    print(row)

conn.close()
