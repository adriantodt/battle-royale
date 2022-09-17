package pw.aru.game.battleroyale.logic

import pw.aru.game.battleroyale.BattleRoyale
import pw.aru.game.battleroyale.actions.Action
import pw.aru.game.battleroyale.actions.Effect
import pw.aru.game.battleroyale.events.Event
import pw.aru.game.battleroyale.floor
import pw.aru.game.battleroyale.phases.Phase
import pw.aru.game.battleroyale.phases.Phase.*
import pw.aru.game.battleroyale.phases.Phase.Stages.*
import pw.aru.game.battleroyale.player.Player
import pw.aru.game.battleroyale.player.PlayerPool
import pw.aru.game.battleroyale.sigmoid
import pw.aru.game.battleroyale.sin
import java.util.*
import java.util.Collections.disjoint

class BattleRoyaleMatch(val game: BattleRoyale, private val r: Random = Random()) {
    private var dayCount: Int = 0
    private var cycle = BLOODBATH
    private var currentThreshold: Double = 1.0
    private lateinit var cachedNpcEvents: LinkedList<Event.Simulable>

    private val notBuriedPlayers = LinkedList<Player>()
    private val deadPlayers = LinkedList<Player>()
    private val playersLeft = LinkedList<Player>()
    private val receiver = PlayerInputReceiver()

    init {
        for (player in game.players) {
            playersLeft.add(Player(player, 0, LinkedHashSet()))
        }
    }

    fun beginPhase(): PlayerInputReceiver {
        val npcs = playersLeft.toMutableList()
        currentThreshold = currThreshold(npcs, dayCount)

        //jump

        if (npcs.size <= 1 || !cycle.playable) {
            receiver.inputStack.clear()
            receiver.doneStack.clear()
            return receiver
        }

        val players = npcs.filter { !it.info.npc }
        npcs.removeAll(players)

        val playerPool = PlayerPool(players, r)
        val npcPool = PlayerPool(npcs, r)

        val playableEvents = LinkedList<Event.Playable>()
        val npcEvents = LinkedList<Event.Simulable>()

        val (harmlessActions, harmfulActions, playableActions) = when (cycle) {
            BLOODBATH -> game.environiment.bloodbathActions
            DAY -> game.environiment.dayActions
            NIGHT -> game.environiment.nightActions
            FEAST -> game.environiment.feastActions
            else -> throw IllegalStateException()
        }

        while (!playerPool.empty) {
            consumePlayableOnce(playerPool, npcPool, playableActions)?.let {
                playableEvents.add(it)
            }
        }

        while (!npcPool.empty) {
            consumeNpcOnce(npcPool, if (r.nextDouble() < currentThreshold) harmlessActions else harmfulActions)?.let {
                npcEvents.add(it)
            }
        }

        cachedNpcEvents = npcEvents

        return receiver
    }

    fun completePhase(): Phase {
        val thisCycle = cycle
        when (thisCycle) {
            DEATH_TRIBUTES -> {
                val phase = TributeToDeaths(this, dayCount, playersLeft.toList(), notBuriedPlayers.toList())
                notBuriedPlayers.clear() // bury the players

                cycle = when (playersLeft.size) {
                    0 -> DRAW
                    1 -> WINNER
                    else -> NIGHT
                }

                return phase
            }
            WINNER -> {
                val winner = playersLeft.single()
                return Winner(this, winner, dayCount, (listOf(winner) + deadPlayers.reversed()))
            }
            DRAW -> {
                return Draw(this, dayCount, deadPlayers.reversed())
            }

            BLOODBATH, DAY, NIGHT, FEAST -> {
                val events = (receiver.doneStack + cachedNpcEvents).shuffled(r)

                val alive = LinkedList<Player>()
                val dead = LinkedList<Player>()

                for (event in events) {
                    when (event) {
                        is Event.Simulable.Harmful -> {
                            dead += event.killedPlayers
                            alive += event.players
                        }
                        is Event.Simulable.Harmless -> {
                            alive += event.players
                        }
                        is Event.Played.Harmful -> {
                            dead += event.killedPlayers
                            alive += event.players
                        }
                        is Event.Played.Harmless -> {
                            alive += event.players
                        }
                    }
                }

                playersLeft.clear()
                playersLeft.addAll(alive)
                deadPlayers.addAll(dead)
                notBuriedPlayers.addAll(dead)

                when (thisCycle) {
                    BLOODBATH -> {
                        cycle = DAY

                        return Bloodbath(
                            this, events, alive
                        )
                    }
                    DAY -> {
                        cycle = DEATH_TRIBUTES

                        return Day(
                            this, dayCount, events, alive
                        )
                    }
                    NIGHT -> {
                        cycle = if (r.nextDouble() < (1 - currentThreshold)) FEAST else DAY

                        return Night(
                            this, dayCount++, events, alive
                        )

                    }
                    FEAST -> {
                        cycle = DAY

                        return Feast(
                            this, dayCount, events, alive
                        )
                    }
                    else -> throw IllegalStateException("impossible to happen")
                }
            }
        }
    }

    // internal

    private fun consumePlayableOnce(
        playerPool: PlayerPool,
        npcPool: PlayerPool,
        actions: List<Action.Playable>
    ): Event.Playable? {
        for (action in actions.shuffled(r)) {
            if (playerPool.size < action.playerAmount) continue
            if (npcPool.size < action.targetAmount) continue

            val playerResult = playerPool.select {
                for (i in 0 until action.playerAmount) {
                    random {
                        val (require, exclude) = action.predicate.getOrDefault(i, Effect.Matcher.empty)
                        it.attributes.containsAll(require) && disjoint(it.attributes, exclude)
                    }
                }
            }

            val npcResult = npcPool.select {
                for (i in action.playerAmount until (action.playerAmount + action.targetAmount)) {
                    random {
                        val (require, exclude) = action.predicate.getOrDefault(i, Effect.Matcher.empty)
                        it.attributes.containsAll(require) && disjoint(it.attributes, exclude)
                    }
                }
            }

            if (playerResult is PlayerPool.SelectResult.Success && npcResult is PlayerPool.SelectResult.Success) {
                val players = EventLogic.calculateHarmless(
                    playerResult.selected + npcResult.selected,
                    action.applies
                )

                val branches = action.possibleOptions.filter { branch ->
                    players.withIndex().all { (i, it) ->
                        val (require, exclude) = branch.predicate.getOrDefault(i, Effect.Matcher.empty)
                        it.attributes.containsAll(require) && disjoint(it.attributes, exclude)
                    }
                }

                return Event.Playable(action, players, branches, action.idleOption)
            }
        }

        return null
    }

    private fun consumeNpcOnce(npcPool: PlayerPool, actions: List<Action.Simulable>): Event.Simulable? {
        for (action in actions.shuffled(r)) {
            if (npcPool.size < action.targetAmount) continue

            val npcResult = npcPool.select {
                for (i in 0 until action.targetAmount) {
                    random {
                        val (require, exclude) = action.predicate.getOrDefault(i, Effect.Matcher.empty)
                        it.attributes.containsAll(require) && disjoint(it.attributes, exclude)
                    }
                }
            }

            if (npcResult is PlayerPool.SelectResult.Success) {

                return when (action) {
                    is Action.Simulable.Harmful -> {
                        EventLogic.calculateHarmful(
                            npcResult.selected,
                            action.applies,
                            action.killed,
                            action.killers
                        ).let { (killedPlayers, alivePlayers) ->
                            Event.Simulable.Harmful(action, killedPlayers, alivePlayers)
                        }
                    }
                    is Action.Simulable.Harmless -> {
                        Event.Simulable.Harmless(
                            action,
                            EventLogic.calculateHarmless(npcResult.selected, action.applies)
                        )
                    }
                }
            }
        }

        return null
    }

    // internal math

    private fun currThreshold(players: List<Player>, day: Int): Double {
        var base =
            if (players.size == game.targetAmount) 1.0 else players.size.toDouble() / game.targetAmount.toDouble()

        if (base == 1.0) base = 1.0 - day.toDouble().sigmoid()

        return ((base.sin() * game.thresholdRt4 + base * 4 + 4) / 9).floor(100.0)
    }

}