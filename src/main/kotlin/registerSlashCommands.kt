import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.user

suspend fun registerSlashCommands(kord: Kord) {

    kord.getGlobalApplicationCommands().collect { it.delete() }

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
    kord.createGlobalChatInputCommand("liste_des_membres", "A slash command that lists all users")
    kord.createGlobalChatInputCommand(
        "about",
        "A slash command that return a user if a user is mentionned or information about the bot"
    ){
        user("user", "The user") {
        }
    }



}