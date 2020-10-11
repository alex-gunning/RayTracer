package org.raytracer

import com.aparapi.Kernel
import com.aparapi.Range

data class Drawable(val coordinates: Triangle, val colour: RGBColour)
data class RGBColour(val r: Int, val g: Int, val b: Int)

class Scene(
    val sceneObjects: List<Drawable> = listOf(),
) {
    fun calcPixelIntensities(
        cameraOrigin: Point,
        viewPlane: Array<Array<Vector>>
    ): List<List<Pair<Float, RGBColour>>> {
        val objectIntersections = sceneObjects.map { drawable ->
            getPixelIntensitiesForTriangle(cameraOrigin, viewPlane, drawable)
        }
        return calcViewWinners(objectIntersections)
    }

    fun getPixelIntensitiesForTriangle(
        cameraOrigin: Point,
        viewPlane: Array<Array<Vector>>,
        drawable: Drawable
    ): List<List<Pair<Float, RGBColour>>> {
        val intersectionsDelta = Scene.batchIntersectionPointDeltas(drawable.coordinates, cameraOrigin, viewPlane)
//            .mapIndexed { rowIdx, row -> row.mapIndexed { colIdx, item -> Pair(item, viewPlane[rowIdx][colIdx]) } }

//        val intersectionsDelta2 = viewPlane.map { row ->
//            row.map { Scene.intersectionPointDelta(drawable.coordinates, cameraOrigin, it) }.zip(row)
////            Scene.batchIntersectionPointDeltas(drawable.coordinates, cameraOrigin, row).zip(row)
//        }
        val intersections =
            Scene.batchIntersectionPoint(intersectionsDelta, viewPlane.map { it.toList() }, cameraOrigin)
//        val intersections = intersectionsDelta.mapIndexed { idx, row -> batchIntersectionPoint(row, viewPlane[idx].toList(), cameraOrigin)}
//        val intersections = intersectionsDelta.map { row ->
//            row.map { Scene.intersectionPoint(it.first, it.second, cameraOrigin) }
//        }
        val bounds = intersections.map { row ->
            row.map { Scene.isWithinBounds(drawable.coordinates, it) }.zip(row)
        }
        val pixelIntensities = bounds.map { row ->
            row.map { Scene.isWithinTriangle(drawable.coordinates, it.second) }
                .zip(row.map { it.second })
        }.map { row ->
            row.map {
                Pair(
                    if (it.first) 1 / cameraOrigin.distanceTo(it.second) else 0.0f,
                    drawable.colour
                )
            }
        }
        return pixelIntensities
    }

    // Go through each pixel and return the closest.
    private fun calcViewWinners(intersections: List<List<List<Pair<Float, RGBColour>>>>): List<List<Pair<Float, RGBColour>>> {
        val theList = List(intersections[0].size) { y ->
            List(intersections[0].size) { x ->
                intersections.fold(Pair(0.0f, RGBColour(0, 0, 0)))
                { max, element -> if (element[y][x].first > max.first) element[y][x] else max }
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

        fun batchIntersectionPoint(
            deltas: FloatArray,
            slope: List<List<Vector>>,
            origin: Vector
        ): List<List<Vector>> {
            val size = slope.size
//            val deltas = FloatArray(size * size) { i -> delta[i / size][i % size] }
            val (ox, oy, oz) = origin
            val slopeX = FloatArray(size * size) { i -> slope[i / size][i % size].x }
            val slopeY = FloatArray(size * size) { i -> slope[i / size][i % size].y }
            val slopeZ = FloatArray(size * size) { i -> slope[i / size][i % size].z }

            val resultX = FloatArray(size * size)
            val resultY = FloatArray(size * size)
            val resultZ = FloatArray(size * size)

            val kernel = object : Kernel() {
                override fun run() {
                    val IDx = getGlobalId(0)
                    val IDy = getGlobalId(1)
                    val idx = (IDx + IDy * getGlobalSize(0))
                    resultX[idx] = deltas[idx] * slopeX[idx] + ox
                    resultY[idx] = deltas[idx] * slopeY[idx] + oy
                    resultZ[idx] = deltas[idx] * slopeZ[idx] + oz
                }
            }
            kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU)

            kernel.execute(Range.create2D(size, size))
            kernel.dispose()
            return List(size) { y ->
                List(size) { x ->
                    Vector(
                        resultX[x + y * size],
                        resultY[x + y * size],
                        resultZ[x + y * size]
                    )
                }
            }
        }

        fun intersectionPointDelta(triangle: Triangle, origin: Vector, slope: Vector): Float {
            val triangleNormal = triangle.normal()
            return -((triangleNormal.dot(origin) + triangle.k()) / triangleNormal.dot(slope))
        }


        fun batchIntersectionPointDeltas(
            triangle: Triangle,
            origin: Vector,
            slope: Array<Array<Vector>>
        ): FloatArray {
            val triangleNormal = triangle.normal()
            val tx = triangleNormal.x
            val ty = triangleNormal.y
            val tz = triangleNormal.z
            val k = triangle.k()
            val numerator = triangleNormal.dot(origin) + k

            val result = FloatArray(slope.size * slope.size)
            val rowSize = slope.first().size
            val x = FloatArray(slope.size * slope.size) { i -> slope[i / rowSize][i % rowSize].x }
            val y = FloatArray(slope.size * slope.size) { i -> slope[i / rowSize][i % rowSize].y }
            val z = FloatArray(slope.size * slope.size) { i -> slope[i / rowSize][i % rowSize].z }

            val kernel = object : Kernel() {
                override fun run() {
                    val IDx = getGlobalId(0)
                    val IDy = getGlobalId(1)
                    val denominator = tx * x[(IDx + IDy * getGlobalSize(0))] +
                            ty * y[(IDx + IDy * getGlobalSize(0))] +
                            tz * z[(IDx + IDy * getGlobalSize(0))]
                    result[IDx+(IDy * rowSize)] = -(numerator / denominator)
                }
            }
            kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU)

            kernel.execute(Range.create2D(slope.size, slope.size))
            kernel.dispose()
            return result
//            val theList = List(size = rowSize) { y -> List(size = rowSize) { x -> result[x + (y * rowSize)] } }
//            return theList
        }

        // A weak sanity-check
        fun isWithinBounds(triangle: Triangle, point: Point): Boolean {
            val xBounds = Pair(
                triangle.p1.x.coerceAtMost(triangle.p2.x.coerceAtMost(triangle.p3.x)),
                triangle.p1.x.coerceAtLeast(triangle.p2.x.coerceAtLeast(triangle.p3.x))
            )
            val yBounds = Pair(
                triangle.p1.y.coerceAtMost(triangle.p2.y.coerceAtMost(triangle.p3.y)),
                triangle.p1.y.coerceAtLeast(triangle.p2.y.coerceAtLeast(triangle.p3.y))
            )
            val zBounds = Pair(
                triangle.p1.z.coerceAtMost(triangle.p2.z.coerceAtMost(triangle.p3.z)),
                triangle.p1.z.coerceAtLeast(triangle.p2.z.coerceAtLeast(triangle.p3.z))
            )
            val (x, y, z) = point
            return (
                    x >= xBounds.first && x <= xBounds.second &&
                            y >= yBounds.first && y <= yBounds.second &&
                            z >= zBounds.first && z <= zBounds.second
                    )
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


