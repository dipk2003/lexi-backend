@echo off
echo Starting compilation with javac...

REM Create output directory
if not exist "target\classes" mkdir "target\classes"

REM Find all Java files
dir /s /b *.java > sources.txt

echo Compiling Java files...
javac -cp "lib/*" -d "target/classes" @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)

del sources.txt
pause
