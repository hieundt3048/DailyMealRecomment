# Checklist tiến độ dự án DailyMealRecomment

Ngày đánh giá: 24/06/2026  
Nguồn đánh giá: `DailyMealRecomment.zip`, nhánh `master`, commit `c1adf80`

## Kết luận nhanh

- Trạng thái build: **`assembleDebug` thành công** ngày 24/06/2026.
- Hoàn thành, chạy được từ đầu đến cuối: **0/20 yêu cầu**.
- Đã có mã hoặc giao diện một phần: **13/20 yêu cầu**.
- Chưa có phần triển khai đáng kể: **7/20 yêu cầu**.
- APK mới đã được build lại từ mã nguồn sạch; unit test và APK instrumentation test cũng đã biên dịch thành công.
- Smoke test giao diện chưa thể thực thi vì máy hiện không có thiết bị Android hoặc AVD kết nối.

Quy ước: `[x]` là phần đã có bằng chứng trong mã nguồn; `[ ]` là phần còn phải làm hoặc chưa kiểm thử được.

## Phạm vi yêu cầu đã sửa

- [x] Chỉ tính tổng lượng calo mục tiêu hằng ngày.
- [x] Khi phân tích món ăn, chỉ cần tên món, khối lượng ước tính và lượng calo.
- [x] Dashboard chỉ so sánh calo đã nạp với calo mục tiêu.
- [x] Trang chi tiết món gợi ý chỉ cần calo, nguyên liệu và cách chế biến.
- [x] Loại khỏi phạm vi: Protein, Carbs, Fat và các chỉ số dinh dưỡng khác.
- [x] Xóa các trường Protein, Carbs và Fat khỏi model, adapter, màn hình phân tích và Dashboard đang sử dụng.

## Việc cần xử lý trước khi phát triển tiếp

- [x] Giải quyết toàn bộ dấu `<<<<<<<`, `=======`, `>>>>>>>` trong:
  - `build.gradle.kts`
  - `gradle/libs.versions.toml`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/example/dailymealrecomment/MainActivity.kt`
  - `app/src/main/java/com/example/dailymealrecomment/CameraActivity.kt`
  - `app/src/main/res/layout/activity_main.xml`
- [x] Thống nhất package `com.example.dailymealrecomment`; sửa các import nhầm từ `com.example.btl`.
- [x] Bổ sung và thống nhất plugin/dependency cho Kotlin tích hợp trong AGP, View Binding, Firebase, Google Identity, CameraX và RecyclerView.
- [x] Chỉ giữ một `LoginActivity` với luồng Google ID token → Firebase Auth.
- [x] Build lại `assembleDebug` thành công từ mã nguồn sạch.
- [x] Biên dịch ba smoke test cho luồng đăng nhập → hồ sơ → trang chính → camera/thư viện → kết quả.
- [ ] Chạy ba smoke test trên thiết bị/emulator; hiện ADB không có thiết bị và máy không có AVD.

## Kết quả build và test gần nhất

- [x] `:app:assembleDebug` — thành công.
- [x] `:app:testDebugUnitTest` — thành công, gồm kiểm thử thuật toán calo theo mục tiêu và ngưỡng tối thiểu.
- [x] `:app:assembleDebugAndroidTest` — thành công, ba smoke test giao diện đã biên dịch.
- [x] `:app:lintDebug` — thành công, 0 lỗi và 80 cảnh báo không chặn build.
- [ ] `:app:connectedDebugAndroidTest` — chưa chạy vì không có thiết bị/emulator kết nối.
- [ ] Xác nhận đăng nhập Google thật: `google-services.json` hiện chưa chứa OAuth client; cần tải lại tệp này từ Firebase sau khi thêm SHA fingerprint và bật Google provider.

## Checklist theo yêu cầu

### 1. Đăng nhập qua Google — Đã triển khai, chờ cấu hình Firebase và kiểm thử thiết bị

- [x] Có nút Google trong giao diện đăng nhập.
- [x] Có mã Google Credential Manager trong `LoginActivity.kt`.
- [x] Đồng bộ ID nút và chỉ giữ một `LoginActivity`.
- [x] Đổi Google ID token thành Firebase credential bằng `GoogleAuthProvider`.
- [x] Xử lý hủy chọn tài khoản, lỗi mạng và trạng thái đang tải.
- [ ] Kiểm thử đăng nhập/đăng xuất trên thiết bị.

### 2. Form chiều cao, cân nặng — Đang làm

- [x] Có trường chiều cao và cân nặng trong Profile.
- [x] Có mã lưu chiều cao, cân nặng vào Firestore.
- [x] Kiểm tra khoảng giá trị hợp lệ và hiển thị lỗi tại trường nhập.
- [x] Tải lại dữ liệu đã lưu khi người dùng mở hồ sơ.
- [ ] Kiểm thử luồng nhập, lưu và sửa dữ liệu.

### 3. Lựa chọn mục tiêu tăng/giảm/giữ cân — Đang làm

- [x] Có ba lựa chọn trên giao diện Profile.
- [x] Có enum mục tiêu và nhánh điều chỉnh calo trong thuật toán.
- [x] Lưu và tải lại mục tiêu của người dùng.
- [x] Kết nối lựa chọn mục tiêu với luồng hồ sơ chính.
- [ ] Kiểm thử đủ ba mục tiêu.

### 4. Lựa chọn chế độ ăn Vegan/Bình thường — Đang làm

- [x] Có hai lựa chọn trên giao diện Profile.
- [x] Có enum `VEGAN` và `NORMAL`.
- [x] Lưu và tải lại chế độ ăn.
- [ ] Dùng chế độ ăn để lọc kết quả gợi ý.
- [ ] Kiểm thử cả hai chế độ.

### 5. Tính tổng lượng calo cần nạp hằng ngày — Đang làm

- [x] Có thuật toán Mifflin–St Jeor và điều chỉnh ±500 kcal theo mục tiêu.
- [x] Sửa package/import để thuật toán biên dịch được.
- [x] Kết nối kết quả tính toán với hồ sơ người dùng và Dashboard.
- [x] Lưu `dailyCalorieTarget` vào cơ sở dữ liệu.
- [ ] Chốt dữ liệu đầu vào: mã hiện tại cần tuổi, giới tính và mức vận động; mức vận động đang bị cố định ở `1.2`.
- [x] Viết kiểm thử đơn vị cho tăng cân, giảm cân, giữ cân và giá trị biên.
- [x] Chỉ trả về calo; không tạo mục tiêu Protein/Carbs/Fat.

### 6. Giao diện Camera/Thư viện và kết quả phân tích — Đang làm

- [x] Có giao diện Camera.
- [x] Có nút Camera và Gallery trên giao diện trang chính.
- [x] Có màn hình hiển thị ảnh và danh sách món nhận diện.
- [x] Có trường sửa tên món, khối lượng và calo.
- [x] Xóa trường Protein, Carbs và Fat khỏi màn hình kết quả.
- [ ] Thêm trạng thái đang phân tích, lỗi, không nhận diện được và thử lại.
- [x] Biên dịch tài nguyên và smoke test bố cục sau khi giải quyết xung đột merge.

### 7. Chụp ảnh trực tiếp từ Camera — Đang làm

- [x] Có mã CameraX để xem trước và chụp ảnh.
- [x] Có mã chuyển URI ảnh chụp sang màn hình phân tích.
- [x] Giải quyết xung đột trong `CameraActivity.kt` và Manifest.
- [x] Khai báo dependency/quyền truy cập tương thích với các phiên bản Android mục tiêu.
- [ ] Kiểm thử cấp/từ chối quyền, chụp ảnh và mở ảnh kết quả.

### 8. Chọn ảnh từ Thư viện — Đang làm

- [x] Có launcher `GetContent` chọn `image/*`.
- [x] Có mã chuyển URI ảnh được chọn sang màn hình phân tích.
- [x] Giải quyết xung đột trong `MainActivity.kt` và `activity_main.xml`.
- [ ] Kiểm thử chọn ảnh, hủy chọn và đọc URI sau khi Activity đổi trạng thái.

### 9. Kết nối API/Mô hình AI nhận diện món ăn — Chưa làm

- [ ] Chọn dịch vụ AI và định nghĩa hợp đồng đầu ra: tên món, khối lượng (g), calo.
- [ ] Gửi ảnh tới dịch vụ AI an toàn; không nhúng khóa bí mật trực tiếp trong ứng dụng.
- [ ] Parse và kiểm tra dữ liệu AI trả về.
- [ ] Xử lý loading, timeout, lỗi mạng và kết quả không chắc chắn.
- [ ] Thay dữ liệu mock trong `FoodAnalysisActivity.kt` bằng kết quả thật.

### 10. Ước tính khối lượng và calo từ kết quả AI — Đang làm

- [x] Model hiện có trường khối lượng và calo.
- [x] Giao diện hiện có thể hiển thị khối lượng và calo.
- [ ] Xây dựng logic nhận/ước tính khối lượng từ AI.
- [ ] Tính calo tương ứng với khối lượng hoặc nhận calo đã chuẩn hóa từ API.
- [x] Xóa Protein, Carbs và Fat khỏi `FoodItem`, `User`, adapter và layout.
- [ ] Kiểm thử làm tròn, dữ liệu thiếu và giá trị bất hợp lý.

### 11. Người dùng điều chỉnh khối lượng — Đang làm

- [x] Trường khối lượng có thể chỉnh sửa và cập nhật model trong bộ nhớ.
- [ ] Khi đổi khối lượng, tự động tính lại lượng calo.
- [ ] Chặn số âm, số 0 và giá trị vượt giới hạn hợp lý.
- [ ] Lưu khối lượng đã sửa vào nhật ký.

### 12. Người dùng sửa tên món ăn — Đang làm

- [x] Trường tên món có thể chỉnh sửa và cập nhật model trong bộ nhớ.
- [ ] Kiểm tra tên rỗng và chuẩn hóa khoảng trắng.
- [ ] Lưu tên đã sửa vào nhật ký.

### 13. Lưu món ăn sau khi quét/sửa vào nhật ký — Chưa làm

- [ ] Thiết kế model bản ghi món ăn chỉ gồm thông tin cần thiết cho calo.
- [ ] Cho người dùng chọn bữa ăn trước khi lưu.
- [ ] Ghi bản ghi vào Firestore/nguồn dữ liệu chính.
- [ ] Hiển thị thành công/thất bại dựa trên kết quả ghi thật.
- [ ] Thay nút Save hiện chỉ hiện Toast bằng thao tác lưu thực tế.

### 14. Giao diện nhật ký theo bữa và Dashboard — Đang làm

- [x] Có giao diện Dashboard calo và danh sách `Today's Log` dạng mock.
- [x] Có adapter hiển thị tên món, bữa, khối lượng và calo.
- [ ] Thiết kế/triển khai các nhóm Sáng, Trưa, Chiều/Tối, Bữa phụ.
- [ ] Thay dữ liệu mock bằng dữ liệu theo ngày từ cơ sở dữ liệu.
- [ ] Thêm trạng thái rỗng và chuyển ngày.

### 15. Cơ sở dữ liệu lịch sử theo loại bữa — Chưa làm

- [ ] Chốt schema người dùng/ngày/bữa/bản ghi món ăn.
- [ ] Tạo repository đọc, thêm, sửa và xóa bản ghi.
- [ ] Phân loại `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`.
- [ ] Truy vấn lịch sử theo ngày và theo bữa.
- [ ] Thiết lập quy tắc bảo mật để người dùng chỉ truy cập dữ liệu của mình.

### 16. Dashboard tỷ lệ calo đã nạp/calo mục tiêu — Đang làm

- [x] Có ProgressBar vòng tròn và các nhãn calo trên giao diện.
- [x] Xóa các ô Protein, Carbs và Fat.
- [ ] Tính `caloĐãNạp = tổng calo các món trong ngày`.
- [ ] Tính tỷ lệ tiến độ có giới hạn hiển thị hợp lý khi vượt mục tiêu.
- [ ] Thay các giá trị hard-code `40%` và `1420` bằng dữ liệu thật.
- [ ] Cập nhật Dashboard ngay sau khi thêm/sửa/xóa món.

### 17. Danh sách thực đơn gợi ý và trang chi tiết — Chưa làm

- [ ] Thiết kế màn hình danh sách 3–5 món/thực đơn gợi ý.
- [ ] Thiết kế màn hình chi tiết món.
- [ ] Hiển thị tên món, khẩu phần, calo và mức phù hợp với calo còn lại.
- [ ] Lọc theo Vegan/Bình thường.

### 18. Tính số calo còn lại trong ngày — Chưa làm

- [ ] Tính `caloCònLại = max(caloMụcTiêu - caloĐãNạp, 0)`.
- [ ] Cập nhật khi nhật ký hoặc mục tiêu thay đổi.
- [ ] Viết kiểm thử cho chưa ăn, ăn một phần, đạt mục tiêu và vượt mục tiêu.

### 19. Gợi ý 3–5 món phù hợp với calo còn lại — Chưa làm

- [ ] Chuẩn bị nguồn dữ liệu món ăn có calo, khẩu phần và loại chế độ ăn.
- [ ] Xây dựng thuật toán xếp hạng theo độ lệch so với calo còn lại.
- [ ] Trả về 3–5 lựa chọn hợp lệ, không lặp và đúng chế độ ăn.
- [ ] Xử lý trường hợp calo còn lại bằng 0 hoặc không có món phù hợp.
- [ ] Viết kiểm thử thuật toán gợi ý.

### 20. Trang chi tiết món ăn gợi ý — Chưa làm

- [ ] Hiển thị tên món, khẩu phần và tổng calo.
- [ ] Hiển thị danh sách nguyên liệu.
- [ ] Hiển thị các bước chế biến chi tiết.
- [ ] Không hiển thị hoặc yêu cầu Protein, Carbs, Fat.
- [ ] Cho phép quay lại danh sách và chọn món khác.

## Cập nhật phiên đăng nhập và giao diện — 24/06/2026

- [x] Màn hình mở đầu kiểm tra Firebase Auth; người chưa đăng nhập luôn được đưa tới trang đăng nhập Google.
- [x] Duy trì phiên đăng nhập sau khi đóng/mở lại ứng dụng cho tới khi người dùng chủ động đăng xuất.
- [x] Điều hướng người đã đăng nhập nhưng chưa hoàn tất hồ sơ tới form chiều cao, cân nặng và các chỉ số cần thiết.
- [x] Điều hướng thẳng tới trang chính khi phiên đăng nhập và hồ sơ đã hoàn tất.
- [x] Lưu hồ sơ vào Firestore và bộ nhớ cục bộ để khôi phục nhanh thông tin cùng mục tiêu calo.
- [x] Đăng xuất xóa Firebase Auth, thông tin Credential Manager và bộ nhớ phiên cục bộ.
- [x] Thiết kế lại trang đăng nhập, hồ sơ và trang chính theo Material Design 3 với xanh lục làm màu chủ đạo.
- [x] Thuật toán hồ sơ chỉ tính tổng lượng calo cần nạp hằng ngày; không yêu cầu Protein, Carbs hoặc Fat.
- [x] Build `assembleDebug`, unit test và lint thành công; 5/5 unit test đạt, lint có 0 lỗi.
- [ ] Chạy smoke test trực tiếp trên máy/giả lập Android cho luồng đăng nhập → hồ sơ → trang chính → camera/thư viện → kết quả (chưa có thiết bị hoặc Android SDK CLI khả dụng trong phiên kiểm tra).
- [ ] Cấu hình đăng nhập Google thật: bật Google provider, thêm SHA-1/SHA-256 và tải lại `google-services.json`; file hiện tại chưa chứa OAuth client nên chưa thể nghiệm thu đăng nhập Google trên thiết bị.

## Mốc nghiệm thu đề xuất

- [ ] Mốc 1 — Dự án build sạch, đăng nhập và hồ sơ hoạt động.
- [ ] Mốc 2 — Tính calo mục tiêu và Dashboard dùng dữ liệu thật.
- [ ] Mốc 3 — Camera/thư viện, AI, sửa kết quả và lưu nhật ký hoạt động đầu-cuối.
- [ ] Mốc 4 — Nhật ký theo bữa và lịch sử theo ngày hoàn chỉnh.
- [ ] Mốc 5 — Gợi ý 3–5 món theo calo còn lại và chế độ ăn, có trang chi tiết.
- [ ] Mốc 6 — Kiểm thử, xử lý lỗi và nghiệm thu trên thiết bị Android.
