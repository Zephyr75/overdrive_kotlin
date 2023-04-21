package overdrive

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL

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
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

    val window = glfwCreateWindow(800, 600, "LearnOpenGL", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    glfwSetFramebufferSizeCallback(window) { _, width, height -> glViewport(0, 0, width, height) }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)

    GL.createCapabilities()

    // build and compile our shader program
    // ------------------------------------
    // vertex shader
    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    val vertexShaderSource = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        void main()
        {
            gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
        }
        """.trimIndent()
    glShaderSource(vertexShader, vertexShaderSource)
    glCompileShader(vertexShader)
    // check for shader compile errors
    var success = glGetShaderi(vertexShader, GL_COMPILE_STATUS)
    if (success == GL_FALSE) {
        val log = glGetShaderInfoLog(vertexShader)
        throw RuntimeException("Vertex shader compilation failed:\n$log")
    }

    // fragment shader
    val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
    val fragmentShaderSource = """
        #version 330 core
        out vec4 FragColor;
        void main()
        {
            FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
        }
        """.trimIndent()
    glShaderSource(fragmentShader, fragmentShaderSource)
    glCompileShader(fragmentShader)
    // check for shader compile errors
    success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS)
    if (success == GL_FALSE) {
        val log = glGetShaderInfoLog(fragmentShader)
        throw RuntimeException("Fragment shader compilation failed:\n$log")
    }

    // link shaders
    val shaderProgram = glCreateProgram()
    glAttachShader(shaderProgram, vertexShader)
    glAttachShader(shaderProgram, fragmentShader)
    glLinkProgram(shaderProgram)
    // check for linking errors
    success = glGetProgrami(shaderProgram, GL_LINK_STATUS)
    if (success == GL_FALSE) {
        val log = glGetProgramInfoLog(shaderProgram)
        throw RuntimeException("Shader program linking failed:\n$log")
    }
    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    // Set up vertex data and buffer(s)
    val vertices = floatArrayOf(
        -0.5f,  0.5f, 0.0f, // top left
        0.5f,  0.5f, 0.0f, // top right
        0.5f, -0.5f, 0.0f, // bottom right
        -0.5f, -0.5f, 0.0f  // bottom left
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
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
    glEnableVertexAttribArray(0)

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
        glUseProgram(shaderProgram);
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

