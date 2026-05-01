package com.aideva.deepdive.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.stereotype.Component;

/**
 * Custom Advisor: logs every prompt and response.
 * Advisors are Spring AI's interceptor pattern — they wrap every LLM call.
 */
@Component
public class LoggingAdvisor implements CallAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        log.info("[AI REQUEST ] user={}", request.userText());
        AdvisedResponse response = chain.nextAroundCall(request);
        String content = response.response().getResult().getOutput().getContent();
        log.info("[AI RESPONSE] content={}...", content.length() > 80 ? content.substring(0, 80) : content);
        return response;
    }

    @Override
    public String getName() { return "LoggingAdvisor"; }

    @Override
    public int getOrder() { return 0; }
}
