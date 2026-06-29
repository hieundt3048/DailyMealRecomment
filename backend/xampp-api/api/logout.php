<?php
declare(strict_types=1);

require_once __DIR__ . '/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail('Phương thức không hợp lệ.', 405);
}

$token = bearer_token();
if ($token !== null && $token !== '') {
    $stmt = db()->prepare('DELETE FROM auth_tokens WHERE token_hash = ?');
    $stmt->execute([hash('sha256', $token)]);
}

ok();
