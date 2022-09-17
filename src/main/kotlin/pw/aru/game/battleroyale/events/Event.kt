package pw.aru.game.battleroyale.events

import pw.aru.game.battleroyale.actions.Action
import pw.aru.game.battleroyale.actions.PlayerActionBranch
import pw.aru.game.battleroyale.player.Player

sealed class Event {
    abstract val action: Action
    abstract val players: List<Player>

    sealed class Simulable : Event() {
        abstract override val action: Action.Simulable

        data class Harmful(
            override val action: Action.Simulable.Harmful,
            override val players: List<Player>,
            val killedPlayers: List<Player>
        ) : Simulable ()

        data class Harmless(
            override val action: Action.Simulable.Harmless,
            override val players: List<Player>
        ) : Simulable ()
    }

    data class Playable(
        override val action: Action.Playable,
        override val players: List<Player>,
        val availableBranches: List<PlayerActionBranch>,
        val idleOption: PlayerActionBranch
    ) : Event()

    sealed class Played : Event() {
        abstract override val action: Action.Playable
        abstract val branch: PlayerActionBranch

        data class Harmful(
            override val action: Action.Playable,
            override val branch: PlayerActionBranch,
            override val players: List<Player>,
            val killedPlayers: List<Player>
        ) : Event.Played()

        data class Harmless(
            override val action: Action.Playable,
            override val branch: PlayerActionBranch,
            override val players: List<Player>
        ) : Event.Played()
    }

    fun format(formatter: EventFormatter): String = formatter.format(this)
}