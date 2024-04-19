import org.jetbrains.skia.Color
import java.awt.image.BufferedImage

class Game(
    val width: Int = 30,
    val height: Int = 30,
    sqSIze: Int = 20,
    var toSurvive: List<Int> = listOf(2, 3),
    var toBorn: List<Int> = listOf(3)
) {
    val rngW = (1..width)
    val rngH = (1..height)
    var isGoing:Boolean = false
    var squareSize = sqSIze
    var changedSquareSize = squareSize
    val nbPositions: List<Pair<Int, Int>> = (0 until 9).filter {
        it != 4
    }.map {
        Pair(it / 3 - 1, it % 3 - 1)
    }

    inner class Ceil(val x: Int, val y: Int) {
        var isAlive: Boolean = false
        var willLive: Boolean = false
        var winStreak: Int = 0

        fun isGood(): Boolean = (x in rngW) && (y in rngH)
        private fun neighbors(): List<Ceil> =
            if (x == 0 || y == 0 || x == width || y == height) emptyList() else
                nbPositions.map { getField(this.x + it.first, this.y + it.second) }

        private fun countAliveNeighbors(): Int = this.neighbors().count { it.isAlive }
        fun wilLive(): Boolean = if (this.isAlive) this.countAliveNeighbors() in toSurvive
        else this.countAliveNeighbors() in toBorn
    }

    private val field: List<Ceil> = (0 until (width + 2) * (height + 2)).map {
        Ceil(it / (height + 2), it % (height + 2))
    }

    fun getField(x: Int, y: Int): Ceil = field[x * (height + 2) + y]

    fun make1Move() {
//        for (x in rngW){
//            for (y in rngH){
//                getField(x,y).willLive = getField(x,y).wilLive()
//            }
//        }
//        for (x in rngW){
//            for (y in rngH){
//                getField(x,y).isAlive = getField(x,y).willLive
//                if (getField(x,y).isAlive) getField(x,y).winStreak++ else getField(x,y).winStreak = 0
//            }
//        }
        field.filter { it.isGood() }.forEach { it.willLive = it.wilLive() }
        field.filter { it.isGood() }.forEach {
            it.isAlive = it.willLive
            if (it.isAlive) it.winStreak++ else it.winStreak = 0
        }
    }

    fun start(){
        isGoing = true
    }

    fun stop() {
        isGoing = false
    }

    fun clear() {
//        for (x in rngW){
//            for (y in rngH){
//                getField(x,y).willLive = getField(x,y).wilLive()
//            }
//        }
        field.forEach { it.isAlive = false }
    }

    fun makeNMoves(n : Int) {
        repeat(n){
            make1Move()
        }
    }

    fun fieldToString(): String {
        return field.filter { it.isGood() }.joinToString("") { if (it.isAlive) "1" else "0" }
    }

    fun fieldTOImg(): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        field.filterIndexed { _, ceil ->
            ceil.x in (1 until 1 + width) &&
                    ceil.y in (1 until 1 + height)
        }.forEach {
            if (it.isAlive) {
                img.setRGB(it.x - 1, it.y - 1, Color.makeRGB(0, 0, 0))
            } else {
                img.setRGB(it.x - 1, it.y - 1, Color.makeRGB(255, 255, 255))
            }
        }
        return img
    }

    fun generateField() {
        field.filter { it.isGood() }.forEach { it.isAlive = Math.random() >= Globals.probability }
    }

}