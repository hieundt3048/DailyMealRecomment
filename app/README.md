# Ứng dụng Theo dõi Calo & Gợi ý Bữa ăn Thông minh

Ứng dụng di động giúp người dùng theo dõi lượng calo hằng ngày, nhận diện món ăn từ hình ảnh và nhận gợi ý bữa ăn phù hợp với mục tiêu cá nhân.

## Phạm vi đã chốt

Ứng dụng **chỉ tính toán, lưu trữ và hiển thị lượng calo**. Protein, Carbs, Fat và các chỉ số dinh dưỡng khác không thuộc phạm vi của dự án.

---

## Tính năng chính

### 1. Quản lý tài khoản & Cá nhân hóa
* **Đăng nhập linh hoạt:** Hỗ trợ đăng ký và đăng nhập hệ thống qua Email cá nhân hoặc tài khoản Google (OAuth2).
* **Cá nhân hóa chỉ số:** Nhập số đo cơ thể đầu vào gồm chiều cao, cân nặng.
* **Tùy chọn mục tiêu:** Lựa chọn linh hoạt giữa các mục tiêu: Tăng cân, giảm cân hoặc giữ dáng.
* **Chế độ ăn riêng biệt:** Hỗ trợ bộ lọc chế độ ăn thuần chay (Vegan) hoặc chế độ ăn bình thường.
* **Tự động tính toán:** Hệ thống tự động tính tổng lượng calo cần nạp hằng ngày theo hồ sơ và mục tiêu của người dùng.

### 2. Phân tích món ăn qua hình ảnh (AI Food Scanning)
* **Quét ảnh đa năng:** Chụp ảnh món ăn trực tiếp từ Camera ứng dụng hoặc chọn ảnh sẵn có từ Thư viện thiết bị.
* **Nhận diện bằng AI:** Tự động nhận diện các món ăn có trong ảnh, ước tính khối lượng (g) và lượng calo của từng món.
* **Chỉnh sửa linh hoạt:** Cho phép người dùng chủ động điều chỉnh lại khối lượng hoặc sửa tên món ăn trong trường hợp AI nhận diện chưa chuẩn xác trước khi lưu dữ liệu.

### 3. Nhật ký dinh dưỡng hằng ngày
* **Phân loại bữa ăn:** Lưu trữ và quản lý lịch sử ăn uống một cách khoa học theo các bữa: Sáng, Trưa, Chiều/Tối và Bữa phụ.
* **Dashboard trực quan:** Biểu đồ Dashboard dạng vòng tròn hiển thị thời gian thực tỷ lệ giữa lượng calo đã nạp so với lượng calo mục tiêu trong ngày.

### 4. Hệ thống gợi ý thực đơn thông minh
* **Tính toán calo thiếu hụt:** Tự động tính toán số calo còn thiếu trong ngày dựa trên công thức thực tế:
  $$\text{Calo còn lại} = \text{Calo mục tiêu} - \text{Calo đã nạp}$$
* **Gợi ý thực đơn:** Đề xuất danh sách từ 3 - 5 món ăn hoặc thực đơn phù hợp nhất với lượng calo còn thiếu và chế độ ăn của người dùng.
* **Chi tiết món ăn:** Hiển thị lượng calo, nguyên liệu và các bước chế biến; không yêu cầu bảng Protein, Carbs hoặc Fat.

---

## Công nghệ sử dụng (Dự kiến)

* **Frontend:** Mobile App
* **Backend:** Kotlin
* **Database:** FireBase
* **AI Service:** Integration với các mô hình nhận diện hình ảnh món ăn (Computer Vision API)
* **Authentication:** Google OAuth2

---
