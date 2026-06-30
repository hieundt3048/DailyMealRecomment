package com.example.dailymealrecomment.data.model

object MealSuggestionCatalog {
    val all: List<MealSuggestion> = listOf(
        MealSuggestion(
            id = CHICKEN_RICE,
            name = "Cơm gà áp chảo",
            calories = 520,
            isVegan = false,
            serving = "1 đĩa • 250 g",
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
            serving = "1 tô • 300 g",
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
            serving = "1 bát • 220 g",
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
            serving = "1 phần • 280 g",
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
            serving = "1 tô • 320 g",
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
    )

    fun findById(id: String?): MealSuggestion? =
        all.firstOrNull { it.id == id }

    private const val CHICKEN_RICE = "chicken_rice"
    private const val TOFU_SALAD = "tofu_salad"
    private const val OAT_BANANA = "oat_banana"
    private const val SALMON_RICE = "salmon_rice"
    private const val VEGGIE_NOODLE = "veggie_noodle"
}
