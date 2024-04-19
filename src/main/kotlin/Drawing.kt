import org.jetbrains.skia.*
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView


fun showStreak(game: Game, canvas: Canvas) {
    val x = stateToCeil(MoveState.mouseX, true, game)  //(MoveState.mouseX - Globals.indent) / game.squareSize + Globals.cornerX
    val y = stateToCeil(MoveState.mouseY, false, game)//(MoveState.mouseY - Globals.indent) / game.squareSize + Globals.cornerY
    if (x > game.width || y > game.height) return
    if (game.getField(x, y).isAlive) {
        val font = Font(
            Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf"),
            Globals.streakSize
        )
        val red = Paint().apply {
            color = Color.makeRGB(255, 0, 0)
        }
        canvas.drawString(
            "${game.getField(x, y).winStreak}",
            MoveState.mouseX.toFloat(),
            MoveState.mouseY.toFloat(),
            font,
            red
        )
    }
}

fun drawGridAndSquares(paint: Paint, canvas: Canvas, game: Game, width: Int, height: Int) {
    val currW = minOf(game.width, (width - Globals.indent) / game.changedSquareSize)
    val currH = minOf(game.height, (height - Globals.indent) / game.changedSquareSize)
    Globals.cornerX += (game.changedSquareSize - game.squareSize) *
            ((MoveState.mouseX - 2) / game.changedSquareSize) / game.squareSize
    Globals.cornerY += (game.changedSquareSize - game.squareSize) *
            ((MoveState.mouseY - 2) / game.changedSquareSize) / game.squareSize
    Globals.cornerX = maxOf(1, minOf(Globals.cornerX, game.width - currW + 1))
    Globals.cornerY = maxOf(1, minOf(Globals.cornerY, game.height - currH + 1))
    game.squareSize = game.changedSquareSize
    val colLength = (if (Globals.cornerY+currH > game.height) currH * game.squareSize + Globals.indent else height).toFloat()
    val rowLength = (if (Globals.cornerX+currW > game.width) currW * game.squareSize + Globals.indent else width).toFloat()
    (0..currW).forEach {
        canvas.drawLine(
            it * game.squareSize + Globals.indent.toFloat(),
            Globals.indent.toFloat(),
            it * game.squareSize + Globals.indent.toFloat(),
            colLength,//(currH * game.squareSize + Globals.indent).toFloat(),
            paint
        )
    }
    (0..currH).forEach {
        canvas.drawLine(
            Globals.indent.toFloat(),
            it * game.squareSize + Globals.indent.toFloat(),
            rowLength,//(currW * game.squareSize + Globals.indent).toFloat(),
            it * game.squareSize + Globals.indent.toFloat(),
            paint
        )
    }
    for (x in 0..minOf(currW, game.width - Globals.cornerX)) {
        for (y in 0..minOf(currH, game.height - Globals.cornerY)) {
            if (game.getField(x + Globals.cornerX, y + Globals.cornerY).isAlive) {
                paintSquare(x, y, canvas, paint, game)
            }
        }
    }


}

fun paintSquare(x: Int, y: Int, canvas: Canvas, paint: Paint, game: Game) {

    canvas.drawRect(
        Rect.makeXYWH(
            (x) * game.squareSize + Globals.indent.toFloat(),
            (y) * game.squareSize + Globals.indent.toFloat(),
            game.squareSize.toFloat(),
            game.squareSize.toFloat()
        ), paint
    )
}

fun gameRender(skiaLayer: SkiaLayer, game: Game) {
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, object : SkikoView {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            displayField(canvas, game, width, height)
            skiaLayer.needRedraw()
            if (game.isGoing) game.make1Move()
        }

        private fun displayField(canvas: Canvas, game: Game, width: Int, height: Int) {

            val white = Paint().apply {
                color = Color.makeRGB(255, 255, 255)
            }
            val black = Paint().apply {
                color = Color.makeRGB(0, 0, 0)
            }
            canvas.drawPaint(white)

            drawGridAndSquares(black, canvas, game, width, height)
            showStreak(game, canvas)
        }
    })
}

object MoveState {
    var mouseX = 0
    var mouseY = 0
}
