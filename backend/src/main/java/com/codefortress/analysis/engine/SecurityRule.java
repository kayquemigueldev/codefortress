package com.codefortress.analysis.engine;

import java.util.List;

public interface SecurityRule {

    String key();

    String version();

    boolean supports(
            ScannableFile file
    );

    List<RuleMatch> evaluate(
            ScannableFile file,
            AnalysisContext context
    );
}