package pw.aru.game.battleroyale.actions

data class Effect(
    val add: List<String> = emptyList(),
    val remove: List<String> = emptyList()
) {
    companion object {
        val empty = Effect()
    }

    data class Matcher(
        val require: List<String> = emptyList(),
        val exclude: List<String> = emptyList()
    ) {
        companion object {
            val empty = Matcher()
        }
    }
}
