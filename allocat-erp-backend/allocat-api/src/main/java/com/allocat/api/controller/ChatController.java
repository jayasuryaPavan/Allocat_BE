package com.allocat.api.controller;

import com.allocat.ai.service.InvenGaduAgentService;
import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "InvenGadu Chat", description = "AI-powered inventory assistant chat endpoints")
public class ChatController {

    private final InvenGaduAgentService agentService;

    @PostMapping
    @Operation(summary = "Chat with InvenGadu", 
               description = "Send a message to the AI inventory assistant and get a response")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        try {
            // Validate request
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.<ChatResponse>builder()
                        .success(false)
                        .message("Message cannot be empty")
                        .build());
            }

            log.info("Processing chat message for conversation: {}", request.getConversationId());
            long startTime = System.currentTimeMillis();
            
            String conversationId = request.getConversationId() != null ? 
                                    request.getConversationId() : "default";
            
            var agent = agentService.getAgent(conversationId);
            
            log.info("Agent retrieved, sending message to LLM");
            String response = agent.chat(request.getMessage());
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Chat completed in {}ms for conversation: {}", duration, conversationId);
            
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setMessage(response);
            chatResponse.setConversationId(conversationId);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", "ollama-llama3");
            metadata.put("tools_used", true);
            chatResponse.setMetadata(metadata);
            
            return ResponseEntity.ok(ApiResponse.<ChatResponse>builder()
                    .success(true)
                    .message("Chat response generated successfully")
                    .data(chatResponse)
                    .build());
                    
        } catch (org.springframework.web.reactive.function.client.WebClientException e) {
            log.error("Ollama connection error", e);
            return ResponseEntity.status(503).body(ApiResponse.<ChatResponse>builder()
                    .success(false)
                    .message("Cannot connect to Ollama. Please ensure Ollama is running at http://localhost:11434")
                    .build());
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.status(500).body(ApiResponse.<ChatResponse>builder()
                    .success(false)
                    .message("Error processing chat: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check InvenGadu health", 
               description = "Verify if Ollama and AI services are available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        try {
            // Try to create a test agent to verify Ollama connection
            var agent = agentService.getAgent("health-check");
            agent.chat("test"); // Test connection
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("ollama", "connected");
            health.put("message", "InvenGadu is ready");
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Health check passed")
                    .data(health)
                    .build());
        } catch (Exception e) {
            log.error("Health check failed", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "unhealthy");
            health.put("ollama", "disconnected");
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Health check failed")
                    .data(health)
                    .build());
        }
    }

    @PostMapping("/new")
    @Operation(summary = "Start new conversation", 
               description = "Clear conversation history and start a new chat session")
    public ResponseEntity<ApiResponse<Map<String, String>>> newConversation(
            @Parameter(description = "Conversation ID (optional)") 
            @RequestParam(required = false) String conversationId) {
        try {
            String id = conversationId != null ? conversationId : "default";
            agentService.clearConversation(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "New conversation started");
            response.put("conversation_id", id);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("New conversation started")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("Error starting new conversation", e);
            return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @Data
    public static class ChatRequest {
        private String message;
        private String conversationId;
    }

    @Data
    public static class ChatResponse {
        private String message;
        private String conversationId;
        private Map<String, Object> metadata;
    }
}

