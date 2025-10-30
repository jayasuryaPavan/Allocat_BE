from fastapi import FastAPI, HTTPException, Header, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
from inven_agent import InvenGaduAgent
from config import settings
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="InvenGadu API",
    description="AI-powered inventory management assistant",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global agent instance (per conversation)
agents: dict[str, InvenGaduAgent] = {}

def get_agent(conversation_id: Optional[str] = None) -> InvenGaduAgent:
    """Get or create agent for a conversation"""
    if not conversation_id:
        conversation_id = "default"
    
    if conversation_id not in agents:
        agents[conversation_id] = InvenGaduAgent()
    
    return agents[conversation_id]

# Request/Response Models
class ChatRequest(BaseModel):
    message: str
    conversation_id: Optional[str] = None

class ChatResponse(BaseModel):
    message: str
    conversation_id: Optional[str] = None
    metadata: Optional[dict] = None

# Health Check Endpoint
@app.get("/health")
async def health_check():
    """Health check endpoint"""
    try:
        # Test Ollama connection
        from langchain_community.llms import Ollama
        llm = Ollama(base_url=settings.ollama_base_url, model=settings.ollama_model)
        llm("test")
        
        return {
            "status": "healthy",
            "ollama": "connected",
            "backend_url": settings.backend_url,
            "model": settings.ollama_model
        }
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        return {
            "status": "unhealthy",
            "error": str(e)
        }

# Chat Endpoint
@app.post("/chat", response_model=ChatResponse)
async def chat(
    request: ChatRequest,
    authorization: Optional[str] = Header(None)
):
    """
    Main chat endpoint for InvenGadu.
    
    Accepts a user message and returns an AI-generated response.
    """
    try:
        # Extract auth token if present
        auth_token = None
        if authorization and authorization.startswith("Bearer "):
            auth_token = authorization.replace("Bearer ", "")
        
        # Get or create agent for this conversation
        agent = get_agent(request.conversation_id)
        
        # Update auth token if provided
        if auth_token:
            agent.set_auth_token(auth_token)
        
        # Process the message
        response = await agent.process_message(
            message=request.message,
            conversation_id=request.conversation_id
        )
        
        return ChatResponse(
            message=response["message"],
            conversation_id=response.get("conversation_id"),
            metadata=response.get("metadata")
        )
        
    except Exception as e:
        logger.error(f"Error in chat endpoint: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process message: {str(e)}"
        )

# New Conversation Endpoint
@app.post("/chat/new")
async def new_conversation(conversation_id: Optional[str] = None):
    """Start a new conversation by clearing memory"""
    try:
        agent = get_agent(conversation_id or "default")
        agent.start_new_conversation()
        
        return {
            "success": True,
            "message": "New conversation started",
            "conversation_id": conversation_id or "default"
        }
    except Exception as e:
        logger.error(f"Error starting new conversation: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# Root endpoint
@app.get("/")
async def root():
    return {
        "service": "InvenGadu API",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "chat": "/chat",
            "new_conversation": "/chat/new"
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.api_host,
        port=settings.api_port,
        reload=settings.api_reload
    )

