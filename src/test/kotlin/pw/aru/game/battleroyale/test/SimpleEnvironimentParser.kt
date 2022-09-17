package pw.aru.game.battleroyale.test

import org.json.JSONObject
import org.json.JSONTokener
import pw.aru.game.battleroyale.BattleRoyaleEnvironment
import pw.aru.game.battleroyale.PhaseActionStore
import pw.aru.game.battleroyale.actions.Action
import pw.aru.game.battleroyale.actions.Effect
import pw.aru.game.battleroyale.actions.PlayerActionBranch
import java.io.File

fun parseEnvironment(file: File): BattleRoyaleEnvironment {
    return BattleRoyaleEnvironment(
        parseActions(file.resolve("bloodbath")),
        parseActions(file.resolve("day")),
        parseActions(file.resolve("night")),
        parseActions(file.resolve("feast"))
    )
}

fun parseActions(file: File): PhaseActionStore {
    val harmful = file.resolve("harmful").listFiles() ?: emptyArray()
    val harmless = file.resolve("harmless").listFiles() ?: emptyArray()
    val playable = file.resolve("playable").listFiles() ?: emptyArray()

    return PhaseActionStore(
        harmless.map(::fileToJSON).map(::parseHarmlessAction),
        harmful.map(::fileToJSON).map(::parseHarmfulAction),
        playable.map(::fileToJSON).map(::parsePlayableAction)
    )
}

fun fileToJSON(file: File): JSONObject {
    return JSONObject(JSONTokener(file.reader()))
}

fun parseHarmlessAction(cfg: JSONObject): Action.Simulable.Harmless {
    val targetAmount = cfg.getInt("targetAmount")
    val predicate = if (cfg.has("predicate")) parsePredicate(cfg.getJSONObject("predicate")) else emptyMap()
    val description = cfg.getString("description")
    val applies = if (cfg.has("applies")) parseApplies(cfg.getJSONObject("applies")) else emptyMap()

    return Action.Simulable.Harmless(targetAmount, description, predicate, applies)
}

fun parseHarmfulAction(cfg: JSONObject): Action.Simulable.Harmful {
    val targetAmount = cfg.getInt("targetAmount")
    val predicate = if (cfg.has("predicate")) parsePredicate(cfg.getJSONObject("predicate")) else emptyMap()
    val description = cfg.getString("description")
    val applies = if (cfg.has("applies")) parseApplies(cfg.getJSONObject("applies")) else emptyMap()

    val killed = cfg.getIntList("killed")
    val killers = if (cfg.has("killers")) cfg.getIntList("killers") else emptyList()

    return Action.Simulable.Harmful(targetAmount, description, killed, killers, predicate, applies)
}

fun parsePlayableAction(cfg: JSONObject): Action.Playable {
    val targetAmount = cfg.getInt("targetAmount")
    val playerAmount = cfg.getInt("playerAmount")
    val predicate = if (cfg.has("predicate")) parsePredicate(cfg.getJSONObject("predicate")) else emptyMap()
    val description = cfg.getString("description")
    val applies = if (cfg.has("applies")) parseApplies(cfg.getJSONObject("applies")) else emptyMap()

    val idleOption = parseBranch(cfg.getJSONObject("idleOption"))
    val options = if (cfg.has("options")) cfg.getJSONArray("options").map { parseBranch(it as JSONObject) } else emptyList()

    return Action.Playable(targetAmount, description, playerAmount, options, idleOption, predicate, applies)
}

fun parseBranch(cfg: JSONObject): PlayerActionBranch {
    val predicate = if (cfg.has("predicate")) parsePredicate(cfg.getJSONObject("predicate")) else emptyMap()
    val result = cfg.getString("result")
    val applies = if (cfg.has("applies")) parseApplies(cfg.getJSONObject("applies")) else emptyMap()

    return if (cfg.has("killed")) {
        val killed = cfg.getIntList("killed")
        val killers = if (cfg.has("killers")) cfg.getIntList("killers") else emptyList()

        PlayerActionBranch.Harmful(result, killed, killers, predicate, applies)
    } else {
        PlayerActionBranch.Harmless(result, predicate, applies)
    }
}

fun parsePredicate(obj: JSONObject): Map<Int, Effect.Matcher> {
    return obj.keySet().map {
        it.rmSuffix().toInt() to parseMatcher(obj.getJSONObject(it))
    }.toMap()
}

fun parseApplies(obj: JSONObject): Map<Int, Effect> {
    return obj.keySet().map {
        it.rmSuffix().toInt() to parseEffect(obj.getJSONObject(it))
    }.toMap()
}

private fun String.rmSuffix(): String {
    return when {
        endsWith("player", true) -> toLowerCase().removeSuffix("player")
        endsWith("p", true) -> toLowerCase().removeSuffix("p")
        else -> this
    }
}

fun parseMatcher(cfg: JSONObject): Effect.Matcher {
    return Effect.Matcher(cfg.getStringList("require"), cfg.getStringList("exclude"))
}

fun parseEffect(cfg: JSONObject): Effect {
    return Effect(cfg.getStringList("add"), cfg.getStringList("remove"))
}

private fun JSONObject.getIntList(key: String): List<Int> {
    return getJSONArray(key).mapNotNull { (it as? Number)?.toInt() }
}

private fun JSONObject.getStringList(key: String): List<String> {
    return getJSONArray(key).mapNotNull { it as? String }
}