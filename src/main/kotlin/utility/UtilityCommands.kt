package utility

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.last

fun embedMaker(title: String,
               thumbnailUrl: String,
               footer: String,
               description: String,
               fields: MutableList<EmbedBuilder.Field> = mutableListOf()
               ): EmbedBuilder {
    val embed = EmbedBuilder()

    embed.color = Color(blue = 255, green = 0, red = 0)
    if (thumbnailUrl != "") {
        embed.thumbnail = EmbedBuilder.Thumbnail()
        embed.thumbnail!!.url = thumbnailUrl
    }
    if (title != "") {
        embed.title = title
    } else {
        embed.title = "Karbot - Music Bot and Administration bot"
    }
    if (footer != "") {
        embed.footer = EmbedBuilder.Footer()
        embed.footer!!.text = footer
    }
    if (description != "") {
        embed.description = description
    }
    if(fields.isNotEmpty()){
        embed.fields = fields
    }
    return embed
}

suspend fun isAuthorized(ctx: Interaction, permission: Permission): Boolean {
    println(ctx.user.asMember(ctx.data.guildId.value!!).getPermissions().values)
    return ctx.user.asMember(ctx.data.guildId.value!!).getPermissions().contains(permission) || ctx.user.asMember(ctx.data.guildId.value!!).getPermissions().contains(Permission.Administrator)
}

suspend fun clearMessages(
    ctx : ChatInputCommandInteraction,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord,
    command : InteractionCommand
){
    if (isAuthorized(ctx, Permission.ManageMessages)) {
        var count = 0
        val listMessage = ctx.getChannel().messages
        if (listMessage.count() <= 1) {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "There are no messages to delete"
                    )
                )
            }
        } else {
            if (command.integers["number"] == null) {
                while (ctx.getChannelOrNull()?.messages?.count()!! > 1) {
                    ctx.getChannel().deleteMessage(ctx.getChannel().messages.last().id)
                    delay(1000)
                    count++
                }
            } else {
                while (count < command.integers["number"]!!) {
                    ctx.getChannel().deleteMessage(ctx.getChannel().messages.last().id)
                    delay(1000)
                    count++
                }
            }
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Messages deleted",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "```${count} messages deleted```"
                    )
                )
            }
            delay(5000)
            ctx.getChannel().deleteMessage(ctx.getChannel().messages.last().id)
            return
        }
    } else {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "You don't have the permission to delete messages"
                )
            )
        }
        return
    }
}



