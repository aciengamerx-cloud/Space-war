package com.example.game.engine

import androidx.compose.ui.graphics.Color

data class Face3D(
    val v1: Int,
    val v2: Int,
    val v3: Int,
    val color: Color,
    val normal: Vector3 = Vector3(0f, 1f, 0f)
)

data class ShipMesh(
    val vertices: List<Vector3>,
    val faces: List<Face3D>,
    val thrusterOffset: Vector3,
    val wingCannonOffsets: List<Vector3>
)

object StarshipModels {
    fun createPlayerShip(shipId: String, primaryColor: Color, accentColor: Color): ShipMesh {
        return when (shipId) {
            "vanguard_phoenix" -> createHeavyCruiserMesh(primaryColor, accentColor)
            "aether_wing" -> createScoutMesh(primaryColor, accentColor)
            "nemesis_dread" -> createDreadnoughtFighterMesh(primaryColor, accentColor)
            else -> createInterceptorMesh(primaryColor, accentColor) // starstreak_mk1
        }
    }

    // Interceptor (Balanced, sleek fighter)
    private fun createInterceptorMesh(baseColor: Color, accentColor: Color): ShipMesh {
        val verts = listOf(
            Vector3(0f, 2f, 15f),      // 0: Nose tip
            Vector3(0f, 4f, -10f),     // 1: Top spine
            Vector3(-4f, 1f, -12f),    // 2: Left engine
            Vector3(4f, 1f, -12f),     // 3: Right engine
            Vector3(-18f, 0f, -8f),    // 4: Left wing tip
            Vector3(18f, 0f, -8f),     // 5: Right wing tip
            Vector3(0f, -3f, -5f),     // 6: Belly
            Vector3(0f, 5f, 2f),       // 7: Cockpit top
            Vector3(-12f, 2f, -6f),    // 8: Left wing flap
            Vector3(12f, 2f, -6f)      // 9: Right wing flap
        )

        val faces = listOf(
            // Nose to Cockpit & Wings
            Face3D(0, 7, 4, baseColor),
            Face3D(0, 5, 7, baseColor),
            Face3D(0, 4, 6, baseColor.copy(alpha = 0.8f)),
            Face3D(0, 6, 5, baseColor.copy(alpha = 0.8f)),
            // Cockpit Canopy Glass
            Face3D(0, 1, 7, Color(0xFF00E5FF)),
            // Wings
            Face3D(7, 1, 4, baseColor),
            Face3D(7, 5, 1, baseColor),
            Face3D(4, 2, 6, accentColor),
            Face3D(5, 6, 3, accentColor),
            // Engine Back
            Face3D(2, 1, 3, Color.DarkGray),
            Face3D(2, 3, 6, Color.Black)
        )

        return ShipMesh(
            vertices = verts,
            faces = faces,
            thrusterOffset = Vector3(0f, 1f, -12f),
            wingCannonOffsets = listOf(Vector3(-16f, 0f, -2f), Vector3(16f, 0f, -2f))
        )
    }

    private fun createHeavyCruiserMesh(baseColor: Color, accentColor: Color): ShipMesh {
        val verts = listOf(
            Vector3(0f, 3f, 20f),     // 0 Nose
            Vector3(-8f, 2f, 10f),    // 1 Left front
            Vector3(8f, 2f, 10f),     // 2 Right front
            Vector3(-22f, 0f, -12f),  // 3 Left wing tip
            Vector3(22f, 0f, -12f),   // 4 Right wing tip
            Vector3(-10f, 4f, -16f),  // 5 Left engine
            Vector3(10f, 4f, -16f),   // 6 Right engine
            Vector3(0f, -5f, -8f),    // 7 Heavy hull bottom
            Vector3(0f, 7f, -2f)      // 8 Shield tower
        )
        val faces = listOf(
            Face3D(0, 8, 1, baseColor),
            Face3D(0, 2, 8, baseColor),
            Face3D(1, 8, 3, accentColor),
            Face3D(2, 4, 8, accentColor),
            Face3D(3, 5, 7, baseColor),
            Face3D(4, 7, 6, baseColor),
            Face3D(0, 1, 7, baseColor),
            Face3D(0, 7, 2, baseColor)
        )
        return ShipMesh(
            vertices = verts,
            faces = faces,
            thrusterOffset = Vector3(0f, 2f, -16f),
            wingCannonOffsets = listOf(Vector3(-20f, 0f, -5f), Vector3(20f, 0f, -5f))
        )
    }

    private fun createScoutMesh(baseColor: Color, accentColor: Color): ShipMesh {
        val verts = listOf(
            Vector3(0f, 1f, 18f),
            Vector3(-12f, 0f, -6f),
            Vector3(12f, 0f, -6f),
            Vector3(0f, 4f, -10f),
            Vector3(0f, -2f, -4f)
        )
        val faces = listOf(
            Face3D(0, 3, 1, baseColor),
            Face3D(0, 2, 3, baseColor),
            Face3D(0, 1, 4, accentColor),
            Face3D(0, 4, 2, accentColor),
            Face3D(1, 3, 2, Color.DarkGray)
        )
        return ShipMesh(
            vertices = verts,
            faces = faces,
            thrusterOffset = Vector3(0f, 1f, -10f),
            wingCannonOffsets = listOf(Vector3(-10f, 0f, -2f), Vector3(10f, 0f, -2f))
        )
    }

    private fun createDreadnoughtFighterMesh(baseColor: Color, accentColor: Color): ShipMesh {
        val verts = listOf(
            Vector3(0f, 4f, 22f),
            Vector3(-15f, 2f, 5f),
            Vector3(15f, 2f, 5f),
            Vector3(-25f, -2f, -14f),
            Vector3(25f, -2f, -14f),
            Vector3(0f, 8f, -8f),
            Vector3(0f, -6f, -10f)
        )
        val faces = listOf(
            Face3D(0, 5, 1, baseColor),
            Face3D(0, 2, 5, baseColor),
            Face3D(1, 5, 3, accentColor),
            Face3D(2, 4, 5, accentColor),
            Face3D(0, 1, 6, baseColor),
            Face3D(0, 6, 2, baseColor)
        )
        return ShipMesh(
            vertices = verts,
            faces = faces,
            thrusterOffset = Vector3(0f, 2f, -14f),
            wingCannonOffsets = listOf(Vector3(-22f, -1f, -8f), Vector3(22f, -1f, -8f))
        )
    }

    fun createEnemyDrone(): ShipMesh {
        val verts = listOf(
            Vector3(0f, 0f, 8f),
            Vector3(-8f, 4f, -4f),
            Vector3(8f, 4f, -4f),
            Vector3(0f, -6f, -4f),
            Vector3(0f, 0f, -8f)
        )
        val faces = listOf(
            Face3D(0, 1, 2, Color(0xFFFF3D00)),
            Face3D(0, 2, 3, Color(0xFFDD2C00)),
            Face3D(0, 3, 1, Color(0xFFFF5252)),
            Face3D(4, 2, 1, Color.DarkGray)
        )
        return ShipMesh(verts, faces, Vector3(0f, 0f, -8f), listOf(Vector3(0f, 0f, 8f)))
    }

    fun createBossDreadnought(): ShipMesh {
        val verts = listOf(
            Vector3(0f, 15f, 60f),       // Nose
            Vector3(-40f, 10f, 20f),     // Left bow
            Vector3(40f, 10f, 20f),      // Right bow
            Vector3(-60f, 0f, -40f),     // Left wing extension
            Vector3(60f, 0f, -40f),      // Right wing extension
            Vector3(-25f, 25f, -50f),    // Left engine deck
            Vector3(25f, 25f, -50f),     // Right engine deck
            Vector3(0f, -20f, -30f),     // Citadel bottom
            Vector3(0f, 35f, 0f)         // Command bridge tower
        )
        val faces = listOf(
            Face3D(0, 8, 1, Color(0xFF37474F)),
            Face3D(0, 2, 8, Color(0xFF37474F)),
            Face3D(1, 8, 3, Color(0xFF263238)),
            Face3D(2, 4, 8, Color(0xFF263238)),
            Face3D(8, 5, 3, Color(0xFFD50000)), // Glowing red command stripe
            Face3D(8, 4, 6, Color(0xFFD50000)),
            Face3D(0, 1, 7, Color(0xFF212121)),
            Face3D(0, 7, 2, Color(0xFF212121))
        )
        return ShipMesh(
            vertices = verts,
            faces = faces,
            thrusterOffset = Vector3(0f, 10f, -50f),
            wingCannonOffsets = listOf(Vector3(-50f, 0f, -20f), Vector3(50f, 0f, -20f), Vector3(0f, 30f, 5f))
        )
    }
}
