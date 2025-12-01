package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.thepaddlers.service.ImageService;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageId, @RequestParam(required=false) Integer width, @RequestParam(required=false) Integer height) throws MalformedURLException {
        // Returns resized image or original if no size specified
        Resource image = imageService.getImage(imageId, width, height);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageId + ".jpg\"")
            .body(image);
    }

    @GetMapping("/signed/{imageId}")
    public ResponseEntity<String> getSignedUrl(@PathVariable String imageId, @RequestParam(required=false) Integer width, @RequestParam(required=false) Integer height) {
        String url = imageService.getSignedUrl(imageId, width, height);
        return ResponseEntity.ok(url);
    }
}

