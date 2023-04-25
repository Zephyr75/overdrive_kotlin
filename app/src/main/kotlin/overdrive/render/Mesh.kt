package overdrive.render

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.BufferUtils
import org.lwjgl.stb.*
import org.joml.*


data class Vertex(val position: Vector3f, val normal: Vector3f, val uv: Vector2f, val tangent: Vector3f, val bitangent: Vector3f, val boneIndices: IntArray, val boneWeights: FloatArray)

data class Texture(val id: Int, val type: String, val path: String)

class Mesh(val vertices: Array<Vertex>, val indices: IntArray, val textures: Array<Texture>) {
    private val vao: Int
    private val vbo: Int
    private val ebo: Int

    init {
        // Create buffers/arrays 
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        ebo = glGenBuffers()

        // Bind the Vertex Array Object
        glBindVertexArray(vao)

        // Load data into vertex buffers
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        // Vertex positions
        glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.SIZE, 0)
        glEnableVertexAttribArray(0)
        // Vertex normals
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.SIZE, 3 * 4)
        glEnableVertexAttribArray(1)
        // Vertex texture coords
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Vertex.SIZE, 6 * 4)
        glEnableVertexAttribArray(2)
        // Vertex tangent
        glVertexAttribPointer(3, 3, GL_FLOAT, false, Vertex.SIZE, 8 * 4)
        glEnableVertexAttribArray(3)
        // Vertex bitangent
        glVertexAttribPointer(4, 3, GL_FLOAT, false, Vertex.SIZE, 11 * 4)
        glEnableVertexAttribArray(4)
        // Vertex bone indices
        glVertexAttribPointer(5, 4, GL_FLOAT, false, Vertex.SIZE, 14 * 4)
        glEnableVertexAttribArray(5)
        // Vertex bone weights
        glVertexAttribPointer(6, 4, GL_FLOAT, false, Vertex.SIZE, 18 * 4)
        glEnableVertexAttribArray(6)
        
        // Unbind the Vertex Array Object
        glBindVertexArray(0)
    }

    fun draw(shader: Shader) {
        // Bind appropriate textures
        var diffuseNr = 1
        var specularNr = 1
        var normalNr = 1
        var heightNr = 1
        for (i in textures.indices) {
            // Activate proper texture unit before binding
            glActiveTexture(GL_TEXTURE0 + i)

            // Retrieve texture number (the N in diffuse_textureN)
            val number: String
            val name = textures[i].type
            when (name) {
                "texture_diffuse" -> {
                    number = diffuseNr.toString()
                    diffuseNr++
                }
                "texture_specular" -> {
                    number = specularNr.toString()
                    specularNr++
                }
                "texture_normal" -> {
                    number = normalNr.toString()
                    normalNr++
                }
                "texture_height" -> {
                    number = heightNr.toString()
                    heightNr++
                }
                else -> number = "1"
            }
            // Now set the sampler to the correct texture unit
            shader.setInt(name + number, i)
            // And finally bind the texture
            glBindTexture(GL_TEXTURE_2D, textures[i].id)
        }

        // Draw mesh
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)

        // Always good practice to set everything back to defaults once configured.
        glActiveTexture(GL_TEXTURE0)
    }        
        
}   
