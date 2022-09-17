package pw.aru.game.battleroyale.events

import pw.aru.game.battleroyale.player.Player
import java.text.MessageFormat
import java.util.*

class EventFormatter(private val converter: (Player) -> String = Player::toString) {

    fun format(event: Event): String {
        return if (event is Event.Played) {
            format(event.branch.result, event.players)
        } else {
            format(event.action.description, event.players)
        }
    }

    fun format(format: String, players: List<Player>): String {
        return EventFormatter[format].format(players.map(converter).toTypedArray())
    }

    fun format(player: Player) = converter(player)

    companion object {
        private val cached = HashMap<String, MessageFormat>()

        operator fun get(format: String) = cached.computeIfAbsent(format.replace("'", "''"), ::MessageFormat)

        fun cache(formats: List<String>) = formats.forEach { get(it) }
    }
}