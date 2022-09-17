package pw.aru.game.battleroyale.logic

import pw.aru.game.battleroyale.actions.PlayerActionBranch
import pw.aru.game.battleroyale.events.Event
import java.util.*

class PlayerInputReceiver {
    val inputStack = LinkedList<Event.Playable>()
    val doneStack = LinkedList<Event.Played>()

    fun done(): Boolean {
        return inputStack.isEmpty()
    }

    fun next(): Event.Playable {
        if (done()) {
            throw IllegalStateException("No more inputs required")
        }

        return inputStack.peek()
    }

    fun applyInput(chosenInput: PlayerActionBranch) {
        val (action, players, availableBranches, idleOption) = inputStack.peek()

        if (!availableBranches.contains(chosenInput) && chosenInput != idleOption) {
            throw IllegalArgumentException("Illegal input")
        }

        inputStack.pop()

        doneStack.addLast(
            when (chosenInput) {
                is PlayerActionBranch.Harmful -> {
                    EventLogic.calculateHarmful(
                        players,
                        chosenInput.applies,
                        chosenInput.killed,
                        chosenInput.killers
                    ).let { (killedPlayers, alivePlayers) ->
                        Event.Played.Harmful(action, chosenInput, alivePlayers, killedPlayers)
                    }
                }
                is PlayerActionBranch.Harmless -> {
                    Event.Played.Harmless(
                        action, chosenInput, EventLogic.calculateHarmless(players, action.applies)
                    )
                }
            }
        )
    }
}