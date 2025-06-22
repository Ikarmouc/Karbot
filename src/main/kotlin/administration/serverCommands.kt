package administration

import dev.kord.core.Kord
import dev.kord.core.behavior.ban
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import kotlinx.coroutines.flow.toList
import utility.embedMaker

suspend fun banUser(kord: Kord,
                    ctx : ChatInputCommandInteraction,
                    user: User?,
                    response: DeferredPublicMessageInteractionResponseBehavior,
                    banReason: String) {
    // Ban the user
    if (user != null) {
        if(ctx.user.id == user.id){
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = "",
                        footer = "Requested by ${ctx.user.username}",
                        description = "You can't ban yourself"
                    )
                )
            }
            return
        }
        if(kord.getGuild(ctx.data.guildId.value!!).ownerId == user.id){
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = "",
                        footer = "Requested by ${ctx.user.username}",
                        description = "You can't ban the owner of the server"
                    )
                )
            }
            return
        }
        if(user.id == kord.selfId){
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = "",
                        footer = "Requested by ${ctx.user.username}",
                        description = "You can't ban me ( ͡° ͜ʖ ͡°)"
                    )
                )
            }
            return
        }
        else{
            user.asMember(ctx.data.guildId.value!!).ban(
                builder = {
                    reason = banReason
                }
            )
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "User banned",
                        thumbnailUrl = "",
                        footer = "Requested by ${ctx.user.username}",
                        description = "User ${user.username} has been banned for the following reason : \n $banReason, he can be unbanned with the server setting on Discord"
                    )
                )
            }
        }
    }

}

suspend fun kickUser(kord: Kord,
                    ctx : ChatInputCommandInteraction,
                    user: User?,
                    response: DeferredPublicMessageInteractionResponseBehavior,
                    kickReason: String) {

    if(user == null){
        response.respond {
            embeds = mutableListOf(embedMaker(
                title = "Error",
                thumbnailUrl = "",
                footer = "Requested by ${ctx.user.username}",
                description = "User not found"
            ))
        }
        return
    }

    if(kord.selfId == user.id){
        response.respond {
            embeds = mutableListOf(embedMaker(
                title = "Error",
                thumbnailUrl = "",
                footer = "Requested by ${ctx.user.username}",
                description = "You can't kick me ( ͡° ͜ʖ ͡°)"
            ))
        }
        return
    }

    if(kord.getGuild(ctx.data.guildId.value!!).ownerId == user.id) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Error",
                    thumbnailUrl = "",
                    footer = "Requested by ${ctx.user.username}",
                    description = "You can't kick the owner of the server"
                )
            )
        }
        return
    }
    else{
        user.asMember(ctx.data.guildId.value!!).kick(reason = kickReason)
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "User kicked",
                    thumbnailUrl = "",
                    footer = "Requested by ${ctx.user.username}",
                    description = "User ${user.username} has been kicked for the following reason : \n $kickReason, he can be unbanned with the server setting on Discord"
                )
            )
        }
    }
}

suspend fun listBansUser(ctx : ChatInputCommandInteraction,
                         kord: Kord,
                         response: DeferredPublicMessageInteractionResponseBehavior) {

    val list = kord.getGuild(ctx.data.guildId.value!!).asGuild().bans

    val result = "Username : Reason \n" + list.toList().map { it.user.asUser().username to (it.reason?.ifEmpty { "No reason" } ?: "No reason") }.joinToString("\n").replace("(", "").replace(")", "").replace(",",":")

    if(list.toList().isEmpty()){
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "List of banned users",
                    thumbnailUrl = "",
                    footer = "Requested by ${ctx.user.username}",
                    description = "No banned users on this server"
                )
            )
        }
    }else{
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "List of banned users",
                    thumbnailUrl = "",
                    footer = "Requested by ${ctx.user.username}",
                    description = result,
                )
            )
        }
    }

}