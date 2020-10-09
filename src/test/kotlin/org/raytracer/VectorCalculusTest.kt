package org.raytracer

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VectorCalculusTest {
    @Test
    @DisplayName("Point works with unary minus")
    fun pointUnaryMinus() {
        val p = Point(x = 1.0, y = 2.0, z = 3.0)
        assert((-p).equals(Point(-1.0, -2.0, -3.0)))
    }

    @Test
    @DisplayName("Point works with plus")
    fun pointPlus() {
        val p = Point(1.0, 2.0, 3.0)
        assert((p + p).equals(Point(2.0, 4.0, 6.0)))
    }

    @Test
    @DisplayName("Point works with minus")
    fun pointMinus() {
        val a = Point(1.0, 2.0, 3.0)
        val b = Point(2.0, 1.0, 3.0)
        assert((a - b).equals(Point(-1.0, 1.0, 0.0)))
    }

    @Test
    @DisplayName("Can find the distance between two points")
    fun pointDistance() {
        val a = Point(2.0, 2.0, 3.0)
        val b = Point(2.0, 0.0, 3.0)
        assertEquals(a.distanceTo(b), 2.0, 0.0)
    }

    @Test
    @DisplayName("Vector Dot product")
    fun vectorDot() {
        val a = Vector(1.0, 2.0, 3.0)
        val b = Vector(2.0, 1.0, 3.0)
        assertEquals(a.dot(b), (13).toDouble(), 0.0)
    }

    @Test
    @DisplayName("Vector Cross product")
    fun vectorCross() {
        val a = Vector(2.0, 1.0, -1.0)
        val b = Vector(-3.0, 4.0, 1.0)
        assertEquals(a.cross(b), Vector(5.0, 1.0, 11.0))
    }

    @Test
    @DisplayName("Vector length")
    fun vectorLength() {
        val a = Vector(0.0, 3.0, -4.0)
        assertEquals(a.length(), 5.0, 0.0)
    }

    @Test
    @DisplayName("Triangle normal")
    fun triangleNormal() {
        val a = Triangle(
            p1 = Point(-1.0,0.0,2.0),
            p2 = Point(0.0, -1.0, 3.0),
            p3 = Point(1.0, 0.0, 2.0))
        assertEquals(a.normal(), Vector(0.0, -2.0, -2.0))
    }

    @Test
    @DisplayName("Triangle plane constant 'k'")
    fun trianglePlaneConstant() {
        val a = Triangle(
            p1 = Point(-1.0,0.0,2.0),
            p2 = Point(0.0, -1.0, 3.0),
            p3 = Point(1.0, 0.0, 2.0))
        assertEquals(a.k(), 4.0, 0.0)
    }
}