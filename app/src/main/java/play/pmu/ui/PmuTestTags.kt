package play.pmu.ui

object PmuTestTags {
    const val START_PARTY = "start_party"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val STATISTICS_SCREEN = "statistics_screen"
    const val SETTINGS_SCREEN = "settings_screen"
    const val GAME_RULES = "game_rules"

    const val ROUND_RESULT = "round_result"
    const val PARTY_RESULT = "party_result"
    const val HOME_BUTTON = "home_button"
    const val NEW_GAME_BUTTON = "new_game_button"

    const val HOME_LIST = "home_list"

    fun miniGame(name: String) = "game_$name"

    fun gameType(name: String) = "game_type_$name"

    fun category(index: Int) = "category_$index"

    fun playerArea(player: String) = "player_area_$player"

    fun ticTacToeCell(row: Int, column: Int) = "tic_tac_toe_cell_${row}_$column"

    fun memoryCard(index: Int) = "memory_card_$index"

    fun duelPrompt(player: String) = "duel_prompt_$player"

    fun answer(player: String, value: Int) = "answer_${player}_$value"

    fun stopTarget(player: String) = "stop_target_$player"

    fun stopButton(player: String) = "stop_button_$player"

    fun reactionHalf(player: String) = "reaction_half_$player"
}
