<?php
declare(strict_types=1);

require_once __DIR__ . '/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail('Phương thức không hợp lệ.', 405);
}

$input = json_input();
$name = trim((string) ($input['name'] ?? ''));
$email = strtolower(trim((string) ($input['email'] ?? '')));
$password = (string) ($input['password'] ?? '');

if (mb_strlen($name) < 2) {
    fail('Họ tên phải có ít nhất 2 ký tự.');
}
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    fail('Email không hợp lệ.');
}
if (strlen($password) < 6) {
    fail('Mật khẩu phải có ít nhất 6 ký tự.');
}

$check = db()->prepare('SELECT id FROM users WHERE email = ? LIMIT 1');
$check->execute([$email]);
if ($check->fetch()) {
    fail('Email này đã được đăng ký.');
}

$stmt = db()->prepare(
    'INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)'
);
$stmt->execute([$name, $email, password_hash($password, PASSWORD_DEFAULT)]);

$userId = (int) db()->lastInsertId();
$user = [
    'id' => $userId,
    'name' => $name,
    'email' => $email,
    'profile_completed' => 0,
];

ok([
    'token' => create_token($userId),
    'user' => user_payload($user),
    'profile' => null,
]);
