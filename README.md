# PumbaNET Client

VPN-клиент для сервиса PumbaNET на протоколе Vless с поддержкой Remnawave API и всеми премиум-функциями (1-15).

## 🚀 Полный список функций

### Базовые функции (1-8) ✅
- ✅ Vless + Remnawave API
- ✅ Импорт конфига (QR, vless://, deep link)
- ✅ Мультипрофильность
- ✅ Тест пинга
- ✅ Тёмная тема
- ✅ Виджет на главный экран
- ✅ Статистика трафика
- ✅ Split Tunneling
- ✅ Автоподключение
- ✅ Kill Switch

### 🔥 Продвинутые функции (9-15)

#### 9. Push-уведомления
- **PushNotificationManager.kt** — менеджер уведомлений
- Каналы: подключение, подписка, новости, общие
- Уведомления о:
  - Подключении/отключении VPN
  - Конце подписки (за N дней)
  - Новостях PumbaNET
  - Важных событиях
- Поддержка Android 13+ (POST_NOTIFICATIONS)

#### 10. Реферальная программа
- **ReferralManager.kt** — трекинг рефералов
- Генерация уникального кода (PUMB-XXXX)
- Реферальная ссылка для sharing
- Трекер количества рефералов
- Бонусные баллы за приглашения
- Интеграция с сервером через API
- Применение реферального кода при регистрации

#### 11. Синхронизация между устройствами
- **CloudSyncManager.kt** — облачная синхронизация
- Экспорт/импорт профилей в JSON
- Поддержка Google Drive / Dropbox
- QR-передача между устройствами
- Merge локальных и облачных данных
- Конфликт-резолюшн (новые записи побеждают)
- Timestamp последней синхронизации

#### 12. iOS версия (документация)
- **ios/README.md** — полное руководство
- Swift + NetworkExtension
- PacketTunnelProvider для VPN
- Аналогичный функционал Android
- Интеграция с Xray-core
- Remnawave API поддержка

#### 13. Desktop клиенты (документация)
- **desktop/README.md** — Tauri проект
- Rust бэкенд + React/Vue фронтенд
- Поддержка Windows/macOS/Linux
- Системный трей
- Автозапуск при старте ОС
- Кроссплатформенная сборка

#### 14. Автообновление конфигов
- **ConfigAutoUpdateManager.kt** — авто-апдейт
- Периодическая проверка сервера (каждые 6 часов)
- Проверка доступности IP:port
- Авто-получение нового конфига при изменении
- Уведомление о новых серверах
- Фоновая проверка через WorkManager

#### 15. Аналитика и логи
- **AnalyticsManager.kt** — логирование и метрики
- Лог подключений (успех/ошибка, длительность)
- Лог ошибок (type, message, stack trace)
- Лог событий (действия пользователя)
- Метрики использования (трафик, сессии)
- Экспорт логов в файл (для отладки)
- Авто-очистка старых логов (>30 дней)
- Отправка на сервер аналитики

## 📁 Структура проекта

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
│   │       ├── PreferencesManager.kt
│   │       ├── PingTest.kt
│   │       ├── PushNotificationManager.kt (9)
│   │       ├── ReferralManager.kt (10)
│   │       ├── CloudSyncManager.kt (11)
│   │       ├── ConfigAutoUpdateManager.kt (14)
│   │       └── AnalyticsManager.kt (15)
│   └── res/
├── ios/ (12)
│   └── README.md
├── desktop/ (13)
│   └── README.md
└── README.md
```

## 🛠 Требования

- Android 8.0+ (API 26)
- Android Studio Arctic Fox+
- Xray-core binary (ARM64)

## 📦 Сборка

1. Откройте проект в Android Studio
2. Добавьте xray-core binary в `app/src/main/assets/xray`
3. Настройте API endpoint в `RemnawaveApiClient.kt`
4. Соберите проект: `./gradlew assembleDebug`

## 🔗 Ссылки

- Репозиторий: https://github.com/Fortumers/pumbanet-client
- PR #1: Импорт конфигов
- PR #2: Премиум фичи 1-8
- PR #3: Продвинутые фичи 9-15

## 🏷 Лицензия

MIT

## 📞 Контакты

Владелец: jurnest resterr (Fortumers)  
Сервис: PumbaNET (Vless + Remnawave)
