package com.aideva.prompteng;

import com.aideva.prompteng.service.EmailClassifierService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ClassifierServiceTest {

    @MockBean
    ChatClient.Builder builder;

    @Test
    void classifyBillingEmail() {
        ChatClient mockClient = mock(ChatClient.class);
        var spec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);

        when(builder.defaultSystem(any())).thenReturn(builder);
        when(builder.build()).thenReturn(mockClient);
        when(mockClient.prompt(any())).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("BILLING");

        EmailClassifierService service = new EmailClassifierService(builder);
        String result = service.classify("I was charged twice");
        assertThat(result).isEqualTo("BILLING");
    }
}
