package administration

import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import utility.embedMaker

suspend fun renameUser(kord: Kord,
                              ctx : ChatInputCommandInteraction,
                              user: User?,
                              response: DeferredPublicMessageInteractionResponseBehavior,
                              newName: String){

    // Rename the user
    if(user == null){
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Error",
                    thumbnailUrl = "",
                    description = "User not found",
                    footer = "Please provide a valid user id",
                ))
        }
        return
    }else{

        user.asMember(ctx.data.guildId.value!!).edit {
            nickname = newName
        }

//        kord.getUser(userId)?.asUser()?.asMember(ctx.invokedCommandGuildId!!)?.edit {
//            nickname = newName
//        }
    }



}
