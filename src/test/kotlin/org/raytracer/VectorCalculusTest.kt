package org.raytracer

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VectorCalculusTest {
    @Test
    @DisplayName("Point works with unary minus")
    fun pointUnaryMinus() {
        val p = Point(x = 1.0f, y = 2.0f, z = 3.0f)
        assert((-p).equals(Point(-1.0f, -2.0f, -3.0f)))
    }

    @Test
    @DisplayName("Point works with plus")
    fun pointPlus() {
        val p = Point(1.0f, 2.0f, 3.0f)
        assert((p + p).equals(Point(2.0f, 4.0f, 6.0f)))
    }

    @Test
    @DisplayName("Point works with minus")
    fun pointMinus() {
        val a = Point(1.0f, 2.0f, 3.0f)
        val b = Point(2.0f, 1.0f, 3.0f)
        assert((a - b).equals(Point(-1.0f, 1.0f, 0.0f)))
    }

    @Test
    @DisplayName("Can find the distance between two points")
    fun pointDistance() {
        val a = Point(2.0f, 2.0f, 3.0f)
        val b = Point(2.0f, 0.0f, 3.0f)
        assertEquals(a.distanceTo(b), 2.0f, 0.0f)
    }

    @Test
    @DisplayName("Vector Dot product")
    fun vectorDot() {
        val a = Vector(1.0f, 2.0f, 3.0f)
        val b = Vector(2.0f, 1.0f, 3.0f)
        assertEquals(a.dot(b), (13).toDouble(), 0.0f)
    }

    @Test
    @DisplayName("Vector Cross product")
    fun vectorCross() {
        val a = Vector(2.0f, 1.0f, -1.0f)
        val b = Vector(-3.0f, 4.0f, 1.0f)
        assertEquals(a.cross(b), Vector(5.0f, 1.0f, 11.0f))
    }

    @Test
    @DisplayName("Vector length")
    fun vectorLength() {
        val a = Vector(0.0f, 3.0f, -4.0f)
        assertEquals(a.length(), 5.0f, 0.0)
    }

    @Test
    @DisplayName("Triangle normal")
    fun triangleNormal() {
        val a = Triangle(
            p1 = Point(-1.0f,0.0f,2.0f),
            p2 = Point(0.0f, -1.0f, 3.0f),
            p3 = Point(1.0f, 0.0f, 2.0f))
        assertEquals(a.normal(), Vector(0.0f, -2.0f, -2.0f))
    }

    @Test
    @DisplayName("Triangle plane constant 'k'")
    fun trianglePlaneConstant() {
        val a = Triangle(
            p1 = Point(-1.0f,0.0f,2.0f),
            p2 = Point(0.0f, -1.0f, 3.0f),
            p3 = Point(1.0f, 0.0f, 2.0f))
        assertEquals(a.k(), 4.0f, 0.0f)
    }
}