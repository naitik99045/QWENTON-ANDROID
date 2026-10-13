import json
from contextlib import asynccontextmanager
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from app import config
from app.routes_health import router as health_router
from app.routes_chat import router as chat_router
from app.routes_commands import router as commands_router
from app.routes_history import router as history_router
from app.audit_log import log_audit_event

# Active WebSocket connections
active_connections: list[WebSocket] = []

async def broadcast_event(event_dict: dict):
    """Broadcast JSON event to all connected WebSockets."""
    dead_connections = []
    text = json.dumps(event_dict)
    for conn in active_connections:
        try:
            await conn.send_text(text)
        except Exception:
            dead_connections.append(conn)
    for dead in dead_connections:
        if dead in active_connections:
            active_connections.remove(dead)

@asynccontextmanager
async def lifespan(app: FastAPI):
    log_audit_event("server_startup", {
        "host": config.HOST,
        "port": config.PORT,
        "provider": config.AI_PROVIDER
    })
    yield
    log_audit_event("server_shutdown", {})

app = FastAPI(
    title="Qwenton Apex Local Server",
    description="Local privacy-first AI execution gateway for Android devices.",
    version="0.1.0",
    lifespan=lifespan
)

# CORS configuration restricted strictly to local loopback to prevent browser CSRF
ALLOWED_ORIGINS = [
    "http://127.0.0.1",
    "http://127.0.0.1:8765",
    "http://localhost",
    "http://localhost:8765"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# Include Subsystem Routers
app.include_router(health_router)
app.include_router(chat_router)
app.include_router(commands_router)
app.include_router(history_router)

# WebSocket Endpoint: /ws/events
@app.websocket("/ws/events")
async def websocket_events(websocket: WebSocket):
    origin = websocket.headers.get("origin")
    if origin and origin not in ALLOWED_ORIGINS and not origin.startswith("http://127.0.0.1") and not origin.startswith("http://localhost"):
        await websocket.close(code=1008, reason="Forbidden origin")
        log_audit_event("websocket_rejected_untrusted_origin", {"origin": origin})
        return

    await websocket.accept()
    active_connections.append(websocket)
    log_audit_event("websocket_client_connected", {})
    
    # Send connection greeting
    await websocket.send_json({"type": "connected"})
    
    try:
        while True:
            # Keep connection open, handle client keepalive or messages
            data = await websocket.receive_text()
            try:
                msg = json.loads(data)
                if msg.get("type") == "ping":
                    await websocket.send_json({"type": "pong"})
            except Exception:
                pass
    except WebSocketDisconnect:
        if websocket in active_connections:
            active_connections.remove(websocket)
        log_audit_event("websocket_client_disconnected", {})
