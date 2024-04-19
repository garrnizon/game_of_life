import org.jetbrains.skija.Color
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

abstract class Button(name: String, game: Game) : JButton(name) {
    abstract fun action(game: Game)

    init {
        this.addActionListener { action(game) }
    }
}

abstract class Panel(nOfRows: Int, nOfCols: Int, buttons: List<Button>) : JPanel(GridLayout(nOfRows, nOfCols)) {
    init {

        assert(buttons.size == nOfCols * nOfRows)

        buttons.forEach { this.add(it) }
    }
}

fun gottenFile(filterDescription: String, filterExtension: String): File? {
    val filter = FileNameExtensionFilter(filterDescription, filterExtension)
    val fileChooser = JFileChooser()
    fileChooser.addChoosableFileFilter(filter)
    fileChooser.fileFilter = filter
    val fileGet = fileChooser.showOpenDialog(null)
    if (fileGet == JFileChooser.APPROVE_OPTION) {
        var file =  fileChooser.selectedFile
        if (file.extension != filterExtension) {
            file = File("${file.path}.$filterExtension")
        }
        return file

    }
    return null
}

class SaveAndLoadPanel(game: Game) : Panel(
    1, 4,
    listOf(SaveTxtButton(game), LoadTxtButton(game), SaveBmpButton(game), LoadBmpButton(game))
)

class SaveTxtButton(game: Game) : Button("Save as txt", game) {
    override fun action(game: Game) {
        val file = gottenFile("Text (*.txt)", "txt")
        if (file != null) {
            file.createNewFile()
            saveGameToTxt(game, file)
        }
    }
}

class LoadTxtButton(game: Game) : Button("Load from txt", game) {
    override fun action(game: Game) {
        val file = gottenFile("Text (*.txt)", "txt")
        if (file != null) reading(file, game)
    }

}

class SaveBmpButton(game: Game) : Button("Save as bmp", game) {
    override fun action(game: Game) {
        val file = gottenFile("BitMap (*.bmp)", "bmp")

        if (file != null) ImageIO.write(game.fieldTOImg(), "BMP", file)
    }
}

class LoadBmpButton(game: Game) : Button("Load as bmp", game) {
    override fun action(game: Game) {
        val file = gottenFile("BitMap (*.bmp)", "bmp")
        if (file != null) writing(game, ImageIO.read(file))
    }

    private fun writing(game: Game, img: BufferedImage) {
        (1..game.width).forEach { x ->
            (1..game.height).forEach {
                game.getField(x, it).isAlive = img.getRGB(x - 1, it - 1) == Color.makeRGB(0, 0, 0)
            }
        }
    }
}

class GamePanel(game: Game) : Panel(
    7, 1,
    listOf(
        GenerateButton(game),
        ClearButton(game),
        Make1MoveButton(game),
        MakeNMovesButton(game),
        StartButton(game),
        StopButton(game),
        ChangeRulesButton(game)
    )
)

class GenerateButton(game: Game) : Button("Generate field", game) {
    override fun action(game: Game) {
        game.generateField()
    }
}

class ClearButton(game: Game) : Button("Clear field", game) {
    override fun action(game: Game) {
        game.clear()
    }
}

class Make1MoveButton(game: Game) : Button("Make 1 move", game) {
    override fun action(game: Game) {
        game.make1Move()
    }
}

class ChangeRulesButton(game: Game) : Button("Change rules", game) {
    override fun action(game: Game) {
        when (JOptionPane.showOptionDialog(
            null,
            "Do you want to change number of necessary neighbours to Born or to Survive?",
            "Changing...",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, arrayOf("Born", "Survive"), "Born"
        )) {
            JOptionPane.YES_OPTION ->
                inputBornSurv("Print all space-separated to-Born values", game)


            JOptionPane.NO_OPTION ->
                inputBornSurv("Print all space-separated to-Survive values", game)
        }
    }

    private fun inputBornSurv(message: String, game: Game) {
        val s = JOptionPane.showInputDialog(message) ?: return
        if (parse(s)) {
            if (message[29] == 'B') { //B - Born, S - survive
                game.toBorn = s.split(" ").filter { it != "" }.map { it.toInt() }.toSet().toList()
            } else {
                game.toSurvive = s.split(" ").filter { it != "" }.map { it.toInt() }.toSet().toList()
            }
        } else {
            inputBornSurv(message, game)
        }
    }
}

class MakeNMovesButton(game: Game) : Button("Make n moves", game) {
    override fun action(game: Game) {
        val n = JOptionPane.showInputDialog("How many moves do you want program to do") ?: return
        if (n.toIntOrNull() == null) {
            showError("Print integer")
            action(game)
        } else {
            game.makeNMoves(n.toInt()) //примерно 5 ходов в секунду
        }
    }
}

class StartButton(game: Game) : Button("Start", game) {
    override fun action(game: Game) {
        game.start()
    }
}

class StopButton(game: Game) : Button("Stop", game) {
    override fun action(game: Game) {
        game.stop()
    }
}