# FoodAI NVIDIA Proxy

Backend nhỏ để app Android gọi NVIDIA AI mà không để lộ `NVIDIA_API_KEY` trong APK.

## Chạy backend

Yêu cầu Node.js 18 trở lên.

cd E:\tai\DailyMealRecomment\backend\food-ai-proxy
npm start

```powershell
cd backend\food-ai-proxy
Copy-Item .env.example .env
notepad .env
npm start
```

Trong `.env`, điền:

```properties
NVIDIA_API_KEY=nvapi-your-key-here
NVIDIA_VISION_MODEL=meta/llama-3.2-11b-vision-instruct
```

Nếu muốn dùng `nvidia/nemotron-3-ultra-550b-a55b` để chuẩn hóa JSON sau bước nhận diện ảnh:

```properties
USE_NEMOTRON_NORMALIZER=true
NVIDIA_NORMALIZER_MODEL=nvidia/nemotron-3-ultra-550b-a55b
```

Lưu ý: cần một model vision/multimodal cho ảnh. Nemotron 3 Ultra phù hợp hơn để chuẩn hóa/suy luận JSON từ kết quả text.

## Kiểm tra backend

```powershell
Invoke-RestMethod http://localhost:8787/health
```

## Trỏ app Android về backend

Trong `local.properties` của project Android:

```properties
# Emulator Android Studio:
FOOD_AI_ENDPOINT=http://10.0.2.2:8787/api/recognize-food

# Điện thoại thật cùng Wi-Fi với máy tính:
# FOOD_AI_ENDPOINT=http://YOUR_PC_LAN_IP:8787/api/recognize-food
```

Với điện thoại thật, lấy IP máy tính bằng:

```powershell
ipconfig
```

Sau đó build lại app.

## Response backend trả cho app

```json
{
  "items": [
    {
      "name": "Cơm gà",
      "weight": 250,
      "calories": 520
    }
  ]
}
```

Backend cố tình chỉ trả `name`, `weight`, `calories`; không trả Protein, Carbs hoặc Fat.
