package pw.aru.game.battleroyale.actions

sealed class PlayerActionBranch {
    /**
     * The effects required
     */
    abstract val predicate: Map<Int, Effect.Matcher>

    /**
     * Description of the amount
     */
    abstract val result: String

    /**
     * The effect applied on each player
     */
    abstract val applies: Map<Int, Effect>

    data class Harmful(
        override val result: String,
        val killed: List<Int>,
        val killers: List<Int>,
        override val predicate: Map<Int, Effect.Matcher> = emptyMap(),
        override val applies: Map<Int, Effect> = emptyMap()
    ) : PlayerActionBranch()

    data class Harmless(
        override val result: String,
        override val predicate: Map<Int, Effect.Matcher> = emptyMap(),
        override val applies: Map<Int, Effect> = emptyMap()
    ) : PlayerActionBranch()
}