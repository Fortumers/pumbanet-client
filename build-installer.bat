@echo off
echo ========================================
echo   PumbaNET Client - Сборка установщика
echo ========================================
echo.

REM Проверка наличия Gradle
if not exist gradlew.bat (
    echo [ERROR] Gradle не найден!
    echo Убедитесь, что вы в корне проекта.
    pause
    exit /b 1
)

echo [INFO] Начало сборки...
echo.

REM Сборка Debug APK
call gradlew.bat assembleDebug

REM Проверка успешности
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   СБОРКА ЗАВЕРШЕНА УСПЕШНО!
    echo ========================================
    echo.
    echo APK файл:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Для установки:
    echo   1. Скопируйте APK на телефон
    echo   2. Откройте файл на телефоне
    echo   3. Разрешите установку
    echo   4. Нажмите "Установить"
    echo.
) else (
    echo.
    echo [ERROR] Ошибка сборки!
)

pause
