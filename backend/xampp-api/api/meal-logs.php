<?php
declare(strict_types=1);

require_once __DIR__ . '/helpers.php';

$user = require_user();
$userId = (int) $user['id'];

function validate_meal_type(string $mealType): string
{
    if (!in_array($mealType, ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'], true)) {
        fail('Loại bữa ăn không hợp lệ.');
    }
    return $mealType;
}

function validate_date_key(string $dateKey): string
{
    if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $dateKey)) {
        fail('Ngày nhật ký không hợp lệ.');
    }
    return $dateKey;
}

function validate_meal_payload(array $item): array
{
    $name = trim((string) ($item['name'] ?? ''));
    $weight = (int) ($item['weight'] ?? 0);
    $calories = (int) ($item['calories'] ?? 0);
    $mealType = validate_meal_type((string) ($item['mealType'] ?? ''));
    $dateKey = validate_date_key((string) ($item['dateKey'] ?? ''));
    $sourceImageUri = $item['sourceImageUri'] ?? null;

    if ($name === '' || $weight <= 0 || $calories <= 0) {
        fail('Tên món, gram hoặc calo không hợp lệ.');
    }

    return [
        'name' => $name,
        'weight' => $weight,
        'calories' => $calories,
        'mealType' => $mealType,
        'dateKey' => $dateKey,
        'sourceImageUri' => is_string($sourceImageUri) ? $sourceImageUri : null,
    ];
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $dateKey = validate_date_key(trim((string) ($_GET['dateKey'] ?? '')));
    $mealType = trim((string) ($_GET['mealType'] ?? ''));
    $where = 'user_id = ? AND date_key = ?';
    $params = [$userId, $dateKey];

    if ($mealType !== '') {
        $where .= ' AND meal_type = ?';
        $params[] = validate_meal_type($mealType);
    }

    $stmt = db()->prepare(
        "SELECT id, name, weight, calories, meal_type, date_key, source_image_uri, created_at_millis
         FROM meal_logs
         WHERE $where
         ORDER BY FIELD(meal_type, 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'), created_at_millis ASC, id ASC"
    );
    $stmt->execute($params);

    $items = array_map(static function (array $row): array {
        return [
            'id' => (int) $row['id'],
            'name' => $row['name'],
            'weight' => (int) $row['weight'],
            'calories' => (int) $row['calories'],
            'mealType' => $row['meal_type'],
            'dateKey' => $row['date_key'],
            'sourceImageUri' => $row['source_image_uri'],
            'createdAtMillis' => (int) $row['created_at_millis'],
        ];
    }, $stmt->fetchAll());

    ok(['items' => $items]);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_input();
    $items = $input['items'] ?? [];
    if (!is_array($items) || count($items) === 0) {
        fail('Chưa có món ăn để lưu.');
    }

    $stmt = db()->prepare(
        'INSERT INTO meal_logs
            (user_id, name, weight, calories, meal_type, date_key, source_image_uri, created_at_millis)
         VALUES
            (?, ?, ?, ?, ?, ?, ?, ?)'
    );

    db()->beginTransaction();
    try {
        foreach ($items as $item) {
            if (!is_array($item)) {
                fail('Dữ liệu món ăn không hợp lệ.');
            }

            $validated = validate_meal_payload($item);
            $createdAtMillis = (int) ($item['createdAtMillis'] ?? round(microtime(true) * 1000));

            $stmt->execute([
                $userId,
                $validated['name'],
                $validated['weight'],
                $validated['calories'],
                $validated['mealType'],
                $validated['dateKey'],
                $validated['sourceImageUri'],
                $createdAtMillis,
            ]);
        }
        db()->commit();
    } catch (Throwable $error) {
        if (db()->inTransaction()) {
            db()->rollBack();
        }
        throw $error;
    }

    ok(['saved' => count($items)]);
}

if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $input = json_input();
    $id = (int) ($input['id'] ?? 0);
    if ($id <= 0) {
        fail('Mã món ăn không hợp lệ.');
    }

    $validated = validate_meal_payload($input);
    $ownerStmt = db()->prepare('SELECT id FROM meal_logs WHERE id = ? AND user_id = ?');
    $ownerStmt->execute([$id, $userId]);
    if (!$ownerStmt->fetch()) {
        fail('Không tìm thấy món ăn để cập nhật.', 404);
    }

    $stmt = db()->prepare(
        'UPDATE meal_logs
         SET name = ?, weight = ?, calories = ?, meal_type = ?, date_key = ?, source_image_uri = ?
         WHERE id = ? AND user_id = ?'
    );
    $stmt->execute([
        $validated['name'],
        $validated['weight'],
        $validated['calories'],
        $validated['mealType'],
        $validated['dateKey'],
        $validated['sourceImageUri'],
        $id,
        $userId,
    ]);

    ok(['updated' => 1]);
}

if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $id = (int) ($_GET['id'] ?? 0);
    if ($id <= 0) {
        fail('Mã món ăn không hợp lệ.');
    }

    $stmt = db()->prepare('DELETE FROM meal_logs WHERE id = ? AND user_id = ?');
    $stmt->execute([$id, $userId]);

    if ($stmt->rowCount() === 0) {
        fail('Không tìm thấy món ăn để xóa.', 404);
    }

    ok(['deleted' => 1]);
}

fail('Phương thức không hợp lệ.', 405);
