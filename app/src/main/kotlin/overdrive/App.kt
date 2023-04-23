package overdrive

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
import overdrive.Shader.*


const val FLOAT_SIZE = 4

// check for input events in GLFW and process them
fun processInput(window: Long) {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetWindowShouldClose(window, true)
    }
}

fun main(args: Array<String>) {
    println("LWJGL version: " + Version.getVersion())

    // TODO remove this
    glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    // fix compilation on macOS
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

    val window = glfwCreateWindow(800, 600, "Overdrive", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    // set up callback for window resizing
    glfwSetFramebufferSizeCallback(window) { _, width, height -> glViewport(0, 0, width, height) }

    glfwMakeContextCurrent(window)
    // TODO remove this
    glfwSwapInterval(1)

    GL.createCapabilities()

    // build and compile our shader program
    val shaderProgram = Shader("src/main/resources/shaders/simple.vert.glsl", "src/main/resources/shaders/simple.frag.glsl") 

    // set up vertex data
    val vertices = floatArrayOf(
        // positions        // colors         // texture coords
         0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top left
         0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // top right
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom right
        -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // bottom left
    )
    val indices = intArrayOf(
        0, 1, 2, // first triangle
        2, 3, 0  // second triangle
    )

    // set up vertex buffers and configure vertex attributes
    val vao = glGenVertexArrays()
    glBindVertexArray(vao)

    val vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    val ebo = glGenBuffers()
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    // configure vertex attributes
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * FLOAT_SIZE, 0)
    glEnableVertexAttribArray(0)

    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * FLOAT_SIZE, 12)
    glEnableVertexAttribArray(1)

    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * FLOAT_SIZE, 24)
    glEnableVertexAttribArray(2)

    // load and create a texture
    val texture1 = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, texture1)
    // set the texture wrapping parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    // set texture filtering parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

    // load image, create texture and generate mipmaps
    val width = intArrayOf(400)
    val height = intArrayOf(400)
    val nrChannels = intArrayOf(0)
    STBImage.stbi_set_flip_vertically_on_load(true)
    val image = STBImage.stbi_load("src/main/resources/images/wallpaper.png", width, height, nrChannels, 0)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width[0], height[0], 0, GL_RGB, GL_UNSIGNED_BYTE, image)
    glGenerateMipmap(GL_TEXTURE_2D)
    STBImage.stbi_image_free(image)

    // load and create a second texture
    val texture2 = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, texture2)
    // set the texture wrapping parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    // set texture filtering parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

    // load image, create texture and generate mipmaps
    val image2 = STBImage.stbi_load("src/main/resources/images/emoji.png", width, height, nrChannels, 0)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, image2)
    glGenerateMipmap(GL_TEXTURE_2D)
    STBImage.stbi_image_free(image2)

    // set uniforms to map each texture to the correct location
    shaderProgram.use()
    shaderProgram.setInt("texture1", 0)
    shaderProgram.setInt("texture2", 1)


    // render loop
    while (!glfwWindowShouldClose(window))
    {
        // input
        processInput(window);

        // render
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // bind textures on corresponding texture units
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture1)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, texture2)
        
        // activate shader
        shaderProgram.use()

        // create transformations
        val model = Matrix4f().identity()
        val view = Matrix4f().identity()
        val projection = Matrix4f().identity()
        model.rotate(-Math.toRadians(55.0f).toFloat(), Vector3f(1.0f, 0.0f, 0.0f))
        view.translate(Vector3f(0.0f, 0.0f, -3.0f))
        projection.perspective(Math.toRadians(45.0f).toFloat(), 800.0f / 600.0f, 0.1f, 100.0f)
        
        val modelLoc = glGetUniformLocation(shaderProgram.ID, "model")
        val viewLoc = glGetUniformLocation(shaderProgram.ID, "view")
        val projectionLoc = glGetUniformLocation(shaderProgram.ID, "projection")

        glUniformMatrix4fv(modelLoc, false, model.get(BufferUtils.createFloatBuffer(16)))
        glUniformMatrix4fv(viewLoc, false, view.get(BufferUtils.createFloatBuffer(16)))
        glUniformMatrix4fv(projectionLoc, false, projection.get(BufferUtils.createFloatBuffer(16)))

        // draw our first triangle
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // swap buffers and poll IO events
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    // optional: de-allocate all resources once they've outlived their purpose:
    // ------------------------------------------------------------------------
    // glDeleteVertexArrays(1, &VAO);
    // glDeleteBuffers(1, &VBO);
    // glDeleteBuffers(1, &EBO);

    // glfw: terminate, clearing all previously allocated GLFW resources.
    // ------------------------------------------------------------------
    glfwTerminate();
}

