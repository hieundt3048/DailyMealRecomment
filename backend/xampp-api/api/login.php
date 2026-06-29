<?php
declare(strict_types=1);

require_once __DIR__ . '/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail('Phương thức không hợp lệ.', 405);
}

$input = json_input();
$email = strtolower(trim((string) ($input['email'] ?? '')));
$password = (string) ($input['password'] ?? '');

$stmt = db()->prepare('SELECT * FROM users WHERE email = ? LIMIT 1');
$stmt->execute([$email]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    fail('Email hoặc mật khẩu không đúng.', 401);
}

ok([
    'token' => create_token((int) $user['id']),
    'user' => user_payload($user),
    'profile' => fetch_profile((int) $user['id']),
]);
