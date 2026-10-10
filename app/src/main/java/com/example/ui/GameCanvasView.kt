package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.game.engine.GameEngine
import com.example.game.engine.Projection3D
import com.example.game.engine.StarshipModels
import com.example.game.engine.Vector3

@Composable
fun GameCanvasView(
    gameEngine: GameEngine,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_canvas_3d")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        gameEngine.onDragSteer(0f, 0f)
                    },
                    onDragCancel = {
                        gameEngine.onDragSteer(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        gameEngine.onDragSteer(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        val screenWidth = size.width
        val screenHeight = size.height
        val focalLength = 450f

        val playerState = gameEngine.playerState.value
        val forwardVec = playerState.getForwardVector()
        val upVec = playerState.getUpVector()

        // 3D Unreal Camera tracking trailing behind Player Ship
        val cameraPos = playerState.pos - forwardVec * 80f + upVec * 22f
        val camRotX = playerState.pitch
        val camRotY = playerState.yaw
        val camRotZ = playerState.roll

        // 1. Draw Space Background
        drawRect(color = Color(0xFF030712))

        // 2. Draw 3D Starfield
        for (star in gameEngine.starfield) {
            val sp = Projection3D.project(star, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
            if (sp.isVisible && sp.zDepth > 5f) {
                val alpha = (1f - (sp.zDepth / 1600f)).coerceIn(0.2f, 1f)
                val starSize = (sp.scale * 1.6f).coerceIn(1f, 4f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = starSize,
                    center = Offset(sp.x, sp.y)
                )
            }
        }

        // 3. Draw 3D Planet Backdrop
        val planet = gameEngine.currentPlanet
        val planetSP = Projection3D.project(planet.pos, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
        if (planetSP.isVisible && planetSP.zDepth > 5f) {
            val pRadius = planet.radius * planetSP.scale
            val pCenter = Offset(planetSP.x, planetSP.y)

            // Planet Atmosphere Glow
            drawCircle(
                color = planet.atmosphereColor.copy(alpha = 0.35f),
                radius = pRadius * 1.12f,
                center = pCenter
            )
            // Planet Body
            drawCircle(
                color = planet.primaryColor,
                radius = pRadius,
                center = pCenter
            )
            // Craters & Shading
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = pRadius * 0.25f,
                center = Offset(pCenter.x - pRadius * 0.3f, pCenter.y - pRadius * 0.1f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = pRadius * 0.18f,
                center = Offset(pCenter.x + pRadius * 0.4f, pCenter.y + pRadius * 0.3f)
            )
        }

        // Collect and Depth-Sort 3D World Entities
        data class RenderableItem(val zDepth: Float, val drawCall: DrawScope.() -> Unit)
        val renderList = mutableListOf<RenderableItem>()

        // 4. Render 3D Asteroids
        for (ast in gameEngine.asteroids) {
            val astSP = Projection3D.project(ast.pos, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
            if (astSP.isVisible && astSP.zDepth > 5f) {
                renderList.add(RenderableItem(astSP.zDepth) {
                    val scale = astSP.scale
                    val center = Vector3(astSP.x, astSP.y, astSP.zDepth)

                    for (face in ast.mesh.faces) {
                        val v1 = ast.mesh.vertices.getOrNull(face.v1)?.rotateX(ast.rotX)?.rotateY(ast.rotY) ?: continue
                        val v2 = ast.mesh.vertices.getOrNull(face.v2)?.rotateX(ast.rotX)?.rotateY(ast.rotY) ?: continue
                        val v3 = ast.mesh.vertices.getOrNull(face.v3)?.rotateX(ast.rotX)?.rotateY(ast.rotY) ?: continue

                        val p1x = center.x + v1.x * scale
                        val p1y = center.y - v1.y * scale
                        val p2x = center.x + v2.x * scale
                        val p2y = center.y - v2.y * scale
                        val p3x = center.x + v3.x * scale
                        val p3y = center.y - v3.y * scale

                        val path = Path().apply {
                            moveTo(p1x, p1y)
                            lineTo(p2x, p2y)
                            lineTo(p3x, p3y)
                            close()
                        }
                        drawPath(path = path, color = face.color)
                        drawPath(path = path, color = Color.Black.copy(alpha = 0.4f), style = Stroke(width = 1f))
                    }
                })
            }
        }

        // 5. Render 3D Enemies
        for (enemy in gameEngine.enemies) {
            val enemySP = Projection3D.project(enemy.pos, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
            if (enemySP.isVisible && enemySP.zDepth > 5f) {
                renderList.add(RenderableItem(enemySP.zDepth) {
                    val scale = enemySP.scale
                    val center = Vector3(enemySP.x, enemySP.y, enemySP.zDepth)

                    for (face in enemy.mesh.faces) {
                        val v1 = enemy.mesh.vertices.getOrNull(face.v1) ?: continue
                        val v2 = enemy.mesh.vertices.getOrNull(face.v2) ?: continue
                        val v3 = enemy.mesh.vertices.getOrNull(face.v3) ?: continue

                        val p1x = center.x + v1.x * scale
                        val p1y = center.y - v1.y * scale
                        val p2x = center.x + v2.x * scale
                        val p2y = center.y - v2.y * scale
                        val p3x = center.x + v3.x * scale
                        val p3y = center.y - v3.y * scale

                        val path = Path().apply {
                            moveTo(p1x, p1y)
                            lineTo(p2x, p2y)
                            lineTo(p3x, p3y)
                            close()
                        }
                        drawPath(path = path, color = face.color)
                        drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.2f))
                    }

                    if (enemy.shield > 0f) {
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                            radius = (if (enemy.isBoss) 60f else 25f) * scale,
                            center = Offset(center.x, center.y),
                            style = Stroke(width = 2f)
                        )
                    }

                    drawCircle(
                        color = Color.Red.copy(alpha = 0.6f),
                        radius = (if (enemy.isBoss) 55f else 22f) * scale,
                        center = Offset(center.x, center.y),
                        style = Stroke(width = 1.5f)
                    )
                })
            }
        }

        // 6. Render Projectiles (Lasers & Missiles)
        for (proj in gameEngine.projectiles) {
            val projSP = Projection3D.project(proj.pos, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
            if (projSP.isVisible && projSP.zDepth > 5f) {
                renderList.add(RenderableItem(projSP.zDepth) {
                    val laserSize = (projSP.scale * if (proj.isMissile) 12f else 25f).coerceIn(4f, 40f)
                    val pCenter = Offset(projSP.x, projSP.y)

                    if (proj.isMissile) {
                        drawCircle(color = proj.color, radius = laserSize * 0.4f, center = pCenter)
                        drawCircle(color = Color.Yellow, radius = laserSize * 0.2f, center = pCenter)
                    } else {
                        drawLine(
                            color = proj.color,
                            start = pCenter,
                            end = Offset(pCenter.x, pCenter.y + laserSize),
                            strokeWidth = 3.5f * projSP.scale
                        )
                    }
                })
            }
        }

        // Sort back-to-front
        renderList.sortByDescending { it.zDepth }
        for (item in renderList) {
            item.drawCall(this)
        }

        // 7. Render Particles
        for (part in gameEngine.particles) {
            val partSP = Projection3D.project(part.pos, cameraPos, camRotX, camRotY, camRotZ, focalLength, screenWidth, screenHeight)
            if (partSP.isVisible && partSP.zDepth > 5f) {
                val alpha = (part.lifeSpan / part.maxLifeSpan).coerceIn(0f, 1f)
                drawCircle(
                    color = part.color.copy(alpha = alpha),
                    radius = (part.size * partSP.scale).coerceIn(1f, 18f),
                    center = Offset(partSP.x, partSP.y)
                )
            }
        }

        // 8. Render Player's Starship in Center Third-Person View
        val playerMesh = StarshipModels.createPlayerShip(
            shipId = playerState.shipId,
            primaryColor = playerState.primaryColor,
            accentColor = playerState.accentColor
        )

        val playerCenter = Offset(screenWidth / 2f, screenHeight * 0.72f)
        val shipScale = 2.4f

        // Draw Player Starship Mesh with local Roll and Pitch response
        for (face in playerMesh.faces) {
            val v1 = playerMesh.vertices.getOrNull(face.v1)?.rotateZ(playerState.rollVel * 0.5f)?.rotateX(playerState.pitchVel * 0.3f) ?: continue
            val v2 = playerMesh.vertices.getOrNull(face.v2)?.rotateZ(playerState.rollVel * 0.5f)?.rotateX(playerState.pitchVel * 0.3f) ?: continue
            val v3 = playerMesh.vertices.getOrNull(face.v3)?.rotateZ(playerState.rollVel * 0.5f)?.rotateX(playerState.pitchVel * 0.3f) ?: continue

            val p1x = playerCenter.x + v1.x * shipScale
            val p1y = playerCenter.y - v1.y * shipScale
            val p2x = playerCenter.x + v2.x * shipScale
            val p2y = playerCenter.y - v2.y * shipScale
            val p3x = playerCenter.x + v3.x * shipScale
            val p3y = playerCenter.y - v3.y * shipScale

            val path = Path().apply {
                moveTo(p1x, p1y)
                lineTo(p2x, p2y)
                lineTo(p3x, p3y)
                close()
            }
            drawPath(path = path, color = face.color)
            drawPath(path = path, color = Color.White.copy(alpha = 0.25f), style = Stroke(width = 1f))
        }

        // Engine Thruster Flame Animation
        val flameSize = 12f + (playerState.velocity / 50f) * 16f
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = flameSize,
            center = Offset(playerCenter.x, playerCenter.y + 22f)
        )
        drawCircle(
            color = Color.White,
            radius = flameSize * 0.5f,
            center = Offset(playerCenter.x, playerCenter.y + 22f)
        )

        if (playerState.shield > 0f) {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                radius = 65f * shipScale,
                center = playerCenter
            )
        }
    }
}
