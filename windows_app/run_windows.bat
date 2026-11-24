@echo off
setlocal
cd /d "%~dp0"

echo ==================================================
echo   AI Study Buddy - Windows Launcher
echo ==================================================

where python >nul 2>nul
if errorlevel 1 (
    echo Python was not found in PATH. Please install Python 3.10+ from https://www.python.org/downloads/
    pause
    exit /b 1
)

if not exist ".venv" (
    echo Creating Python virtual environment...
    python -m venv .venv
)

call ".venv\Scripts\activate.bat"

echo Installing / verifying dependencies...
pip install -q -r requirements.txt

if not exist ".env" (
    copy ".env.example" ".env" >nul
    echo.
    echo [ACTION REQUIRED] A .env file was created in windows_app\.env.
    echo Please paste your Gemini API key into windows_app\.env before generating a revision pack.
    echo.
)

echo Starting AI Study Buddy in your Windows browser...
streamlit run app.py
pause
