# PumbaNET Client — Ultimate VPN Solution

Полнофункциональное VPN-приложение с **30 премиум-функциями** для сервиса PumbaNET.

## 🚀 Все 30 функций

### Базовые (1-8) ✅
1. ✅ Мультипрофильность
2. ✅ Тест пинга
3. ✅ Тёмная тема
4. ✅ Виджет
5. ✅ Статистика трафика
6. ✅ Split Tunneling
7. ✅ Автоподключение
8. ✅ Kill Switch

### Продвинутые (9-15) ✅
9. ✅ Push-уведомления
10. ✅ Реферальная программа
11. ✅ Синхронизация между устройствами
12. ✅ iOS версия (документация)
13. ✅ Desktop клиенты (документация)
14. ✅ Автообновление конфигов
15. ✅ Аналитика и логи

### Ultimate (16-30) ✅
16. ✅ **Биометрическая аутентификация** — Face ID / Touch ID / Fingerprint
17. ✅ **WireGuard поддержка** — генерация ключей, импорт .conf
18. ✅ **Автоматические правила** — по геолокации, времени, Wi-Fi
19. ✅ **Обход блокировок** — AntiZapret / CensorTracker интеграция
20. ✅ **Семейный доступ** — профили, родительский контроль, лимиты
21. ✅ **P2P оптимизация** — выделенные серверы, port forwarding
22. ✅ **Игровой режим** — низкий пинг, приоритет трафика, ping/jitter
23. ✅ **Резервное копирование** — Google Drive, шифрование
24. ✅ **Встроенный браузер** — proxy, приватный режим
25. ✅ **Монетизация** — Google Play Billing, freemium, крипто-оплата
26. ✅ **Мультипротокольность** — Trojan, Shadowsocks, Hysteria2, Tuic (QUIC)
27. ✅ **Мониторинг серверов** — статус, загрузка, рекомендации
28. ✅ **CLI** — termux/adb, Tasker интеграция
29. ✅ **Web-панель** — управление через браузер, статистика
30. ✅ **White Label** — готовое решение для вашего VPN-сервиса

## 📁 Структура проекта (полная)

```
pumbanet-client/
├── app/src/main/
│   ├── java/com/pumbanet/client/
│   │   ├── MainActivity.kt
│   │   ├── VpnService.kt
│   │   ├── ConfigImportActivity.kt
│   │   ├── ProfilesActivity.kt
│   │   ├── SettingsActivity.kt
│   │   ├── SelectAppsActivity.kt
│   │   ├── PumbaNetWidgetProvider.kt
│   │   ├── model/
│   │   │   ├── VpnProfile.kt
│   │   │   ├── ConnectionStats.kt
│   │   │   └── AppInfo.kt
│   │   └── utils/
│   │       # Core (1-8)
│   │       ├── PreferencesManager.kt
│   │       ├── PingTest.kt
│   │       # Advanced (9-15)
│   │       ├── PushNotificationManager.kt
│   │       ├── ReferralManager.kt
│   │       ├── CloudSyncManager.kt
│   │       ├── ConfigAutoUpdateManager.kt
│   │       ├── AnalyticsManager.kt
│   │       # Ultimate (16-30)
│   │       ├── BiometricAuthManager.kt (16)
│   │       ├── WireGuardManager.kt (17)
│   │       ├── AutomationManager.kt (18)
│   │       ├── BypassManager.kt (19)
│   │       ├── FamilyAccessManager.kt (20)
│   │       ├── P2POptimizer.kt (21)
│   │       ├── GamingModeManager.kt (22)
│   │       ├── BackupManager.kt (23)
│   │       ├── InAppBrowserManager.kt (24)
│   │       ├── MonetizationManager.kt (25)
│   │       ├── MultiProtocolManager.kt (26)
│   │       ├── ServerMonitoringManager.kt (27)
│   │       └── CliManager.kt (28)
│   └── res/
├── ios/ (12)
│   └── README.md
├── desktop/ (13)
│   └── README.md
├── web-panel/ (29)
│   └── README.md
├── white-label/ (30)
│   └── README.md
└── README.md
```

## 📊 Статистика проекта

- **Файлов**: 60+
- **Строк кода**: ~8000+
- **Функций**: 30
- **Платформ**: Android, iOS (doc), Desktop (doc), Web
- **Протоколов**: Vless, WireGuard, Trojan, Shadowsocks, Hysteria2, Tuic

## 🛠 Требования

- Android 8.0+ (API 26)
- Android Studio Arctic Fox+
- Xray-core binary (ARM64)
- WireGuard-go (опционально)

## 📦 Сборка

```bash
# Клонирование
git clone https://github.com/Fortumers/pumbanet-client.git
cd pumbanet-client

# Добавление xray-core
mkdir -p app/src/main/assets
cp /path/to/xray app/src/main/assets/

# Сборка APK
./gradlew assembleDebug
```

## 🔗 Pull Requests

1. **PR #1**: Импорт конфигов (QR, vless://, Remnawave API)
2. **PR #2**: Премиум фичи 1-8
3. **PR #3**: Продвинутые фичи 9-15
4. **PR #4**: Ultimate фичи 16-30 ← новый

## 💼 White Label

Готовое решение для запуска вашего VPN-сервиса:
- Полное брендирование
- Все 30 функций
- Мультиплатформенность
- Серверная часть + Web-панель

📧 sales@pumbanet.com | 🌐 pumbanet.com/white-label

## 🏷 Лицензия

MIT (кроме White Label — коммерческая)

## 📞 Контакты

Владелец: jurnest resterr (Fortumers)  
Сервис: PumbaNET (Vless + Remnawave)  
GitHub: https://github.com/Fortumers/pumbanet-client
