<?php
declare(strict_types=1);

require_once __DIR__ . '/helpers.php';

$user = require_user();
$userId = (int) $user['id'];

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    ok(['profile' => fetch_profile($userId)]);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail('Phương thức không hợp lệ.', 405);
}

$input = json_input();
$heightCm = (float) ($input['heightCm'] ?? 0);
$weightKg = (float) ($input['weightKg'] ?? 0);
$age = (int) ($input['age'] ?? 0);
$isMale = filter_var($input['isMale'] ?? true, FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
$goal = (string) ($input['goal'] ?? 'MAINTAIN_WEIGHT');
$dietType = (string) ($input['dietType'] ?? 'NORMAL');
$activityLevel = (float) ($input['activityLevel'] ?? 1.2);
$dailyCalorieTarget = (int) ($input['dailyCalorieTarget'] ?? 0);

if ($heightCm < 100 || $heightCm > 250 || $weightKg < 30 || $weightKg > 350 || $age < 13 || $age > 100) {
    fail('Chỉ số cơ thể không hợp lệ.');
}
if (!in_array($goal, ['LOSE_WEIGHT', 'MAINTAIN_WEIGHT', 'GAIN_WEIGHT'], true)) {
    fail('Mục tiêu không hợp lệ.');
}
if (!in_array($dietType, ['NORMAL', 'VEGAN'], true)) {
    fail('Chế độ ăn không hợp lệ.');
}
if ($dailyCalorieTarget <= 0) {
    fail('Mục tiêu calo không hợp lệ.');
}

$stmt = db()->prepare(
    'INSERT INTO user_profiles
        (user_id, height_cm, weight_kg, age, is_male, goal, diet_type, activity_level, daily_calorie_target)
     VALUES
        (?, ?, ?, ?, ?, ?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE
        height_cm = VALUES(height_cm),
        weight_kg = VALUES(weight_kg),
        age = VALUES(age),
        is_male = VALUES(is_male),
        goal = VALUES(goal),
        diet_type = VALUES(diet_type),
        activity_level = VALUES(activity_level),
        daily_calorie_target = VALUES(daily_calorie_target)'
);
$stmt->execute([
    $userId,
    $heightCm,
    $weightKg,
    $age,
    $isMale === false ? 0 : 1,
    $goal,
    $dietType,
    $activityLevel,
    $dailyCalorieTarget,
]);

$updateUser = db()->prepare('UPDATE users SET profile_completed = 1 WHERE id = ?');
$updateUser->execute([$userId]);

ok(['profile' => fetch_profile($userId)]);
