<div align="center">

# 🚀 PumbaNET Client

**Мощный VPN клиент для сервиса PumbaNET на протоколе Vless**

[![Release](https://img.shields.io/github/v/release/Fortumers/pumbanet-client?style=for-the-badge&logo=github&color=6366F1)](https://github.com/Fortumers/pumbanet-client/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/Fortumers/pumbanet-client/release-apk.yml?style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/Fortumers/pumbanet-client/actions)
[![Platform](https://img.shields.io/badge/Android-8.0+-brightgreen?style=for-the-badge&logo=android)](https://www.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Fortumers/pumbanet-client/total?style=for-the-badge&logo=github)](https://github.com/Fortumers/pumbanet-client/releases)

[📥 Скачать](#-скачать) • [📖 Документация](#-документация) • [🔧 Функции](#-функции) • [🐛 Issues](https://github.com/Fortumers/pumbanet-client/issues)

</div>

---

## 🌟 Особенности

<div align="center">

| 🔐 Безопасность | ⚡ Скорость | 🎨 UI/UX |
|:---:|:---:|:---:|
| Vless + Reality | Xray-core | Material Design 3 |
| Encrypted Storage | TUN интерфейс | Тёмная тема |
| Kill Switch | Мультипрофильность | Анимации |

</div>

---

## 🔥 Функции

### 🔐 Безопасность
- ✅ **Vless протокол** с полной поддержкой (TCP, WS, gRPC, xhttp)
- ✅ **REALITY** поддержка (pbk, sid, sni, fp)
- ✅ **Encrypted Storage** — токены и конфиги в Android Keystore
- ✅ **Kill Switch** — блокировка интернета без VPN
- ✅ **Split Tunneling** — выбор приложений для VPN

### ⚡ Производительность
- ✅ **Xray-core** — современное ядро
- ✅ **TUN интерфейс** — правильный VPN на уровне системы
- ✅ **Автоматическое переподключение** при обрыве
- ✅ **Мониторинг процесса** Xray с обработкой ошибок

### 🎨 Интерфейс
- ✅ **Material Design 3** — современный UI
- ✅ **Тёмная тема** по умолчанию
- ✅ **Плавные анимации** 60 FPS
- ✅ **Glassmorphism** эффекты
- ✅ **Виджет** на главный экран

### 🌐 Интеграции
- ✅ **Remnawave API** — автоматическое получение конфигов
- ✅ **PumbaNET Backend** — подписки, серверы, статистика
- ✅ **QR-код** сканер для импорта
- ✅ **vless://** deep links

---

## 📥 Скачать

### Готовый APK

1. Перейдите на **[Releases](https://github.com/Fortumers/pumbanet-client/releases)**
2. Скачайте последний `PumbaNET-debug.apk`
3. Установите на Android устройство

**📖 Полная инструкция**: [DOWNLOAD.md](DOWNLOAD.md)

### Требования

| Параметр | Значение |
|----------|----------|
| **Android** | 8.0+ (API 26) |
| **Место** | 50MB+ |
| **RAM** | 2GB+ |
| **Архитектура** | ARM64 |

---

## 🔧 Быстрый старт

### 1. Установка

```bash
# Скачать APK из Releases
# Или собрать самостоятельно:
git clone https://github.com/Fortumers/pumbanet-client.git
cd pumbanet-client
./gradlew assembleDebug
```

### 2. Настройка

1. Запустите приложение
2. Отсканируйте QR-код или вставьте vless:// ссылку
3. Нажмите **"Подключить"**

### 3. Готово! 🎉

Теперь весь ваш трафик идёт через PumbaNET VPN.

---

## 📖 Документация

### Для пользователей

- **[DOWNLOAD.md](DOWNLOAD.md)** — как скачать и установить
- **[INSTALL.md](INSTALL.md)** — подробная инструкция по установке
- **[TODO_CRITICAL.md](TODO_CRITICAL.md)** — известные проблемы

### Для разработчиков

- **[ARCHITECTURE_v2.md](ARCHITECTURE_v2.md)** — архитектура приложения
- **[README_v2.md](README_v2.md)** — документация v2
- **[.github/RELEASE.md](.github/RELEASE.md)** — как создать релиз

---

## 🏗 Архитектура

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
```

**📖 Подробнее**: [ARCHITECTURE_v2.md](ARCHITECTURE_v2.md)

---

## 🛠 Технологии

| Категория | Технологии |
|-----------|------------|
| **Язык** | Kotlin |
| **UI** | Material Design 3, XML |
| **VPN** | Android VpnService, TUN |
| **Xray** | Xray-core binary |
| **HTTP** | OkHttp, Retrofit |
| **Storage** | EncryptedSharedPreferences, Keystore |
| **Build** | Gradle, GitHub Actions |

---

## 🤝 Вклад

Приветствуются PR, issues и feedback!

### Как помочь:

1. **Fork** репозиторий
2. Создай ветку `feature/your-feature`
3. Сделай коммиты
4. Отправь **Pull Request**

### Разработка:

```bash
git clone https://github.com/Fortumers/pumbanet-client.git
cd pumbanet-client
./gradlew assembleDebug
```

---

## 📄 Лицензия

[MIT License](LICENSE)

---

## 📞 Контакты

- **Владелец**: jurnest resterr ([@Fortumers](https://github.com/Fortumers))
- **Сервис**: PumbaNET (Vless + Remnawave)
- **Issues**: https://github.com/Fortumers/pumbanet-client/issues

---

<div align="center">

**⭐ Поставьте звезду, если проект полезен!**

Made with ❤️ by Fortumers

</div>
