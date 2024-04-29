package music

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.rest.loadItem
import dotenv
import utility.embedMaker
import listeners.queueList


//@OptIn(KordVoice::class)
//suspend fun playMusic(
//    lavalink: LavaKord,
//    response: DeferredPublicMessageInteractionResponseBehavior,
//    ctx: ChatInputCommandInteraction,
//    connections: MutableMap<Snowflake, VoiceConnection>,
//    guildId: Snowflake,
//){
//
//    // link player to the
//    val voiceConnection = connections[ctx.data.guildId.value!!]
//    // NE PAS BOUGER SINON *inserer son metal pipe*
//    val link = lavalink.getLink(guildId.value)
//
//    if (voiceConnection == null) {
//        val voiceChannel =
//            ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
//        if (voiceChannel == null) {
//            response.respond {
//                content = "Pas dans un vocal"
//            }
//            return
//        } else {
//            val connection = voiceChannel.connect {
//            }
//            connections.put(ctx.data.guildId.value!!, connection)
//        }
//    }
//    val song = ctx.command.strings["song"].toString()
//    val query: String
//    if (song.contains("https://")) {
//        query = song
//    } else {
//        query = "ytsearch: ${song}"
//    }
//
//
//    val item = lavalink.nodes.get(0).loadItem(query)
//
//    val voiceChan = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceState().getChannelOrNull()
//    if (voiceChan == null) {
//        response.respond {
//            content = "Pas dans un vovo !"
//        }
//        return
//    } else {
//        if(!listSessions.contains(guildId)){
//            link.connectAudio(voiceChan.id.value)
//            connections.get(guildId)!!.connect()
//            listSessions.put(guildId,link.node)
//        }else{
//            link.onNewSession(listSessions.get(guildId)!!)
//        }
//
//    }
//
//    when (item) {
//
//        is LoadResult.TrackLoaded -> {
//            if (link.player.playingTrack == null) {
//                link.player.playTrack(item.data)
//                response.respond {
//                    content = "" +
//                            "```" +
//                            "Musique en cours de lecture : " + item.data.info.title +
//                            "```"
//                }
//                return
//            } else {
//                response.respond {
//                    content = "" +
//                            "```" +
//                            "Musique ajoutée dans la file d'attente : " + item.data.info.title +
//                            "```"
//                }
//                return
//            }
//
//
//        }
//
//        is LoadResult.PlaylistLoaded -> {
//
////            var playlist = item.data.tracks
////            playlist.toMutableList().removeFirst()
////
//
//            response.respond {
//                content = "Playlist chargee ! Voir /queue pour voir la playlist !"
//            }
//
//        }
//
//        is LoadResult.SearchResult -> {
//
//
//                if (link.player.playingTrack == null) {
//                    link.player.playTrack(item.data.tracks.get(0))
//                    response.respond {
//                        content = "" +
//                                "```" +
//                                "Musique en cours de lecture : " + item.data.tracks.get(0).info.title +
//                                "```"
//                    }
//            }
//
//        }
//
//        is LoadResult.NoMatches -> {
//            response.respond {
//                content = "Aucune correspondance"
//            }
//            return
//        }
//
//        is LoadResult.LoadFailed -> {
//            response.respond {
//                content = "Erreur de chargement"
//            }
//            return
//        }
//
//    }
//}

@OptIn(KordVoice::class)
suspend fun playMusic(
    lavalink: LavaKord,
    response: DeferredPublicMessageInteractionResponseBehavior,
    ctx: ChatInputCommandInteraction,
    connections: MutableMap<Snowflake, VoiceConnection>
) {

    val guildId = ctx.data.guildId.value!!
    // link player to the
    val voiceConnection = connections[ctx.data.guildId.value!!]
    // NE PAS BOUGER SINON *inserer son metal pipe*
    val link: Link = lavalink.getLink(ctx.data.guildId.value.toString())
    if (voiceConnection == null) {
        val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
        if(voiceChannel == null){
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = ctx.kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "You need to be in a voice channel to use this command"
                    )
                )
                return
            }

        }else{
            val connection = voiceChannel.connect {
            }
            connections[ctx.data.guildId.value!!] = connection
        }
    }
    val song = ctx.command.strings["song"].toString()
    val query: String
    if (song.contains("https://")) {
        query = song
    }else{
        query = "ytsearch: ${song}"
    }

    val item = lavalink.nodes.get(0).loadItem(query)

    val voiceChan = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceState().getChannelOrNull()
    if (voiceChan == null ) {
        response.respond{
            embeds = mutableListOf(
                embedMaker(
                    title = "Error",
                    thumbnailUrl = ctx.kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "The bot is not connected to a voice channel"
                )
            )
        }
        return
    } else {
            connections[ctx.data.guildId.value]!!.connect()
            link.connectAudio(voiceChan.id.value)
    }

    when (item) {
        is LoadResult.TrackLoaded -> {
            if (link.player.playingTrack == null) {
                println("Playing '${item.data.info.title}' on guild " + ctx.getChannel().asChannel().data.name.value!!)
                link.player.playTrack(item.data)

                response.respond {
                    embeds = mutableListOf(
                        embedMaker(
                            title = "Music playing",
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
                            title = "Music added to queue",
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
                        title = "Playlist loaded !",
                        thumbnailUrl = ctx.kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        description = "See /queue to see the playlist !",
                        footer = "Number of tracks : " + item.data.tracks.size.toString()
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
                    println(link.state)
                    response.respond {
                        embeds = mutableListOf(
                            embedMaker(
                                title = "Music playing",
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
                                title = "Music added to queue",
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
                        title = "Error",
                        thumbnailUrl = ctx.kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "No matches found for this query. Please try again !"
                    )
                )
            }
        }

        is LoadResult.LoadFailed -> {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Erreur",
                        thumbnailUrl = ctx.kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Error while loading the track"))
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
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "The bot is not connected to a voice channel"
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
                    title = "Music stopped",
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
                        title = "Music skipped",
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
                        title = "Music skipped",
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
                    title = "Error",
                    thumbnailUrl = "${dotenv.get("ASSETS_SERVER_URL")}/utility/kekw.gif",
                    footer = "",
                    description = "There is no music playing"
                )
            )
        }

    }

}

suspend fun resumeMusic(
    link: Link,
    response: DeferredPublicMessageInteractionResponseBehavior,
) {
    link.player.pause(false)
    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "Music resumed",
                thumbnailUrl = link.player.playingTrack!!.info.artworkUrl.toString(),
                footer = "",
                description = "Music resumed type /pause to pause the music"
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
                title = "Music paused",
                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                footer = "",
                description = "Music paused type /resume to resume the music"
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
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Volume must be between 0 and 100"
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
                    title = "Volume changed",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Volume set to ${ctx.command.integers["volume"]} %"
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
                    title = "Queue shuffled",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "Queue has been shuffled ! All tracks are now in a random order"
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
                    description = "There is no music in the queue to shuffle !"
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
                        title = "Repeat mode",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Repeat mode enabled"
                    )
                )
            }
            return
        } else {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Repeat mode",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Repeat mode disabled"
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
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "There is no music in the queue !"
                )
            )
        }
        return
    }
    val listText: String
    if (queueList.size > 20) {
        val shortPlaylistList = ArrayList<Track>()
        for (i in 0..10) {
            shortPlaylistList.add(queueList[ctx.data.guildId.value?.value]!!.get(i))
        }
        listText = shortPlaylistList.joinToString("\n") { "💿 " + it.info.title }
    } else {
        listText = queueList[ctx.data.guildId.value?.value]!!.getQueue().joinToString("\n") { "💿 " + it.info.title }
    }
    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "Queue",
                thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                footer = queueList[ctx.data.guildId.value?.value]!!.size.toString() + " tracks",
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
                    title = "Now playing",
                    thumbnailUrl = link.player.playingTrack!!.info.artworkUrl.toString(),
                    footer = "Author : " + link.player.playingTrack!!.info.author,
                    description = link.player.playingTrack!!.info.title,
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
                    description = "There is no music playing !"
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
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "The track parameter is missing"
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
                            title = "Music added to the queue",
                            thumbnailUrl = item.data.info.artworkUrl.toString(),
                            footer = item.data.info.author,
                            description = item.data.info.title
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
                            title = "Music added to the queue after the current track",
                            thumbnailUrl = item.data.info.artworkUrl.toString(),
                            footer = item.data.info.author,
                            description = item.data.info.title
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
                            title = "Playlist added to the queue",
                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            footer = "",
                            description = "Check /queue to see the playlist !"
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
                            title = "Playlist playlist added to the queue after the current track",
                            thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                            footer = "",
                            description = "Voir /queue pour voir la playlist !"
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
                            title = "Music added to the queue",
                            thumbnailUrl = item.data.tracks[0].info.artworkUrl.toString(),
                            footer = item.data.tracks[0].info.author,
                            description = item.data.tracks[0].info.title
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
                            title = "Music added to the queue after the current track",
                            thumbnailUrl = item.data.tracks[0].info.artworkUrl.toString(),
                            footer = item.data.tracks[0].info.author,
                            description = item.data.tracks[0].info.title
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
                        title = "Error",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "No matches found for this query. Please try again !"
                    )
                )
            }
            return
        }

        is LoadResult.LoadFailed -> {
            response.respond {
                embeds = mutableListOf(
                    embedMaker(
                        title = "Error",
                        thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                        footer = "",
                        description = "Error while loading the track"
                    )
                )
            }
            return
        }
    }
}

@OptIn(KordVoice::class)
suspend fun joinChannel(
    ctx: ChatInputCommandInteraction,
    kord: Kord,
    response: DeferredPublicMessageInteractionResponseBehavior,
    connections: MutableMap<Snowflake, VoiceConnection>
){
    val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
    if (voiceChannel == null) {
        response.respond {
            embeds = mutableListOf(
                embedMaker(
                    title = "Error",
                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
                    footer = "",
                    description = "You must be in a vocal channel to use this command"
                )
            )
        }
        return
    }

    if (connections.contains(ctx.data.guildId.value!!)) {
        connections[ctx.data.guildId.value!!]!!.disconnect()
        connections.remove(ctx.data.guildId.value!!)
    }
    val connection = voiceChannel.connect {}
    connections.put(ctx.data.guildId.value!!, connection)
    response.respond {
        content = "```Je suis la !```"
    }


//
//    val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
//    if (voiceChannel == null) {
//        response.respond {
//            embeds = mutableListOf(
//                embedMaker(
//                    title = "Error",
//                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                    footer = "",
//                    description = "You must be in a vocal channel to use this command"
//                )
//            )
//        }
//        return
//    }
//    if (connections.contains(ctx.data.guildId.value!!)) {
//        connections.get(ctx.data.guildId.value!!)!!.disconnect()
//        connections.remove(ctx.data.guildId.value!!)
//    }
//    val connection = voiceChannel.connect {
//    }
//    connections[ctx.data.guildId.value!!] = connection
//    if(ctx.command.data.name.value == "join"){
//        response.respond {
//            embeds = mutableListOf(
//                embedMaker(
//                    title = "Voice channel joined",
//                    thumbnailUrl = kord.getSelf().avatar?.cdnUrl?.toUrl().toString(),
//                    footer = "",
//                    description = "The bot has successfully joined the vocal channel ! "
//                )
//            )
//        }
//    }
//    return
}



