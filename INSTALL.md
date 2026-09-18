# Установка PumbaNET Client

## 📥 Быстрая установка

### Вариант 1: Скачать готовый APK

1. Перейдите на страницу [Releases](https://github.com/Fortumers/pumbanet-client/releases)
2. Скачайте последний `PumbaNET-v1.0.0.apk`
3. Откройте файл на Android устройстве
4. Разрешите установку из неизвестных источников
5. Нажмите "Установить"

### Вариант 2: Сборка из исходников

#### Требования
- Android Studio Arctic Fox или новее
- JDK 17
- Android SDK 34

#### Инструкция

1. **Клонируйте репозиторий**
   ```bash
   git clone https://github.com/Fortumers/pumbanet-client.git
   cd pumbanet-client
   ```

2. **Создайте keystore для подписи**
   ```bash
   cd keystore
   keytool -genkey -v -keystore release-key.jks -alias pumbanet -keyalg RSA -keysize 2048 -validity 9125
   ```

3. **Настройте переменные окружения**
   ```bash
   export KEYSTORE_PASSWORD="ваш_пароль"
   export KEY_ALIAS="pumbanet"
   export KEY_PASSWORD="ваш_пароль_ключа"
   ```

4. **Соберите APK**
   ```bash
   ./gradlew assembleRelease
   ```

5. **Найдите APK**
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

## 📱 Установка на устройство

### Шаг 1: Разрешите установку из неизвестных источников

**Android 8+**:
1. Настройки → Приложения → Специальный доступ → Установка неизвестных приложений
2. Выберите ваш браузер или файловый менеджер
3. Включите "Разрешить из этого источника"

**Android 7 и ниже**:
1. Настройки → Безопасность
2. Включите "Неизвестные источники"

### Шаг 2: Установите APK

1. Откройте файловый менеджер
2. Найдите скачанный `PumbaNET-v1.0.0.apk`
3. Нажмите на файл
4. Нажмите "Установить"
5. Дождитесь завершения

### Шаг 3: Запустите приложение

1. Найдите иконку PumbaNET на главном экране
2. Нажмите для запуска
3. Предоставьте необходимые разрешения

## 🔧 Сборка разных версий

### Debug версия (для тестирования)

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

### Release версия (продакшен)

```bash
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

### Universal APK (все архитектуры)

```bash
./gradlew assembleReleaseUniversal
```

APK: `app/build/outputs/apk/release/app-release-universal.apk`

### Split APKs (по архитектуре)

```bash
./gradlew assembleRelease
```

APK:
- `app-armeabi-v7a-release.apk` (32-bit ARM)
- `app-arm64-v8a-release.apk` (64-bit ARM)
- `app-x86-release.apk` (32-bit Intel)
- `app-x86_64-release.apk` (64-bit Intel)

## 📋 Требования к устройству

- **Android**: 8.0+ (API 26)
- **RAM**: 2GB+
- **Место**: 50MB+
- **Архитектура**: ARM64 (рекомендуется)

## ❓ Troubleshooting

### Ошибка "App not installed"

**Причина**: Конфликт с предыдущей версией

**Решение**:
1. Удалите старую версию PumbaNET
2. Перезагрузите устройство
3. Установите заново

### Ошибка "Parse error"

**Причина**: Неполная загрузка APK

**Решение**:
1. Скачайте APK заново
2. Проверьте размер файла (должен быть ~15-20MB)

### Ошибка "Insufficient storage"

**Причина**: Недостаточно места

**Решение**:
1. Освободите минимум 100MB
2. Очистите кэш
3. Удалите ненужные приложения

## 📞 Поддержка

Возникли проблемы? Создайте issue:
https://github.com/Fortumers/pumbanet-client/issues
