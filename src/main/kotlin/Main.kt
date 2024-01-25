import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import kotlin.time.Duration.Companion.seconds

fun sum(firstNum: Long, secondNum: Long): String {
    return "${firstNum + secondNum}"
}
@OptIn(KordVoice::class)
suspend fun main(args: Array<String>) {
    val kord = Kord(token = args[0])
    //val lavaplayerManager = DefaultAudioPlayerManager()
    val connections : MutableMap<Snowflake, VoiceConnection> = mutableMapOf() //Map contenant les links de chacun des serveurs
    val lavalink = connectLavalink(kord)
    registerSlashCommands(kord)
    globalChatCommandlistener(kord, connections,lavalink)
    globalMessageListener(kord,connections)
    voiceActivityListener(kord,connections,lavalink)
    println("Bot is now running try /about for more information")
    kord.login{
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        presence {
            status = PresenceStatus.from("online")
            watching("Vos commandes (/help)")
        }


    }
}

fun connectLavalink(kord : Kord) : LavaKord {
    val lavalink: LavaKord
    lavalink = kord.lavakord() {
        link {
            autoReconnect = false
            retry = linear(5.seconds, 20.seconds, 10)
        }
    }
    lavalink.addNode("ws://localhost:2333", "KarbotLavalink","Lavalink")
    return lavalink
}