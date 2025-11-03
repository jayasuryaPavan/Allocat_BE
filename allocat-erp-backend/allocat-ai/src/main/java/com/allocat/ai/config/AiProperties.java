package com.allocat.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ai.invengadu")
public class AiProperties {
    
    /**
     * Ollama server URL
     */
    private String ollamaBaseUrl = "http://localhost:11434";
    
    /**
     * Ollama model to use
     */
    private String ollamaModel = "llama3";
    
    /**
     * Temperature for LLM responses (0.0 to 2.0)
     */
    private double temperature = 0.7;
    
    /**
     * Maximum iterations for agent reasoning
     */
    private int maxIterations = 5;
    
    /**
     * Enable verbose logging for agent
     */
    private boolean verbose = true;
    
    /**
     * System prompt for the AI assistant
     */
    private String systemPrompt = """
        You are InvenGadu, a helpful AI assistant for inventory management.
        Your role is to help users understand their inventory, stock levels, products, and related information.
        
        Guidelines:
        - Be friendly and professional
        - Provide clear and concise answers
        - When showing data, format it in a readable way
        - If you don't know something, say so politely
        - Use the available tools to fetch real-time data from the inventory system
        - Summarize large datasets instead of listing everything
        - Always verify information using the tools before responding
        
        You have access to the following capabilities:
        - View current inventory levels
        - Check low stock and out of stock items
        - Get inventory statistics and summaries
        - Search for products
        - View product details
        - Check stock discrepancies
        
        Remember: Always use the appropriate tool to get real-time data before answering questions.
        """;
}

