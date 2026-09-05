package com.codefortress.analysis.engine;

import com.codefortress.analysis.discovery.SourceFileCategory;

public record ScannableFile(
        String normalizedPath,
        SourceFileCategory category,
        String content,
        int lineCount
) {
}