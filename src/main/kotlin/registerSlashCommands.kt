import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.integer

suspend fun registerSlashCommands(kord: Kord) {

    kord.createGlobalChatInputCommand(
        "ping",
        "Return the ping of the bot"
    )

    kord.createGlobalChatInputCommand(
        "sum",
        "A slash command that sums two numbers"
    ) {

        integer("first_num", "The first number") {
            required = true
        }
        integer("second_num", "The second number") {
            required = true
        }
    }

    
}