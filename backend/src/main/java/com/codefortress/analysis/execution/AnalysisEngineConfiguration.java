package com.codefortress.analysis.execution;

import com.codefortress.analysis.engine.SecurityRuleExecutor;
import com.codefortress.analysis.engine.rules.HardcodedSecretRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AnalysisEngineConfiguration {

    @Bean
    public SecurityRuleExecutor securityRuleExecutor() {
        return new SecurityRuleExecutor(
                List.of(
                        new HardcodedSecretRule()
                )
        );
    }
}