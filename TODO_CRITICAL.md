# 🔴 Критичные TODO для рабочего VPN

## Выполнено ✅

- [x] Исправлена TUN ↔ Xray связка
- [x] Переписан VlessLinkParser с полной поддержкой (reality, xhttp, ws, grpc)
- [x] Убрано дублирование парсеров
- [x] ConfigManager — единый источник правды
- [x] EncryptedSharedPreferences для токенов

## Осталось сделать 🚧

### Приоритет 1 (критично)

- [ ] **Интегрировать Xray TUN inbound**
  - Файл: `VpnService.kt`
  - Задача: Xray должен слушать TUN fd, а не запускаться отдельно
  - Решение: Использовать `xray-core` с параметром `-fd` или через JNI

- [ ] **Добавить xray-core binary в assets**
  - Путь: `app/src/main/assets/xray`
  - Архитектура: arm64-v8a (основная), armeabi-v7a, x86_64
  - Источник: https://github.com/XTLS/Xray-core/releases

- [ ] **Исправить MainActivity**
  - Использовать `ConfigManager` вместо `getSharedPreferences`
  - Использовать `VlessLinkParser.parse()` вместо своего парсера

- [ ] **Удалить QRScannerActivity**
  - Дублирует `JourneyApps` в `ImportConfigActivity`
  - Убрать из `AndroidManifest.xml`

### Приоритет 2 (архитектура)

- [ ] **Исправить lifecycle VpnService**
  - Foreground first → потом Xray
  - Сохранение конфига в `onStartCommand()` для восстановления

- [ ] **Обработка intent == null**
  - В `onStartCommand()` проверять и восстанавливать из `ConfigManager`

- [ ] **Добавить логирование**
  - Логировать все этапы подключения
  - Export logs для отладки

### Приоритет 3 (улучшения)

- [ ] **PumbaNET Client архитектура**
  - Разделить: PumbaNET Client ↔ Remnawave
  - Пользователь получает client ID, а не API token
  - Централизованное управление: отзыв, update, блокировка

- [ ] **UI улучшения**
  - Список серверов с пингом
  - Статистика трафика в реальном времени
  - Индикатор подписки

- [ ] **Тестирование**
  - Протестировать с разными типами конфигов:
    - VLESS TCP
    - VLESS + TLS
    - VLESS + REALITY
    - VLESS + WebSocket
    - VLESS + gRPC
    - VLESS + xhttp

## Тестовые конфиги

### VLESS TCP (базовый)
```
vless://UUID@server:443?encryption=none#Test
```

### VLESS + TLS
```
vless://UUID@server:443?encryption=none&security=tls&sni=server.com#Test
```

### VLESS + REALITY
```
vless://UUID@server:443?encryption=none&security=reality&pbk=PUBLIC_KEY&sid=SHORT_ID&sni=server.com&fp=chrome#Test
```

### VLESS + WebSocket + TLS
```
vless://UUID@server:443?encryption=none&security=tls&type=ws&path=/path&host=server.com#Test
```

### VLESS + xhttp + REALITY
```
vless://UUID@server:443?encryption=none&security=reality&type=xhttp&pbk=KEY&sid=ID&sni=server.com&fp=chrome&path=/path&host=server.com&mode=auto#Test
```

## Следующие шаги

1. **Собрать APK** с исправлениями
2. **Протестировать** на реальном устройстве
3. **Проверить** прохождение трафика через TUN
4. **Добавить** xray-core binary
5. **Финальное тестирование** всех типов конфигов
