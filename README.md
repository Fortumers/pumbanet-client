# PumbaNET Client

VPN-клиент для сервиса PumbaNET на протоколе Vless с интеграцией Remnawave API.

## Описание

Приложение предоставляет безопасное подключение к серверам PumbaNET через протокол Vless с поддержкой:
- Импорт конфигов через QR-код или ссылку vless://
- Интеграция с Remnawave API для автоматического получения подписок
- Фоновый VPN-сервис на базе Xray-core

## Особенности

- ✅ Поддержка протокола Vless
- ✅ Сканирование QR-кодов конфигов
- ✅ Импорт из ссылки vless://
- ✅ Интеграция с Remnawave API
- ✅ Автоматическая авторизация
- ✅ Отображение трафика подписки
- ✅ Минималистичный UI
- ✅ Фоновый VPN-сервис

## Структура проекта

```
pumbanet-client/
├── app/
│   ├── src/main/
│   │   ├── java/com/pumbanet/client/
│   │   │   ├── MainActivity.kt          # Главный экран
│   │   │   ├── LoginActivity.kt         # Экран входа (Remnawave)
│   │   │   ├── ImportConfigActivity.kt  # Импорт конфига (QR/vless)
│   │   │   ├── VpnService.kt            # VPN-сервис
│   │   │   └── RemnawaveApi.kt          # API клиент Remnawave
│   │   ├── res/layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_main_updated.xml
│   │   │   └── activity_import_config.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── config/
│   └── vless-config-example.json
└── README.md
```

## Требования

- Android 8.0+ (API 26)
- Android Studio Arctic Fox+
- Xray-core binary (ARM64)
- Remnawave Panel 3.0+ (для API интеграции)

## Сборка

### 1. Клонирование

```bash
git clone https://github.com/Fortumers/pumbanet-client.git
cd pumbanet-client
```

### 2. Добавление Xray-core

Скачайте xray-core для Android (ARM64) и поместите в:
```
app/src/main/assets/xray
```

Или интегрируйте через JNI/библиотеку.

### 3. Сборка APK

```bash
./gradlew assembleDebug
```

APK будет в: `app/build/outputs/apk/debug/app-debug.apk`

## Настройка Remnawave API

### 1. Создание API токена

В панели Remnawave:
1. Откройте **Settings → API Tokens**
2. Нажмите **Create Token**
3. Скопируйте токен

### 2. Вход в приложение

1. Запустите приложение
2. Введите URL вашей панели (например, `https://panel.pumbanet.local`)
3. Вставьте API токен
4. Нажмите **Войти**

### 3. Импорт конфига

После входа:
- Нажмите **Импортировать конфиг**
- Отсканируйте QR-код или вставьте ссылку `vless://`
- Конфиг сохранится и будет использоваться для подключения

## API Endpoints (Remnawave)

Приложение использует следующие endpoints:

| Endpoint | Метод | Описание |
|----------|-------|----------|
| `/api/user` | GET | Информация о пользователе |
| `/api/subscriptions` | GET | Список подписок |
| `/api/subscriptions/{id}/config` | GET | Конфиг подписки |

## Технологии

- **Kotlin** — основной язык
- **AndroidX** — современные компоненты Android
- **Kotlin Coroutines** — асинхронные запросы к API
- **ZXing** — сканирование QR-кодов
- **Xray-core** — ядро VPN (Vless protocol)

## Лицензия

MIT

## Контакты

Владелец: jurnest resterr (Fortumers)  
Сервис: PumbaNET (Vless + Remnawave)  
Локация: Helsinki, Finland

## Скриншоты

*Экран входа через Remnawave API*
*Главный экран с кнопкой подключения*
*Экран импорта конфига (QR/vless://)*

---

**Примечание:** Для работы требуется настроенный сервер PumbaNET с панелью Remnawave.
