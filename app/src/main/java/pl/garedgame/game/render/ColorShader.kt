package pl.garedgame.game.render

import android.opengl.GLES30

class ColorShader private constructor() : ShaderProgram() {

    companion object {
        val instance = ColorShader()
    }

    override val VERTEX_SHADER = """
            precision mediump float;
            uniform mat4 $MVP_MATRIX;
            attribute vec4 $POSITION;
            attribute vec4 $COLOR;
            varying vec4 color;

            void main(){
                gl_Position = $MVP_MATRIX * $POSITION;
                color = $COLOR;
            }
        """.trimIndent()

    override val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec4 color;

            void main() {
                gl_FragColor = color;
            }
        """.trimIndent()

    override fun onUse() {
        super.onUse()
        vColor = GLES30.glGetAttribLocation(iProgramId, COLOR)
    }
}
