import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.flow.toList


@OptIn(KordVoice::class)
suspend fun voiceActivityListener(kord: Kord,connections: MutableMap<Snowflake, VoiceConnection>) {
    kord.on<VoiceStateUpdateEvent> {

        /* */
        val oldStateUsers = old?.getChannelOrNull()?.voiceStates?.toList()?.map { it.userId }
        if (oldStateUsers != null && oldStateUsers.size <= 1) {
            connections.get(old?.getChannelOrNull()?.guildId!!)?.leave()
            connections.remove(old?.getChannelOrNull()?.guildId!!)
        }
    }
}