import java.io.File

class Globals {
    companion object {
        var cornerX = 1
        var cornerY = 1
        const val indent = 1
        const val minSquareSize = 1
        val defaultFile = File("default.txt")
        const val streakSize = 20F
        const val probability: Double = 0.8
        const val windowHeight = 600
        const val windowWidth = 1500

        init {
            defaultFile.createNewFile()
        }
    }


}