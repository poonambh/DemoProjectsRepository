package com.psl.adms.web.config;

import brave.Tracing;
import brave.context.log4j2.ThreadContextScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {


    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    Tracing tracing() {
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(ThreadContextScopeDecorator.create())
                        .build()
                )
    .build();
        return tracing;
    }
}
