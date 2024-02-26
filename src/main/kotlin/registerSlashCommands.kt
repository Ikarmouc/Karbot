import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.*



//suspend fun clearCommands(kord: Kord){
//    kord.getGlobalApplicationCommands().collect { it.delete() }
//}
suspend fun registerSlashCommands(kord: Kord) {

    //clearCommands(kord)

    kord.createGlobalChatInputCommand(
        "ping",
        "Return the ping of the bot"
    )

    kord.createGlobalChatInputCommand(
        "about",
        "A slash command that return a user if a user is mentionned or information about the bot"
    ){
        user("user", "The user") {
        }
    }
    kord.createGlobalChatInputCommand(
        "join",
        "Join the voice channel of the user"
    )

    kord.createGlobalChatInputCommand(
        "disconnect",
        "Disconnect from the vocal channel"
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

    kord.createGlobalMessageCommand(
        "delete message"
    ){
        
    }

    kord.createGlobalChatInputCommand (
        "stop",
        "stop the music"
    )

    kord.createGlobalChatInputCommand(
        "pause",
        "pause the music"
    )

    kord.createGlobalChatInputCommand(
        "resume",
        "resume the music"
    )

    kord.createGlobalChatInputCommand(
        "skip",
        "skip the music"
    )

    kord.createGlobalChatInputCommand(
        "info",
        "get the track info"
    )

    kord.createGlobalChatInputCommand(
        "queue",
        "get the queue"
    )

    kord.createGlobalChatInputCommand(
        "volume",
        "set the volume"
    ){
        integer("volume", "The volume"){
            required = true
            minValue = 0
            maxValue = 100
        }
    }

    kord.createGlobalChatInputCommand(
        "shuffle",
        "shuffle the queue"
    )

    kord.createGlobalChatInputCommand(
        "repeat",
        "repeat the queue"
    ){
        boolean("repeat", "repeat the queue"){
            required = true

        }
    }

    kord.createGlobalChatInputCommand(
        "insert",
        "insert a track in the queue right after the current track"
    ){
        string("track", "The track to insert"){
            required = true
        }
    }
}