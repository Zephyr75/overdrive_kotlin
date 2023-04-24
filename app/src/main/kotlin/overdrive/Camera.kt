package overdrive

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

enum class CameraMovement {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT
}

const val YAW = -90.0f
const val PITCH = 0.0f
const val SPEED = 2.5f
const val SENSITIVITY = 0.1f
const val ZOOM = 45.0f

class Camera {
    var position: Vector3f
    var front: Vector3f = Vector3f(0.0f, 0.0f, -1.0f)
    var up: Vector3f = Vector3f(0.0f, 1.0f, 0.0f)
    var right: Vector3f = Vector3f(1.0f, 0.0f, 0.0f)
    var worldUp: Vector3f
    var yaw: Float
    var pitch: Float
    var movementSpeed: Float
    var mouseSensitivity: Float
    var zoom: Float

    init {
        position = Vector3f(0.0f, 0.0f, 3.0f)
        worldUp = Vector3f(0.0f, 1.0f, 0.0f)
        yaw = YAW
        pitch = PITCH
        movementSpeed = SPEED
        mouseSensitivity = SENSITIVITY
        zoom = ZOOM
        updateCameraVectors()
    }

    fun getViewMatrix(): Matrix4f {
        return Matrix4f().lookAt(position, position.add(front, Vector3f()), up)
    }

    fun updateCameraVectors() {
        val front = Vector3f()
        front.x = (Math.cos(Math.toRadians(yaw.toDouble())) * Math.cos(Math.toRadians(pitch.toDouble()))).toFloat()
        front.y = Math.sin(Math.toRadians(pitch.toDouble())).toFloat()
        front.z = (Math.sin(Math.toRadians(yaw.toDouble())) * Math.cos(Math.toRadians(pitch.toDouble()))).toFloat()
        this.front = front.normalize()
        right = this.front.cross(worldUp, Vector3f()).normalize()
        up = right.cross(this.front, Vector3f()).normalize()
    }

    fun processKeyboard(direction: CameraMovement, deltaTime: Float) {
        val velocity = movementSpeed * deltaTime
        when (direction) {
            CameraMovement.FORWARD -> position.add(front.mul(velocity))
            CameraMovement.BACKWARD -> position.sub(front.mul(velocity))
            CameraMovement.LEFT -> position.sub(right.mul(velocity))
            CameraMovement.RIGHT -> position.add(right.mul(velocity))
        }
    }

    fun processMouseMovement(xOffset: Float, yOffset: Float, constrainPitch: Boolean = true) {
        var yaw = this.yaw + xOffset * mouseSensitivity
        var pitch = this.pitch + yOffset * mouseSensitivity

        if (constrainPitch) {
            if (pitch > 89.0f)
                pitch = 89.0f
            if (pitch < -89.0f)
                pitch = -89.0f
        }

        this.yaw = yaw
        this.pitch = pitch

        updateCameraVectors()
    }

    fun processMouseScroll(yOffset: Float) {
        var zoom = this.zoom - yOffset
        if (zoom < 1.0f)
            zoom = 1.0f
        if (zoom > 45.0f)
            zoom = 45.0f

        this.zoom = zoom
    }
  

}
