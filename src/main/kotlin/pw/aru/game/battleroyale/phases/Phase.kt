package pw.aru.game.battleroyale.phases

import pw.aru.game.battleroyale.logic.BattleRoyaleMatch
import pw.aru.game.battleroyale.events.Event
import pw.aru.game.battleroyale.player.Player

sealed class Phase {
    abstract val match: BattleRoyaleMatch

    enum class Stages(val playable: Boolean = true) {
        BLOODBATH,
        DAY,
        DEATH_TRIBUTES(false),
        NIGHT,
        FEAST,
        WINNER(false),
        DRAW(false)
    }

    data class Bloodbath(
        override val match: BattleRoyaleMatch,
        val events: List<Event>,
        private val players: List<Player>
    ) : Phase()


    data class Day(
        override val match: BattleRoyaleMatch,
        val number: Int,
        val events: List<Event>,
        private val players: List<Player>
    ) : Phase()

    data class TributeToDeaths(
        override val match: BattleRoyaleMatch,
        val number: Int,
        val players: List<Player>,
        val fallenTributes: List<Player>
    ) : Phase()

    data class Night(
        override val match: BattleRoyaleMatch,
        val number: Int,
        val events: List<Event>,
        private val players: List<Player>
    ) : Phase()

    data class Feast(
        override val match: BattleRoyaleMatch,
        val number: Int,
        val events: List<Event>,
        private val players: List<Player>
    ) : Phase()

    data class Winner(
        override val match: BattleRoyaleMatch,
        val winner: Player,
        val number: Int,
        val ranking: List<Player>
    ):Phase()

    data class Draw(
        override val match: BattleRoyaleMatch,
        val number: Int,
        val ranking: List<Player>
    ) : Phase()
}
