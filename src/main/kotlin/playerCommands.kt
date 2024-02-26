import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.rest.loadItem


@OptIn(KordVoice::class)
suspend fun playMusic(
    lavalink: LavaKord,
    link: Link,
    response: DeferredPublicMessageInteractionResponseBehavior,
    ctx: ChatInputCommandInteraction,
    connections: MutableMap<Snowflake, VoiceConnection>
) {
    val guildId = ctx.data.guildId.value!!
    val song = ctx.command.strings["song"].toString()
    val query: String = if (song.contains("https://")) {
        song
    } else {
        "ytsearch: $song"
    }
    val item = lavalink.nodes[0].loadItem(query)
    val voiceChan = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceState().getChannelOrNull()
    if (voiceChan == null) {
        response.respond {
            embeds = mutableListOf(embedMaker("Erreur", "", "", "Vous n'etes pas sur un vocal !"))
        }

    } else {
        if (!listSessions.contains(guildId)) {
            link.connectAudio(voiceChan.id.value)
            connections[guildId]!!.connect()
            listSessions[guildId] = link.node
        } else {
            //link.onNewSession(listSessions.get(guildId)!!)
        }

    }

    when (item) {


        is LoadResult.TrackLoaded -> {
            if (link.player.playingTrack == null) {
                println("Playing track on guild " + ctx.getChannel().asChannel().data.name.value!!)
                link.player.playTrack(item.data)
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Musique en cours de lecture",
                            thumbnailUrl = item.data.info.artworkUrl.toString(),
                            footer = "Author : " + item.data.info.author,
                            description = item.data.info.title
                        )
                    )
                }
                return

            } else {
                if (queueList.contains(guildId.value)) {
                    queueList[guildId.value]!!.add(item.data)
                } else {
                    queueList[guildId.value] = Queue()
                    queueList[guildId.value]!!.add(item.data)
                }
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Musique ajoutée dans la file d'attente",
                            thumbnailUrl = item.data.info.artworkUrl.toString(),
                            footer = item.data.info.author, description = item.data.info.title
                        )
                    )
                }
                return
            }


        }

        is LoadResult.PlaylistLoaded -> {

            val playlist = item.data.tracks
            playlist.toMutableList().removeFirst()

            if (queueList.contains(guildId.value)) {
                queueList[guildId.value]!!.addAll(playlist)
            } else {
                queueList[guildId.value] = Queue()
                queueList[guildId.value]!!.addAll(playlist)
            }

            if (link.player.playingTrack == null) {

                link.player.playTrack(queueList[guildId.value]!!.get(0))
                queueList[guildId.value]!!.removeAt(0)
            }
            println("Playlist size : " + queueList[guildId.value]!!.size)
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        "Playlist chargee !",
                        "",
                        "",
                        "Voir /queue pour voir la playlist !"
                    )
                )
            }

        }

        is LoadResult.SearchResult -> {

            if (!queueList.contains(guildId.value)) {
                queueList[guildId.value] = Queue()
            }
            if (queueList[guildId.value]!!.isQueueEmpty()) {
                if (link.player.playingTrack == null) {
                    link.player.playTrack(item.data.tracks[0])
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Musique en cours de lecture",
                                thumbnailUrl = item.data.tracks[0].info.artworkUrl.toString(),
                                footer = "Author : " + item.data.tracks[0].info.author,
                                description = item.data.tracks[0].info.title
                            )
                        )
                    }
                    return
                } else {
                    if (queueList.contains(guildId.value)) {
                        queueList[guildId.value]!!.add(item.data.tracks[0])
                    } else {
                        queueList[guildId.value] = Queue()
                        queueList[guildId.value]!!.add(item.data.tracks[0])
                    }
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Musique ajoutée dans la file d'attente",
                                thumbnailUrl = item.data.tracks[0].info.artworkUrl.toString(),
                                footer = "Author : " + item.data.tracks[0].info.author,
                                description = item.data.tracks[0].info.title
                            )
                        )
                    }
                    return
                }
            }

        }

        is LoadResult.NoMatches -> {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        "Erreur",
                        "",
                        "",
                        "Aucun résultat pour cette requête. Veuillez reessayer !"
                    )
                )
            }
        }

        is LoadResult.LoadFailed -> {
            response.respond {
                embeds = mutableListOf(embedMaker("Erreur", "", "", "Erreur de chargement"))
            }
        }
    }
}

@OptIn(KordVoice::class)
suspend fun checkVoiceConnection(
    connections: MutableMap<Snowflake, VoiceConnection>,
    ctx: ChatInputCommandInteraction,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord
): Boolean {
    val voiceConnection = connections[ctx.data.guildId.value!!]
    // NE PAS BOUGER SINON *inserer son metal pipe*
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
        return false
    }
    return true
}


suspend fun stopMusic(
    link: Link,
    ctx: ChatInputCommandInteraction,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord
) {
    if (link.player.playingTrack != null) {
        if (queueList.contains(ctx.data.guildId.value?.value!!)) {
            queueList[ctx.data.guildId.value?.value!!]!!.clearQueue()
        } else {
            queueList[ctx.data.guildId.value?.value!!] = Queue()
            queueList[ctx.data.guildId.value?.value!!]!!.clearQueue()
        }
        link.player.stopTrack()
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Musique arrêtée",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = ""
                )
            )
        }
        return
    }
}

suspend fun skipMusic(
    link: Link,
    ctx: ChatInputCommandInteraction,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord
) {
    if (link.player.playingTrack != null) {
        if (queueList[ctx.data.guildId.value?.value!!]!!.isQueueNotEmpty()) {
            link.player.stopTrack()
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Musique passée",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = ""
                    )
                )
            }
        } else {
            link.player.stopTrack()
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Musique passée",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = ""
                    )
                )
            }
        }

    } else {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Erreur",
                    thumbnailUrl = "https://tenor.com/view/kekw-twitch-twitchtv-twitch-emote-kek-gif-19085736",
                    footer = "",
                    description = "Il n'y a pas de musique en cours de lecture"
                )
            )
        }

    }

}

suspend fun resumeMusic(
    link: Link,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord
) {
    link.player.pause(false)
    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "Musique reprise",
                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                footer = "",
                description = "Musique reprise /pause pour mettre en pause"
            )
        )
    }
}

suspend fun pauseMusic(
    link: Link,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord
) {
    link.player.pause(true)
    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "Musique mise en pause",
                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                footer = "",
                description = "Musique mise en pause /resume pour reprendre"
            )
        )
    }
}

suspend fun setVolume(
    link: Link,
    response: DeferredPublicMessageInteractionResponseBehavior,
    kord: Kord,
    ctx: ChatInputCommandInteraction
) {
    if (ctx.command.integers["volume"]!! < 0 || ctx.command.integers["volume"]!! > 100 && ctx.command.integers["volume"] == null) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Erreur",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Le volume doit etre compris entre 0 et 100"
                )
            )
        }
        return
    } else {
        link.player.applyFilters {
            volume = ctx.command.integers["volume"]!!.toFloat() / 100
        }
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Volume",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Volume mis à ${ctx.command.integers["volume"]} %"
                )
            )
        }
        return
    }
}


suspend fun shuffleQueue(
    ctx: ChatInputCommandInteraction,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior
) {
    if (queueList.contains(ctx.data.guildId.value?.value)) {
        queueList[ctx.data.guildId.value?.value]!!.randomizeQueue()
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "File d'attente",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Ordre de la file d'attente changé aléatoirement"
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
                    description = "Il n'y a pas de musique dans la file d'attente"
                )
            )
        }
    }
}

suspend fun repeatMode(
    ctx: ChatInputCommandInteraction,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior
) {
    if (queueList.contains(ctx.data.guildId.value?.value)) {
        queueList[ctx.data.guildId.value?.value]!!.setRepeatTrack(ctx.command.booleans["repeat"]!!)
        if (ctx.command.booleans["repeat"]!!) {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Mode répétition",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Mode répétition activé"
                    )
                )
            }
            return
        } else {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Mode répétition",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Mode répétition desactivé"
                    )
                )
            }
            return
        }
    }
}

suspend fun getQueueList(
    ctx: ChatInputCommandInteraction,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior
) {
    if (queueList[ctx.data.guildId.value?.value]!!.isQueueEmpty() || !queueList.contains(ctx.data.guildId.value?.value)) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Erreur",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Il n'y a pas de musique dans la file d'attente"
                )
            )
        }
        return
    }
    val listText: String
    if (queueList.size > 20) {
        val shortPlaylistList = ArrayList<Track>()
        for (i in 0..20) {
            shortPlaylistList.add(queueList[ctx.data.guildId.value?.value]!!.get(i))
        }
        listText = shortPlaylistList.joinToString("\n") { "💿 " + it.info.title }
    } else {
        listText = queueList[ctx.data.guildId.value?.value]!!.getQueue().joinToString("\n") { "💿 " + it.info.title }
    }
    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "File d'attente",
                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                footer = "",
                description = listText
            )
        )
    }
}

suspend fun getPlayerInfo(
    link: Link,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior
) {
    if (link.player.playingTrack != null) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Informations sur le bot",
                    thumbnailUrl = link.player.playingTrack!!.info.artworkUrl.toString(),
                    footer = "Auteur : " + link.player.playingTrack!!.info.author,
                    description = link.player.playingTrack!!.info.title,
                )
            )
        }
    } else {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    "Erreur",
                    kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    "",
                    "Il n'y a pas de musique en cours de lecture"
                )
            )
        }
    }
}

suspend fun insertTrack(
    ctx: ChatInputCommandInteraction,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior,
    lavalink: LavaKord
) {
    if (ctx.command.strings["track"] == null) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    "Erreur",
                    kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    "",
                    "Il n'y a pas de titre ou lien dans la commande"
                )
            )
        }
        return
    }
    val newPlaylist = mutableMapOf<ULong, Queue>()
    val song = ctx.command.strings["song"].toString()
    val query: String = if (song.contains("https://")) {
        song
    } else {
        "ytsearch: $song"
    }
    val item = lavalink.nodes[0].loadItem(query)

    when (item) {
        is LoadResult.TrackLoaded -> {
            if (!queueList.contains(ctx.data.guildId.value?.value)) {
                queueList[ctx.data.guildId.value?.value!!] = Queue()
                queueList[ctx.data.guildId.value?.value!!]!!.add(item.data)
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Musique ajoutée",
                            item.data.info.artworkUrl.toString(),
                            item.data.info.author,
                            item.data.info.title
                        )
                    )
                }
                return
            } else {
                newPlaylist[ctx.data.guildId.value?.value!!] = Queue()
                newPlaylist[ctx.data.guildId.value?.value!!]!!.add(item.data)
                newPlaylist.putAll(queueList)
                queueList.clear()
                queueList.putAll(newPlaylist)
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Musique ajoutée apres la musique en cours",
                            item.data.info.artworkUrl.toString(),
                            item.data.info.author,
                            item.data.info.title
                        )
                    )
                }
                return
            }

        }

        is LoadResult.PlaylistLoaded -> {
            val playlist = item.data.tracks
            if (!queueList.contains(ctx.data.guildId.value?.value)) {
                queueList[ctx.data.guildId.value?.value!!] = Queue()
                queueList[ctx.data.guildId.value?.value!!]!!.addAll(playlist)

                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Playlist ajoutée",
                            kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            "",
                            "Voir /queue pour voir la playlist !"
                        )
                    )
                }
                return
            } else {
                newPlaylist[ctx.data.guildId.value?.value!!] = Queue()
                newPlaylist[ctx.data.guildId.value?.value!!]!!.addAll(playlist)
                newPlaylist.putAll(queueList)
                queueList.clear()
                queueList.putAll(newPlaylist)
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Playlist ajoutée apres la musique en cours",
                            kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            "",
                            "Voir /queue pour voir la playlist !"
                        )
                    )
                }
                return
            }
        }

        is LoadResult.SearchResult -> {
            if (!queueList.contains(ctx.data.guildId.value?.value)) {
                queueList[ctx.data.guildId.value?.value!!] = Queue()
                queueList[ctx.data.guildId.value?.value!!]!!.add(item.data.tracks[0])
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Musique ajoutée",
                            item.data.tracks[0].info.artworkUrl.toString(),
                            item.data.tracks[0].info.author,
                            item.data.tracks[0].info.title
                        )
                    )
                }
                return
            } else {
                newPlaylist[ctx.data.guildId.value?.value!!] = Queue()
                newPlaylist[ctx.data.guildId.value?.value!!]!!.add(item.data.tracks[0])
                newPlaylist.putAll(queueList)
                queueList.clear()
                queueList.putAll(newPlaylist)
                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            "Musique ajoutée apres la musique en cours",
                            item.data.tracks[0].info.artworkUrl.toString(),
                            item.data.tracks[0].info.author,
                            item.data.tracks[0].info.title
                        )
                    )
                }
                return
            }
        }

        is LoadResult.NoMatches -> {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        "Erreur",
                        kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        "",
                        "Aucun résultat pour cette requête. Veuillez reessayer !"
                    )
                )
            }
            return
        }

        is LoadResult.LoadFailed -> {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        "Erreur",
                        kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        "",
                        "Erreur de chargement"
                    )
                )
            }
            return
        }
    }
}



