package pw.aru.game.battleroyale.player

abstract class PlayerInfo {
    abstract val name: String

    abstract val npc: Boolean

    override fun toString(): String {
        return name
    }
}