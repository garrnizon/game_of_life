import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.*
import java.io.File
import javax.swing.*
import kotlin.system.exitProcess


fun main() {
    openWindow(Game(1024, 1024))
}

fun wheel(rotation: Int, game: Game) {
    if (game.changedSquareSize - rotation > Globals.minSquareSize) {
        game.changedSquareSize -= rotation
    }
}

fun stateToCeil(state: Int, isX: Boolean, game: Game) : Int{
    return (state - Globals.indent) / game.squareSize + (if (isX) Globals.cornerX else Globals.cornerY)
}

fun click(ex: Int, ey: Int, game: Game) {
    //if (!flag) return
    val x = stateToCeil(ex, true, game)//(ex - Globals.indent) / game.squareSize + Globals.cornerX
    val y = stateToCeil(ey, false, game)//(ey - Globals.indent) / game.squareSize + Globals.cornerY
    if (x > game.width || y > game.height) return
    game.getField(x, y).isAlive = !game.getField(x, y).isAlive
    //println("$ex $ey -> $x $y")
}

fun saveGameToTxt(game: Game, file: File) {
    file.writeText("${game.width} ${game.height} ${game.squareSize}\n")
    file.appendText("${game.toBorn.joinToString(" ")}\n${game.toSurvive.joinToString(" ")}\n")
    file.appendText(game.fieldToString())
}

fun reading(file: File, game: Game) {
    val lines = file.readLines(Charsets.UTF_8)
    if (lines.isEmpty()) {
        showError("File is empty")
        return
    }

    val params = lines[0].split(" ").filter { it != "" }.map { it.toIntOrNull() }

    if (params.any { it == null }) {
        showError("Width, Height and Size of square must be integer")
        return
    }
    params as List<Int>
    if (params.size != 3) {
        showError("Expected 3 arguments in line \"${lines[0]}\", found ${params.size}")
        return
    }

    if (lines.size < 2) {
        showError("values of necessary neighbours to Born not found")
        return
    }
    if (!parse(lines[1])) return

    if (lines.size < 3) {
        showError("values of necessary neighbours to Survive not found")
        return
    }

    if (!parse(lines[2])) return

    if (lines.size < 4) {
        showError("Ceils' conditions not found")
        return
    }
    val width = minOf(params[0], game.width)
    // если указанная ширина не совпадает с нашей, то будем отрисовывать меньшую из них
    val height = minOf(params[1], game.height) //аналогично
    game.changedSquareSize = params[2]
    game.toBorn = lines[1].split(" ").filter { it != "" }.map { it.toInt() }.toSet().toList()
    game.toSurvive = lines[2].split(" ").filter { it != "" }.map { it.toInt() }.toSet().toList()
    for (i in 0 until minOf(width * height, lines[3].length)) {//пробегаемся либо по необходимиму
        // в зависимости от наших размеров, либо по данным, при это оставляю остальные любыми
        game.getField(i / width + 1, i % width + 1).isAlive = lines[3][i] != '0'
    }

}

fun openWindow(game: Game) = runBlocking(Dispatchers.Swing) {
    val skiaLayer = SkiaLayer()
    SwingUtilities.invokeLater {
        val window = JFrame("Life game").apply {
            preferredSize = Dimension(Globals.windowWidth, Globals.windowHeight)
            isVisible = true
            isResizable = false
        }
        val keyListener: KeyListener = object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}

            override fun keyReleased(e: KeyEvent) {}

            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_W -> Globals.cornerY--

                    KeyEvent.VK_S -> Globals.cornerY++

                    KeyEvent.VK_A -> Globals.cornerX--

                    KeyEvent.VK_D -> Globals.cornerX++
                }
            }
        }
        val mouseListener: MouseListener = object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {}
            override fun mouseExited(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {click(e.x, e.y, game)}
            override fun mouseEntered(e: MouseEvent) {}
            override fun mouseReleased(e: MouseEvent) {}
        }
        val wheelListener = MouseWheelListener { e -> wheel(e.wheelRotation, game) }
        val adapter: MouseMotionAdapter = object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                MoveState.mouseX = e.x
                MoveState.mouseY = e.y

            }

            override fun mouseDragged(e: MouseEvent) {

            }
        }
        window.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(we: WindowEvent) {
                when (JOptionPane.showConfirmDialog(
                    window,
                    "Do you want to save current game?",
                    "Closing",
                    JOptionPane.YES_NO_OPTION
                )) {
                    JOptionPane.YES_OPTION -> saveGameToTxt(game, Globals.defaultFile)
                }
                exitProcess(0)
            }
        })
//        skiaLayer.addKeyListener(klistener)
        when (JOptionPane.showConfirmDialog(
            window,
            "Do you want to download last saved game?",
            "Opening",
            JOptionPane.YES_NO_OPTION
        )) {
            JOptionPane.YES_OPTION -> reading(Globals.defaultFile, game)
        }
        skiaLayer.addMouseWheelListener(wheelListener)
        skiaLayer.addMouseListener(mouseListener)
        skiaLayer.addKeyListener(keyListener)
        skiaLayer.addMouseMotionListener(adapter)
        //skiaLayer.isFocusable = true
        gameRender(skiaLayer, game)
        window.add(SaveAndLoadPanel(game), BorderLayout.SOUTH)
        val gp = GamePanel(game)
        window.add(gp, BorderLayout.EAST)
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
    }

}

fun parse(s: String): Boolean {
    if (s.isEmpty()) {
        showError("Empty input")
        return false
    }
    val nums = s.split(" ").filter { it != "" }.map { it.toIntOrNull() }
    if (nums.any { it == null }) {
        showError("Print NUMBERS")
        return false
    }
    nums as List<Int>
    if (nums.any { it < 0 }) {
        showError("Numbers should be not less than 0")
        return false
    }
    if (nums.any { it > 8 }) {
        showError("Numbers should be not more than 8")
        return false
    }
    return true
}

fun showError(message: String) {
    JOptionPane.showMessageDialog(null, message)
}