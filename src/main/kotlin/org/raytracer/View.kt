package org.raytracer

import java.awt.BorderLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

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

class Controls : KeyListener {
    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_RIGHT -> yaw += 1
            KeyEvent.VK_LEFT -> yaw -= 1
            KeyEvent.VK_UP -> pitch -= 1
            KeyEvent.VK_DOWN -> pitch += 1
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }
}

var yaw = 0
var pitch = 0

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
        frame!!.addKeyListener(Controls())
    } else label!!.icon = ImageIcon(image)
}

fun main() {
    val requiredPixelDimension = 501
    val cameraOrigin = Vector(0.0f, 0.0f, 0.0f)
    val viewPlaneAngles = Scene.buildViewPlaneAngles(
        pixDimension = requiredPixelDimension,
        pixelSize = 0.0030f,
//        pixelSize = 0.1f,
        yawDegrees = yaw,
        pitchDegrees = pitch
    )
    val triangularObject1 = Triangle(
        Point(0.0f, 0.25f, 1.0f),
        Point(-0.5f, -0.5f, 1.0f),
        Point(0.5f, -0.5f, 2.0f)
    )
    val triangularObject2 = Triangle(
        Point(0.7f, 1.0f, 2.5f),
        Point(0.3f, 0.5f, 2.0f),
        Point(1.1f, 0.5f, 2.5f)
    )
    val myScene = Scene(
        listOf(
            Drawable(triangularObject1, RGBColour(180, 0, 180)),
            Drawable(triangularObject2, RGBColour(0, 200, 0))
        )
    )
    val image = BufferedImage(requiredPixelDimension, requiredPixelDimension, BufferedImage.TYPE_INT_RGB)
    val raster: WritableRaster = image.raster
    var avgSum = 0L
    var numSoFar = 0
    while (true) {
        val startTime = System.currentTimeMillis()
        val viewPlane = Scene.buildViewPlaneAngles(
            pixDimension = requiredPixelDimension, pixelSize = 0.0030f,
            yawDegrees = yaw,
            pitchDegrees = pitch
        )
        val pixTime = System.currentTimeMillis()
        val pixelIntensities = myScene.calcPixelIntensities(cameraOrigin, viewPlane = viewPlane)
        println("Pix intensity calc time: ${System.currentTimeMillis() - pixTime}")
        val colourTime = System.currentTimeMillis()
        val rgb = pixelIntensities.flatten().map {
            arrayOf(
                (it.first * it.second.r).toInt(),
                (it.first * it.second.g).toInt(),
                (it.first * it.second.b).toInt()
            )
        }.toTypedArray().flatten()
        println("RGB calc time: ${System.currentTimeMillis() - colourTime}")
        raster.setPixels(0, 0, requiredPixelDimension, requiredPixelDimension, rgb.toIntArray())
        display(image)
        avgSum += (System.currentTimeMillis() - startTime)
        numSoFar++
        println("Avg ${1000/(avgSum/numSoFar)} fps")
//    val display = pixelIntensities.map { row -> row.map { pixelRepresentation(it) } }
//    display.forEach{ row -> row.map { print(it) }
//        println(" ")
//    }
    }
}

