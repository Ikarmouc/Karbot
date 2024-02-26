import dev.arbjerg.lavalink.protocol.v4.Track

class Queue() {
    private val list = mutableListOf<Track>()
    val size: Int
        get() = list.size
    val isEmpty: Boolean

    var repeat: Boolean
    var position: Int = 0

    init {
        isEmpty = list.isEmpty()
        repeat = false
    }

    fun setRepeatTrack(value: Boolean) {
        repeat = value
    }

    fun getTrackPosition(): Int {
        return position
    }
    fun incrementPosition() {
        if(position < size){
            position++
        }
    }
    fun getRepeatTrack(): Boolean {
        return repeat
    }
    fun clearQueue() {
        list.clear()
        println("Queue cleared")
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
        println("Queue randomized")
    }

    fun addAll(tracks: List<Track>) {
        list.addAll(tracks)
        println("Tracks added to queue")
    }

    fun isQueueNotEmpty(): Boolean {
        return list.isNotEmpty()
    }

    fun isQueueEmpty(): Boolean {
        return list.isEmpty()
    }

//    fun addFirst(track: Track) {
//        list.add(0, track)
//        println("Track added to queue")
//    }

}