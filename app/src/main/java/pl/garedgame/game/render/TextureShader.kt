package pl.garedgame.game.render

import android.opengl.GLES30

class TextureShader private constructor() : ShaderProgram() {

    companion object {
        val instance = TextureShader()
    }

    override val VERTEX_SHADER = """
        precision mediump float;
        uniform mat4 $MVP_MATRIX;
        attribute vec4 $POSITION;
        attribute vec2 $TEXTURE_COORDINATE;
        varying vec2 position;

        void main(){
            gl_Position = $MVP_MATRIX * $POSITION;
            position = $TEXTURE_COORDINATE;
        }
    """.trimIndent()

    override val FRAGMENT_SHADER = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 position;

        void main() {
            gl_FragColor = texture2D(uTexture, position);
        }
    """.trimIndent()

    override fun onUse() {
        super.onUse()
        vTexturePosition = GLES30.glGetAttribLocation(iProgramId, TEXTURE_COORDINATE)
    }
}
