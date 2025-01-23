package site.dogether.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RequiredArgsConstructor
@RestController
public class RestApiDocsController {

    private final ResourceLoader resourceLoader;

    @GetMapping("/api/docs")
    public ResponseEntity<Resource> docs() {
        final Resource resource = resourceLoader.getResource("classpath:static/docs/index.html");
        return ResponseEntity.ok(resource);
    }
}
