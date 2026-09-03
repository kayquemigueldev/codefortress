package com.codefortress.analysis.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class SourceArchiveInspector {

    private static final int BUFFER_SIZE = 8_192;

    private final long maxSizeBytes;

    public SourceArchiveInspector(
            @Value("${ANALYSIS_UPLOAD_MAX_SIZE:10MB}")
            DataSize maxSize
    ) {
        if (maxSize == null || maxSize.toBytes() < 1) {
            throw new IllegalArgumentException(
                    "maxSize must be greater than zero"
            );
        }

        this.maxSizeBytes = maxSize.toBytes();
    }

    public InspectedSourceArchive inspect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw invalid(
                    "SOURCE_ARCHIVE_REQUIRED",
                    "A non-empty source archive is required"
            );
        }

        String sourceFilename = sanitizeFilename(
                file.getOriginalFilename()
        );

        if (file.getSize() > maxSizeBytes) {
            throw tooLarge();
        }

        MessageDigest digest = createSha256Digest();
        byte[] signature = new byte[4];
        int signatureLength = 0;
        long totalBytes = 0;

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead == 0) {
                    continue;
                }

                totalBytes += bytesRead;

                if (totalBytes > maxSizeBytes) {
                    throw tooLarge();
                }

                if (signatureLength < signature.length) {
                    int bytesToCopy = Math.min(
                            bytesRead,
                            signature.length - signatureLength
                    );

                    System.arraycopy(
                            buffer,
                            0,
                            signature,
                            signatureLength,
                            bytesToCopy
                    );

                    signatureLength += bytesToCopy;
                }

                digest.update(buffer, 0, bytesRead);
            }
        } catch (IOException exception) {
            throw invalid(
                    "SOURCE_ARCHIVE_READ_FAILED",
                    "The source archive could not be read"
            );
        }

        if (totalBytes == 0) {
            throw invalid(
                    "SOURCE_ARCHIVE_REQUIRED",
                    "A non-empty source archive is required"
            );
        }

        if (!hasZipSignature(signature, signatureLength)) {
            throw invalid(
                    "INVALID_SOURCE_ARCHIVE",
                    "The uploaded file is not a valid ZIP archive"
            );
        }

        String sourceReference = HexFormat
                .of()
                .formatHex(digest.digest());

        return new InspectedSourceArchive(
                sourceFilename,
                sourceReference,
                totalBytes
        );
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null
                || originalFilename.isBlank()) {
            throw invalid(
                    "INVALID_SOURCE_FILENAME",
                    "The source archive filename is invalid"
            );
        }

        String normalizedPath = originalFilename
                .trim()
                .replace('\\', '/');

        int lastSeparator = normalizedPath.lastIndexOf('/');

        String baseName = normalizedPath.substring(
                lastSeparator + 1
        );

        if (!baseName
                .toLowerCase(Locale.ROOT)
                .endsWith(".zip")) {
            throw invalid(
                    "INVALID_SOURCE_ARCHIVE_TYPE",
                    "Only ZIP source archives are supported"
            );
        }

        String nameWithoutExtension = baseName.substring(
                0,
                baseName.length() - 4
        );

        String sanitizedName = nameWithoutExtension
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceFirst("^\\.+", "");

        if (sanitizedName.isBlank()) {
            sanitizedName = "source";
        }

        int maximumNameLength = 251;

        if (sanitizedName.length() > maximumNameLength) {
            sanitizedName = sanitizedName.substring(
                    0,
                    maximumNameLength
            );
        }

        return sanitizedName + ".zip";
    }

    private boolean hasZipSignature(
            byte[] signature,
            int signatureLength
    ) {
        if (signatureLength < 4) {
            return false;
        }

        boolean startsWithPk =
                signature[0] == 0x50
                        && signature[1] == 0x4B;

        boolean regularZip =
                signature[2] == 0x03
                        && signature[3] == 0x04;

        boolean emptyZip =
                signature[2] == 0x05
                        && signature[3] == 0x06;

        return startsWithPk && (regularZip || emptyZip);
    }

    private MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    private InvalidSourceArchiveException tooLarge() {
        return invalid(
                "SOURCE_ARCHIVE_TOO_LARGE",
                "The source archive exceeds the allowed size"
        );
    }

    private InvalidSourceArchiveException invalid(
            String code,
            String message
    ) {
        return new InvalidSourceArchiveException(
                code,
                message
        );
    }
}
