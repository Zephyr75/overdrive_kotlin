package render

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
import overdrive.render.*

// process all keyboard inputs
fun processKeyboardInputs(window: Long, cam: Camera, deltaTime: Float) {
    // quit on escape
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetWindowShouldClose(window, true)
    }
    
    // camera movement
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

fun callbackMouseMovement(xpos: Double, ypos: Double, cam: Camera, first: Boolean, lastX: Double, lastY: Double) : Triple<Boolean, Double, Double> {
    var newX = lastX
    var newY = lastY

    if (first) {
        newX = xpos
        newY = ypos
    }

    val xoffset = xpos - newX
    val yoffset = newY - ypos // reversed since y-coordinates go from bottom to top

    newX = xpos
    newY = ypos

    cam.processMouseMovement(xoffset, yoffset)

    return Triple(false, newX, newY)
}

fun callbackMouseScroll(yoffset: Double, cam: Camera) {
    cam.processMouseScroll(yoffset.toFloat())
}


