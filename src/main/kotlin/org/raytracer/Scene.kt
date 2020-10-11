package org.raytracer

data class Drawable(val coordinates: Triangle, val colour: RGBColour)
data class RGBColour(val r: Int, val g: Int, val b: Int)

class Scene(
    val sceneObjects: List<Drawable> = listOf(),
) {
    fun calcPixelIntensities(
        cameraOrigin: Point,
        viewPlane: Array<Array<Vector>>
    ): Array<Array<Pair<Float, RGBColour>>> {
        val objectIntersections = sceneObjects.map { drawable ->
            getPixelIntensitiesForTriangle(cameraOrigin, viewPlane, drawable)
        }
        return calcViewWinners(objectIntersections)
    }

    fun getPixelIntensitiesForTriangle(
        cameraOrigin: Point,
        viewPlane: Array<Array<Vector>>,
        drawable: Drawable
    ): Pair<Array<Array<Float>>, RGBColour> {
        val intersections =
            BatchScene.calcIntersectionPoints(drawable.coordinates, viewPlane, cameraOrigin)
        val pixelIntensities = BatchScene.framebufferForTriangle(drawable.coordinates, intersections, cameraOrigin)
//        val intersection = viewPlane.map { row ->
//            row.map { Scene.intersectionPointDelta(drawable.coordinates, cameraOrigin, it) }.zip(row)
//        }.map { row ->
//            row.map { Scene.intersectionPoint(it.first, it.second, cameraOrigin) }
//        }
//        val bounds = intersection.map { row ->
//            row.map { /*Scene.isWithinBounds(drawable.coordinates, it)*/true }.zip(row)
//        }
//        val pixelIntensitie = bounds.map { row ->
//            row.map { if (it.first) Scene.isWithinTriangle(drawable.coordinates, it.second) else false }
//                .zip(row.map { it.second })
//        }.map { row ->
//            row.map {
//                    if (it.first) 1f / cameraOrigin.distanceTo(it.second) else 0.0f
//            }.toTypedArray()
//        }.toTypedArray()

        return Pair(pixelIntensities, drawable.colour)
    }

    // Go through each pixel and return the closest.
    private fun calcViewWinners(intersections: List<Pair<Array<Array<Float>>, RGBColour>>): Array<Array<Pair<Float, RGBColour>>> {
        val theList = Array(intersections[0].first.size) { y ->
            Array(intersections[0].first.size) { x ->
                intersections.fold(Pair(0.0f, RGBColour(0, 0, 0)))
                { max, element -> if (element.first[y][x] > max.first) Pair(element.first[y][x], element.second) else max }
            }
        }
        return theList
    }

    companion object {
        fun buildViewPlaneAngles(
            pixDimension: Int,
            pixelSize: Float = 0.5f,
            pitchDegrees: Int = 0,
            yawDegrees: Int = 0
        ): Array<Array<Vector>> {
            return Array(pixDimension) { y ->
                Array(pixDimension) { x ->
                    angle(
                        x = x,
                        y = y,
                        pixDimension = pixDimension,
                        pixelSize = pixelSize,
                        pitchDegrees = pitchDegrees,
                        yawDegrees = yawDegrees
                    )
                }
            }.reversedArray() // Reversed to match the standard y-cartesian coordinate system.
        }

        private fun angle(
            x: Int,
            y: Int,
            pixelSize: Float,
            pixDimension: Int,
            pitchDegrees: Int,
            yawDegrees: Int
        ): Vector {
            val focus = Vector(
                x = pixelSize * (x - pixDimension / 2),
                y = pixelSize * (y - pixDimension / 2),
                z = 1.0f
            )
            return focus.rotY(yawDegrees).rotX(pitchDegrees)
        }

        fun intersectionPoint(delta: Float, slope: Vector, origin: Vector) =
            Vector(
                x = delta * slope.x + origin.x,
                y = delta * slope.y + origin.y,
                z = delta * slope.z + origin.z
            )



        fun intersectionPointDelta(triangle: Triangle, origin: Vector, slope: Vector): Float {
            val triangleNormal = triangle.normal()
            return -((triangleNormal.dot(origin) + triangle.k()) / triangleNormal.dot(slope))
        }


        // A weak sanity-check
        fun isWithinBounds(triangle: Triangle, point: Point): Boolean {
            val (x, y, z) = point
            val xBounds = Pair(
                triangle.p1.x.coerceAtMost(triangle.p2.x.coerceAtMost(triangle.p3.x)),
                triangle.p1.x.coerceAtLeast(triangle.p2.x.coerceAtLeast(triangle.p3.x))
            )
            if (x < xBounds.first || x > xBounds.second) {
                return false
            }
            val yBounds = Pair(
                triangle.p1.y.coerceAtMost(triangle.p2.y.coerceAtMost(triangle.p3.y)),
                triangle.p1.y.coerceAtLeast(triangle.p2.y.coerceAtLeast(triangle.p3.y))
            )
            if (y < yBounds.first || y > yBounds.second) {
                return false
            }
            val zBounds = Pair(
                triangle.p1.z.coerceAtMost(triangle.p2.z.coerceAtMost(triangle.p3.z)),
                triangle.p1.z.coerceAtLeast(triangle.p2.z.coerceAtLeast(triangle.p3.z))
            )
            if (z < zBounds.first || z > zBounds.second) {
                return false
            }
            return true
        }

        // A full assertive check
        fun isWithinTriangle(triangle: Triangle, intersection: Vector): Boolean {
            val v1: Vector = triangle.p3 - triangle.p2
            val v2: Vector = triangle.p3 - triangle.p1
            val v3: Vector = triangle.p2 - triangle.p1

            val a1 = v1.cross(intersection - triangle.p3)
            val a2 = v2.cross(intersection - triangle.p3)
            val a3 = v3.cross(intersection - triangle.p2)

            val b1 = v1.cross(triangle.p1 - triangle.p3)
            val b2 = v2.cross(triangle.p2 - triangle.p3)
            val b3 = v3.cross(triangle.p3 - triangle.p2)

            val c1 = a1.dot(b1)
            val c2 = a2.dot(b2)
            val c3 = a3.dot(b3)

            return c1 >= 0 && c2 >= 0 && c3 >= 0
        }

    }
}


