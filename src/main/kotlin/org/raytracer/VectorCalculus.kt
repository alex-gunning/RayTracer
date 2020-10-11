package org.raytracer

import com.aparapi.Kernel
import com.aparapi.Range
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

data class Point(val x: Float, val y: Float, val z: Float) {
    operator fun unaryMinus() = Point(-x, -y, -z)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y, z - other.z)
    fun distanceTo(other: Point): Float = kotlin.math.sqrt(
        (this.x - other.x).pow(2) +
                (this.y - other.y).pow(2) +
                (this.z - other.z).pow(2)
    )
}

typealias Vector = Point // A vertex with starting point at the origin.

fun Vector.dot(other: Vector): Float =
    x * other.x + y * other.y + z * other.z

fun Vector.cross(other: Vector): Vector =
    Vector(
        x = (y * other.z) - (z * other.y),
        y = -((x * other.z) - (z * other.x)),
        z = (x * other.y) - (y * other.x)
    )

fun Vector.length(): Float = kotlin.math.sqrt(this.dot(this))

fun degreesToRadians(degrees: Int): Float = (Math.PI / 180 * degrees).toFloat()

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
    fun length(): Float {
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
    fun k(): Float = -(this.normal().dot(p1))
}


fun speedAddition() {
    val size = 251001
    val a = FloatArray(size) { (Math.random() * 100).toFloat() }
    val b = FloatArray(size) { (Math.random() * 100).toFloat() }
    val sum = FloatArray(size)

    val kernel = object: Kernel() {
        override fun run() {
            val gid = globalId
            sum[gid] = a[gid] * b[gid]
        }
    }

    kernel.execute(Range.create(size))
    kernel.dispose()
    val i = 2
}