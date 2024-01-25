import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

var queue = ArrayList<Track>()
var clearQueue = false
var listSessions = mutableMapOf<Snowflake,Node>()
fun clearQueue() {
    while (clearQueue) {
        runBlocking {
            queue.clear()
            println("Clearing ...")
        }
        if(queue.size == 0 ){
            clearQueue = false
        }
    }
}
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
        var playing = true

        link.player.on<TrackEndEvent> {
            println("Track end")
            println("queue : " + queue.size)
                if (queue.isEmpty()) {
                    return@on
                } else if (queue.isNotEmpty()) {
                    if (link.player.playingTrack == null && !playing) {
                        playing = true
                        println("queue : " + queue.size)
                        link.player.playTrack(queue.first())
                    }
                }

        }

        link.player.on<TrackStartEvent> {
            if(link.player.playingTrack != null && playing){
                println("Track start")
                if (queue.isNotEmpty()) {
                    queue.removeAt(0)
                }
                playing = false
            }

        }

        when (commandName) {
            "ping" -> {
                val resultat =
                    "```Ping vers l'api Discord : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms```"
                response.respond {
                    content = resultat
                }
            }

            "sum" -> {
                val firstNum = command.integers["first_num"]!!
                val secondNum = command.integers["second_num"]!!
                val result: String = sum(firstNum, secondNum)
                response.respond {
                    content = result
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
                        embeds = mutableListOf(embed)
                    }
                }
            }

            "rejoindre" -> {
                val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                if (voiceChannel == null) {
                    response.respond {
                        content = "Viens dans un vovo ou conséquences  !"
                    }
                    return@on
                }

                if (connections.contains(ctx.data.guildId.value!!)) {
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                }
                val connection = voiceChannel.connect {

                }
                connections.put(ctx.data.guildId.value!!, connection)
                response.respond {
                    content = "```Je suis la !```"
                }
                return@on

            }

            "play" -> {
                // link player to the
                val voiceConnection = connections[ctx.data.guildId.value!!]
                // NE PAS BOUGER SINON *inserer son metal pipe*
                link = lavalink.getLink(guildId.value)

                if (voiceConnection == null) {
                    val voiceChannel =
                        ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                    if (voiceChannel == null) {
                        response.respond {
                            content = "Pas dans un vocal"
                        }
                        return@on
                    } else {
                        val connection = voiceChannel.connect {
                        }
                        connections.put(ctx.data.guildId.value!!, connection)
                    }
                }
                val song = ctx.command.strings["song"].toString()
                val query: String
                if (song.contains("https://")) {
                    query = song
                } else {
                    query = "ytsearch: ${song}"
                }


                val item = lavalink.nodes.get(0).loadItem(query)

                val voiceChan = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceState().getChannelOrNull()
                if (voiceChan == null) {
                    response.respond {
                        content = "Pas dans un vovo !"
                    }
                    return@on
                } else {
                    if(!listSessions.contains(guildId)){
                        link.connectAudio(voiceChan.id.value)
                        connections.get(guildId)!!.connect()
                        listSessions.put(guildId,link.node)
                    }else{
                        link.onNewSession(listSessions.get(guildId)!!)
                    }

                }

                when (item) {

                    is LoadResult.TrackLoaded -> {
                        if (link.player.playingTrack == null) {
                            link.player.playTrack(item.data)
                            response.respond {
                                content = "" +
                                        "```" +
                                        "Musique en cours de lecture : " + item.data.info.title +
                                        "```"
                            }
                            return@on
                        } else {
                            queue.add(queue.size, item.data)
                            response.respond {
                                content = "" +
                                        "```" +
                                        "Musique ajoutée dans la file d'attente : " + item.data.info.title +
                                        "```"
                            }
                            return@on
                        }


                    }

                    is LoadResult.PlaylistLoaded -> {

                        var playlist = item.data.tracks
                        playlist.toMutableList().removeFirst()

                        for (track in playlist) {
                            queue.add(queue.size, track)
                        }

                        if (link.player.playingTrack == null) {
                            link.player.playTrack(queue.first())
                        }
                        queue.removeAt(0)
                        println("Playlist chargee, taille : " + queue.size)

                        response.respond {
                            content = "Playlist chargee ! Voir /queue pour voir la playlist !"
                        }

                    }

                    is LoadResult.SearchResult -> {

                        if (queue.isEmpty()) {
                            if (link.player.playingTrack == null) {
                                link.player.playTrack(item.data.tracks.get(0))
                                response.respond {
                                    content = "" +
                                            "```" +
                                            "Musique en cours de lecture : " + item.data.tracks.get(0).info.title +
                                            "```"
                                }
                            } else {
                                queue.add(queue.size, item.data.tracks.get(0))
                                response.respond {
                                    content = "" +
                                            "```" +
                                            "Musique ajoutée : " + item.data.tracks.get(0).info.title +
                                            "```"
                                }
                            }
                        } else {
                            println("queue : " + queue.size)
                            queue.add(queue.size, item.data.tracks.get(0))
                            response.respond {
                                content = "" +
                                        "```" +
                                        "Musique ajoutée : " + item.data.tracks.get(0).info.title +
                                        "```"
                            }
                            return@on
                        }

                    }

                    is LoadResult.NoMatches -> {
                        response.respond {
                            content = "Aucune correspondance"
                        }
                        return@on
                    }

                    is LoadResult.LoadFailed -> {
                        response.respond {
                            content = "Erreur de chargement"
                        }
                        return@on
                    }

                }

            }

            "skip" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                // NE PAS BOUGER SINON *inserer son metal pipe*
                link = lavalink.getLink(guildId.value)
                println("queue : " + queue.size)
                if (voiceConnection == null) {
                    response.respond {
                        content = "Pas dans un vocal"
                    }
                    return@on
                }
                if (link.player.playingTrack != null) {
                    link.player.stopTrack()
                    response.respond {
                        content = "```Musique passée" + "```"
                    }
                    return@on
                } else {
                    response.respond {
                        content = "`Je ne suis pas en train de viber` <:KEKW:1034887996136226836>"     /// Should never happen
                    }
                    return@on
                }
            }

            "stop" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                // NE PAS BOUGER SINON *inserer son metal pipe*
                //val link: Link = lavalink.getLink(guildId.value)
                if (voiceConnection == null) {
                    response.respond {
                        content = "Pas dans un vocal"
                    }
                    return@on
                }
                if (link.player.playingTrack != null) {
                    // Make sure the queue is REALLY EMPTY FOR THE LOVE OF GOD
                    clearQueue = true
                    while (clearQueue) {
                        runBlocking {
                            queue.clear()
                            println("Clearing ...")
                        }
                        if(queue.size == 0 ){
                            clearQueue = false
                        }
                    }

                    link.player.stopTrack()
                    response.respond {
                        content = "```Musique arrêtée```"
                    }
                    return@on
                }
            }

            "déconnecter" -> {

                val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                if (voiceChannel == null) {
                    response.respond {
                        content = "Viens dans un vovo ou conséquences  !"
                    }
                }

                if (connections.contains(ctx.data.guildId.value!!)) {

                    if (lavalink.getLink(ctx.data.guildId.toString()).player.playingTrack != null) {
                        lavalink.getLink(ctx.data.guildId.toString()).player.stopTrack()
                    }
                    connections.get(ctx.data.guildId.value!!)!!.leave()
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                    response.respond {
                        content = "```On me voit plus!```"
                    }
                }
            }

            "clear" -> {
                if (isAutorized(ctx, Permission.ManageMessages)) {
                    var count = 0
                    val listMessage = ctx.getChannel().messages
                    if (listMessage.count() <= 1) {
                        response.respond {
                            content = "Il n'y a pas de message"
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
                            content = "```${count} messages supprimés```"
                        }
                    }
                } else {
                    response.respond {
                        content = "```Vous n'avez pas la permission de supprimer des messages```"
                    }
                    return@on
                }

            }

            "assign_role" -> {
                if (isAutorized(ctx, Permission.ManageRoles)) {
                    val user = ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)
                    val listRolesUser =
                        ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)?.roles?.toList()
                    val role = ctx.command.roles.get("role")!!.asRole()
                    if (listRolesUser != null) {
                        if (listRolesUser.contains(role)) {
                            response.respond {
                                content = "Le role est déja attribué à l'utilisateur"
                            }
                        } else {
                            if (role.name == "@everyone") {
                                response.respond {
                                    content = " Vous ne pouvez pas ajouter le role everyone"
                                }
                            } else {

                                if (role.tags?.data?.botId == null) {
                                    ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)?.addRole(role.id)
                                    response.respond {
                                        content = "Role attribué pour ${user?.username}"
                                    }
                                    return@on
                                } else {
                                    response.respond {
                                        content =
                                            "Ce role ne peut pas etre ajouté, il s'agit d'un role destiné a un bot"
                                    }
                                    return@on
                                }

                            }

                        }
                    }
                } else {
                    response.respond {
                        content = "```Vous n'avez pas la permission de supprimer des messages```"
                    }
                    return@on
                }
            }

            "unassign_role" -> {
                if (isAutorized(ctx, Permission.ManageRoles)) {
                    val user = ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)
                    val listRolesUser =
                        ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)?.roles?.toList()

                    val role = ctx.command.roles.get("role")!!.asRole()

                    if (listRolesUser != null) {
                        if (listRolesUser.contains(role)) {
                            if (role.name == "@everyone") {
                                response.respond {
                                    content = " Vous ne pouvez pas supprimer le role everyone"
                                }
                            } else {
                                if (role.tags?.data?.botId == null) {
                                    ctx.command.users.get("user")?.asMember(ctx.data.guildId.value!!)
                                        ?.removeRole(role.id)
                                    response.respond {
                                        content = "Role supprimé pour ${user?.username}"
                                    }
                                } else {
                                    response.respond {
                                        content =
                                            "Ce role ne peut pas etre supprimé, il s'agit d'un role destiné a un bot"
                                    }
                                }

                            }


                        } else {
                            response.respond {
                                content = "Le role n'est pas attribué à l'utilisateur"
                            }
                        }
                    }
                } else {
                    response.respond {
                        content = "```Vous n'avez pas la permission de supprimer des messages```"
                    }
                    return@on
                }
            }

            "pause" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                    return@on
                }
                val link: Link = lavalink.getLink(guildId.value)
                link.player.pause(true)

                response.respond {
                    content = "```Pause```"
                }
            }

            "resume" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                }
                val link: Link = lavalink.getLink(guildId.value)
                link.player.pause(false)
                response.respond {
                    content = "```Reprise```"
                }
            }

            "info" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                }
                val link: Link = lavalink.getLink(guildId.value)
                response.respond {
                    content = "" +
                            "```" +
                            "Musique en cours de lecture : " + link.player.playingTrack?.info?.title +
                            "\nVolume : " + link.player.volume +
                            "```"
                }

            }

            "disconnect" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                }else {
                    val link: Link = lavalink.getLink(guildId.value)
                    link.player.stopTrack()
                    voiceConnection.disconnect()
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                    response.respond {
                        content = "```Le bot est maintenant deconnecté```"
                    }
                }
            }

            "queue" -> {
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                }
                if(queue.isEmpty()){
                    response.respond {
                        content = "```La liste d'attente est vide```"
                    }
                    return@on
                }
                val link: Link = lavalink.getLink(guildId.value)
                response.respond {
                    content = "```Liste d'attente sur le vocal : \n" + queue.map {"\uD83D\uDCBF" + it.info.title }.joinToString("\n") + "```"
                }
            }

            "volume" -> {
                if(ctx.command.integers["volume"]!! < 0 || ctx.command.integers["volume"]!! > 100){
                    response.respond {
                        content = "```Le volume doit etre comprise entre 0 et 100```"
                    }
                    return@on
                }
                if(ctx.command.integers["volume"] == null){
                    response.respond {
                        content = "```Le volume doit etre comprise entre 0 et 100```"
                    }
                    return@on

                }
                val voiceConnection = connections[ctx.data.guildId.value!!]
                if (voiceConnection == null) {
                    response.respond {
                        content = "```Le bot n'est pas connecté sur un vocal```"
                    }
                    return@on
                }
                val link: Link = lavalink.getLink(guildId.value)
                link.player.applyFilters {
                    volume = ctx.command.integers["volume"]!!.toFloat()/100
                }
                response.respond {
                    content = "```Volume mis à ${ctx.command.integers["volume"]} %```"
                }
            }

            else -> {
                response.respond {
                    content = "```Erreur de commande, veuillez reessayer```"
                }
                error("Erreur de commande, veuillez reessayer")

            }
        }


    }

}


suspend fun isAutorized(ctx: Interaction, perm: Permission): Boolean {
    if (ctx.user.asMember(ctx.data.guildId.value!!).getPermissions().contains(perm)) {
        return true
    } else {
        return false
    }

}


