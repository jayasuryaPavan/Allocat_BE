from langchain_community.llms import Ollama
from langchain.agents import initialize_agent, AgentType
from langchain.memory import ConversationBufferMemory
from tools import SpringBootTools
from config import settings
from typing import Optional, Dict
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class InvenGaduAgent:
    """
    LangChain agent that uses Ollama and Spring Boot tools
    to answer inventory-related questions.
    """
    
    def __init__(self, auth_token: Optional[str] = None):
        # Initialize Ollama LLM
        self.llm = Ollama(
            base_url=settings.ollama_base_url,
            model=settings.ollama_model,
            temperature=0.7
        )
        
        # Initialize tools
        self.tools_instance = SpringBootTools(auth_token=auth_token)
        self.tools = self.tools_instance.get_tools()
        
        # Initialize memory for conversation context
        self.memory = ConversationBufferMemory(
            memory_key="chat_history",
            return_messages=True,
            output_key="output"
        )
        
        # Initialize agent
        self.agent = initialize_agent(
            tools=self.tools,
            llm=self.llm,
            agent=AgentType.CONVERSATIONAL_REACT_DESCRIPTION,
            memory=self.memory,
            verbose=True,
            handle_parsing_errors=True,
            max_iterations=5,
            return_intermediate_steps=False
        )
        
        # System prompt for the agent
        self.system_prompt = """You are InvenGadu, a helpful AI assistant for inventory management.
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
        
        Remember: Always use the appropriate tool to get real-time data before answering questions."""
    
    async def process_message(
        self,
        message: str,
        conversation_id: Optional[str] = None
    ) -> Dict[str, any]:
        """
        Process a user message and return a response.
        
        Args:
            message: User's message/question
            conversation_id: Optional conversation ID for tracking
            
        Returns:
            Dictionary with message, conversation_id, and metadata
        """
        try:
            # Add system context to the message
            full_message = f"{self.system_prompt}\n\nUser: {message}\nAssistant:"
            
            # Run the agent
            logger.info(f"Processing message: {message[:100]}...")
            response = self.agent.run(input=full_message)
            
            # Clean up the response
            if response:
                response = response.replace(self.system_prompt, "").strip()
                if response.startswith("Assistant:"):
                    response = response.replace("Assistant:", "").strip()
            
            logger.info(f"Agent response generated successfully")
            
            return {
                "message": response or "I apologize, but I couldn't generate a response. Please try rephrasing your question.",
                "conversation_id": conversation_id or "default",
                "metadata": {
                    "model": settings.ollama_model,
                    "tools_used": True
                }
            }
            
        except Exception as e:
            logger.error(f"Error processing message: {str(e)}", exc_info=True)
            return {
                "message": f"I apologize, but I encountered an error while processing your request: {str(e)}. Please try again or rephrase your question.",
                "conversation_id": conversation_id,
                "metadata": {
                    "error": str(e),
                    "model": settings.ollama_model
                }
            }
    
    def start_new_conversation(self):
        """Start a new conversation by clearing memory"""
        self.memory.clear()
        logger.info("Conversation memory cleared")
    
    def set_auth_token(self, token: str):
        """Update authentication token for API calls"""
        self.tools_instance.auth_token = token
        if token:
            self.tools_instance.headers["Authorization"] = f"Bearer {token}"
        logger.info("Authentication token updated")

