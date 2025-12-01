package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import net.coobird.thumbnailator.Thumbnails;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    private final Path imageDir = Paths.get("/var/images"); // Change as needed

    public Resource getImage(String imageId, Integer width, Integer height) throws MalformedURLException {
        Path imagePath = imageDir.resolve(imageId + ".jpg");
        if (width != null && height != null) {
            Path thumbPath = imageDir.resolve(imageId + "_" + width + "x" + height + ".jpg");
            File thumbFile = thumbPath.toFile();
            if (!thumbFile.exists()) {
                try {
                    Thumbnails.of(imagePath.toFile()).size(width, height).toFile(thumbFile);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to resize image", e);
                }
            }
            return new UrlResource(thumbPath.toUri());
        }
        return new UrlResource(imagePath.toUri());
    }

    public String getSignedUrl(String imageId, Integer width, Integer height) {
        // Stub: In production, generate a signed CDN URL (e.g., AWS S3, CloudFront)
        String baseUrl = "https://cdn.example.com/images/" + imageId + ".jpg";
        if (width != null && height != null) {
            baseUrl = "https://cdn.example.com/images/" + imageId + "_" + width + "x" + height + ".jpg";
        }
        // Add signature logic here
        return baseUrl + "?signed=1";
    }
}

