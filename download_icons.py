import urllib.request
import xml.etree.ElementTree as ET
import os

icons = [
    ('ic_home', 'home'),
    ('ic_category', 'category'),
    ('ic_products', 'inventory_2'),
    ('ic_cart', 'shopping_cart'),
    ('ic_orders', 'receipt_long'),
    ('ic_account', 'person'),
    ('ic_person', 'person'),
    ('ic_arrow_back', 'arrow_back'),
    ('ic_add', 'add'),
    ('ic_delete', 'delete'),
    ('ic_search', 'search'),
    ('ic_send', 'send'),
    ('ic_close', 'close'),
    ('ic_info', 'info'),
    ('ic_chevron_right', 'chevron_right'),
    ('ic_chevron_left', 'chevron_left'),
    ('ic_email', 'mail'),
    ('ic_lock', 'lock'),
    ('ic_location', 'my_location'),
    ('ic_calendar', 'calendar_today'),
    ('ic_history', 'history'),
    ('ic_dashboard', 'dashboard'),
    ('ic_chatbot', 'smart_toy'),
    ('ic_vouchers', 'local_activity')
]

template = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="960"
    android:viewportHeight="960"
    android:tint="?attr/colorControlNormal">
  <path
      android:fillColor="@android:color/white"
      android:pathData="{path_data}"/>
</vector>
"""

out_dir = r"e:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-customer\src\main\res\drawable"
admin_out_dir = r"e:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-admin\src\main\res\drawable"

def fetch_svg_path(icon_name):
    url = f"https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/{icon_name}/default/24px.svg"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        xml_data = urllib.request.urlopen(req).read().decode('utf-8')
        root = ET.fromstring(xml_data)
        # Find all path elements (handle namespace)
        namespaces = {'ns': 'http://www.w3.org/2000/svg'}
        paths = root.findall('.//ns:path', namespaces)
        if not paths:
            # try without namespace
            paths = root.findall('.//path')
        if paths:
            return paths[0].attrib['d']
    except Exception as e:
        print(f"Error fetching {icon_name}: {e}")
    return None

for file_name, icon_name in icons:
    print(f"Fetching {icon_name}...")
    path_data = fetch_svg_path(icon_name)
    if path_data:
        xml_content = template.replace("{path_data}", path_data)
        
        # Save to app-customer
        customer_path = os.path.join(out_dir, f"{file_name}.xml")
        with open(customer_path, "w", encoding="utf-8") as f:
            f.write(xml_content)
            
        # Save to app-admin
        admin_path = os.path.join(admin_out_dir, f"{file_name}.xml")
        with open(admin_path, "w", encoding="utf-8") as f:
            f.write(xml_content)
        print(f"Saved {file_name}.xml")
    else:
        print(f"Failed to save {file_name}")

print("Done downloading icons.")
