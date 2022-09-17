package pw.aru.game.battleroyale.player

import java.util.*

class PlayerPool(players: List<Player>, private val r: Random = Random()) {
    private val pool: MutableList<Player> = ArrayList(players)

    val size: Int
        get() = pool.size

    val empty: Boolean
        get() = pool.isEmpty()

    fun select(function: Selector.() -> Unit): SelectResult {
        val s = Selector()

        return try {
            s.function()
            pool.removeAll(s.selected)
            SelectResult.Success(s.selected)
        } catch (_: SelectorNotFoundException) {
            SelectResult.Failed
        }
    }

    inner class Selector {
        private val internalPool: MutableList<Player> = ArrayList(pool)
        internal val selected: MutableList<Player> = ArrayList()

        @JvmOverloads
        fun first(amount: Int = 1, required: Boolean = true, predicate: (Player) -> Boolean = { true }): Boolean {
            val matches = internalPool.filter(predicate)
                .toList()

            if (matches.size < amount) {
                if (required) throw SelectorNotFoundException()
                else return false
            }

            val selection = matches.slice(0 until amount)
            internalPool.removeAll(selection)
            selected.addAll(selection)

            return true
        }

        @JvmOverloads
        fun random(amount: Int = 1, required: Boolean = true, predicate: (Player) -> Boolean = { true }): Boolean {
            val matches = internalPool.filter(predicate)
                .toList()

            if (matches.size < amount) {
                if (required) throw SelectorNotFoundException()
                else return false
            }

            val selection = matches.shuffled(r).slice(0 until amount)
            internalPool.removeAll(selection)
            selected.addAll(selection)

            return true
        }
    }

    sealed class SelectResult {
        object Failed : SelectResult()


        data class Success(
            val selected: List<Player>
        ) : SelectResult()
    }

    private class SelectorNotFoundException : RuntimeException()
}
