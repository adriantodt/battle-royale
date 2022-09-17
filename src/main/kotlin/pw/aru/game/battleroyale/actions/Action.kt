package pw.aru.game.battleroyale.actions

sealed class Action {
    /**
     * The amount of players required
     */
    abstract val targetAmount: Int

    /**
     * The effects required
     */
    abstract val predicate: Map<Int, Effect.Matcher>

    /**
     * Description of the amount
     */
    abstract val description: String

    /**
     * The effect applied on each player
     */
    abstract val applies: Map<Int, Effect>

    sealed class Simulable : Action() {
        data class Harmful(
            override val targetAmount: Int,
            override val description: String,
            val killed: List<Int>,
            val killers: List<Int>,
            override val predicate: Map<Int, Effect.Matcher> = emptyMap(),
            override val applies: Map<Int, Effect> = emptyMap()
        ) : Simulable()

        data class Harmless(
            override val targetAmount: Int,
            override val description: String,
            override val predicate: Map<Int, Effect.Matcher> = emptyMap(),
            override val applies: Map<Int, Effect> = emptyMap()
        ) : Simulable()
    }

    /**
     * Note: in this case, players are mapped with IDs (0 until playerAmount)
     * and players are mapped with IDs (playerAmount until (targetAmount + playerAmount)
     */
    data class Playable(
        override val targetAmount: Int,
        override val description: String,
        /**
         * The amount of players required
         */
        val playerAmount: Int,
        /**
         * All the options the players can take
         */
        val possibleOptions: List<PlayerActionBranch>,

        /**
         * Use this in case of timeout
         */
        val idleOption: PlayerActionBranch,

        override val predicate: Map<Int, Effect.Matcher> = emptyMap(),
        /**
         * Effects which will be applied independent if option took
         */
        override val applies: Map<Int, Effect> = emptyMap()
    ) : Action()
}