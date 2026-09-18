# PumbaNET — Modern UI Design

Современный Material Design 3 frontend для PumbaNET Client

## 🎨 Дизайн-система

### Цветовая палитра

#### Primary Colors
- **Primary**: `#6366F1` (Indigo)
- **Primary Dark**: `#4F46E5`
- **Primary Light**: `#A5B4FC`

#### Secondary Colors
- **Secondary**: `#10B981` (Emerald)
- **Secondary Dark**: `#059669`
- **Secondary Light**: `#6EE7B7`

#### Background (Dark Theme)
- **Background**: `#0F172A` (Slate 900)
- **Surface**: `#1E293B` (Slate 800)
- **Surface Variant**: `#334155` (Slate 700)

#### Status Colors
- **Success**: `#10B981`
- **Warning**: `#F59E0B`
- **Error**: `#EF4444`
- **Info**: `#3B82F6`

### Градиенты

```xml
<gradient
    startColor="#6366F1"
    centerColor="#8B5CF6"
    endColor="#EC4899"
    angle="135" />
```

### Glassmorphism

Эффект матового стекла для карточек:
- Полупрозрачный фон: `#1E293B`
- Тонкая обводка: `#334155`
- Закругление: `16dp`
- Без тени (elevation: 0)

## 🎭 Компоненты

### Карточки

```
┌─────────────────────────┐
│  🟢 Profile Name        │
│     server.com:443      │
│     ⚡ 45ms  🔒 Vless   │
│              ⭐ 🗑️      │
└─────────────────────────┘
```

- MaterialCardView
- CornerRadius: 16dp
- StrokeColor: Glass border
- Elevation: 0dp

### Кнопки

#### Primary Button (Gradient)
```
┌─────────────────────┐
│   ПОДКЛЮЧИТЬ        │
└─────────────────────┘
```
- Height: 64dp
- CornerRadius: 16dp
- Gradient background
- Elevation: 8dp

#### Outlined Button
```
┌─────────────────────┐
│   📋 Профили        │
└─────────────────────┘
```
- StrokeWidth: 1dp
- CornerRadius: 12dp
- Icon + Text

### Переключатели

```
Тёмная тема          [●━━━]
Kill Switch          [●━━━]
Автоподключение      [━━━●]
```

- SwitchMaterial
- Thumb Tint: Primary
- Track Tint: Primary Light

### Floating Action Button

```
     ┌───┐
     │ + │
     └───┘
```
- Position: Bottom Right
- Margin: 24dp
- Background: Primary
- Icon: Add

## 🎬 Анимации

### Переходы между экранами

```xml
<translate
    fromXDelta="100%"
    toXDelta="0%"
    duration="300ms"
    interpolator="decelerate" />
```

### Появление элементов

```xml
<alpha
    fromAlpha="0.0"
    toAlpha="1.0"
    duration="400ms" />
```

### Масштабирование

```xml
<scale
    fromXScale="0.8"
    toXScale="1.0"
    interpolator="overshoot" />
```

## 📱 Экраны

### Главный экран

```
┌──────────────────────────┐
│      🚀 PumbaNET         │
│    Vless + Remnawave     │
└──────────────────────────┘

┌──────────────────────────┐
│      🔴 Отключено        │
│   Профиль не выбран      │
└──────────────────────────┘

┌──────────────────────────┐
│    ПОДКЛЮЧИТЬ            │
└──────────────────────────┘

┌─────────┐ ┌─────────┐
│📋Профили│ │⚙️Настройки│
└─────────┘ └─────────┘
┌─────────┐ ┌─────────┐
│📥Импорт │ │📊Статистика│
└─────────┘ └─────────┘
```

### Список профилей

```
┌──────────────────────────┐
│ ← Профили VPN            │
├──────────────────────────┤
│ 🟢 Netherlands           │
│    45ms  ⭐  🗑️          │
├──────────────────────────┤
│ ⚪ Germany               │
│    52ms  ⭐  🗑️          │
├──────────────────────────┤
│                    ┌───┐ │
│                    │ + │ │
│                    └───┘ │
└──────────────────────────┘
```

### Настройки

```
┌──────────────────────────┐
│ ← Настройки              │
├──────────────────────────┤
│ Внешний вид              │
├──────────────────────────┤
│ Тёмная тема       [●━━━] │
├──────────────────────────┤
│ Безопасность             │
├──────────────────────────┤
│ Kill Switch       [●━━━] │
│ Автоподключение   [━━━●] │
├──────────────────────────┤
│ Статистика               │
├──────────────────────────┤
│  ↑ 125 MB   ↓ 580 MB     │
└──────────────────────────┘
```

## 🎯 Принципы дизайна

1. **Минимализм** — ничего лишнего
2. **Контраст** — читаемость на первом месте
3. **Консистентность** — единый стиль везде
4. **Плавность** — 60fps анимации
5. **Доступность** — WCAG 2.1 AA

## 📦 Зависимости

```kotlin
implementation("com.google.android.material:material:1.12.0")
implementation("androidx.core:core-ktx:1.13.1")
implementation("androidx.appcompat:appcompat:1.7.0")
```

## 🎨 Figma

Макеты доступны в Figma:
- Главный экран
- Профили
- Настройки
- Импорт конфига
- Статистика

## 📱 Адаптивность

- Поддержка всех размеров экранов
- Landscape ориентация
- Foldable устройства
- Tablets

## 🌙 Темы

- **Dark** (по умолчанию)
- **Light** (опционально)
- **System** (авто)

## 🚀 Производитель

- 60 FPS
- < 100ms отклик
- Оптимизированные drawable
- Lazy loading списков
