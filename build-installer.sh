#!/bin/bash

# PumbaNET Client - Automatic Installer Builder
# Этот скрипт автоматически соберёт APK для установки

echo "🚀 PumbaNET Client - Сборка установщика"
echo "========================================"

# Проверка наличия Gradle
if ! command -v ./gradlew &> /dev/null; then
    echo "❌ Gradle не найден. Убедитесь, что вы в корне проекта."
    exit 1
fi

# Сборка Debug APK (без подписи, для быстрой установки)
echo "📦 Сборка debug APK..."
./gradlew assembleDebug

# Проверка успешности сборки
if [ $? -eq 0 ]; then
    echo "✅ Сборка завершена успешно!"
    echo ""
    echo "📱 APK файл находится здесь:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📥 Для установки:"
    echo "   1. Скопируйте APK на Android устройство"
    echo "   2. Откройте файл на устройстве"
    echo "   3. Разрешите установку из неизвестных источников"
    echo "   4. Нажмите 'Установить'"
    echo ""
    echo "🎉 Готово!"
else
    echo "❌ Ошибка сборки!"
    exit 1
fi
