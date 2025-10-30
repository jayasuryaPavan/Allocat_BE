from pydantic_settings import BaseSettings
from typing import Optional, List

class Settings(BaseSettings):
    # Backend API Configuration
    backend_url: str = "http://localhost:8080/api"
    backend_timeout: int = 30
    
    # Ollama Configuration
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "llama3"
    
    # FastAPI Configuration
    api_host: str = "0.0.0.0"
    api_port: int = 8000
    api_reload: bool = True
    
    # CORS Configuration
    cors_origins: List[str] = [
        "http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:8081",
    ]
    
    # Security (optional - for future authentication)
    api_key: Optional[str] = None
    
    # Conversation Settings
    max_conversation_history: int = 10
    enable_memory: bool = True
    
    class Config:
        env_file = ".env"
        case_sensitive = False

settings = Settings()

