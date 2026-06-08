import urllib.request
import xml.etree.ElementTree as ET
import os

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
        namespaces = {'ns': 'http://www.w3.org/2000/svg'}
        paths = root.findall('.//ns:path', namespaces)
        if not paths:
            paths = root.findall('.//path')
        if paths:
            return paths[0].attrib['d']
    except Exception as e:
        print(f"Error fetching {icon_name}: {e}")
    return None

path_data = fetch_svg_path('image')
if path_data:
    xml_content = template.replace("{path_data}", path_data)
    with open(os.path.join(out_dir, "ic_image.xml"), "w", encoding="utf-8") as f:
        f.write(xml_content)
    with open(os.path.join(admin_out_dir, "ic_image.xml"), "w", encoding="utf-8") as f:
        f.write(xml_content)
    print("Done downloading ic_image.xml")
