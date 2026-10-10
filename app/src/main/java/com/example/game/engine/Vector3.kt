package com.example.game.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0001f) this / len else Vector3()
    }

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun rotateX(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x * c - y * s, x * s + y * c, z)
    }

    fun distanceTo(other: Vector3): Float = (this - other).length()
}
