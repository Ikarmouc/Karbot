package listeners

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Event
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on

import kotlinx.coroutines.flow.toList


@OptIn(KordVoice::class)
suspend fun voiceActivityListener(kord: Kord,connections: MutableMap<Snowflake, VoiceConnection>,lavalink: LavaKord) {
    kord.on<VoiceStateUpdateEvent> {
        val stateUsers = state.getChannelOrNull()?.voiceStates?.toList()?.map { it.userId.value.toString() }
        if (stateUsers != null) {
            if(stateUsers.contains("1125404724603146301") && stateUsers.size <= 1){
                if(queueList.get(state.guildId.value)!!.isQueueNotEmpty()){
                    queueList.get(state.guildId.value)!!.clearQueue()
                    queueList.remove(state.guildId.value)
                }
                lavalink.getLink(state.guildId.value).player.stopTrack()
                lavalink.getLink(state.guildId.value).disconnectAudio()
                connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
                connections.remove(old?.getChannelOrNull()?.guildId!!)
            }
        }
    }


    lavalink.on<TrackEndEvent>{
        println("Track end")

        if(queueList.contains(guildId)){
            if(queueList.get(guildId)!!.isQueueNotEmpty()){
                if(!queueList.get(guildId)!!.getRepeatTrack()){
                    lavalink.getLink(guildId).player.playTrack(queueList.get(guildId)!!.get(0))
                    queueList.get(guildId)!!.removeAt(0)
                }else{
                    queueList.get(guildId)!!.incrementPosition()
                    lavalink.getLink(guildId).player.playTrack(queueList.get(guildId)!!.get(queueList.get(guildId)!!.getTrackPosition()))
                }
            }
        }

        val channelId = lavalink.getLink(guildId).lastChannelId!!
        val channel = kord.getChannelOf<GuildMessageChannel>(Snowflake(channelId))
        if(lavalink.getLink(guildId).player.playingTrack != null){
            channel?.createEmbed {
                title = "Karbot - The track has ended"
                thumbnail {
                    url = lavalink.getLink(guildId).player.playingTrack!!.info.artworkUrl.toString()
                }
                color = dev.kord.common.Color(0, 0, 255)
                description = "Now playing: ${lavalink.getLink(guildId).player.playingTrack!!.info.title}"
                footer {
                    text = lavalink.getLink(guildId).player.playingTrack!!.info.author
                }
            }
        }
    }

    lavalink.on<Event>{
        println(lavalink.getLink(guildId).state)
    }
}

