package org.raytracer

import java.awt.BorderLayout
import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

data class Drawable(val coordinates: Triangle, val colour: RGBColour)
data class RGBColour(val r: Int, val g: Int, val b: Int)

class Scene(
    val sceneObjects: List<Drawable> = listOf(),
) {
    fun calcPixelIntensities(cameraOrigin: Point, viewPlane: Array<Array<Vector>>): List<List<Pair<Double, RGBColour>>> {
        val objectIntersections = sceneObjects.map { drawable ->
            val intersections = viewPlane.map { row ->
                row.map { Scene.intersectionPointDelta(drawable.coordinates, cameraOrigin, it) }.zip(row)
            }.map { row ->
                row.map { Scene.intersectionPoint(it.first, it.second, cameraOrigin) }
            }
            val bounds = intersections.map { row ->
                row.map { Scene.isWithinBounds(drawable.coordinates, it) }.zip(row)
            }
            val pixelIntensities = bounds.map { row ->
                row.map { if (it.first) Scene.isWithinTriangle(drawable.coordinates, it.second) else false }
                   .zip(row.map { it.second })
            }.map { row ->
                row.map {
                    Pair(
                        if (it.first) 1 / cameraOrigin.distanceTo(it.second) else 0.0,
                        drawable.colour
                    )
                }
            }
            pixelIntensities
        }
        return calcViewWinners(objectIntersections)
    }

    // Go through each pixel and return the closest.
    private fun calcViewWinners(intersections: List<List<List<Pair<Double, RGBColour>>>>): List<List<Pair<Double, RGBColour>>> {
        return List(intersections[0].size) { y ->
            List(intersections[0].size) { x ->
                intersections.fold(Pair(0.0, RGBColour(0,0,0)))
                    { max, element -> if(element[y][x].first > max.first) element[y][x] else max }
            }
        }
    }

    companion object {
        fun buildViewPlaneAngles(
            pixDimension: Int,
            pixelSize: Double = 0.5,
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
            pixelSize: Double,
            pixDimension: Int,
            pitchDegrees: Int,
            yawDegrees: Int
        ): Vector {
            val focus = Vector(
                x = pixelSize * (x - pixDimension / 2),
                y = pixelSize * (y - pixDimension / 2),
                z = 1.0
            )
            return focus.rotY(yawDegrees).rotX(pitchDegrees)
        }

        fun intersectionPoint(delta: Double, slope: Vector, origin: Vector) =
            Vector(
                x = delta * slope.x + origin.x,
                y = delta * slope.y + origin.y,
                z = delta * slope.z + origin.z
            )

        fun intersectionPointDelta(triangle: Triangle, origin: Vector, slope: Vector) =
            -((triangle.normal().dot(origin) + triangle.k()) / triangle.normal().dot(slope))

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


fun pixelRepresentation(intensity: Double): Char {
    if (intensity in 0.01..0.3) return '0'
    if (intensity in 0.3..0.4) return '1'
    if (intensity in 0.4..0.45) return '2'
    if (intensity in 0.45..0.50) return '3'
    if (intensity in 0.50..0.55) return '4'
    if (intensity in 0.55..0.60) return '5'
    if (intensity in 0.60..0.65) return '6'
    if (intensity in 0.65..0.70) return '7'
    if (intensity in 0.70..1.00) return '8'
    return '.'
}
//    val display = pixelIntensities.map { row -> row.map { pixelRepresentation(it) } }
//    display.forEach{ row -> row.map { print(it) }
//        println(" ")
//    }

private var frame: JFrame? = null
private var label: JLabel? = null
fun display(image: BufferedImage) {
    if (frame == null) {
        frame = JFrame()
        frame!!.title = "Raytraced Image"
        frame!!.setSize(image.width, image.height)
        frame!!.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        label = JLabel()
        label!!.icon = ImageIcon(image)
        frame!!.contentPane.add(label, BorderLayout.CENTER)
        frame!!.setLocationRelativeTo(null)
        frame!!.pack()
        frame!!.isVisible = true
    } else label!!.icon = ImageIcon(image)
}

fun main() {
    val requiredPixelDimension = 801
    val cameraOrigin = Vector(0.0, 0.0, 0.0)
    val viewPlaneAngles = Scene.buildViewPlaneAngles(pixDimension = requiredPixelDimension, pixelSize = 0.0015)
    val triangularObject1 = Triangle(
        Point(0.0, 0.25, 1.0),
        Point(-0.5, -0.5, 1.5),
        Point(0.5, -0.5, 2.0)
    )
    val triangularObject2 = Triangle(
        Point(0.7, 1.0, 2.5),
        Point(0.3, 0.5, 2.0),
        Point(1.1, 0.5, 2.5)
    )
    val myScene = Scene(
        listOf(
            Drawable(triangularObject1, RGBColour(180, 0, 180)),
            Drawable(triangularObject2, RGBColour(0, 200, 0))
        )
    )
    val pixelIntensities = myScene.calcPixelIntensities(cameraOrigin, viewPlaneAngles)
    val rgb = pixelIntensities.flatten().map {
        arrayOf(
            (it.first * it.second.r).toInt(),
            (it.first * it.second.g).toInt(),
            (it.first * it.second.b).toInt()
        )
    }.toTypedArray().flatten()

    val image = BufferedImage(requiredPixelDimension, requiredPixelDimension, BufferedImage.TYPE_INT_RGB)
    val raster: WritableRaster = image.raster
    raster.setPixels(0, 0, requiredPixelDimension, requiredPixelDimension, rgb.toIntArray())
    display(image)
    val a = 3
}

