package org.raytracer

import com.aparapi.Kernel
import com.aparapi.Range

class BatchScene {
    companion object {
        fun calcIntersectionPoints(
            triangle: Triangle,
            viewPlane: Array<Array<Vector>>,
            origin: Vector
        ): Array<Array<Vector>> {
            val triangleNormal = triangle.normal()
            val tx = triangleNormal.x
            val ty = triangleNormal.y
            val tz = triangleNormal.z
            val k = triangle.k()
            val numerator = triangleNormal.dot(origin) + k
            val size = viewPlane.size

            val (ox, oy, oz) = origin
            val slopeX = FloatArray(size * size) { i -> viewPlane[i / size][i % size].x }
            val slopeY = FloatArray(size * size) { i -> viewPlane[i / size][i % size].y }
            val slopeZ = FloatArray(size * size) { i -> viewPlane[i / size][i % size].z }

            val resultX = FloatArray(size * size)
            val resultY = FloatArray(size * size)
            val resultZ = FloatArray(size * size)

            val kernel = object : Kernel() {
                override fun run() {
                    val IDx = getGlobalId(0)
                    val IDy = getGlobalId(1)
                    val idx = (IDx + IDy * getGlobalSize(0))
                    // Calc delta
                    val denominator = tx * slopeX[idx] +
                            ty * slopeY[idx] +
                            tz * slopeZ[idx]
                    val delta = -(numerator / denominator)

                    // Calc intersection point using delta
                    resultX[idx] = delta * slopeX[idx] + ox
                    resultY[idx] = delta * slopeY[idx] + oy
                    resultZ[idx] = delta * slopeZ[idx] + oz
                }
            }
            kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU)
            kernel.execute(Range.create2D(size, size))
            kernel.dispose()

            return Array(size) { y ->
                Array(size) { x ->
                    Vector(
                        resultX[x + y * size],
                        resultY[x + y * size],
                        resultZ[x + y * size]
                    )
                }
            }
        }

        fun framebufferForTriangle(
            triangle: Triangle,
            intersections: Array<Array<Vector>>,
            origin: Point
        ): Array<Array<Float>> {
            val cameraOriginX = origin.x
            val cameraOriginY = origin.y
            val cameraOriginZ = origin.z

            val triangleP3X = triangle.p3.x
            val triangleP3Y = triangle.p3.y
            val triangleP3Z = triangle.p3.z

            val triangleP2X = triangle.p2.x
            val triangleP2Y = triangle.p2.y
            val triangleP2Z = triangle.p2.z

            val v1: Vector = triangle.p3 - triangle.p2
            val v2: Vector = triangle.p3 - triangle.p1
            val v3: Vector = triangle.p2 - triangle.p1

            val v1x = v1.x
            val v1y = v1.y
            val v1z = v1.z

            val v2x = v2.x
            val v2y = v2.y
            val v2z = v2.z

            val v3x = v3.x
            val v3y = v3.y
            val v3z = v3.z

            val b1 = v1.cross(triangle.p1 - triangle.p3)
            val b2 = v2.cross(triangle.p2 - triangle.p3)
            val b3 = v3.cross(triangle.p3 - triangle.p2)

            val b1x = b1.x
            val b1y = b1.y
            val b1z = b1.z

            val b2x = b2.x
            val b2y = b2.y
            val b2z = b2.z

            val b3x = b3.x
            val b3y = b3.y
            val b3z = b3.z

            val size = intersections.size
            val intersectionsX = FloatArray(size * size) { i -> intersections[i / size][i % size].x }
            val intersectionsY = FloatArray(size * size) { i -> intersections[i / size][i % size].y }
            val intersectionsZ = FloatArray(size * size) { i -> intersections[i / size][i % size].z }

            val result = FloatArray(size * size)

            val kernel = object : Kernel() {
                override fun run() {
                    val IDx = getGlobalId(0)
                    val IDy = getGlobalId(1)
                    val idx = (IDx + IDy * getGlobalSize(0))

                    // Calc params for a{1,2,3} calculations

                    // Params
                    val a1x = intersectionsX[idx] - triangleP3X
                    val a1y = intersectionsY[idx] - triangleP3Y
                    val a1z = intersectionsZ[idx] - triangleP3Z

                    val a2x = intersectionsX[idx] - triangleP3X
                    val a2y = intersectionsY[idx] - triangleP3Y
                    val a2z = intersectionsZ[idx] - triangleP3Z

                    val a3x = intersectionsX[idx] - triangleP2X
                    val a3y = intersectionsY[idx] - triangleP2Y
                    val a3z = intersectionsZ[idx] - triangleP2Z

                    // Cross product components {X,Y,Z}
                    val a1crossX = (v1y * a1z) - (v1z * a1y)
                    val a1crossY = -((v1x * a1z) - (v1z * a1x))
                    val a1crossZ = (v1x * a1y) - (v1y * a1x)

                    val a2crossX = (v2y * a2z) - (v2z * a2y)
                    val a2crossY = -((v2x * a2z) - (v2z * a2x))
                    val a2crossZ = (v2x * a2y) - (v2y * a2x)

                    val a3crossX = (v3y * a3z) - (v3z * a3y)
                    val a3crossY = -((v3x * a3z) - (v3z * a3x))
                    val a3crossZ = (v3x * a3y) - (v3y * a3x)

                    // Dot products
                    val c1 = a1crossX * b1x + a1crossY * b1y + a1crossZ * b1z
                    val c2 = a2crossX * b2x + a2crossY * b2y + a2crossZ * b2z
                    val c3 = a3crossX * b3x + a3crossY * b3y + a3crossZ * b3z

                    if (c1 >= 0 && c2 >= 0 && c3 >= 0) {
                        // Distance formula sqrt((x - y) ^ 2 + ..))
                        val x = (cameraOriginX - intersectionsX[idx]) * (cameraOriginX - intersectionsX[idx])
                        val y = (cameraOriginY - intersectionsY[idx]) * (cameraOriginY - intersectionsY[idx])
                        val z = (cameraOriginZ - intersectionsZ[idx]) * (cameraOriginZ - intersectionsZ[idx])
                        result[idx] = 1/sqrt(x + y + z)
                    } else {
                        result[idx] = 0.0f
                    }
                }
            }
            kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU)

            kernel.execute(Range.create2D(size, size))
            kernel.dispose()

            return Array(size) { y ->
                Array(size) { x ->
                    result[x + y * size]
                }
            }
        }
    }
}