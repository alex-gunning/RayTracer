package org.raytracer

import kotlin.math.cos
import kotlin.math.sin

data class Point(val x: Double, val y: Double, val z: Double) {
    operator fun unaryMinus() = Point(-x, -y, -z)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y, z - other.z)
}

typealias Vector = Point // A vertex with starting point at the origin.

fun Vector.dot(other: Vector): Double =
    x * other.x + y * other.y + z * other.z

fun Vector.cross(other: Vector): Vector =
    Vector(
        x = (y * other.z) - (z * other.y),
        y = -((x * other.z) - (z * other.x)),
        z = (x * other.y) - (y * other.x)
    )

fun Vector.length(): Double = kotlin.math.sqrt(this.dot(this))

fun degreesToRadians(degrees: Int): Double = Math.PI / 180 * degrees

fun Vector.rotY(thetaDegrees: Int): Vector {
    val thetaRadians = degreesToRadians(thetaDegrees)
    return Vector(
        x = this.x * cos(thetaRadians) + this.z * sin(thetaRadians),
        y = this.y,
        z = -(this.x * sin(thetaRadians)) + this.z * cos(thetaRadians)
    )
}

fun Vector.rotX(thetaDegrees: Int): Vector {
    val thetaRadians = degreesToRadians(thetaDegrees)
    return Vector(
        x = this.x,
        y = this.y * cos(thetaRadians) - this.z * sin(thetaRadians),
        z = this.y * sin(thetaRadians) + this.z * cos(thetaRadians)
    )
}

data class Line(val p1: Point, val p2: Point) {
    fun length(): Double {
        val v1: Vector = p2 - p1
        return v1.length()
    }
}

data class Plane(val point: Point, val normal: Vector)
data class Triangle(val p1: Point, val p2: Point, val p3: Point) {
    fun normal(): Vector {
        val v1: Vector = p3 - p2
        val v2: Vector = p2 - p1
        return v1.cross(v2)
    }
    // Plane constant "k"
    fun k(): Double = -(this.normal().dot(p1))
}