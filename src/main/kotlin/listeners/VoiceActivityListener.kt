package listeners

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.voice.VoiceConnection
import dev.schlaubi.lavakord.LavaKord
import kotlinx.coroutines.flow.toList




@OptIn(KordVoice::class)
suspend fun voiceActivityListener(kord: Kord,connections: MutableMap<Snowflake, VoiceConnection>,lavalink: LavaKord) {


    kord.on<VoiceStateUpdateEvent> {

        if(old?.getChannelOrNull()?.voiceStates?.toList()?.size == 1){
            println("Bot left the channel")
            lavalink.getLink(old?.getChannelOrNull()?.guildId!!.value).player.stopTrack()
            connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
            connections.remove(old?.getChannelOrNull()?.guildId!!)
        }
        // check if the bot alone in the channel

        if(state.channelId == null){
            return@on
        }


        if(old?.getChannelOrNull()?.voiceStates?.toList()?.size == 1 && old?.getChannelOrNull()!!.voiceStates.toList()[0].userId == old?.kord?.getSelf()?.id||
            old?.getChannelOrNull()?.voiceStates?.toList()?.size == 1 ){
            if(!queueList.contains(old?.getChannelOrNull()?.guildId!!.value)){
//                if(queueList[old?.getChannelOrNull()?.guildId!!.value]!!.isQueueNotEmpty()){
//                    queueList[old?.getChannelOrNull()?.guildId!!.value]!!.clearQueue()
//                    queueList.remove(old?.getChannelOrNull()?.guildId!!.value)
//                }
            }

            lavalink.getLink(old?.getChannelOrNull()?.guildId!!.value).player.stopTrack()
            connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
            connections.remove(old?.getChannelOrNull()?.guildId!!)
        }
        else{
            println(state.getChannelOrNull()?.voiceStates?.toList()?.size)
        }

//        if (stateUsers != null) {
//            if(stateUsers.contains("1125404724603146301") && stateUsers.size <= 1){
//                if(queueList.get(state.guildId.value)!!.isQueueNotEmpty()){
//                    queueList.get(state.guildId.value)!!.clearQueue()
//                    queueList.remove(state.guildId.value)
//                }
//                lavalink.getLink(state.guildId.value).player.stopTrack()
//                lavalink.getLink(state.guildId.value).disconnectAudio()
//                connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
//                connections.remove(old?.getChannelOrNull()?.guildId!!)
//
//            }
//        }

    }

    /*lavalink.on<TrackEndEvent>{
        if(lavalink.getLink(guildId).player.playingTrack != null){
            println("WTF IS GOING ON HERE ! ")
            return@on
        }
        if(queueList.contains(guildId)){
            if(queueList.get(guildId)!!.isQueueNotEmpty()){
                if(!queueList.get(guildId)!!.getRepeatTrack()){
                    lavalink.getLink(guildId).player.playTrack(queueList.get(guildId)!!.get(0))
                    // wait until the track is playing
                    while(lavalink.getLink(guildId).player.playingTrack == null){
                        println("Waiting for track to play")
                    }
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
    */
}

