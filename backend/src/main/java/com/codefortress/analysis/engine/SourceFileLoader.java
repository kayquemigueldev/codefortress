package com.codefortress.analysis.engine;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Component
public class SourceFileLoader {

    public ScannableFile load(
            DiscoveredSourceFile sourceFile
    ) {
        Objects.requireNonNull(
                sourceFile,
                "sourceFile must not be null"
        );

        try {
            String content = Files.readString(
                    sourceFile.absolutePath(),
                    StandardCharsets.UTF_8
            );

            int lineCount = Math.toIntExact(
                    content.lines().count()
            );

            return new ScannableFile(
                    sourceFile.relativePath(),
                    sourceFile.category(),
                    content,
                    lineCount
            );
        } catch (
                IOException | ArithmeticException exception
        ) {
            throw new SourceFileLoadingException(
                    "SOURCE_FILE_LOADING_FAILED",
                    "The source file could not be loaded",
                    exception
            );
        }
    }
}