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
import overdrive.Camera.*
import java.util.Vector


const val FLOAT_SIZE = 4

val cam = Camera()
var lastX = 800.0f / 2.0f
var lastY = 600.0f / 2.0f
var firstMouse = true

var deltaTime = 0.0f
var lastFrame = 0.0f

// check for input events in GLFW and process them
fun processInput(window: Long) {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetWindowShouldClose(window, true)
    }
    
    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
        cam.processKeyboard(CameraMovement.FORWARD, deltaTime)
    }
    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
        cam.processKeyboard(CameraMovement.BACKWARD, deltaTime)
    }
    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
        cam.processKeyboard(CameraMovement.LEFT, deltaTime)
    }
    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
        cam.processKeyboard(CameraMovement.RIGHT, deltaTime)
    }
}

fun mouseCallback(window: Long, xpos: Double, ypos: Double) {
    if (firstMouse) {
        lastX = xpos.toFloat()
        lastY = ypos.toFloat()
        firstMouse = false
    }

    var xoffset = xpos.toFloat() - lastX
    var yoffset = lastY - ypos.toFloat() // reversed since y-coordinates go from bottom to top

    lastX = xpos.toFloat()
    lastY = ypos.toFloat()

    cam.processMouseMovement(xoffset, yoffset)
}

fun scrollCallback(window: Long, xoffset: Double, yoffset: Double) {
    cam.processMouseScroll(yoffset.toFloat())
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

    glfwSetCursorPosCallback(window, ::mouseCallback)
    glfwSetScrollCallback(window, ::scrollCallback)

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)


    glfwMakeContextCurrent(window)
    // TODO remove this
    glfwSwapInterval(1)

    GL.createCapabilities()

    glEnable(GL_DEPTH_TEST);

    // build and compile our shader program
    val shaderProgram = Shader("src/main/resources/shaders/simple.vert.glsl", "src/main/resources/shaders/simple.frag.glsl") 

    // set up vertex data
    val vertices = floatArrayOf(
        // positions        // colors         // texture coords
        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
         0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
         0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
         0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

        -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
        -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
         0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
         0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
         0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
         0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
        -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
    )
    val indices = intArrayOf(
        0, 1, 2, // first triangle
        2, 3, 0  // second triangle
    )

    val cubePositions = arrayOf(
        Vector3f( 0.0f,  0.0f,  0.0f),
        Vector3f( 2.0f,  5.0f, -15.0f),
        Vector3f(-1.5f, -2.2f, -2.5f),
        Vector3f(-3.8f, -2.0f, -12.3f),
        Vector3f( 2.4f, -0.4f, -3.5f),
        Vector3f(-1.7f,  3.0f, -7.5f),
        Vector3f( 1.3f, -2.0f, -2.5f),
        Vector3f( 1.5f,  2.0f, -2.5f),
        Vector3f( 1.5f,  0.2f, -1.5f),
        Vector3f(-1.3f,  1.0f, -1.5f),
    )

    // set up vertex buffers and configure vertex attributes
    val vao = glGenVertexArrays()
    glBindVertexArray(vao)

    val vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    // val ebo = glGenBuffers()
    // glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    // glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    // configure vertex attributes
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * FLOAT_SIZE, 0)
    glEnableVertexAttribArray(0)

    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * FLOAT_SIZE, 12)
    glEnableVertexAttribArray(1)

    // glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * FLOAT_SIZE, 24)
    // glEnableVertexAttribArray(2)

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
        
        // per-frame time logic
        var currentFrame = glfwGetTime()
        deltaTime = (currentFrame - lastFrame).toFloat()
        lastFrame = currentFrame.toFloat()

        // input
        processInput(window);

        // render
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // bind textures on corresponding texture units
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture1)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, texture2)
        
        // activate shader
        shaderProgram.use()

        // create transformations
        val projection = Matrix4f().identity()
        projection.perspective(Math.toRadians(cam.zoom).toFloat(), 800.0f / 600.0f, 0.1f, 100.0f)
        val projectionLoc = glGetUniformLocation(shaderProgram.ID, "projection")
        glUniformMatrix4fv(projectionLoc, false, projection.get(BufferUtils.createFloatBuffer(16)))

        val view = cam.getViewMatrix()
        val viewLoc = glGetUniformLocation(shaderProgram.ID, "view")
        glUniformMatrix4fv(viewLoc, false, view.get(BufferUtils.createFloatBuffer(16)))
        
        val modelLoc = glGetUniformLocation(shaderProgram.ID, "model")



        // render boxes
        glBindVertexArray(vao)
        for (i in 0 until 10)
        {
            // calculate the model matrix for each object and pass it to shader before drawing
            val model = Matrix4f().identity()
            model.translate(cubePositions[i])
            val angle = 20.0f * i
            model.rotate(Math.toRadians(angle.toDouble()).toFloat(), Vector3f(1.0f, 0.3f, 0.5f))
            glUniformMatrix4fv(modelLoc, false, model.get(BufferUtils.createFloatBuffer(16)))

            glDrawArrays(GL_TRIANGLES, 0, 36)
        }




        // // draw our first triangle
        // glBindVertexArray(vao);
        // // glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        // glDrawArrays(GL_TRIANGLES, 0, 36);

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

