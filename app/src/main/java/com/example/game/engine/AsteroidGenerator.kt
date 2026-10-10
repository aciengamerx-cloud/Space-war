package com.example.game.engine

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class AsteroidMesh(
    val vertices: List<Vector3>,
    val faces: List<Face3D>,
    val radius: Float
)

object AsteroidGenerator {
    fun create3DAsteroid(radius: Float, seed: Long): AsteroidMesh {
        val rand = Random(seed)
        val vertices = mutableListOf<Vector3>()
        
        // Sphere vertex distribution with random displacement for rugged rocky surface
        val phiSteps = 6
        val thetaSteps = 8

        for (i in 0..phiSteps) {
            val phi = Math.PI.toFloat() * i / phiSteps
            for (j in 0 until thetaSteps) {
                val theta = (2 * Math.PI.toFloat()) * j / thetaSteps
                
                val noise = 0.75f + rand.nextFloat() * 0.5f
                val r = radius * noise
                val x = r * kotlin.math.sin(phi) * kotlin.math.cos(theta)
                val y = r * kotlin.math.cos(phi)
                val z = r * kotlin.math.sin(phi) * kotlin.math.sin(theta)
                vertices.add(Vector3(x, y, z))
            }
        }

        val faces = mutableListOf<Face3D>()
        val baseGray = 100 + rand.nextInt(50)
        val rockColor = Color(baseGray, baseGray - 10, baseGray - 20)

        for (i in 0 until phiSteps) {
            for (j in 0 until thetaSteps) {
                val nextJ = (j + 1) % thetaSteps
                val idx1 = i * thetaSteps + j
                val idx2 = i * thetaSteps + nextJ
                val idx3 = (i + 1) * thetaSteps + j
                val idx4 = (i + 1) * thetaSteps + nextJ

                if (idx1 < vertices.size && idx2 < vertices.size && idx3 < vertices.size) {
                    val shade = 0.8f + (rand.nextFloat() * 0.4f - 0.2f)
                    val faceColor = Color(
                        (rockColor.red * shade).coerceIn(0f, 1f),
                        (rockColor.green * shade).coerceIn(0f, 1f),
                        (rockColor.blue * shade).coerceIn(0f, 1f)
                    )
                    faces.add(Face3D(idx1, idx2, idx3, faceColor))
                    if (idx4 < vertices.size) {
                        faces.add(Face3D(idx2, idx4, idx3, faceColor))
                    }
                }
            }
        }

        return AsteroidMesh(vertices, faces, radius)
    }
}
