package pw.aru.game.battleroyale

import pw.aru.game.battleroyale.actions.Action

data class PhaseActionStore(
    val harmlessActions: List<Action.Simulable.Harmless>,
    val harmfulActions: List<Action.Simulable.Harmful>,
    val playableActions: List<Action.Playable>
)