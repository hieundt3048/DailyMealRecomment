<?php
declare(strict_types=1);

require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Auth-Token');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function json_input(): array
{
    $raw = file_get_contents('php://input') ?: '';
    $data = json_decode($raw, true);
    return is_array($data) ? $data : [];
}

function respond(array $payload, int $status = 200): void
{
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

function ok(array $payload = []): void
{
    respond(['success' => true] + $payload);
}

function fail(string $message, int $status = 400): void
{
    respond(['success' => false, 'message' => $message], $status);
}

function bearer_token(): ?string
{
    $headers = [];
    if (function_exists('getallheaders')) {
        $headers = getallheaders() ?: [];
    }

    $header = $_SERVER['HTTP_AUTHORIZATION']
        ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION']
        ?? $_SERVER['Authorization']
        ?? $headers['Authorization']
        ?? $headers['authorization']
        ?? '';

    if (stripos($header, 'Bearer ') === 0) {
        return trim(substr($header, 7));
    }

    $token = $_SERVER['HTTP_X_AUTH_TOKEN']
        ?? $headers['X-Auth-Token']
        ?? $headers['x-auth-token']
        ?? $_GET['_token']
        ?? '';

    $token = trim((string) $token);
    return $token === '' ? null : $token;
}

function create_token(int $userId): string
{
    $token = bin2hex(random_bytes(32));
    $hash = hash('sha256', $token);
    $expiresAt = (new DateTimeImmutable('+365 days'))->format('Y-m-d H:i:s');

    $stmt = db()->prepare(
        'INSERT INTO auth_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)'
    );
    $stmt->execute([$userId, $hash, $expiresAt]);

    return $token;
}

function require_user(): array
{
    $token = bearer_token();
    if ($token === null || $token === '') {
        fail('Phiên đăng nhập không hợp lệ.', 401);
    }

    $hash = hash('sha256', $token);
    $stmt = db()->prepare(
        'SELECT users.*
         FROM auth_tokens
         INNER JOIN users ON users.id = auth_tokens.user_id
         WHERE auth_tokens.token_hash = ? AND auth_tokens.expires_at > NOW()
         LIMIT 1'
    );
    $stmt->execute([$hash]);
    $user = $stmt->fetch();

    if (!$user) {
        fail('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 401);
    }

    $update = db()->prepare('UPDATE auth_tokens SET last_used_at = NOW() WHERE token_hash = ?');
    $update->execute([$hash]);

    return $user;
}

function user_payload(array $user): array
{
    return [
        'id' => (int) $user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'profileCompleted' => (bool) $user['profile_completed'],
    ];
}

function fetch_profile(int $userId): ?array
{
    $stmt = db()->prepare('SELECT * FROM user_profiles WHERE user_id = ? LIMIT 1');
    $stmt->execute([$userId]);
    $profile = $stmt->fetch();

    if (!$profile) {
        return null;
    }

    return [
        'heightCm' => (float) $profile['height_cm'],
        'weightKg' => (float) $profile['weight_kg'],
        'age' => (int) $profile['age'],
        'isMale' => (bool) $profile['is_male'],
        'goal' => $profile['goal'],
        'dietType' => $profile['diet_type'],
        'activityLevel' => (float) $profile['activity_level'],
        'dailyCalorieTarget' => (int) $profile['daily_calorie_target'],
    ];
}
