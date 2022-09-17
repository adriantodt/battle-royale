package pw.aru.game.battleroyale.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import pw.aru.game.battleroyale.actions.Action
import pw.aru.game.battleroyale.actions.Effect
import pw.aru.hungergames.game.HarmfulAction
import pw.aru.hungergames.game.HarmlessAction
import pw.aru.hungergames.loader.parseHarmfulActions
import pw.aru.hungergames.loader.parseHarmlessActions
import java.io.File
import java.util.*

fun main() {
    val out = File("converter/battleroyale")

    println("Reading...")

    val actions = File("converter/hungergames").listFiles().mapNotNull { file ->
        val name = file.nameWithoutExtension.replace('_', '/')

        when {
            name.contains("harmful") -> {
                name to parseHarmfulActions(file.readLines()).map {
                    toBattleRoyale(it)
                }
            }
            name.contains("harmless") -> {
                name to parseHarmlessActions(file.readLines()).map {
                    toBattleRoyale(it)
                }
            }
            else -> null
        }
    }

    println()
    println("=-=-=- Converting to BattleRoyale format -=-=-=")

    val mapper = ObjectMapper()
    mapper.enable(SerializationFeature.INDENT_OUTPUT)

    val lookup = LinkedHashMap<String, LinkedList<String>>()

    for ((folderName, l) in actions) {
        val folder = File(out, folderName)
        folder.mkdirs()
        val parent = folderName.split('/').joinToString("/") { it.capitalize() }

        for (action in l) {
            println()

            when (action) {
                is Action.Simulable.Harmful -> {
                    println("[$parent - Harmful Action]")
                    println("Description: ${action.description}")
                    println("Killed: Players ${action.killed.joinToString()}")
                    if (action.killers.isEmpty()) {
                        println("No Killers")
                    } else {
                        println("Killers: Players ${action.killers.joinToString()}")
                    }
                }
                is Action.Simulable.Harmless -> {
                    println("[$parent - Harmless Action]")
                    println("Description: ${action.description}")
                }
            }

            val suggestions = lookup[action.description]?.mapIndexed { i, s -> "=${i + 1}" to s } ?: emptyList()

            if (suggestions.isNotEmpty()) {
                println("[suggested ids]:")
                suggestions.forEach { (i, s) ->
                    println(" '$i': $s")
                }
            }

            print("[id] > ")
            val input = readLine()!!

            val name = suggestions.toMap().getOrElse(input) { input.replace(' ', '_') }

            lookup.getOrPut(action.description, ::LinkedList).add(name)

            File(folder, "$name.json").writeText(mapper.writeValueAsString(action))

            println()
        }
    }
}

fun toBattleRoyale(it: HarmfulAction): Action.Simulable.Harmful {
    val (targetAmount, description, killed, killers, appliesEffects, predicateEffects) = it

    return Action.Simulable.Harmful(
        targetAmount,
        description,
        killed,
        killers,
        predicateEffects.mapValues { (_, v) -> toMatcher(v) },
        appliesEffects.mapValues { (_, v) -> toEffect(v) }
    )
}

fun toBattleRoyale(it: HarmlessAction): Action.Simulable.Harmless {
    val (targetAmount, description, appliesEffects, predicateEffects) = it

    return Action.Simulable.Harmless(
        targetAmount,
        description,
        predicateEffects.mapValues { (_, v) -> toMatcher(v) },
        appliesEffects.mapValues { (_, v) -> toEffect(v) }
    )
}

fun toMatcher(it: List<String>): Effect.Matcher {
    return Effect.Matcher(it, emptyList())
}

fun toEffect(it: pw.aru.hungergames.game.Effect): Effect {
    return Effect(it.add, it.remove)
}

