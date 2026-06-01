package com.weichatcheck.engine

import net.sourceforge.tess4j.Tesseract
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

class OCRFallback {
    private val tesseract = Tesseract()

    init {
        val dataPath = File("tessdata").absolutePath
        tesseract.setDatapath(dataPath)
        tesseract.setLanguage("chi_sim+eng")
    }

    fun recognize(x: Int, y: Int, width: Int, height: Int): String {
        return try {
            val robot = Robot()
            val capture = robot.createScreenCapture(Rectangle(x, y, width, height))
            val tempFile = File.createTempFile("screenshot", ".png")
            tempFile.deleteOnExit()
            ImageIO.write(capture, "png", tempFile)
            tesseract.doOCR(tempFile)
        } catch (e: Exception) {
            ""
        }
    }

    fun recognizeFile(imageFile: File): String {
        return try {
            tesseract.doOCR(imageFile)
        } catch (e: Exception) {
            ""
        }
    }
}
