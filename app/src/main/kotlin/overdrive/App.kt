package overdrive

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL
import overdrive.Shader.*

// process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly
fun processInput(window: Long) {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetWindowShouldClose(window, true)
    }
}

// glfw: whenever the window size changed (by OS or user resize) this callback function executes
fun framebufferSizeCallback(window: Long, width: Int, height: Int) {
    glViewport(0, 0, width, height)
}


fun main(args: Array<String>) {
    println("LWJGL version: " + Version.getVersion())

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

    val window = glfwCreateWindow(800, 600, "LearnOpenGL", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    glfwSetFramebufferSizeCallback(window) { _, width, height -> glViewport(0, 0, width, height) }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)

    GL.createCapabilities()

    val shaderProgram = Shader("src/main/resources/shaders/simple.vert.glsl", "src/main/resources/shaders/simple.frag.glsl") 

    // Set up vertex data and buffer(s)
    val vertices = floatArrayOf(
        -0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f, // top left
        0.5f,  0.5f, 0.0f, 0.0f, 1.0f, 0.0f, // top right
        0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom right
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f,  // bottom left
    )
    val indices = intArrayOf(
        0, 1, 2, // first triangle
        2, 3, 0  // second triangle
    )

    val vao = glGenVertexArrays()

    glBindVertexArray(vao)

    val vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    val ebo = glGenBuffers()
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    // Configure vertex attributes
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0)
    glEnableVertexAttribArray(0)

    glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 12)
    glEnableVertexAttribArray(1)

    // render loop
    // -----------
    while (!glfwWindowShouldClose(window))
    {
        // input
        // -----
        processInput(window);

        // render
        // ------
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // draw our first triangle
        shaderProgram.use()
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
        // -------------------------------------------------------------------------------
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

