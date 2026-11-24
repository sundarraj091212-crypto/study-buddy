# AI Study Buddy – Windows Web Launcher

Run **AI Study Buddy** directly on Windows in your web browser.

## Quick Launch (Windows)

1. Export/download this project folder to your Windows PC.
2. Open `windows_app/` and copy `.env.example` to `.env`, then set your key:
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key
   ```
   (Get a key from Google AI Studio: https://aistudio.google.com/apikey)
3. Double-click **`run_windows.bat`**, or run manually in PowerShell / Command Prompt:
   ```bat
   cd windows_app
   python -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt
   streamlit run app.py
   ```
4. Your default Windows browser will open at `http://localhost:8501`.
