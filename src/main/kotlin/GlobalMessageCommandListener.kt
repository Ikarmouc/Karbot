import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GlobalMessageCommandInteractionCreateEvent
import dev.kord.core.on
suspend fun globalMessageListener(kord: Kord){

    kord.on<GlobalMessageCommandInteractionCreateEvent> {

        val response = interaction.deferPublicResponse()
        val commandName = interaction.invokedCommandName
        println(commandName)
        response.respond {
            content = "test"
        }


    }
}