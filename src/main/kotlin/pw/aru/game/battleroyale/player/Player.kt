package pw.aru.game.battleroyale.player

import pw.aru.game.battleroyale.events.EventFormatter

data class Player(
    val info: PlayerInfo,
    val kills: Int,
    val attributes: Set<String>
) {
    override fun toString() = info.toString()

    fun format(formatter: EventFormatter): String = formatter.format(this)
}

