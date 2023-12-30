import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.*


suspend fun clearCommands(kord: Kord){
    kord.getGlobalApplicationCommands().collect { it.delete() }
}
suspend fun registerSlashCommands(kord: Kord) {

    clearCommands(kord)

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
    kord.createGlobalChatInputCommand(
        "about",
        "A slash command that return a user if a user is mentionned or information about the bot"
    ){
        user("user", "The user") {
        }
    }
    kord.createGlobalChatInputCommand(
        "rejoindre",
        "Join the voice channel of the user"
    )

    kord.createGlobalChatInputCommand(
        "disconnect",
        "quitte le vocal"
    )

    kord.createGlobalChatInputCommand(
        "play",
        "Play a song"
    ){
        string("song", "The name of the song"){
            required = true
        }
    }


    kord.createGlobalChatInputCommand(
        "clear",
        "Clear messages",
    ){
        integer("number", "The number of messages to clear"){
            required = false
            minValue = 1
            maxValue = 100
        }

    }


    kord.createGlobalChatInputCommand(
        "assign_role",
        "Assign a role to a user",
    ){
        user("user", "The user") {
            required = true
        }
        role("role", "The role") {
            required = true
        }
    }


    kord.createGlobalChatInputCommand(
        "unassign_role",
        "Unassign a role from a user",
    ){
        user("user", "The user") {
            required = true
        }
        role("role", "The role") {
            required = true
        }
    }


}