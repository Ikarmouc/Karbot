import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.common.Color
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.lavakord
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.getPlayer
import dev.schlaubi.lavakord.rest.loadItem
import java.net.ConnectException

@OptIn(KordVoice::class)
suspend fun globalChatCommandlistener(kord: Kord,connections : MutableMap<Snowflake, VoiceConnection>) {


//    val connections = mutableMapOf<Snowflake, VoiceConnection>()
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val linksMap : MutableMap<Snowflake, Link> = mutableMapOf()   //Map contenant les links de chacun des serveurs
        val response = interaction.deferPublicResponse()
        val command = interaction.command

        val ctx = interaction



        when(command.data.name.value){
            "ping" -> {
                val resultat = "```Ping vers l'api Discord : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms```"
                response.respond {
                    content = resultat
                }
            }
            "sum" -> {
                val firstNum = command.integers["first_num"]!!
                val secondNum = command.integers["second_num"]!!
                val result: String = sum(firstNum,secondNum)
                response.respond {
                    content = result
                }
            }
            "about" -> {
                val embed = EmbedBuilder()
                embed.color = Color(0, 0, 0)
                var customUsername = command.users["user"]?.globalName
                if (customUsername == null){
                   customUsername = command.users["user"]?.username
                }
                embed.title = "Informations sur l'utilisateur : ${command.users["user"]?.username}"
                embed.description = "```" +
                    "Nom : ${customUsername}\n" +
                    "Identifiant : ${command.users["user"]?.username}\n ```"

                if(command.users["user"] != null){
                    val thumbnail = EmbedBuilder.Thumbnail()
                    thumbnail.url = command.users["user"]?.avatar?.cdnUrl?.toUrl().toString()
                    embed.thumbnail = thumbnail

                    response.respond {
                        embeds = mutableListOf(embed)
                    }
                }else{
                    val pingDiscord : String = response.kord.gateway.averagePing?.inWholeMilliseconds.toString()
                    embed.title = "Informations sur le bot : "
                    embed.description = "```" + "Version Kord : 0.11.1 \n" + "Version bot : 0.0.3-alpha \n" + "```"
                    embed.field("Ping vers l'api Discord", false){
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
                val connection = voiceChannel.connect {}
                connections.put(ctx.data.guildId.value!!, connection)
                response.respond {
                    content = "```Je suis la !```"
                }

            }

            "play" -> {
                val lavalink :LavaKord
                lavalink = ctx.kord.lavakord() {
                        link {
                            autoReconnect = false
                            retry = linear(2.seconds, 50.seconds, 1)
                        }
                    }
                    lavalink.addNode("ws://localhost:2333", "KarbotLavalink","Lavalink")

                

                println()
                val lavaplayerManager = DefaultAudioPlayerManager()
                AudioSourceManagers.registerRemoteSources(lavaplayerManager)


//                val link: Link
//                println(lavalink.nodes.get(0))
//                link = lavalink.getLink(ctx.data.guildId.value!!)
//
//
//                val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
//                if (voiceChannel == null) {
//                    response.respond {
//                        content = "Viens dans un vovo ou conséquences  !"
//                    }
//                    return@on
//                }
//
//                val song = command.strings["song"]
//                println(song)
//                if (connections.contains(ctx.data.guildId.value!!)) {
//                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
//                }
//                val player = lavaplayerManager.createPlayer()
//                val query = "ytsearch: ${song}"
//
//                val item = link.loadItem(query)
//
//
//
//
//                when(item.loadType){
//                    ResultStatus.TRACK->{
//                        println("track: ${item.data}")
//                    }
//
//                    ResultStatus.PLAYLIST->{
//
//                    }
//
//                    ResultStatus.SEARCH->{
//
//                    }
//
//                    ResultStatus.NONE->{
//
//                    }
//
//                    ResultStatus.ERROR->{
//
//                    }
//
//                    else->{
//
//                    }
//
//                }

                response.respond {
                    content = "It's debug time my dudes !"
                }
                // query the player
//                val connection = voiceChannel.connect {
//                    // the audio provider should provide frames of audio
//                    audioProvider { AudioFrame.fromData(player.provide()?.data) }
//                }


            }

            "déconnecter" -> {

                val voiceChannel = ctx.user.asMember(ctx.data.guildId.value!!).getVoiceStateOrNull()?.getChannelOrNull()
                if (voiceChannel == null) {
                    response.respond {
                        content = "Viens dans un vovo ou conséquences  !"
                    }
                }

                if (connections.contains(ctx.data.guildId.value!!)) {
                    connections.get(ctx.data.guildId.value!!)!!.leave()
                    connections.remove(ctx.data.guildId.value!!)!!.shutdown()
                    response.respond {
                        content = "```On me voit plus!```"
                    }
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


