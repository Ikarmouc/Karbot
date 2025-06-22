package ChatCommands

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import utility.embedMaker

suspend fun sendMessage(
    title: String,
    message: String,
    channel : Snowflake,
    kord: dev.kord.core.Kord
) {
    MessageChannelBehavior(channel,kord).createMessage {
        embeds = mutableListOf(embedMaker(title = title,
            description = message,
            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
            footer = "Karbot - Multi-purpose bot",
        ))
    }
}