# InvenGadu Backend - Setup Guide

AI-powered inventory management assistant that integrates with your Spring Boot backend using LangChain and Ollama.

## Prerequisites

1. **Python 3.8+** (recommended: Python 3.10 or higher)
2. **Ollama** - Local LLM runtime (see installation below)
3. **Spring Boot Backend** - Running on `http://localhost:8080/api`
4. **Virtual Environment** (recommended)

## Installation Steps

### 1. Install Ollama

#### Windows Installation:

**Option A: Using Installer (Recommended)**
1. Download Ollama from: https://ollama.ai/download
2. Run the installer (OllamaSetup.exe)
3. After installation, Ollama will automatically start as a service
4. Verify installation:
   ```powershell
   ollama --version
   ```

**Option B: Using Winget (if available)**
```powershell
winget install Ollama.Ollama
```

**Option C: Manual Download**
1. Download from: https://github.com/ollama/ollama/releases
2. Extract and add to PATH
3. Run `ollama serve` manually

#### Pull Required Model:
After installing Ollama, download the model:
```powershell
ollama pull llama3
```

Or use a lighter model if you have limited resources:
```powershell
ollama pull llama3.2:1b    # Smaller, faster model
ollama pull llama3:8b      # Medium model (default)
```

### 2. Setup Python Environment

```powershell
# Navigate to project directory
cd inven_gadu_backend

# Create virtual environment
python -m venv venv

# Activate virtual environment
.\venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

### 3. Configure Environment Variables

```powershell
# Copy example file
copy env.example .env

# Edit .env file with your settings
notepad .env
```

**Important Settings in `.env`:**
- `BACKEND_URL`: Your Spring Boot API URL (default: `http://localhost:8080/api`)
- `OLLAMA_BASE_URL`: Ollama server URL (default: `http://localhost:11434`)
- `OLLAMA_MODEL`: Model name (default: `llama3`)

### 4. Verify Spring Boot Backend is Running

Ensure your Spring Boot backend is running and accessible:
```powershell
# Test backend connection
curl http://localhost:8080/api/health
```

Or visit in browser: `http://localhost:8080/api/health`

### 5. Start Ollama (if not running as service)

```powershell
# Start Ollama server
ollama serve
```

Keep this terminal open. Ollama should be running on `http://localhost:11434`

### 6. Start InvenGadu Backend

```powershell
# Make sure virtual environment is activated
.\venv\Scripts\activate

# Start FastAPI server
python main.py
```

The API will be available at: `http://localhost:8000`

## Testing the Setup

### 1. Health Check

```powershell
# Test InvenGadu backend
curl http://localhost:8000/health

# Expected response:
# {
#   "status": "healthy",
#   "ollama": "connected",
#   "backend_url": "http://localhost:8080/api",
#   "model": "llama3"
# }
```

### 2. Test Chat Endpoint

```powershell
# Test chat functionality
curl -X POST http://localhost:8000/chat `
  -H "Content-Type: application/json" `
  -d '{\"message\": \"Show me inventory stats\"}'
```

### 3. Visit API Documentation

Open in browser: `http://localhost:8000/docs`

FastAPI automatically provides interactive API documentation.

## Troubleshooting

### Ollama Not Found

**Problem:** `ollama: command not found`

**Solutions:**
1. Ensure Ollama is installed and added to PATH
2. Restart terminal/PowerShell after installation
3. Check if Ollama service is running:
   ```powershell
   Get-Service | Where-Object {$_.Name -like "*ollama*"}
   ```

### Ollama Connection Error

**Problem:** Cannot connect to Ollama at `http://localhost:11434`

**Solutions:**
1. Start Ollama manually: `ollama serve`
2. Check if port 11434 is in use:
   ```powershell
   netstat -ano | findstr :11434
   ```
3. Verify Ollama is running:
   ```powershell
   curl http://localhost:11434/api/tags
   ```

### Backend Connection Error

**Problem:** Cannot connect to Spring Boot backend

**Solutions:**
1. Verify Spring Boot is running: `http://localhost:8080/api/health`
2. Check `BACKEND_URL` in `.env` file
3. Ensure CORS is configured in Spring Boot (if calling from browser)

### Model Not Found

**Problem:** Ollama model `llama3` not found

**Solutions:**
1. Pull the model: `ollama pull llama3`
2. List available models: `ollama list`
3. Update `OLLAMA_MODEL` in `.env` to use an available model

### Python Dependencies Error

**Problem:** Import errors or missing packages

**Solutions:**
1. Ensure virtual environment is activated
2. Reinstall dependencies:
   ```powershell
   pip install --upgrade -r requirements.txt
   ```
3. Check Python version: `python --version` (should be 3.8+)

## Alternative: Testing Without Ollama (Development)

If you want to test the API integration without Ollama temporarily, you can:

1. Modify `inven_agent.py` to use a mock LLM
2. Or use OpenAI API instead (requires API key)
3. Or skip the LLM and return direct API responses

For production use, Ollama is recommended for local, privacy-focused AI.

## Project Structure

```
inven_gadu_backend/
├── config.py           # Configuration settings
├── tools.py            # LangChain tools for Spring Boot API
├── inven_agent.py      # LangChain agent with Ollama
├── main.py             # FastAPI application
├── requirements.txt    # Python dependencies
├── env.example         # Environment variables template
└── .env               # Your local configuration (create from env.example)
```

## API Endpoints

- `GET /` - API information
- `GET /health` - Health check (tests Ollama and backend connections)
- `POST /chat` - Main chat endpoint for inventory queries
- `POST /chat/new` - Start a new conversation

## Usage Examples

### Chat with Inventory Assistant

```powershell
# Ask about inventory
curl -X POST http://localhost:8000/chat `
  -H "Content-Type: application/json" `
  -d '{\"message\": \"What items are low on stock?\"}'

# With authentication token
curl -X POST http://localhost:8000/chat `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN" `
  -d '{\"message\": \"Show me inventory summary\"}'
```

### Start New Conversation

```powershell
curl -X POST http://localhost:8000/chat/new `
  -H "Content-Type: application/json" `
  -d '{\"conversation_id\": \"user123\"}'
```

## Next Steps

1. Install Ollama and pull the model
2. Start your Spring Boot backend
3. Configure `.env` file
4. Start InvenGadu backend
5. Test the `/health` endpoint
6. Begin chatting with your inventory assistant!

For more information, visit:
- Ollama: https://ollama.ai
- LangChain: https://python.langchain.com
- FastAPI: https://fastapi.tiangolo.com

