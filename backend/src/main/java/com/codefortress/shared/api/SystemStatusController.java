package com.codefortress.shared.api;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

    @GetMapping("/status")
    public SystemStatusResponse getStatus() {
        return new SystemStatusResponse(
                "CodeFortress API",
                "operational",
                "foundation",
                Instant.now()
        );
    }
}