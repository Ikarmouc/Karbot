import dev.kord.common.Color
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.*
import kotlinx.coroutines.flow.*

var listSessions = mutableMapOf<Snowflake, Node>()
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
        var link: Link = lavalink.getLink(guildId.value)

        when (commandName) {
            "ping" -> {
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Requête Ping",
                            thumbnailUrl = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif",
                            footer = "",
                            description = "Ping vers l'api Discord : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms"
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
                embed.title = "Informations sur l'utilisateur : ${command.users["user"]?.username}"
                embed.description = "```" +
                        "Nom : ${customUsername}\n" +
                        "Identifiant : ${command.users["user"]?.username}\n ```"

                if (command.users["user"] != null) {
                    val thumbnail = EmbedBuilder.Thumbnail()
                    thumbnail.url = command.users["user"]?.avatar?.cdnUrl?.toUrl().toString()
                    embed.thumbnail = thumbnail

                    response.respond {
                        embeds = mutableListOf(embed)
                    }
                } else {
                    val pingDiscord: String = response.kord.gateway.averagePing?.inWholeMilliseconds.toString()
                    embed.title = "Informations sur le bot : "
                    embed.description = "```" + "Version Kord : 0.11.1 \n" + "Version bot : 0.0.3-alpha \n" + "```"
                    embed.field("Ping vers l'api Discord", false) {
                        "⬆⬇ $pingDiscord ms"
                    }
                    val thumdnail = EmbedBuilder.Thumbnail()
                    thumdnail.url = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif"
                    embed.thumbnail = thumdnail
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Informations sur le bot",
                                thumbnailUrl = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif",
                                footer = "",
                                description = "Version Kord : 0.11.1 \n" + "Version bot : 0.0.3-alpha \n"

                            )
                        )
                    }
                }
            }

            "rejoindre" -> {
                val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                if (voiceChannel == null) {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Erreur",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Vous n'etes pas dans un vocal"
                            )
                        )
                    }
                    return@on
                }
                if (connections.contains(ctx.data.guildId.value!!)) {
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                }
                val connection = voiceChannel.connect {
                }
                connections[ctx.data.guildId.value!!] = connection
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Rejoindre un vocal",
                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            footer = "",
                            description = "Le bot a rejoint le vocal !"
                        )
                    )
                }
                return@on

            }

            "play" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    val voiceChannel =
                        ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                    if (voiceChannel == null) {
                        response.respond {
                            embeds = mutableListOf(
                                embedMaker(
                                    title = "Erreur",
                                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                    footer = "",
                                    description = "Vous n'etes pas dans un vocal"
                                )
                            )
                        }
                        return@on
                    } else {
                        val connection = voiceChannel.connect {
                        }
                        connections[ctx.data.guildId.value!!] = connection
                        link = lavalink.getLink(guildId.value)
                    }
                }
                playMusic(lavalink, link, response, ctx, connections)
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
                if (isAutorized(ctx, Permission.ManageMessages)) {
                    var count = 0
                    val listMessage = ctx.getChannel().messages
                    if (listMessage.count() <= 1) {
                        response.respond {
                            embeds = mutableListOf(
                                embedMaker(
                                    title = "Erreur",
                                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                    footer = "",
                                    description = "Il n'y a pas de message"
                                )
                            )
                        }
                    } else {
                        if (command.integers["number"] == null) {
                            while (ctx.getChannelOrNull()?.messages?.count()!! > 1) {
                                ctx.getChannel().deleteMessage(ctx.getChannel().messages.last().id)
                                count++
                            }
                        } else {
                            while (count < command.integers["number"]!!) {
                                ctx.getChannel().deleteMessage(ctx.getChannel().messages.last().id)
                                count++
                            }
                        }
                        response.respond {
                            embeds = mutableListOf(
                                embedMaker(
                                    title = "Messages supprimés",
                                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                    footer = "",
                                    description = "```${count} messages supprimés```"
                                )
                            )
                        }
                    }
                } else {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Erreur",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Vous n'avez pas la permission de supprimer des messages"
                            )
                        )
                    }
                    return@on
                }

            }

            "assign_role" -> {
                if (isAutorized(ctx, Permission.ManageRoles)) {
                    val user = ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)
                    val listRolesUser =
                        ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)?.roles?.toList()
                    val role = ctx.command.roles["role"]!!.asRole()
                    if (listRolesUser != null) {
                        if (listRolesUser.contains(role)) {
                            response.respond {
                                embeds = mutableListOf(
                                    embedMaker(
                                        title = "Erreur",
                                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                        footer = "",
                                        description = "Le role est déja attribué à l'utilisateur"
                                    )
                                )
                            }
                        } else {
                            if (role.name == "@everyone") {
                                response.respond {
                                    embeds = mutableListOf(
                                        embedMaker(
                                            title = "Erreur",
                                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                            footer = "",
                                            description = "Vous ne pouvez pas ajouter le role everyone"
                                        )
                                    )
                                }
                            } else {

                                if (role.tags?.data?.botId == null) {
                                    ctx.command.users["user"]?.asMember(ctx.data.guildId.value!!)?.addRole(role.id)
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Role attribué",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Role ${role.name} ajouté pour ${user?.username}"
                                            )
                                        )
                                    }
                                    return@on
                                } else {
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Erreur",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Ce role ne peut pas etre attribué, il s'agit d'un role destiné a un bot"
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
                                title = "Erreur",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Vous n'avez pas la permission d'ajouter un role"
                            )
                        )
                    }
                    return@on
                }
            }

            "unassign_role" -> {
                if (isAutorized(ctx, Permission.ManageRoles)) {
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
                                            title = "Erreur",
                                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                            footer = "",
                                            description = "Vous ne pouvez pas supprimer le role everyone"
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
                                                title = "Role supprimé",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Role ${role.name} supprimé pour ${user?.username}"
                                            )
                                        )
                                    }
                                } else {
                                    response.respond {
                                        embeds = mutableListOf(
                                            embedMaker(
                                                title = "Erreur",
                                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                                footer = "",
                                                description = "Ce role ne peut pas etre supprimé, il s'agit d'un role destiné a un bot"
                                            )
                                        )
                                    }
                                }

                            }


                        } else {
                            response.respond {
                                embeds = mutableListOf(
                                    embedMaker(
                                        title = "Erreur",
                                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                        footer = "",
                                        description = "Le role n'est pas attribué à l'utilisateur"
                                    )
                                )
                            }
                        }
                    }
                } else {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Erreur",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Vous n'avez pas la permission de supprimer un role"
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
//                    link.player.pause(true)
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Musique en pause",
//                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                footer = "",
//                                description = "Musique en pause /resume pour reprendre"
//                            )
//                        )
//                    }


//                val voiceConnection = connections[ctx.data.guildId.value!!]
//                if (voiceConnection == null) {
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Erreur",
//                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                footer = "",
//                                description = "Le bot n'est pas connecté sur un vocal"
//                            )
//                        )
//                    }
//                    return@on
//                }
//                val link: Link = lavalink.getLink(guildId.value)
//                    link.player.pause(true)
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Musique en pause",
//                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                footer = "",
//                                description = "Musique en pause /resume pour reprendre"
//                            )
//                        )
//                    }
                // }
            }

            "resume" -> {
                if (checkVoiceConnection(connections, ctx, response, kord)) {
                    resumeMusic(link, response, kord)
                    return@on
                }

//                val voiceConnection = connections[ctx.data.guildId.value!!]
//                if (voiceConnection == null) {
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Erreur",
//                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                footer = "",
//                                description = "Le bot n'est pas connecté sur un vocal"
//                            )
//                        )
//                    }
//                }
//                val link: Link = lavalink.getLink(guildId.value)
//                link.player.pause(false)
//                response.respond {
//                    embeds = mutableListOf(
//                        embedMaker(
//                            title = "Musique reprise",
//                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                            footer = "",
//                            description = "Musique reprise /pause pour mettre en pause"
//                        )
//                    )
//                }
            }

            "info" -> {

                if(checkVoiceConnection(connections,ctx,response,kord)){
                    getPlayerInfo(link,kord,response)
                }
//                val voiceConnection = connections[ctx.data.guildId.value!!]
//                if (voiceConnection == null) {
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Erreur",
//                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                footer = "",
//                                description = "Le bot n'est pas connecté sur un vocal",
//
//                                )
//                        )
//                    }
//                }
//                if (link.player.playingTrack != null) {
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                title = "Informations sur le bot",
//                                thumbnailUrl = link.player.playingTrack!!.info.artworkUrl.toString(),
//                                footer = "Auteur : " + link.player.playingTrack!!.info.author,
//                                description = link.player.playingTrack!!.info.title,
//                            )
//                        )
//                    }
//                } else {
//                    response.respond {
//                        embeds = mutableListOf(
//                            embedMaker(
//                                "Erreur",
//                                kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                                "",
//                                "Il n'y a pas de musique en cours de lecture"
//                            )
//                        )
//                    }
//                }
            }

            "disconnect" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Erreur",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Le bot n'est pas connecté sur un vocal"
                            )
                        )
                    }
                } else {
                    link.player.stopTrack()
                    link.destroy()
                    voiceConnection.disconnect()
                    queueList[guildId.value]!!.clearQueue()
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Bot deconnecté",
                                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                                footer = "",
                                description = "Le bot est maintenant deconnecté"
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

            else -> {
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Erreur",
                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            footer = "",
                            description = "Erreur de commande, veuillez reessayer"
                        )
                    )
                }
                error("Erreur de commande, veuillez reessayer")

            }
        }




    }

}



