package com.codefortress.analysis.engine;

import java.util.List;

public interface SecurityRule {

    RuleMetadata metadata();

    boolean supports(ScannableFile file);

    List<RuleMatch> evaluate(
            ScannableFile file,
            AnalysisContext context
    );
}