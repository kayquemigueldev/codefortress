package com.codefortress.analysis.execution;

import com.codefortress.analysis.engine.FindingFactory;
import com.codefortress.analysis.engine.FindingFingerprintGenerator;
import com.codefortress.analysis.engine.SecurityRuleExecutor;
import com.codefortress.analysis.engine.rules.HardcodedSecretRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.codefortress.analysis.engine.rules.DebugModeEnabledRule;

import java.util.List;

@Configuration
public class AnalysisEngineConfiguration {

    @Bean
    public SecurityRuleExecutor securityRuleExecutor() {
        return new SecurityRuleExecutor(
                List.of(
                        new HardcodedSecretRule(),
                        new DebugModeEnabledRule()
                )
        );
    }

    @Bean
    public FindingFingerprintGenerator findingFingerprintGenerator() {
        return new FindingFingerprintGenerator();
    }

    @Bean
    public FindingFactory findingFactory(
            FindingFingerprintGenerator fingerprintGenerator
    ) {
        return new FindingFactory(
                fingerprintGenerator
        );
    }
}