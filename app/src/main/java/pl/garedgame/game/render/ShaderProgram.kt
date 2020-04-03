package pl.garedgame.game.render

import android.opengl.GLES30
import pl.garedgame.game.util.debug

abstract class ShaderProgram {

    companion object {
        const val MVP_MATRIX = "uMVPMatrix"
        const val POSITION = "vPosition"
        const val TEXTURE_COORDINATE = "vTextureCoordinate"
        const val COLOR = "vColor"

        private var currentProgramId: Int = -1

        fun loadShaderProgram(strSource: String, iType: Int): Int {
            val compiled = IntArray(1)
            val iShader = GLES30.glCreateShader(iType)
            GLES30.glShaderSource(iShader, strSource)
            GLES30.glCompileShader(iShader)
            GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] <= 0) {
                throw RuntimeException("Compilation failed : " + GLES30.glGetShaderInfoLog(iShader))
            }
            return iShader
        }
    }

    protected abstract val VERTEX_SHADER: String
    protected abstract val FRAGMENT_SHADER: String

    var iProgramId: Int = 0
        private set
    var vPosition: Int = 0
        protected set
    var uMVPMatrix: Int = 0
        protected set
    var vTexturePosition: Int = 0
        protected set
    var vColor: Int = 0
        protected set

    fun load() = load(VERTEX_SHADER, FRAGMENT_SHADER)

    private fun load(vertexShader: String, fragmentShader: String) {
        val iVShader: Int = loadShaderProgram(vertexShader, GLES30.GL_VERTEX_SHADER)
        val iFShader: Int = loadShaderProgram(fragmentShader, GLES30.GL_FRAGMENT_SHADER)
        iProgramId = GLES30.glCreateProgram()
        val link = IntArray(1)
        GLES30.glAttachShader(iProgramId, iVShader)
        GLES30.glAttachShader(iProgramId, iFShader)
        GLES30.glLinkProgram(iProgramId)

        GLES30.glGetProgramiv(iProgramId, GLES30.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            throw RuntimeException("Program couldn't be loaded")
        }
        GLES30.glDeleteShader(iVShader)
        GLES30.glDeleteShader(iFShader)
        debug {
            "ShaderProgram load finish"
        }
    }

    protected open fun onUse() {
        uMVPMatrix = GLES30.glGetUniformLocation(iProgramId, MVP_MATRIX)
        vPosition = GLES30.glGetAttribLocation(iProgramId, POSITION)
    }

    fun use() {
        if (currentProgramId != iProgramId) {
            GLES30.glUseProgram(iProgramId)
            onUse()
            currentProgramId = iProgramId
        }
    }
}
