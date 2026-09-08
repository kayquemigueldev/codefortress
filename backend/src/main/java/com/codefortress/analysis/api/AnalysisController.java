package com.codefortress.analysis.api;

import com.codefortress.analysis.listing.ListAnalysesService;
import com.codefortress.analysis.upload.UploadAnalysisService;
import com.codefortress.analysis.upload.UploadedAnalysis;
import com.codefortress.analysis.finding.ListFindingsService;
import com.codefortress.analysis.finding.UpdateFindingStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/projects/{projectId}/analyses"
)
public class AnalysisController {

    private final UploadAnalysisService uploadAnalysisService;
    private final ListAnalysesService listAnalysesService;
    private final ListFindingsService listFindingsService;
    private final UpdateFindingStatusService updateFindingStatusService;

    public AnalysisController(
            UploadAnalysisService uploadAnalysisService,
            ListAnalysesService listAnalysesService,
            ListFindingsService listFindingsService,
            UpdateFindingStatusService updateFindingStatusService
    ) {
        this.uploadAnalysisService = uploadAnalysisService;
        this.listAnalysesService = listAnalysesService;
        this.listFindingsService = listFindingsService;
        this.updateFindingStatusService =
                updateFindingStatusService;
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

    @GetMapping
    public List<AnalysisHistoryResponse> list(

            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        return listAnalysesService
                .list(ownerId, projectId)
                .stream()
                .map(AnalysisHistoryResponse::from)
                .toList();
    }

    @GetMapping("/{analysisId}/findings")
    public List<FindingResponse> listFindings(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID analysisId
    ) {
        UUID ownerId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return listFindingsService
                .list(
                        ownerId,
                        projectId,
                        analysisId
                )
                .stream()
                .map(FindingResponse::from)
                .toList();
    }

    @PatchMapping(
            "/{analysisId}/findings/{findingId}/status"
    )
    public FindingResponse updateFindingStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID analysisId,
            @PathVariable UUID findingId,
            @Valid
            @RequestBody
            UpdateFindingStatusRequest request
    ) {
        UUID ownerId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return FindingResponse.from(
                updateFindingStatusService.update(
                        ownerId,
                        projectId,
                        analysisId,
                        findingId,
                        request.status()
                )
        );
    }

}