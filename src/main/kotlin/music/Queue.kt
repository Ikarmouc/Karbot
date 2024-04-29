package music

import dev.arbjerg.lavalink.protocol.v4.Track

class Queue() {
    private val list = mutableListOf<Track>()
    val size: Int
        get() = list.size

    var repeat: Boolean
    var position: Int = 0

    init {
        repeat = false
    }

    fun setRepeatTrack(value: Boolean) {
        repeat = value
    }

    fun clearQueue() {
        list.clear()
        println("music.Queue cleared")
    }

    fun add(track: Track) {
        list.add(track)
        println("Track added to queue")
    }

    fun removeAt(index: Int) {
        list.removeAt(index)
        println("Track removed from queue")
    }

    fun get(index: Int): Track {
        return list[index]
    }

    fun getQueue(): MutableList<Track> {
        return list
    }

    fun randomizeQueue() {
        list.shuffle()
    }

    fun addAll(tracks: List<Track>) {
        list.addAll(tracks)
    }

    fun isQueueNotEmpty(): Boolean {
        return list.isNotEmpty()
    }

    fun isQueueEmpty(): Boolean {
        return list.isEmpty()
    }


}