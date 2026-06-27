import urllib.request
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

url = "https://vua-vui-ve-default-rtdb.firebaseio.com/products.json"
req = urllib.request.Request(url)
response = urllib.request.urlopen(req, context=ctx)
data = json.loads(response.read().decode('utf-8'))

for k, v in data.items():
    if "Bí Đỏ" in v.get("name", ""):
        print(f"Activating {v['name']} ({k})")
        update_url = f"https://vua-vui-ve-default-rtdb.firebaseio.com/products/{k}.json"
        req_patch = urllib.request.Request(update_url, data=json.dumps({"is_active": True}).encode('utf-8'), method='PATCH')
        urllib.request.urlopen(req_patch, context=ctx)
