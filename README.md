# PumbaNET Client

VPN-клиент для сервиса PumbaNET на протоколе Vless с поддержкой Remnawave API.

## Описание

Приложение предоставляет безопасное подключение к серверам PumbaNET через протокол Vless с поддержкой TLS/Reality. Включает импорт конфигов через QR-код, ссылки vless:// и автоматическую авторизацию через Remnawave API.

## Особенности

- ✅ Поддержка протокола Vless
- ✅ Интеграция с Xray-core
- ✅ Импорт конфига через QR-код
- ✅ Импорт по ссылке vless://
- ✅ Авторизация через Remnawave API
- ✅ Обработка deep links (vless://)
- ✅ Минималистичный UI
- ✅ Фоновый VPN-сервис

## Структура проекта

```
pumbanet-client/
├── app/
│   ├── src/main/
│   │   ├── java/com/pumbanet/client/
│   │   │   ├── MainActivity.kt              # Главный экран
│   │   │   ├── VpnService.kt                # VPN сервис
│   │   │   ├── ConfigImportActivity.kt      # Импорт конфига
│   │   │   ├── RemnawaveLoginActivity.kt    # Авторизация
│   │   │   └── RemnawaveApiClient.kt        # API клиент
│   │   ├── res/layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_config_import.xml
│   │   │   └── activity_remnawave_login.xml
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

## Сборка

1. Откройте проект в Android Studio
2. Добавьте xray-core binary в `app/src/main/assets/xray`
3. Настройте API endpoint в `RemnawaveApiClient.kt`:
   ```kotlin
   private val baseUrl = "https://pumbanet.yourdomain.com/api/v1"
   ```
4. Соберите проект: `./gradlew assembleDebug`

## Настройка Remnawave API

1. В файле `RemnawaveApiClient.kt` укажите ваш API endpoint
2. API должен поддерживать endpoints:
   - `GET /api/v1/user/config` — получение конфига пользователя
3. Авторизация через Bearer токен

## Импорт конфига

Приложение поддерживает три способа импорта:

### 1. QR-код
- Нажмите "Импорт конфига" → "Сканировать QR-код"
- Наведите камеру на QR-код с ссылкой vless://

### 2. Ссылка vless://
- Нажмите "Импорт конфига" → "Вставить ссылку vless://"
- Вставьте ссылку в формате:
  ```
  vless://uuid@host:port?encryption=none&security=tls#name
  ```

### 3. Remnawave API
- Нажмите "Импорт конфига" → "Войти через Remnawave"
- Авторизуйтесь на странице PumbaNET
- Конфиг загрузится автоматически

### 4. Deep link из браузера
- Откройте ссылку vless:// в браузере
- Приложение откроется автоматически с импортом

## Лицензия

MIT

## Контакты

Владелец: jurnest resterr (Fortumers)  
Сервис: PumbaNET (Vless + Remnawave)

## Скриншоты

- Главный экран с кнопкой подключения
- Экран импорта конфига (QR, ссылка, API)
- WebView авторизации Remnawave
