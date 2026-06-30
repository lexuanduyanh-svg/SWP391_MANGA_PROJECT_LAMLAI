package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.entities.PageEntity;
import com.mangastudio.workflow.repositories.PageRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pages")
public class PageController {

  private final PageRepository pageRepository;
  private final Path storageBase;

  public PageController(PageRepository pageRepository) {
    this.pageRepository = pageRepository;
    this.storageBase = Path.of("storage-server", "manuscripts");
  }

  /**
   * GET /api/pages/{pageId}/image
   *
   * <p>Serves the manuscript image file for the given page. Falls back to a placeholder SVG if the
   * file is not found.
   */
  @GetMapping("/{pageId}/image")
  public ResponseEntity<?> getPageImage(@PathVariable Long pageId) {
    PageEntity page =
        pageRepository
            .findById(pageId)
            .orElseThrow(() -> new IllegalArgumentException("Page not found"));

    String filePath = page.getManuscriptFilePath();
    if (filePath == null || filePath.isBlank()) {
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(placeholderImage("No file"));
    }

    // Try storage-base relative path, then raw path
    Path imagePath = storageBase.resolve(filePath);
    if (!Files.exists(imagePath)) {
      imagePath = Path.of(filePath);
    }

    try {
      byte[] bytes = Files.readAllBytes(imagePath);
      String contentType = Files.probeContentType(imagePath);
      if (contentType == null) contentType = "image/png";
      return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(bytes);
    } catch (IOException e) {
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(placeholderImage(filePath));
    }
  }

  /** Generate a simple placeholder SVG as PNG bytes. */
  private byte[] placeholderImage(String label) {
    String svg =
        "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='600'>"
            + "<rect width='400' height='600' fill='#f0f0f0' stroke='#ccc' stroke-width='2'/>"
            + "<text x='200' y='280' text-anchor='middle' fill='#999' font-size='16'>"
            + "Page image: "
            + label
            + "</text>"
            + "<text x='200' y='310' text-anchor='middle' fill='#bbb' font-size='12'>"
            + "(placeholder)</text>"
            + "</svg>";
    return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }
}
