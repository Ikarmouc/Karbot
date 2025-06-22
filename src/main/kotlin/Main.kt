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
import io.github.cdimascio.dotenv.Dotenv
import listeners.globalChatCommandlistener
import listeners.globalChatListener
import listeners.globalMessageListener
import listeners.voiceActivityListener
import utility.registerAutoCompleteCommands
import utility.registerSlashCommands

@OptIn(KordVoice::class)
suspend fun main() {
    val dotenv = Dotenv.load()
    if(dotenv.get("BOT_TOKEN") == null){
        println("Please create a .env file with a BOT_TOKEN variable")
        return
    }
    val kord = Kord(token = dotenv.get("BOT_TOKEN")!!)
    val presenceText: String
    val connections : MutableMap<Snowflake, VoiceConnection> = mutableMapOf() //Map contenant les links de chacun des serveurs
    val lavalink = connectLavalink(kord)
    if(dotenv.get("DEV_MODE") == "true"){
        presenceText = "Dev mode"
        kord.getGlobalApplicationCommands().collect {it.delete()}
        registerSlashCommands(kord, dotenv)
    }else{
        presenceText = "Vos commandes (/help)"
    }
    registerAutoCompleteCommands(kord)
    globalChatListener(kord)
    globalChatCommandlistener(kord, connections,lavalink)
    globalMessageListener(kord)
    voiceActivityListener(kord, lavalink)
    println("Bot is now running try /about for more information")
    kord.login{
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        presence {
            status = PresenceStatus.from("online")
            watching(presenceText)
        }
    }
}

fun connectLavalink(kord : Kord) : LavaKord {
    val lavalink: LavaKord = kord.lavakord() {
        link {
            autoReconnect = true
            autoMigrateOnDisconnect = false
            resumeTimeout = 0
            retry = linear(5.seconds, 20.seconds, 20)

        }
    }
    lavalink.addNode("ws://localhost:2333", "KarbotLavalink","Lavalink")
    return lavalink

}