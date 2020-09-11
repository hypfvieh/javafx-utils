package com.github.hypfvieh.javafx.other;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Utility to manipulate images.
 *
 * @author hypfvieh
 * @since v11.0.X - 2020-09-11
 */
public class ImageUtil {
    /**
     * Resize an image to given height/width keep the aspect ratio.
     *
     * @param sourceImg The source of the image to resize.
     * @param destImg The destination of the resized image.
     * @param _width The maximum width you want the new image to be, use 0 for source width.
     * @param _height The maximum height you want the new image to be, use 0 for source height.
     * @return byte array containing resized image
     *
     * @throws IOException on read/write error
     */
    public static byte[] resizeImage(File _imageFile, int _width, int _height) throws IOException {
        BufferedImage origImage;

        origImage = ImageIO.read(_imageFile);
        int type = origImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : origImage.getType();

        //*Special* if the width or height is 0 use image src dimensions
        if (_width == 0) {
            _width = origImage.getWidth();
        }
        if (_height == 0) {
            _height = origImage.getHeight();
        }

        int fHeight = _height;
        int fWidth = _width;

        //Work out the resized width/height
        if (origImage.getHeight() > _height || origImage.getWidth() > _width) {
            fHeight = _height;
            int wid = _width;
            float sum = (float)origImage.getWidth() / (float)origImage.getHeight();
            fWidth = Math.round(fHeight * sum);

            if (fWidth > wid) {
                //rezise again for the width this time
                fHeight = Math.round(wid/sum);
                fWidth = wid;
            }
        }

        BufferedImage resizedImage = new BufferedImage(fWidth, fHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(origImage, 0, 0, fWidth, fHeight, null);
        g.dispose();

        // writes to output file
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(resizedImage, "png", baos);
            return baos.toByteArray();
        }

    }
}
