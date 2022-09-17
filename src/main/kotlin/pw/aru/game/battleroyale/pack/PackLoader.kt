package pw.aru.game.battleroyale.pack

import java.io.InputStream

class PackLoader(inputStream: InputStream) {

}

data class Pack(
    val name: String,
    val author: String,
    val description: String
)