# Running Fully Offline Local Models in Termux with Qwenton Apex

This guide explains how to configure **100% offline, on-device AI inference** using `llama.cpp` and GGUF quantized models running directly on your Android device inside Termux.

---

## 🚀 Why On-Device Local Models?
By default, Qwenton Apex provides deterministic tool execution and can optionally connect to hosted APIs (Gemini, Claude, OpenAI). However, to achieve **absolute privacy and zero data egress**, you can run an optimized small language model (SLM) directly in Termux using ARM64 NEON acceleration.

---

## 🛠️ Step 1: Install Build Tools in Termux

Inside Termux on your Android device:
```bash
pkg update && pkg install -y clang cmake git make
```

---

## ⚙️ Step 2: Build `llama.cpp`

Clone and compile `llama.cpp` optimized for your device's ARM64 CPU:
```bash
cd ~/
git clone --depth 1 https://github.com/ggerganov/llama.cpp
cd llama.cpp
cmake -B build
cmake --build build --config Release -j 4
```
This builds the `llama-server` binary located at `~/llama.cpp/build/bin/llama-server`.

---

## 📦 Step 3: Download a Quantized GGUF Model

Small, modern instruction-tuned models perform remarkably well on Android (consuming ~400MB to 1.2GB RAM):

### Recommended Models:
1. **Qwen 2.5 0.5B Instruct (Ultra-Fast, ~350 MB RAM)**:
   ```bash
   pkg install -y wget
   mkdir -p ~/models && cd ~/models
   wget https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf
   ```

2. **Qwen 2.5 1.5B Instruct (Balanced Reasoning, ~1.1 GB RAM)**:
   ```bash
   wget https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf
   ```

3. **Llama 3.2 1B Instruct (~800 MB RAM)**:
   ```bash
   wget https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf
   ```

---

## 🌐 Step 4: Start the Local Model Server

Run `llama-server` listening on loopback `127.0.0.1:8080`:
```bash
~/llama.cpp/build/bin/llama-server \
    -m ~/models/qwen2.5-0.5b-instruct-q4_k_m.gguf \
    -c 2048 \
    --host 127.0.0.1 \
    --port 8080 \
    -t 4
```

---

## 🔗 Step 5: Configure Qwenton Apex Server

In `~/qwenton-apex-server/.env`:
```env
AI_PROVIDER=local
LOCAL_LLM_URL=http://127.0.0.1:8080/v1
```

Restart your Qwenton Apex server:
```bash
cd ~/qwenton-apex-server
./start.sh
```

Now, every chat interaction, voice prompt, and tool reasoning step executes **entirely on your Android device with zero cloud connectivity**.
