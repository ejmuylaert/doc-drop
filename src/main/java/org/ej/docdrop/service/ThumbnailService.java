package org.ej.docdrop.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ThumbnailService {

    public Path createThumbnail(Path pdfPath) throws ThumbnailException, IOException {
        PDDocument pdfDocument = loadFile(pdfPath);
        BufferedImage bufferedImage = renderFirstPage(pdfDocument);

        Image scaledInstance = bufferedImage.getScaledInstance(280, -1, 0);
        BufferedImage targetImage = new BufferedImage(scaledInstance.getWidth(null),
                scaledInstance.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = targetImage.createGraphics();
        graphics.drawImage(scaledInstance, 0, 0, null);
        graphics.dispose();

        Path imageFile = Files.createTempFile("thumbnail", "jpg");
        boolean created = ImageIO.write(targetImage, "jpg", imageFile.toFile());

        if (!created) {
            throw new ThumbnailException("Error creating JPG file from PDF document", null);
        }

        return imageFile;
    }

    private PDDocument loadFile(Path pdfPath) throws ThumbnailException {
        try {
            return Loader.loadPDF(pdfPath.toFile());
        } catch (IOException e) {
            throw new ThumbnailException("Error parsing PDF file, is file format correct?", e);
        }
    }

    private BufferedImage renderFirstPage(PDDocument doc) throws ThumbnailException {
        PDFRenderer pdfRenderer = new PDFRenderer(doc);

        try {
            return pdfRenderer.renderImage(0);
        } catch (IOException e) {
            throw new ThumbnailException("Error creating image from first page", e);
        }
    }
}