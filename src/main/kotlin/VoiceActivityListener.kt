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


        val stateUsers = state.getChannelOrNull()?.voiceStates?.toList()?.map { it.userId.value.toString() }

        if (stateUsers != null) {
            if(stateUsers.contains("1125404724603146301") && stateUsers.size <= 1){
                clearQueue()
                lavalink.getLink(state.guildId.value).player.stopTrack()
                lavalink.getLink(state.guildId.value).disconnectAudio()
                connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
                connections.remove(old?.getChannelOrNull()?.guildId!!)
            }
        }
    }


}