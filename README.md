# PumbaNET Client

VPN-клиент для сервиса PumbaNET на протоколе Vless.

## Описание

Приложение предоставляет безопасное подключение к серверам PumbaNET через протокол Vless с поддержкой TLS/Reality.

## Особенности

- Поддержка протокола Vless
- Интеграция с Xray-core
- Минималистичный UI
- Фоновый VPN-сервис

## Структура проекта

```
pumbanet-client/
├── app/
│   ├── src/main/
│   │   ├── java/com/pumbanet/client/
│   │   │   ├── MainActivity.kt
│   │   │   └── VpnService.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── config/
│   └── vless-config-example.json
└── README.md
```

## Требования

- Android 8.0+
- Android Studio Arctic Fox+
- Xray-core binary

## Сборка

1. Откройте проект в Android Studio
2. Добавьте xray-core binary в `app/src/main/assets/`
3. Соберите проект: `./gradlew assembleDebug`

## Лицензия

MIT

## Контакты

Владелец: jurnest resterr (Fortumers)
Сервис: PumbaNET (Vless + Remnawave)
