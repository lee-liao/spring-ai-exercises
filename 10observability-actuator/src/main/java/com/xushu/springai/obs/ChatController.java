package com.xushu.springai.obs;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;

    public ChatController(ChatClient chatClient, ObservationRegistry observationRegistry, Tracer tracer) {
        this.chatClient = chatClient;
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(name = "message", defaultValue = "Hello") String message,
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {

        return Observation.createNotStarted("chat.request", observationRegistry)
                .lowCardinalityKeyValue("user.id", userId)
                .lowCardinalityKeyValue("http.path", "/chat")
                .observe(() -> {
                    String result = chatClient.prompt()
                            .user(message)
                            .call()
                            .content();

                    // Get and log trace ID and span ID
                    var currentSpan = tracer.currentSpan();
                    if (currentSpan != null) {
                        String traceId = currentSpan.context().traceId();
                        String spanId = currentSpan.context().spanId();
                        System.out.println("Trace ID: " + traceId + ", Span ID: " + spanId);

                        // You can also return it in response headers or logs
                        // result = "Trace ID: " + traceId + "\n" + result;
                    }

                    return result;
                });
    }
}
