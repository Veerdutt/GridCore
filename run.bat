@echo off
if not exist out mkdir out
javac -d out src\*.java src\warehouse\*.java src\pathfinding\*.java src\routing\*.java src\dispatch\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
java -cp out Main
pause
