package pw.aru.game.battleroyale

data class BattleRoyaleEnvironment(
    val bloodbathActions: PhaseActionStore,
    val dayActions: PhaseActionStore,
    val nightActions: PhaseActionStore,
    val feastActions: PhaseActionStore
)

