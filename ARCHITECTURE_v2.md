# PumbaNET Client v2 — Архитектура

## 🏗 Правильная архитектура

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│  (MainActivity, Fragments, ViewModels)                  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              VPN State Machine                           │
│  (Disconnected → Connecting → Connected → Error)        │
└─────────────────────────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│    VPN Service          │   │   PumbaNET API          │
│  (TUN Manager)          │   │  (Subscriptions)        │
└─────────────────────────┘   └─────────────────────────┘
            │                               │
            ▼                               ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│   Xray Controller       │   │   Secure Storage        │
│  (Binary + Config)      │   │  (Keystore + Encrypted) │
└─────────────────────────┘   └─────────────────────────┘
            │                               │
            ▼                               ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│   Xray-core             │   │   VLESS Parser          │
│  (TUN inbound)          │   │  (Full parameter supp.) │
└─────────────────────────┘   └─────────────────────────┘
            │
            ▼
┌─────────────────────────┐
│   PumbaNET Node         │
│  (VLESS outbound)       │
└─────────────────────────┘
```

## 📦 Компоненты

### 1. VPN State Machine (`VpnState.kt`)
- **Disconnected** — VPN отключен
- **Preparing** — Проверка конфига, binary, разрешений
- **RequestingPermission** — Запрос VPN permission
- **CreatingTun** — Создание TUN интерфейса
- **StartingXray** — Запуск Xray
- **ValidatingConfig** — Проверка конфига Xray
- **Connecting** — Соединение с сервером
- **Connected** — VPN полностью подключен
- **Disconnecting** — Корректное завершение
- **Error** — Ошибка подключения/работы
- **Reconnecting** — Автоматическое переподключение

### 2. TUN Manager (`TunManager.kt`)
- Создаёт Android TUN интерфейс
- Настраивает маршрутизацию (0.0.0.0/0)
- Поддержка split tunneling
- Поддержка Kill Switch
- Передаёт TUN fd в Xray

### 3. Xray Controller (`XrayController.kt`)
- Копирование binary из assets
- Установка chmod 700
- Проверка binary
- Валидация конфига (`xray run -test`)
- Запуск с TUN fd
- Мониторинг процесса
- Корректная остановка (destroy + waitFor)

### 4. VLESS Parser (`VlessParser.kt`)
- UUID validation
- Host/port validation
- Полная поддержка параметров:
  - security: none, tls, reality
  - type: tcp, ws, http, h2, grpc, xhttp
  - reality: pbk, sid, sni, fp, spx
  - transport: path, host, serviceName, mode
- Генерация полного Xray JSON конфига

### 5. Secure Storage (`SecureStorage.kt`)
- Android Keystore
- EncryptedSharedPreferences
- Шифрование:
  - API токенов
  - VLESS конфигов
  - Credential'ов

### 6. PumbaNET API (`PumbaNetApi.kt`)
- OkHttp + Retrofit
- HTTPS only
- Endpoints:
  - GET /api/v1/subscription
  - GET /api/v1/servers
  - GET /api/v1/servers/{id}/config
  - POST /api/v1/subscription/usage
- Правильная обработка ошибок (401, 403, 500)

## 🔄 Поток подключения

```
User нажимает "Подключить"
         │
         ▼
VPN State Machine: Preparing
         │
         ├── Проверка конфига (SecureStorage)
         ├── Проверка binary (XrayController)
         └── Проверка подписки (PumbaNET API)
         │
         ▼
VPN State Machine: RequestingPermission
         │
         ▼
User разрешает VPN
         │
         ▼
VPN State Machine: CreatingTun
         │
         ├── TUN Manager.setupTun()
         └── Получение TUN fd
         │
         ▼
VPN State Machine: ValidatingConfig
         │
         ├── VLESS Parser.parse()
         └── XrayController.validateConfig()
         │
         ▼
VPN State Machine: StartingXray
         │
         └── XrayController.startXray(config, tunFd)
         │
         ▼
VPN State Machine: Connecting
         │
         └── Xray соединяется с сервером
         │
         ▼
VPN State Machine: Connected
         │
         └── Трафик идёт через TUN → Xray → VLESS
```

## 🔐 Безопасность

### Secure Storage
- API токены → EncryptedSharedPreferences
- VLESS конфиги → EncryptedSharedPreferences
- Keystore-backed encryption

### Network Security
- HTTPS only (API)
- Certificate validation
- No cleartext traffic

### Xray Binary
- Проверка подписи (рекомендуется)
- Хранение в filesDir (не cache)
- chmod 700

## 📊 State Management

VPN State Machine — единственный источник истины о состоянии VPN.

UI подписывается на изменения состояния через:
- LiveData
- Flow
- Callbacks

## 🎯 Преимущества v2

1. **Правильная TUN ↔ Xray связка** — трафик реально идёт через VPN
2. **Единый VLESS parser** — нет дублирования
3. **Secure Storage** — credential'ы зашифрованы
4. **State Machine** — правильное управление состояниями
5. **OkHttp + Retrofit** — современный HTTP клиент
6. **HTTPS only** — безопасность API
7. **Полная поддержка параметров** — reality, xhttp, ws, grpc
8. **Валидация** — UUID, host, port, config
9. **Мониторинг Xray** — обработка exit code
10. **Корректный lifecycle** — foreground first, cleanup

## 🚧 TODO

- [ ] Добавить xray-core binary в assets
- [ ] Реализовать UI на State Machine
- [ ] Добавить subscription parser (base64)
- [ ] Network monitoring/reconnect
- [ ] DNS leak protection
- [ ] protect() для Xray outbound
- [ ] Testing с разными типами конфигов
