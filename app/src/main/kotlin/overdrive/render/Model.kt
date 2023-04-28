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
import org.lwjgl.assimp.*

data class Model(private val path: String, private val gamma: Boolean = false) {
    private val directory = "/home/Github/overdrive/app/src/main/resources/models/"
    private val texturesLoaded = mutableListOf<Texture>()
    private val meshes = mutableListOf<Mesh>()

    init {
        loadModel(path)
    }

    fun draw(shader: Shader) {
        for (mesh in meshes) {
            mesh.draw(shader)
        }
    }

    private fun loadModel() {
        val importer = Assimp()
        val scene = importer.importFile(directory + path, Assimp.aiProcess_Triangulate or Assimp.aiProcess_FlipUVs)

        if (scene == null || scene.mRootNode() == null || scene.mFlags() and Assimp.AI_SCENE_FLAGS_INCOMPLETE != 0) {
            println("ERROR::ASSIMP::${importer.errorString()}")
            return
        }

        processNode(scene.mRootNode()!!, scene)

    }

    private fun processNode(node: AINode, scene: AIScene) {
        for (i in 0 until node.mNumMeshes()) {
            val mesh = AIMesh.create(scene.mMeshes()!!.get(node.mMeshes()!!.get(i)))
            meshes.add(processMesh(mesh, scene))
        }

        for (i in 0 until node.mNumChildren()) {
            processNode(AINode.create(node.mChildren()!!.get(i)), scene)
        }
    }



}
