package utility

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.core.entity.interaction.Interaction
import dev.kord.rest.builder.message.EmbedBuilder

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

suspend fun isAutorized(ctx: Interaction, perm: Permission): Boolean {
    return ctx.user.asMember(ctx.data.guildId.value!!).getPermissions().contains(perm)
}

