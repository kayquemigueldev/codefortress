package com.codefortress.analysis.execution;

import com.codefortress.analysis.engine.FindingFactory;
import com.codefortress.analysis.engine.FindingFingerprintGenerator;
import com.codefortress.analysis.engine.SecurityRuleExecutor;
import com.codefortress.analysis.engine.rules.HardcodedSecretRule;
import com.codefortress.analysis.engine.rules.DebugModeEnabledRule;
import com.codefortress.analysis.engine.rules.TlsCertificateVerificationDisabledRule;
import com.codefortress.analysis.engine.rules.PermissiveCorsConfigurationRule;
import com.codefortress.analysis.engine.rules.SqlInjectionRiskRule;
import com.codefortress.analysis.engine.rules.WeakCryptographicHashRule;
import com.codefortress.analysis.engine.rules.SensitiveInformationExposureRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Configuration
public class AnalysisEngineConfiguration {

    @Bean
    public SecurityRuleExecutor securityRuleExecutor() {
        return new SecurityRuleExecutor(
                List.of(
                        new HardcodedSecretRule(),
                        new DebugModeEnabledRule(),
                        new TlsCertificateVerificationDisabledRule(),
                        new PermissiveCorsConfigurationRule(),
                        new SqlInjectionRiskRule(),
                        new WeakCryptographicHashRule(),
                        new SensitiveInformationExposureRule()
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