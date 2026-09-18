# PumbaNET Desktop Client

VPN-клиент для сервиса PumbaNET на Desktop (Feature 13)

## Технологии

- **Tauri** — фреймворк для desktop приложений
- **Rust** — бэкенд
- **React/Vue** — фронтенд
- **Xray-core** — VPN ядро

## Требования

- Node.js 18+
- Rust 1.70+
- npm/yarn

## Структура проекта

```
pumbanet-desktop/
├── src-tauri/
│   ├── src/
│   │   ├── main.rs
│   │   ├── vpn/
│   │   │   ├── mod.rs
│   │   │   ├── manager.rs
│   │   │   └── config.rs
│   │   └── api/
│   │       └── client.rs
│   ├── Cargo.toml
│   └── tauri.conf.json
├── src/
│   ├── components/
│   │   ├── ConnectButton.tsx
│   │   ├── ProfileList.tsx
│   │   └── Settings.tsx
│   ├── App.tsx
│   └── main.tsx
├── package.json
└── README.md
```

## Установка

### 1. Клонирование

```bash
git clone https://github.com/Fortumers/pumbanet-client.git
cd pumbanet-client/desktop
```

### 2. Установка зависимостей

```bash
# Frontend
npm install

# Backend (Rust)
cd src-tauri
cargo build
```

### 3. Запуск в режиме разработки

```bash
npm run tauri dev
```

### 4. Сборка релиза

```bash
npm run tauri build
```

## Функционал

- ✅ Подключение/отключение VPN
- ✅ Управление профилями
- ✅ Импорт через QR/vless://
- ✅ Статистика трафика
- ✅ Тёмная тема
- ✅ Автоподключение
- ✅ Kill Switch

## Системные требования

- **Windows**: 10/11 (x64)
- **macOS**: 11+ (Intel/Apple Silicon)
- **Linux**: Ubuntu 20.04+, Fedora 35+, Arch

## Интеграция с Xray-core

### Rust (src-tauri/src/vpn/manager.rs)

```rust
use std::process::Command;

pub struct VpnManager {
    process: Option<Child>,
}

impl VpnManager {
    pub fn start(&mut self, config_path: &str) -> Result<(), Box<dyn Error>> {
        let child = Command::new("xray")
            .arg("run")
            .arg("-c")
            .arg(config_path)
            .spawn()?;
        
        self.process = Some(child);
        Ok(())
    }
    
    pub fn stop(&mut self) {
        if let Some(mut process) = self.process.take() {
            process.kill().ok();
        }
    }
}
```

## Сборка под разные платформы

### Windows

```bash
npm run tauri build -- --target x86_64-pc-windows-msvc
```

### macOS

```bash
npm run tauri build -- --target x86_64-apple-darwin
npm run tauri build -- --target aarch64-apple-darwin
```

### Linux

```bash
npm run tauri build -- --target x86_64-unknown-linux-gnu
```

## Ссылки

- [Tauri Documentation](https://tauri.app/v1/guides/)
- [Xray-core](https://github.com/XTLS/Xray-core)
