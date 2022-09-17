package pw.aru.game.battleroyale

internal fun Double.floor(factor: Double = 1.0) = Math.floor(this * factor) / factor

internal fun Double.sigmoid() = 1 / (1 + Math.exp(-this))

internal fun Double.sin() = Math.sin(this)