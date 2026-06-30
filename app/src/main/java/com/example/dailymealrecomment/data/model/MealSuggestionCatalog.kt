package com.example.dailymealrecomment.data.model

import com.example.dailymealrecomment.R

object MealSuggestionCatalog {
    val all: List<MealSuggestion> = listOf(
        MealSuggestion(
            id = CHICKEN_RICE,
            name = "Cơm gà áp chảo",
            calories = 520,
            isVegan = false,
            weightGrams = 250,
            serving = "1 đĩa • 250 g",
            imageResId = R.drawable.img_meal_chicken_rice_photo,
            ingredients = listOf(
                "120 g ức gà",
                "120 g cơm trắng",
                "Rau luộc hoặc salad xanh",
                "1 thìa cà phê dầu ô-liu",
                "Muối, tiêu, tỏi băm",
            ),
            recipeSteps = listOf(
                "Ướp ức gà với muối, tiêu và tỏi trong 10 phút.",
                "Áp chảo gà với ít dầu đến khi chín vàng hai mặt.",
                "Ăn cùng cơm trắng và rau xanh để cân bằng khẩu phần.",
            ),
        ),
        MealSuggestion(
            id = TOFU_SALAD,
            name = "Salad đậu phụ",
            calories = 430,
            isVegan = true,
            weightGrams = 300,
            serving = "1 tô • 300 g",
            imageResId = R.drawable.img_meal_tofu_salad_photo,
            ingredients = listOf(
                "150 g đậu phụ áp chảo",
                "Rau xà lách, dưa leo, cà chua",
                "50 g bắp hạt",
                "1 thìa mè rang",
                "Sốt chanh, dầu ô-liu và tiêu",
            ),
            recipeSteps = listOf(
                "Áp chảo đậu phụ đến khi xém nhẹ.",
                "Trộn rau, bắp và mè rang trong tô lớn.",
                "Thêm đậu phụ, rưới sốt chanh dầu ô-liu rồi dùng ngay.",
            ),
        ),
        MealSuggestion(
            id = OAT_BANANA,
            name = "Yến mạch chuối",
            calories = 360,
            isVegan = true,
            weightGrams = 220,
            serving = "1 bát • 220 g",
            imageResId = R.drawable.img_meal_oat_banana_photo,
            ingredients = listOf(
                "50 g yến mạch cán dẹt",
                "1 quả chuối chín",
                "180 ml sữa hạt không đường",
                "1 thìa cà phê hạt chia",
                "Một ít quế bột",
            ),
            recipeSteps = listOf(
                "Nấu yến mạch với sữa hạt trong 4–5 phút.",
                "Cắt chuối thành lát mỏng.",
                "Cho chuối, hạt chia và quế lên trên rồi dùng ấm.",
            ),
        ),
        MealSuggestion(
            id = SALMON_RICE,
            name = "Cơm cá hồi",
            calories = 610,
            isVegan = false,
            weightGrams = 280,
            serving = "1 phần • 280 g",
            imageResId = R.drawable.img_meal_salmon_rice_photo,
            ingredients = listOf(
                "120 g cá hồi phi lê",
                "130 g cơm gạo lứt hoặc cơm trắng",
                "Rau cải luộc",
                "Nước tương ít muối",
                "Chanh và tiêu",
            ),
            recipeSteps = listOf(
                "Ướp cá hồi với chanh, tiêu và ít nước tương.",
                "Áp chảo cá hồi đến khi mặt ngoài vàng và bên trong vừa chín.",
                "Dọn cá hồi cùng cơm và rau cải luộc.",
            ),
        ),
        MealSuggestion(
            id = VEGGIE_NOODLE,
            name = "Bún rau củ",
            calories = 390,
            isVegan = true,
            weightGrams = 320,
            serving = "1 tô • 320 g",
            imageResId = R.drawable.img_meal_veggie_noodle_photo,
            ingredients = listOf(
                "120 g bún tươi",
                "Nấm, cà rốt, cải thìa",
                "Đậu hũ chiên hoặc hấp",
                "Nước dùng rau củ",
                "Hành lá và rau thơm",
            ),
            recipeSteps = listOf(
                "Nấu nước dùng rau củ với nấm và cà rốt.",
                "Trụng bún và cải thìa đến khi vừa mềm.",
                "Cho bún, rau, đậu hũ vào tô rồi chan nước dùng nóng.",
            ),
        ),
        MealSuggestion(
            id = BANH_MI,
            name = "Bánh mì",
            calories = 450,
            isVegan = false,
            weightGrams = 220,
            serving = "1 ổ • 220 g",
            imageResId = R.drawable.banh_mi,
            ingredients = listOf(
                "1 ổ bánh mì",
                "Thịt nguội hoặc thịt nướng",
                "Dưa leo, đồ chua, rau thơm",
                "Một ít pate hoặc sốt",
            ),
            recipeSteps = listOf(
                "Làm nóng bánh mì cho vỏ giòn.",
                "Cho nhân thịt, rau, dưa leo và đồ chua vào bánh.",
                "Thêm sốt vừa đủ rồi dùng ngay.",
            ),
        ),
        MealSuggestion(
            id = BANH_XEO,
            name = "Bánh xèo",
            calories = 520,
            isVegan = false,
            weightGrams = 300,
            serving = "1 phần • 300 g",
            imageResId = R.drawable.banh_xeo,
            ingredients = listOf(
                "Bột bánh xèo",
                "Tôm, thịt hoặc nhân tùy chọn",
                "Giá đỗ, hành lá",
                "Rau sống và nước chấm",
            ),
            recipeSteps = listOf(
                "Pha bột bánh xèo theo tỉ lệ vừa phải.",
                "Đổ bánh trên chảo nóng, thêm nhân và giá.",
                "Chiên đến khi bánh vàng giòn rồi ăn cùng rau sống.",
            ),
        ),
        MealSuggestion(
            id = BUN_BO,
            name = "Bún bò",
            calories = 600,
            isVegan = false,
            weightGrams = 400,
            serving = "1 tô • 400 g",
            imageResId = R.drawable.bun_bo,
            ingredients = listOf(
                "Bún tươi",
                "Thịt bò",
                "Nước dùng bò",
                "Rau thơm, hành lá, giá",
            ),
            recipeSteps = listOf(
                "Nấu nước dùng bò với gia vị vừa ăn.",
                "Trụng bún và xếp thịt bò vào tô.",
                "Chan nước dùng nóng, thêm rau thơm rồi thưởng thức.",
            ),
        ),
        MealSuggestion(
            id = CHA_CHIEN,
            name = "Chả chiên",
            calories = 350,
            isVegan = false,
            weightGrams = 150,
            serving = "1 phần • 150 g",
            imageResId = R.drawable.cha_chien,
            ingredients = listOf(
                "Chả lụa hoặc chả cá",
                "Một ít dầu ăn",
                "Dưa leo hoặc rau ăn kèm",
            ),
            recipeSteps = listOf(
                "Cắt chả thành lát vừa ăn.",
                "Chiên áp chảo với ít dầu đến khi vàng hai mặt.",
                "Dùng cùng rau ăn kèm để bớt ngấy.",
            ),
        ),
        MealSuggestion(
            id = VIT_QUAY,
            name = "Vịt quay",
            calories = 650,
            isVegan = false,
            weightGrams = 280,
            serving = "1 phần • 280 g",
            imageResId = R.drawable.vit_quay,
            ingredients = listOf(
                "Thịt vịt quay",
                "Rau ăn kèm",
                "Dưa leo",
                "Nước chấm vừa ăn",
            ),
            recipeSteps = listOf(
                "Chặt vịt quay thành miếng vừa ăn.",
                "Chuẩn bị rau và dưa leo ăn kèm.",
                "Dùng lượng nước chấm vừa phải để kiểm soát calo.",
            ),
        ),
    )

    fun findById(id: String?): MealSuggestion? =
        all.firstOrNull { it.id == id }

    private const val CHICKEN_RICE = "chicken_rice"
    private const val TOFU_SALAD = "tofu_salad"
    private const val OAT_BANANA = "oat_banana"
    private const val SALMON_RICE = "salmon_rice"
    private const val VEGGIE_NOODLE = "veggie_noodle"
    private const val BANH_MI = "banh_mi"
    private const val BANH_XEO = "banh_xeo"
    private const val BUN_BO = "bun_bo"
    private const val CHA_CHIEN = "cha_chien"
    private const val VIT_QUAY = "vit_quay"
}
