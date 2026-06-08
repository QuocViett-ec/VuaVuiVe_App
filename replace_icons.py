import os
import re

replacements = {
    r"@android:drawable/ic_menu_today": r"@drawable/ic_home",
    r"@android:drawable/ic_menu_gallery": r"@drawable/ic_image", # default, or we can use specific
    r"@android:drawable/ic_menu_add": r"@drawable/ic_add",
    r"@android:drawable/ic_menu_recent_history": r"@drawable/ic_orders",
    r"@android:drawable/ic_menu_myplaces": r"@drawable/ic_account",
    r"@android:drawable/ic_input_add": r"@drawable/ic_add",
    r"@android:drawable/ic_menu_info_details": r"@drawable/ic_info",
    r"@android:drawable/ic_media_next": r"@drawable/ic_chevron_right",
    r"@android:drawable/ic_media_previous": r"@drawable/ic_chevron_left",
    r"@android:drawable/ic_input_delete": r"@drawable/ic_delete",
    r"@android:drawable/ic_menu_close_clear_cancel": r"@drawable/ic_close",
    r"@android:drawable/ic_menu_search": r"@drawable/ic_search",
    r"@android:drawable/ic_menu_send": r"@drawable/ic_send",
    r"@android:drawable/ic_dialog_email": r"@drawable/ic_email",
    r"@android:drawable/ic_lock_lock": r"@drawable/ic_lock",
    r"@android:drawable/ic_menu_mylocation": r"@drawable/ic_location",
    r"android\.R\.drawable\.ic_menu_gallery": r"R.drawable.ic_image",
}

# Special fix for bottom nav menu
bottom_nav_replacements = {
    r"@drawable/ic_image": r"@drawable/ic_products",
    r"@drawable/ic_add": r"@drawable/ic_cart"
}

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content
    for old, new in replacements.items():
        new_content = re.sub(old, new, new_content)

    if "bottom_nav_menu.xml" in filepath:
        for old, new in bottom_nav_replacements.items():
            new_content = re.sub(old, new, new_content)

    if content != new_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filepath}")

for root_dir in [r"e:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-customer\src\main", r"e:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-admin\src\main"]:
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(".xml") or filename.endswith(".java") or filename.endswith(".kt"):
                process_file(os.path.join(dirpath, filename))

print("Done replacing references.")
