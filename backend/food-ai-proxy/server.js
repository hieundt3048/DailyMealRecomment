"use strict";

const http = require("node:http");
const fs = require("node:fs");
const path = require("node:path");

loadDotEnvFile();

const config = {
  port: numberFromEnv("PORT", 8787),
  nvidiaApiKey: process.env.NVIDIA_API_KEY || "",
  nvidiaBaseUrl: trimTrailingSlash(process.env.NVIDIA_BASE_URL || "https://integrate.api.nvidia.com/v1"),
  nvidiaVisionModel: process.env.NVIDIA_VISION_MODEL || "meta/llama-3.2-11b-vision-instruct",
  useNemotronNormalizer: (process.env.USE_NEMOTRON_NORMALIZER || "false").toLowerCase() === "true",
  nvidiaNormalizerModel: process.env.NVIDIA_NORMALIZER_MODEL || "nvidia/nemotron-3-ultra-550b-a55b",
  nvidiaTimeoutMs: numberFromEnv("NVIDIA_TIMEOUT_MS", 45_000),
  maxUploadBytes: numberFromEnv("MAX_UPLOAD_MB", 8) * 1024 * 1024,
};

const server = http.createServer(async (req, res) => {
  try {
    setCorsHeaders(res);

    if (req.method === "OPTIONS") {
      return sendJson(res, 204, {});
    }

    const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);

    if (req.method === "GET" && url.pathname === "/health") {
      return sendJson(res, 200, {
        ok: true,
        service: "food-ai-nvidia-proxy",
        visionModel: config.nvidiaVisionModel,
        normalizerModel: config.useNemotronNormalizer ? config.nvidiaNormalizerModel : null,
        hasNvidiaApiKey: Boolean(config.nvidiaApiKey),
      });
    }

    if (req.method === "POST" && url.pathname === "/api/recognize-food") {
      const result = await recognizeFoodFromRequest(req);
      return sendJson(res, 200, result);
    }

    sendJson(res, 404, { error: "NOT_FOUND" });
  } catch (error) {
    const status = error.statusCode || 500;
    sendJson(res, status, {
      error: error.code || "INTERNAL_ERROR",
      message: error.publicMessage || "Không thể phân tích ảnh món ăn.",
    });
  }
});

server.listen(config.port, "0.0.0.0", () => {
  console.log(`FoodAI NVIDIA proxy is running on http://0.0.0.0:${config.port}`);
  console.log(`Vision model: ${config.nvidiaVisionModel}`);
  if (config.useNemotronNormalizer) {
    console.log(`JSON normalizer: ${config.nvidiaNormalizerModel}`);
  }
});

async function recognizeFoodFromRequest(req) {
  if (!config.nvidiaApiKey) {
    throw httpError(
      500,
      "NVIDIA_API_KEY_MISSING",
      "Backend chưa cấu hình NVIDIA_API_KEY.",
    );
  }

  const contentType = req.headers["content-type"] || "";
  if (!contentType.toLowerCase().includes("multipart/form-data")) {
    throw httpError(415, "UNSUPPORTED_MEDIA_TYPE", "Endpoint chỉ nhận multipart/form-data.");
  }

  const body = await readRequestBody(req, config.maxUploadBytes);
  const parts = parseMultipart(body, contentType);
  const imagePart = parts.find((part) => part.name === "image" || part.name === "file" || part.filename);

  if (!imagePart || !imagePart.data.length) {
    throw httpError(400, "IMAGE_MISSING", "Không tìm thấy ảnh trong request.");
  }

  const mimeType = imagePart.contentType || "image/jpeg";
  if (!mimeType.toLowerCase().startsWith("image/")) {
    throw httpError(400, "INVALID_IMAGE_TYPE", "File gửi lên không phải ảnh.");
  }

  const visionText = await callNvidiaVisionModel(imagePart.data, mimeType);
  const normalizedText = config.useNemotronNormalizer
    ? await callNvidiaNormalizer(visionText)
    : visionText;
  const items = normalizeFoodItems(normalizedText);

  return { items };
}

async function callNvidiaVisionModel(imageBuffer, mimeType) {
  const imageDataUrl = `data:${mimeType};base64,${imageBuffer.toString("base64")}`;
  const payload = {
    model: config.nvidiaVisionModel,
    temperature: 0.1,
    max_tokens: 800,
    messages: [
      {
        role: "system",
        content:
          "You are FoodAI, an assistant for Vietnamese calorie tracking. Return only valid JSON. " +
          "The Android app only accepts food item name, estimated weight in grams, and calories in kcal. " +
          "Do not include protein, carbs, fat, or other nutrition fields.",
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text:
              "Analyze this food photo. Estimate visible food items. " +
              "Return exactly this JSON shape: {\"items\":[{\"name\":\"Tên món\",\"weight\":100,\"calories\":200}]}. " +
              "Use Vietnamese food names when possible. If no food is visible, return {\"items\":[]}.",
          },
          {
            type: "image_url",
            image_url: {
              url: imageDataUrl,
            },
          },
        ],
      },
    ],
  };

  return callNvidiaChatCompletions(payload, config.nvidiaVisionModel);
}

async function callNvidiaNormalizer(rawVisionOutput) {
  const payload = {
    model: config.nvidiaNormalizerModel,
    temperature: 0,
    max_tokens: 700,
    messages: [
      {
        role: "system",
        content:
          "You normalize food recognition output into strict JSON for an Android calorie app. " +
          "Return only JSON. Keep only name, weight, calories. Remove protein, carbs, fat and all other fields.",
      },
      {
        role: "user",
        content:
          "Normalize this result to {\"items\":[{\"name\":\"Tên món\",\"weight\":100,\"calories\":200}]}.\n\n" +
          rawVisionOutput,
      },
    ],
  };

  return callNvidiaChatCompletions(payload, config.nvidiaNormalizerModel);
}

async function callNvidiaChatCompletions(payload, modelName) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), config.nvidiaTimeoutMs);

  try {
    const response = await fetch(`${config.nvidiaBaseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${config.nvidiaApiKey}`,
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(payload),
      signal: controller.signal,
    });

    const responseText = await response.text();
    if (!response.ok) {
      throw httpError(
        502,
        "NVIDIA_API_ERROR",
        `NVIDIA API lỗi khi gọi model ${modelName}.`,
        responseText,
      );
    }

    const responseJson = safeJsonParse(responseText);
    const messageContent = responseJson?.choices?.[0]?.message?.content;
    if (!messageContent) {
      throw httpError(
        502,
        "NVIDIA_EMPTY_RESPONSE",
        `NVIDIA API không trả nội dung hợp lệ từ model ${modelName}.`,
      );
    }

    return Array.isArray(messageContent)
      ? messageContent.map((part) => part.text || "").join("\n")
      : String(messageContent);
  } catch (error) {
    if (error.name === "AbortError") {
      throw httpError(504, "NVIDIA_TIMEOUT", "NVIDIA API phản hồi quá lâu.");
    }
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

function normalizeFoodItems(aiText) {
  const parsed = extractJson(aiText);
  const rawItems = Array.isArray(parsed)
    ? parsed
    : parsed.items || parsed.foods || parsed.results || [];

  if (!Array.isArray(rawItems)) {
    throw httpError(502, "INVALID_AI_JSON", "AI trả JSON không có danh sách món ăn.");
  }

  return rawItems
    .map((item) => {
      const name = firstNonBlank(item, ["name", "foodName", "food_name", "dish", "label"]);
      const weight = firstPositiveInt(item, [
        "weight",
        "weightGram",
        "weightGrams",
        "weight_g",
        "weight_grams",
        "grams",
        "estimatedWeight",
        "estimatedWeightGrams",
        "estimated_weight_g",
      ]);
      const calories =
        firstPositiveInt(item, ["calories", "calorie", "kcal", "estimatedCalories", "estimated_calories"]) ||
        firstPositiveInt(item?.nutrition || {}, ["calories", "kcal"]);

      if (!name || !weight || !calories) return null;

      return {
        name,
        weight,
        calories,
      };
    })
    .filter(Boolean);
}

function extractJson(text) {
  const cleaned = String(text || "")
    .replace(/```json/gi, "```")
    .replace(/```/g, "")
    .trim();

  const direct = safeJsonParse(cleaned);
  if (direct) return direct;

  const firstObject = cleaned.indexOf("{");
  const lastObject = cleaned.lastIndexOf("}");
  if (firstObject !== -1 && lastObject > firstObject) {
    const parsedObject = safeJsonParse(cleaned.slice(firstObject, lastObject + 1));
    if (parsedObject) return parsedObject;
  }

  const firstArray = cleaned.indexOf("[");
  const lastArray = cleaned.lastIndexOf("]");
  if (firstArray !== -1 && lastArray > firstArray) {
    const parsedArray = safeJsonParse(cleaned.slice(firstArray, lastArray + 1));
    if (parsedArray) return parsedArray;
  }

  throw httpError(502, "INVALID_AI_JSON", "AI không trả JSON hợp lệ.");
}

function parseMultipart(buffer, contentType) {
  const boundaryMatch = /boundary=(?:"([^"]+)"|([^;]+))/i.exec(contentType);
  const boundaryValue = boundaryMatch?.[1] || boundaryMatch?.[2];
  if (!boundaryValue) {
    throw httpError(400, "MULTIPART_BOUNDARY_MISSING", "Request thiếu multipart boundary.");
  }

  const boundary = Buffer.from(`--${boundaryValue}`);
  const parts = [];
  let position = buffer.indexOf(boundary);

  while (position !== -1) {
    const nextPosition = buffer.indexOf(boundary, position + boundary.length);
    if (nextPosition === -1) break;

    let part = buffer.subarray(position + boundary.length, nextPosition);
    if (part[0] === 45 && part[1] === 45) break;
    if (part[0] === 13 && part[1] === 10) part = part.subarray(2);
    if (part.at(-2) === 13 && part.at(-1) === 10) part = part.subarray(0, part.length - 2);

    const headerEnd = part.indexOf(Buffer.from("\r\n\r\n"));
    if (headerEnd !== -1) {
      const headerText = part.subarray(0, headerEnd).toString("utf8");
      const data = part.subarray(headerEnd + 4);
      parts.push(parseMultipartPart(headerText, data));
    }

    position = nextPosition;
  }

  return parts;
}

function parseMultipartPart(headerText, data) {
  const headers = {};
  for (const line of headerText.split("\r\n")) {
    const separator = line.indexOf(":");
    if (separator === -1) continue;
    headers[line.slice(0, separator).trim().toLowerCase()] = line.slice(separator + 1).trim();
  }

  const disposition = headers["content-disposition"] || "";
  return {
    name: /name="([^"]+)"/.exec(disposition)?.[1] || "",
    filename: /filename="([^"]*)"/.exec(disposition)?.[1] || "",
    contentType: headers["content-type"] || "",
    data,
  };
}

function readRequestBody(req, maxBytes) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let totalBytes = 0;

    req.on("data", (chunk) => {
      totalBytes += chunk.length;
      if (totalBytes > maxBytes) {
        reject(httpError(413, "UPLOAD_TOO_LARGE", "Ảnh vượt quá giới hạn upload."));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });

    req.on("end", () => resolve(Buffer.concat(chunks)));
    req.on("error", reject);
  });
}

function firstNonBlank(source, keys) {
  for (const key of keys) {
    const value = source?.[key];
    if (typeof value === "string" && value.trim()) {
      return value.trim();
    }
  }
  return "";
}

function firstPositiveInt(source, keys) {
  for (const key of keys) {
    const value = Number(source?.[key]);
    if (Number.isFinite(value) && value > 0) {
      return Math.round(value);
    }
  }
  return 0;
}

function safeJsonParse(value) {
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function sendJson(res, statusCode, body) {
  res.writeHead(statusCode, { "Content-Type": "application/json; charset=utf-8" });
  if (statusCode === 204) {
    res.end();
  } else {
    res.end(JSON.stringify(body));
  }
}

function setCorsHeaders(res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
}

function httpError(statusCode, code, publicMessage, detail) {
  const error = new Error(detail || publicMessage);
  error.statusCode = statusCode;
  error.code = code;
  error.publicMessage = publicMessage;
  return error;
}

function trimTrailingSlash(value) {
  return value.replace(/\/+$/, "");
}

function numberFromEnv(name, fallback) {
  const value = Number(process.env[name]);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

function loadDotEnvFile() {
  const envPath = path.join(__dirname, ".env");
  if (!fs.existsSync(envPath)) return;

  const lines = fs.readFileSync(envPath, "utf8").split(/\r?\n/);
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const separator = trimmed.indexOf("=");
    if (separator === -1) continue;
    const key = trimmed.slice(0, separator).trim();
    const rawValue = trimmed.slice(separator + 1).trim();
    const value = rawValue.replace(/^['"]|['"]$/g, "");
    if (!process.env[key]) process.env[key] = value;
  }
}
