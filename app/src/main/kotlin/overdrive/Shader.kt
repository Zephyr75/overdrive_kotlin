package overdrive

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class Shader(vertexPath: String, fragmentPath: String) {
    var ID: Int = 0

    init {
        // read shader source codes from files
        val vertexCode = readFile(vertexPath)
        val fragmentCode = readFile(fragmentPath)
        val vShaderCode = vertexCode?.let { it } ?: ""
        val fShaderCode = fragmentCode?.let { it } ?: ""

        // compile vertex shader
        val vertex = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertex, vShaderCode)
        glCompileShader(vertex)
        checkCompileErrors(vertex, "VERTEX")

        // compile fragment shader
        val fragment = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragment, fShaderCode)
        glCompileShader(fragment)
        checkCompileErrors(fragment, "FRAGMENT")

        // link shaders
        ID = glCreateProgram()
        glAttachShader(ID, vertex)
        glAttachShader(ID, fragment)
        glLinkProgram(ID)
        checkCompileErrors(ID, "PROGRAM")

        // delete shaders
        glDeleteShader(vertex)
        glDeleteShader(fragment)
    }

    fun use() {
        glUseProgram(ID)
    }

    fun setBool(name: String, value: Boolean) {
        glUniform1i(glGetUniformLocation(ID, name), if (value) 1 else 0)
    }

    fun setInt(name: String, value: Int) {
        glUniform1i(glGetUniformLocation(ID, name), value)
    }

    fun setFloat(name: String, value: Float) {
        glUniform1f(glGetUniformLocation(ID, name), value)
    }

    fun setMat4(name: String, value: FloatArray) {
        glUniformMatrix4fv(glGetUniformLocation(ID, name), false, value)
    }

    private fun readFile(path: String): String? {
        val sb = StringBuilder()
        try {
            BufferedReader(FileReader(path)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return sb.toString()
    }

    private fun checkCompileErrors(shader: Int, type: String) {
        val success: Int
        if (type != "PROGRAM") {
            success = glGetShaderi(shader, GL_COMPILE_STATUS)
            if (success == GL_FALSE) {
                val infoLog = glGetShaderInfoLog(shader, 1024)
                println("ERROR::SHADER_COMPILATION_ERROR of type: $type\n${infoLog.toString()}\n -- --------------------------------------------------- -- ")
            }
        } else {
            success = glGetProgrami(shader, GL_LINK_STATUS)
            if (success == GL_FALSE) {
                val infoLog = glGetProgramInfoLog(shader, 1024)
                println("ERROR::PROGRAM_LINKING_ERROR of type: $type\n${infoLog.toString()}\n -- --------------------------------------------------- -- ")
            }
        }
    }
}
