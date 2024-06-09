package top.kagg886.cctr.util

import java.awt.image.BufferedImage

fun List<BufferedImage>.mergeVertical(): BufferedImage {
    val images = this
    val width = images.stream().mapToInt { obj: BufferedImage -> obj.width }.max().asInt
    val height = images.stream().mapToInt { obj: BufferedImage -> obj.height }.sum()

    val rtn = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics2D = rtn.createGraphics()

    var newH = 0
    for (image in images) {
        graphics2D.drawImage(image, 0, newH, image.width, image.height, null)
        newH += image.height
    }

    graphics2D.dispose()
    return rtn
}