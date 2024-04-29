package listeners

import dev.kord.common.Color
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.*
import dotenv
import utility.embedMaker
import getWeather
import utility.isAuthorized
import kotlinx.coroutines.flow.*
import music.*
import utility.clearMessages

var queueList = mutableMapOf<ULong, Queue>()
@OptIn(KordVoice::class)
suspend fun globalChatCommandlistener(
    kord: Kord,
    connections: MutableMap<Snowflake, VoiceConnection>,
    lavalink: LavaKord
) {

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()
        val command = interaction.command
        val ctx = interaction
        val commandName = command.data.name.value
        val guildId: Snowflake = ctx.data.guildId.value!!
        val link: Link = lavalink.getLink(guildId.value)

        when (commandName) {
            "ping" -> {
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Ping request",
                            thumbnailUrl = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif",
                            footer = "",
                            description = "Discord Api latency : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms"
                        )
                    )
                }
            }

            "about" -> {
                val embed = EmbedBuilder()
                embed.color = Color(0, 0, 0)
                var customUsername = command.users["user"]?.globalName
                if (customUsername == null) {
                    customUsername = command.users["user"]?.username
                }
                embed.title = "About user : ${command.users["user"]?.username}"
                embed.description = "```" +
                        "Server name : ${customUsername}\n" +
                        "Id : ${command.users["user"]?.username}\n ```"

                if (command.users["user"] != null) {
                    val thumbnail = EmbedBuilder.Thumbnail()
                    thumbnail.url = command.users["user"]?.avatar?.cdnUrl?.toUrl().toString()
                    embed.thumbnail = thumbnail

                    response.respond {
                        embeds = mutableListOf(embed)
                    }
                } else {
                    val pingDiscord: String = response.kord.gateway.averagePing?.inWholeMilliseconds.toString()
                    embed.title = "About the bot : "
                    embed.description = "```" + "Kord version : 0.11.1 \n" + "${dotenv.get("BOT_VERSION")}\n" + "```"
                    embed.field("Discord api latency", false) {
                        "⬆⬇ $pingDiscord ms"
                    }
                    val thumdnail = EmbedBuilder.Thumbnail()
                    thumdnail.url = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif"
                    embed.thumbnail = thumdnail
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "About the bot",
                                thumbnailUrl = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif",
                                footer = "",
                                description = "Kord version : 0.11.1 \n" + "Bot version: ${dotenv.get("BOT_VERSION")}\n"
                            )
                        )
                    }
                }
            }

            "join" -> {
                joinChannel(
                    ctx = ctx,
                    kord = kord,
                    connections = connections,
                    response = response
                )
            }

            "play" -> {
//                val voiceConnection = connections[ctx.data.guildId.value!!]
//                if (voiceConnection == null) {
//                    val voiceChannel =
//                        ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
//                    if (voiceChannel == null) {
//                        response.respond {
//                            embeds = mutableListOf(
//                                embedMaker(
//                                    title = "Error",
//                                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                    footer = "",
//                                    description = "You must be in a vocal channel to use this command"
//                                )
//                            )
//                        }
//                        return@on
//                    } else {
//                        val connection = voiceChannel.connect {
//                        }
//                        connections[ctx.data.guildId.value!!] = connection
//                        link = lavalink.getLink(guildId.value)
//                    }
//                }
//                joinChannel(
//                    ctx = ctx,
//                    kord = kord,
//                    connections = connections,
//                    response = response
//                )
                playMusic(lavalink = lavalink, response = response, ctx = ctx, connections = connections)
                return@on
            }

            "skip" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    skipMusic(link, ctx, response, kord)
                }
            }

            "stop" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    stopMusic(link, ctx, response, kord)
                }
            }

            "clear" -> {
                clearMessages(
                    ctx = ctx,
                    response = response,
                    kord = kord,
                    command = command
                )
            }
            "assign_role" -> {
                if (isAuthorized(ctx, Permission.ManageRoles)) {
                    val user = ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)
                    val listRolesUser =
                        ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)?.roles?.toList()
                    val role = ctx.command.roles["role"]!!.asRole()
                    if (listRolesUser != null) {
                        if (listRolesUser.contains(role)) {
                            response.respond {
                                embeds = mutableListOf(
                                    embedMaker(
                                        title = "Error",
                                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                        footer = "",
                                        description = "Role is already assigned to the user"
                                    )
                                )
                            }
                        } else {
                            if (role.name == "@everyone") {
                                response.respond {
                                    embeds = mutableListOf(
                                        embedMaker(
                                            title = "Error",
                                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                            footer = "",
                                            description = "You can't assign the @everyone role"
                                        )
                                    )
                                }
                            } else {

                                if (role.tags?.data?.botId == null) {
                                    ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)?.addRole(role.id)
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Role added",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Role ${role.name} added for ${user?.username}"
                                            )
                                        )
                                    }
                                    return@on
                                } else {
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Error",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "This role can't be added, it's a bot role"
                                            )
                                        )
                                    }
                                    return@on
                                }

                            }

                        }
                    }
                } else {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Error",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "You don't have the permission to assign a role"
                            )
                        )
                    }
                    return@on
                }
            }

            "unassign_role" -> {
                if (isAuthorized(ctx, Permission.ManageRoles)) {
                    val user = ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)
                    val listRolesUser =
                        ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)?.roles?.toList()

                    val role = ctx.command.roles["role"]!!.asRole()

                    if (listRolesUser != null) {
                        if (listRolesUser.contains(role)) {
                            if (role.name == "@everyone") {
                                response.respond {
                                    embeds = mutableListOf(
                                        embedMaker(
                                            title = "Error",
                                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                            footer = "",
                                            description = "You can't remove the @everyone role"
                                        )
                                    )
                                }
                            } else {
                                if (role.tags?.data?.botId == null) {
                                    ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)
                                        ?.removeRole(role.id)
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Role removed",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Role ${role.name} removed for ${user?.username}"
                                            )
                                        )
                                    }
                                } else {
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Error",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "You can't remove a bot role"
                                            )
                                        )
                                    }
                                }

                            }


                        } else {
                            response.respond {
                                embeds = mutableListOf(
                                    embedMaker(
                                        title = "Error",
                                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                        footer = "",
                                        description = "Role is not assigned to the user"
                                    )
                                )
                            }
                        }
                    }
                } else {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Error",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "You don't have the permission to remove a role"
                            )
                        )
                    }
                    return@on
                }
            }

            "pause" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    pauseMusic(link, response, kord)
                    return@on
                }
            }

            "resume" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    resumeMusic(link, response)
                    return@on
                }
            }

            "info" -> {

                if(checkVoiceConnection(connections,ctx,response,kord)){
                    getPlayerInfo(link,kord,response)
                }
            }

            "disconnect" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Error",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "The bot is not connected to a vocal channel"
                            )
                        )
                    }
                } else {
                    link.player.stopTrack()
                    link.destroy()
                    voiceConnection.disconnect()
                    if(queueList.contains(guildId.value)){
                        queueList[guildId.value]!!.clearQueue()
                    }
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Bot disconnected",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "The bot has been disconnected from the vocal channel"
                            )
                        )
                    }
                }
            }

            "queue" -> {
                if(checkVoiceConnection(connections,ctx,response,kord)){
                    getQueueList(ctx,kord,response)
                }
            }

            "volume" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    setVolume(link, response, kord, ctx)
                }
            }

            "shuffle" -> {
                if(checkVoiceConnection(connections,ctx,response,kord)){
                    shuffleQueue(ctx,kord,response)
                }
            }

            "repeat" -> {
                if(checkVoiceConnection(connections,ctx,response,kord)){
                    repeatMode(ctx,kord,response)
                }
            }


            "insert" -> {
                if(checkVoiceConnection(connections,ctx,response,kord)){
                    insertTrack(ctx,kord,response,lavalink)
                }
            }

            "weather_info" -> {
                val city = command.strings["city"].toString().uppercase()
                getWeather(city, response)
            }
            else -> {
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Error",
                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            footer = "",
                            description = "Command error, please try again"
                        )
                    )
                }
                error("Error with command $commandName, no such command exists.")

            }
        }




    }

}



