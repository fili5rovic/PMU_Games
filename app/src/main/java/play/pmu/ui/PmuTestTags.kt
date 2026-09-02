package play.pmu.ui

/**
 * Imena `testTag`-ova za elemente koje voze instrumentacioni testovi.
 *
 * Stoje na jednom mestu da bi test i UI koristili isti tekst - preimenovanje
 * dugmeta tada ne obara test, a test ne zavisi od vidljivog teksta (koji se
 * prevodi i menja).
 */
object PmuTestTags {
    const val START_PARTY = "start_party"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val STATISTICS_SCREEN = "statistics_screen"
    const val SETTINGS_SCREEN = "settings_screen"
    const val GAME_RULES = "game_rules"

    /** Ekran rezultata runde i partije. */
    const val ROUND_RESULT = "round_result"
    const val PARTY_RESULT = "party_result"
    const val HOME_BUTTON = "home_button"
    const val NEW_GAME_BUTTON = "new_game_button"

    /** Lista na pocetnom ekranu - test kroz nju skroluje do igre. */
    const val HOME_LIST = "home_list"

    /** Pojedinacne igre sa pocetnog ekrana: tag je "game_" + ime MiniGame konstante. */
    fun miniGame(name: String) = "game_$name"

    /** Pantomima i kviz sa pocetnog ekrana. */
    fun gameType(name: String) = "game_type_$name"

    /** Kategorija na ekranu izbora kategorije. */
    fun category(index: Int) = "category_$index"

    /** Polovina ekrana jednog igraca: "player_area_ONE" / "player_area_TWO". */
    fun playerArea(player: String) = "player_area_$player"

    /** Polje iks-oks table, po vrsti i koloni. */
    fun ticTacToeCell(row: Int, column: Int) = "tic_tac_toe_cell_${row}_$column"

    /** Kartica u memoriji, po mestu u mrezi. */
    fun memoryCard(index: Int) = "memory_card_$index"

    /** Postavljeno pitanje u duelu. */
    fun duelPrompt(player: String) = "duel_prompt_$player"

    /** Dugme sa odgovorom u duelu: "answer_ONE_12". */
    fun answer(player: String, value: Int) = "answer_${player}_$value"

    /** Prikazano ciljno vreme u igri "stani na vreme". */
    fun stopTarget(player: String) = "stop_target_$player"

    /** STOP dugme u igri "stani na vreme". */
    fun stopButton(player: String) = "stop_button_$player"

    /** Polovina ekrana u duelu refleksa. */
    fun reactionHalf(player: String) = "reaction_half_$player"
}
