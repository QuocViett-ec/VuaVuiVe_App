# UI Design Constraints - Vựa Vui Vẻ App

## Purpose

Tài liệu này định nghĩa design system chung cho app Vựa Vui Vẻ. Khi chỉnh UI, Antigravity phải dùng các constraints này làm chuẩn visual, nhưng vẫn giữ nguyên logic, ID và cấu trúc XML hiện tại.

Project hiện tại dùng Android XML layout + Java/Kotlin.

Không dùng Jetpack Compose.
Không copy XML/Compose generated code.
Không hardcode dữ liệu mẫu từ Figma/Stitch.
Không ép pixel-perfect bằng fixed width/height/absolute offset.

---

## Global Design Direction

Phong cách tổng thể:

* Tươi & Tận Tâm
* Grocery app Việt Nam
* Sạch, sáng, dễ đọc
* Nền kem ấm
* Xanh lá là màu nhận diện chính
* Card bo góc mềm
* Spacing thoáng
* Product grid 2 cột khi phù hợp
* Ngôn ngữ giao diện: Tiếng Việt có dấu đầy đủ

---

## Color Palette

```txt
Background / Neutral: #FCF7E4
Primary Green: #1B5E20
Secondary Green: #2E7D32
Tertiary Green: #66BB6A
Card White: #FFFFFF
Text Primary: #1D1C11
Text Secondary: #41493E
Muted Text: #6B7280
Discount Red: #DC2626
Warning Yellow: #FFD700
```

Android resource naming suggestion:

```xml
<color name="vvv_background">#FCF7E4</color>
<color name="vvv_primary">#1B5E20</color>
<color name="vvv_secondary">#2E7D32</color>
<color name="vvv_tertiary">#66BB6A</color>
<color name="vvv_card_white">#FFFFFF</color>
<color name="vvv_text_primary">#1D1C11</color>
<color name="vvv_text_secondary">#41493E</color>
<color name="vvv_text_muted">#6B7280</color>
<color name="vvv_discount_red">#DC2626</color>
<color name="vvv_warning_yellow">#FFD700</color>
```

---

## Typography

Preferred style:

* Font: Plus Jakarta Sans if already available or easy to add safely.
* If font resource is not available, use default Android sans-serif and keep TODO.
* Headline: bold, clear, friendly.
* Body: readable, not too small.
* Label: compact but still readable.

Suggested sizes:

```txt
Headline: 18sp - 22sp
Section title: 16sp - 18sp
Product name: 14sp - 15sp
Body: 13sp - 14sp
Label/caption: 10sp - 12sp
Price: 18sp - 20sp, bold
Bottom nav label: 10sp - 12sp
```

---

## Layout Constraints

Target frame reference:

```txt
Android mobile frame: 390x844
```

Important:

* Do not hardcode the whole layout to exactly 390dp width.
* Use match_parent, constraint, weight, RecyclerView/GridLayoutManager where appropriate.
* 390x844 is only the design preview target, not a fixed layout size.

Product layout:

* Product grid should use 2 columns where applicable.
* Product cards should be responsive inside RecyclerView.
* Avoid absolute offset positioning.

---

## Components

### Button

Button style:

* Primary button: green `#1B5E20`
* Text/icon: white
* Corner radius: 8dp - 16dp
* Height: 40dp - 52dp depending on context

### Search Bar

Search style:

* Background: light cream / white
* Border: subtle neutral stroke
* Corner radius: 16dp - 24dp
* Search icon left
* Placeholder in Vietnamese
* Height around 44dp - 52dp

### Bottom Navigation

Bottom nav style:

* Background: cream / off-white
* Active icon: primary green
* Active item background: light green `#A0F399` or similar
* Inactive icon/text: muted green-gray
* Labels in Vietnamese:

  * Trang chủ
  * Danh mục
  * Công thức / Đơn hàng depending on current app navigation
  * Giỏ hàng
  * Cá nhân / Tài khoản

Do not change navigation IDs unless explicitly required.

### Product Card

Product card style:

* White card
* Rounded corners 12dp - 16dp
* Light elevation/shadow
* Product image area with light cream/green tint
* Product image rounded top corners
* Product name max 2 lines
* Rating small and compact
* Price green, bold
* Unit text smaller
* Discount badge red if available
* Quick add button green with white plus/cart icon

Do not change ProductAdapter logic or product card IDs.

---

## Language

All visible UI copy should be Vietnamese with proper accents.

Examples:

```txt
Trang chủ
Danh mục
Công thức
Giỏ hàng
Cá nhân
Tìm kiếm trái cây, rau củ...
Thêm vào giỏ
Xem tất cả
Chào mừng bạn đến với
VỰA VUI VẺ
```

---

## Safety Rules

When applying this design system:

1. Read `docs/ui_current_app_audit.md`.
2. Read `docs/ui_figma_mapping.md`.
3. Keep all IDs used by Java/Kotlin.
4. Do not change Activity/Fragment/Adapter/ViewModel/Repository/API logic.
5. Do not replace full XML layout unless explicitly approved.
6. Only style current XML step by step.
7. Build after each checkpoint:

```bash
./gradlew :app-customer:assembleDebug
```
