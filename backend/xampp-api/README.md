# FoodAI XAMPP API

Backend PHP/MySQL thay cho Firebase cho app DailyMealRecomment.

## Cài đặt nhanh

1. Bật Apache và MySQL trong XAMPP.
2. Mở phpMyAdmin, import file `schema.sql`.
3. Copy nội dung thư mục này vào:

   `C:\xampp\htdocs\foodai-api`

4. API base URL sẽ là:

   `http://localhost/foodai-api/api`

## Cấu hình Android

Trong `local.properties` của project Android:

```properties
# Emulator Android Studio
XAMPP_API_BASE_URL=http://10.0.2.2/foodai-api/api

# Điện thoại thật: đổi thành IP máy tính đang chạy XAMPP
# XAMPP_API_BASE_URL=http://192.168.1.10/foodai-api/api
```

Nếu dùng điện thoại thật, máy tính và điện thoại phải cùng Wi-Fi, Apache phải được firewall cho phép.

## Endpoint

- `POST /register.php`
- `POST /login.php`
- `POST /logout.php`
- `GET /profile.php`
- `POST /profile.php`
- `GET /meal-logs.php?dateKey=yyyy-MM-dd`
- `GET /meal-logs.php?dateKey=yyyy-MM-dd&mealType=BREAKFAST`
- `POST /meal-logs.php`
- `PUT /meal-logs.php`
- `DELETE /meal-logs.php?id=<meal_log_id>`

`mealType` chỉ nhận một trong bốn giá trị:

- `BREAKFAST`
- `LUNCH`
- `DINNER`
- `SNACK`

Các endpoint sau đăng nhập dùng header:

```http
Authorization: Bearer <token>
```

Backend cũng nhận `X-Auth-Token` hoặc query `_token` để app Android vẫn hoạt động ổn trong môi trường local/debug.
