import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.common.entity.PresenceStatus


public fun sum(firstNum: Long, secondNum: Long): String {
    return "${firstNum + secondNum}"
}
suspend fun main(args: Array<String>) {
    val kord = Kord(token = args[0])
    registerSlashCommands(kord)
    globalChatCommandlistener(kord)
    kord.login{
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        presence {
            status = PresenceStatus.from("online")
        }
    }
}