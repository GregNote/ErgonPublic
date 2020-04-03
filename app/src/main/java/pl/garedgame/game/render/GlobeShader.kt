package pl.garedgame.game.render

import android.opengl.GLES30

class GlobeShader private constructor() : ShaderProgram() {

    companion object {
        val instance = GlobeShader()
    }

    override val VERTEX_SHADER: String = """
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
            float spacing = 6.0f;
            float size = 2.0f;
            float blur = 1.0f;
            vec2 resolution = vec2(800.0f,600.0f);
            vec2 count = vec2(resolution/spacing);
            vec4 color = texture2D(uTexture, position);

            vec2 pos = mod(gl_FragCoord.xy, vec2(spacing)) - vec2(spacing/2.0);
            float dist_squared = dot(pos, pos);
            gl_FragColor = mix(color, vec4(0.0), smoothstep(size, size + blur, dist_squared));
        }
    """.trimIndent()

    override fun onUse() {
        super.onUse()
        vTexturePosition = GLES30.glGetAttribLocation(iProgramId, TEXTURE_COORDINATE)
    }
}
