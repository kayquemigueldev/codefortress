package com.codefortress.analysis.api;

import com.codefortress.analysis.upload.UploadAnalysisService;
import com.codefortress.analysis.upload.UploadedAnalysis;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/projects/{projectId}/analyses"
)
public class AnalysisController {

    private final UploadAnalysisService uploadAnalysisService;

    public AnalysisController(
            UploadAnalysisService uploadAnalysisService
    ) {
        this.uploadAnalysisService = uploadAnalysisService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse upload(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @RequestPart("file") MultipartFile file
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        UploadedAnalysis uploadedAnalysis =
                uploadAnalysisService.upload(
                        ownerId,
                        projectId,
                        file
                );

        return AnalysisResponse.from(uploadedAnalysis);
    }
}