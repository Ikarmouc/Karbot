import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on

suspend fun globalChatCommandlistener(kord: Kord){

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()
        if (interaction == null) {
            return@on
        }
        val command = interaction.command
        when(command.data.name.value){
            "ping" -> {
                val resultat : String = "```Ping vers l'api Discord : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms```"
                response.respond {
                    content = resultat
                }
            }
            "sum" -> {
                val firstNum = command.integers["first_num"]!!
                val secondNum = command.integers["second_num"]!!
                val result: String = sum(firstNum,secondNum)
                response.respond {
                    content = result
                }
            }
            else -> {
                val resultat : String ="Erreur"
                response.respond {
                    error("Erreur de commande, veuillez reessayer")
                }
            }
        }
    }
}