package com.example.game.engine

data class ScreenPoint(
    val x: Float,
    val y: Float,
    val scale: Float,
    val zDepth: Float,
    val isVisible: Boolean
)

class Projection3D {
    companion object {
        fun project(
            worldPoint: Vector3,
            cameraPos: Vector3,
            cameraRotX: Float,
            cameraRotY: Float,
            cameraRotZ: Float = 0f,
            focalLength: Float,
            screenWidth: Float,
            screenHeight: Float
        ): ScreenPoint {
            // Translate relative to camera position
            val rel = worldPoint - cameraPos

            // Rotate into camera space (Yaw, Pitch, Roll)
            val rotY = rel.rotateY(-cameraRotY)
            val rotX = rotY.rotateX(-cameraRotX)
            val rotated = rotX.rotateZ(-cameraRotZ)

            val z = rotated.z
            // Near clipping plane
            if (z <= 5f) {
                return ScreenPoint(0f, 0f, 0f, z, false)
            }

            val scale = focalLength / z
            val screenX = screenWidth / 2f + rotated.x * scale
            val screenY = screenHeight / 2f - rotated.y * scale

            val isVisible = screenX in -400f..(screenWidth + 400f) &&
                    screenY in -400f..(screenHeight + 400f)

            return ScreenPoint(screenX, screenY, scale, z, isVisible)
        }
    }
}
