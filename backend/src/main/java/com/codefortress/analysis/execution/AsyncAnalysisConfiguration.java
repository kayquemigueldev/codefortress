package com.codefortress.analysis.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncAnalysisConfiguration {

    private final int coreThreads;
    private final int maxThreads;
    private final int queueCapacity;

    public AsyncAnalysisConfiguration(
            @Value(
                    "${ANALYSIS_WORKER_CORE_THREADS:2}"
            )
            int coreThreads,

            @Value(
                    "${ANALYSIS_WORKER_MAX_THREADS:4}"
            )
            int maxThreads,

            @Value(
                    "${ANALYSIS_WORKER_QUEUE_CAPACITY:100}"
            )
            int queueCapacity
    ) {
        if (coreThreads < 1) {
            throw new IllegalArgumentException(
                    "coreThreads must be greater than zero"
            );
        }

        if (maxThreads < coreThreads) {
            throw new IllegalArgumentException(
                    "maxThreads must be greater than "
                            + "or equal to coreThreads"
            );
        }

        if (queueCapacity < 0) {
            throw new IllegalArgumentException(
                    "queueCapacity must not be negative"
            );
        }

        this.coreThreads = coreThreads;
        this.maxThreads = maxThreads;
        this.queueCapacity = queueCapacity;
    }

    @Bean(name = "analysisTaskExecutor")
    public Executor analysisTaskExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(coreThreads);
        executor.setMaxPoolSize(maxThreads);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(
                "codefortress-analysis-"
        );

        executor.setWaitForTasksToCompleteOnShutdown(
                true
        );

        executor.setAwaitTerminationSeconds(30);

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.initialize();

        return executor;
    }
}