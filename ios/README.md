# PumbaNET Client for iOS

VPN-клиент для сервиса PumbaNET на iOS (Feature 12)

## Требования

- iOS 14.0+
- Xcode 14+
- Swift 5.7+

## Архитектура

```
PumbaNET-iOS/
├── PumbaNET/
│   ├── App/
│   │   ├── PumbaNETApp.swift
│   │   └── AppDelegate.swift
│   ├── Views/
│   │   ├── ContentView.swift
│   │   ├── ProfileListView.swift
│   │   ├── SettingsView.swift
│   │   └── ImportConfigView.swift
│   ├── Models/
│   │   ├── VpnProfile.swift
│   │   └── ConnectionStats.swift
│   ├── Services/
│   │   ├── VpnService.swift
│   │   ├── ConfigManager.swift
│   │   └── ApiClient.swift
│   └── Utils/
│       ├── PingTest.swift
│       └── PreferencesManager.swift
├── PumbaNETExtension/
│   └── PacketTunnelProvider.swift
└── README.md
```

## Интеграция с NetworkExtension

### PacketTunnelProvider.swift

```swift
import NetworkExtension

class PacketTunnelProvider: NEPacketTunnelProvider {
    
    override func startTunnel(options: [String : NSObject]?, completionHandler: @escaping (Error?) -> Void) {
        // Запуск Xray-core с конфигом
        // Настройка туннеля
        completionHandler(nil)
    }
    
    override func stopTunnel(with reason: NEProviderStopReason, completionHandler: @escaping () -> Void) {
        // Остановка Xray
        completionHandler()
    }
    
    override func handleAppMessage(_ messageData: Data, completionHandler: ((Data?) -> Void)?) {
        // Обработка команд от приложения
    }
}
```

## Установка

1. Откройте `PumbaNET.xcodeproj` в Xcode
2. Настройте Signing & Capabilities:
   - App Groups
   - Network Extensions
3. Соберите проект: Product → Build

## Интеграция с Remnawave API

Аналогично Android версии:
- `GET /api/v1/user/config` — получение конфига
- Bearer token авторизация

## Ссылки

- [NetworkExtension Documentation](https://developer.apple.com/documentation/networkextension)
- [NEPacketTunnelProvider](https://developer.apple.com/documentation/networkextension/nepackettunnelprovider)
