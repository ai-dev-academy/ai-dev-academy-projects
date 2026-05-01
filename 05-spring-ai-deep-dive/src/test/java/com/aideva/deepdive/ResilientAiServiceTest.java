package com.aideva.deepdive;

import com.aideva.deepdive.service.ResilientAiService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResilientAiServiceTest {

    @Test
    void returnsFallbackWhenAiFails() {
        ChatClient mockClient = mock(ChatClient.class);
        var spec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);

        when(mockClient.prompt()).thenReturn(spec);
        when(spec.user(any(String.class))).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenThrow(new RuntimeException("Service unavailable"));

        ResilientAiService service = new ResilientAiService(mockClient);
        String result = service.chat("Hello");

        assertThat(result).contains("temporarily unavailable");
    }

    @Test
    void returnsNormalResponseOnSuccess() {
        ChatClient mockClient = mock(ChatClient.class);
        var spec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);

        when(mockClient.prompt()).thenReturn(spec);
        when(spec.user(any(String.class))).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("Spring AI uses a builder pattern.");

        ResilientAiService service = new ResilientAiService(mockClient);
        assertThat(service.chat("Explain Spring AI")).contains("Spring AI");
    }
}
