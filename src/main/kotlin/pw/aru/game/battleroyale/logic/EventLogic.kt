package pw.aru.game.battleroyale.logic

import pw.aru.game.battleroyale.actions.Effect
import pw.aru.game.battleroyale.player.Player

object EventLogic {
    fun calculateHarmless(players: List<Player>, effects: Map<Int, Effect>): List<Player> {
        return players.mapIndexed { index, player ->
            val (add, remove) = effects.getOrDefault(index, Effect.empty)

            player.copy(
                attributes = player.attributes - remove + add
            )
        }
    }

    fun calculateHarmful(players: List<Player>, effects: Map<Int, Effect>, killed: List<Int>, killers: List<Int>): HarmResult {
        val (killedPlayers, alivePlayers) = players.withIndex().partition {
            killed.contains(it.index)
        }

        return HarmResult(
            killedPlayers.map { (index, player) ->
                val (add, remove) = effects.getOrDefault(index, Effect.empty)

                player.copy(
                    attributes = player.attributes - remove + add
                )
            },
            alivePlayers.map { (index, player) ->
                val (add, remove) = effects.getOrDefault(index, Effect.empty)

                player.copy(
                    kills = if (killers.contains(index)) player.kills + killedPlayers.size else player.kills,
                    attributes = player.attributes - remove + add
                )
            }
        )
    }

    data class HarmResult(
        val killedPlayers: List<Player>,
        val alivePlayers: List<Player>
    )
}