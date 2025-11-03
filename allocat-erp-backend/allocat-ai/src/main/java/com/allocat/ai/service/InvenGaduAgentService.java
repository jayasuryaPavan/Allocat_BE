package com.allocat.ai.service;

import com.allocat.ai.config.AiProperties;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages InvenGadu AI agents for conversations.
 * Each conversation gets its own agent instance with separate memory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvenGaduAgentService {

    private final AiProperties aiProperties;
    private final InventoryTools inventoryTools;
    
    // Store agents per conversation ID
    private final Map<String, InvenGaduAgent> agents = new ConcurrentHashMap<>();

    /**
     * Get or create an agent for a conversation
     */
    public InvenGaduAgent getAgent(String conversationId) {
        return agents.computeIfAbsent(conversationId, id -> {
            log.info("Creating new InvenGadu agent for conversation: {}", id);
            return createAgent();
        });
    }

    /**
     * Create a new InvenGadu agent with Ollama and tools
     */
    private InvenGaduAgent createAgent() {
        try {
            log.info("Creating Ollama chat model: {} at {}", 
                    aiProperties.getOllamaModel(), 
                    aiProperties.getOllamaBaseUrl());
            
            // Create Ollama chat model
            OllamaChatModel chatModel = OllamaChatModel.builder()
                    .baseUrl(aiProperties.getOllamaBaseUrl())
                    .modelName(aiProperties.getOllamaModel())
                    .temperature(aiProperties.getTemperature())
                    .timeout(Duration.ofSeconds(120))  // 2 minutes - allow enough time for LLM response
                    .build();

            // Create chat memory (last 10 messages)
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            // Create AI service WITHOUT tools (llama3 doesn't support tools natively)
            // We'll use a wrapper that manually calls tools based on intent
            log.info("Building AI service (llama3 - tools will be called manually)...");
            InvenGaduAgent baseAgent = AiServices.builder(InvenGaduAgent.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(chatMemory)
                    .build();
            
            // Wrap in a tool-enabled agent
            log.info("AI agent created successfully");
            return new ToolEnabledAgent(baseAgent, inventoryTools);
        } catch (Exception e) {
            log.error("Failed to create AI agent", e);
            throw new RuntimeException("Failed to initialize AI agent: " + e.getMessage(), e);
        }
    }

    /**
     * Clear conversation memory
     */
    public void clearConversation(String conversationId) {
        InvenGaduAgent agent = agents.get(conversationId);
        if (agent != null) {
            // Note: LangChain4j doesn't provide direct memory clearing,
            // so we recreate the agent
            agents.remove(conversationId);
            log.info("Cleared conversation memory for: {}", conversationId);
        }
    }

    /**
     * Remove agent (cleanup)
     */
    public void removeAgent(String conversationId) {
        agents.remove(conversationId);
        log.info("Removed agent for conversation: {}", conversationId);
    }

    /**
     * Interface for the AI agent
     */
    public interface InvenGaduAgent {
        @SystemMessage("""
            You are InvenGadu, a helpful AI assistant for inventory management.
            Your role is to help users understand their inventory, stock levels, products, and related information.
            
            Guidelines:
            - Be friendly and professional
            - Provide clear and concise answers
            - When showing data, format it in a readable way
            - If you don't know something, say so politely
            - The system will automatically fetch inventory data for you
            - Summarize large datasets instead of listing everything
            
            You help users with:
            - Viewing current inventory levels
            - Checking low stock and out of stock items
            - Getting inventory statistics and summaries
            - Searching for products
            - Viewing product details
            - Checking stock discrepancies
            
            Format responses in a friendly, conversational way.
            """)
        String chat(String userMessage);
    }
    
    /**
     * Wrapper that enables tools for models that don't support them natively
     */
    private static class ToolEnabledAgent implements InvenGaduAgent {
        private final InvenGaduAgent baseAgent;
        private final InventoryTools tools;
        
        public ToolEnabledAgent(InvenGaduAgent baseAgent, InventoryTools tools) {
            this.baseAgent = baseAgent;
            this.tools = tools;
        }
        
        @Override
        public String chat(String userMessage) {
            String lowerMessage = userMessage.toLowerCase();
            
            // Route to appropriate tool based on keywords
            String toolResult = null;
            
            if (containsKeywords(lowerMessage, "low stock", "restock", "reorder", "needs restocking", "below minimum")) {
                toolResult = tools.getLowStock(userMessage);
            } else if (containsKeywords(lowerMessage, "out of stock", "empty", "zero stock", "no inventory")) {
                toolResult = tools.getOutOfStock(userMessage);
            } else if (containsKeywords(lowerMessage, "stats", "statistics", "summary", "overview", "total")) {
                toolResult = tools.getInventoryStats(userMessage);
            } else if (containsKeywords(lowerMessage, "search", "find", "look for", "show me")) {
                // Extract search term
                String searchTerm = extractSearchTerm(userMessage);
                toolResult = tools.searchProducts(searchTerm);
            } else if (lowerMessage.matches(".*product.*id.*\\d+.*") || lowerMessage.matches(".*id.*\\d+.*")) {
                // Extract product ID
                String productId = extractProductId(userMessage);
                toolResult = tools.getProductById(productId);
            } else if (containsKeywords(lowerMessage, "discrepancy", "difference", "issue", "problem")) {
                toolResult = tools.getStockDiscrepancies(userMessage);
            } else if (containsKeywords(lowerMessage, "inventory", "stock", "available", "current")) {
                toolResult = tools.getInventory(userMessage);
            }
            
            // If we found data, enhance it with AI response
            if (toolResult != null && !toolResult.contains("Error")) {
                String enhancedPrompt = String.format(
                    "User asked: %s\n\nHere's the data from the system:\n%s\n\nPlease provide a friendly, conversational response explaining this data.",
                    userMessage, toolResult
                );
                return baseAgent.chat(enhancedPrompt);
            }
            
            // If no specific tool match, just use the base agent
            return baseAgent.chat(userMessage);
        }
        
        private boolean containsKeywords(String text, String... keywords) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
        
        private String extractSearchTerm(String message) {
            // Try to extract search term after "search", "find", etc.
            String[] patterns = {"search for", "find", "show me", "look for"};
            for (String pattern : patterns) {
                int idx = message.toLowerCase().indexOf(pattern);
                if (idx >= 0) {
                    String after = message.substring(idx + pattern.length()).trim();
                    // Remove common words
                    after = after.replaceAll("^(products?|items?|with|containing|called)\\s+", "");
                    if (!after.isEmpty() && after.length() < 100) {
                        return after.split("\\s+")[0]; // Take first word
                    }
                }
            }
            // Fallback: return a cleaned version of the message
            return message.replaceAll("\\b(search|find|show|look|for|me)\\b", "").trim();
        }
        
        private String extractProductId(String message) {
            // Extract first number found
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group();
            }
            return "";
        }
    }
}

