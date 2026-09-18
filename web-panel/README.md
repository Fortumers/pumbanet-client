# PumbaNET Web Panel (Feature 29)

Веб-панель управления через браузер

## Функционал

- ✅ Управление профилями (добавление, удаление, редактирование)
- ✅ Статистика использования (трафик, сессии, время)
- ✅ Настройка аккаунта (подписка, оплата, рефералы)
- ✅ Мониторинг серверов (статус, загрузка, пинг)
- ✅ Семейный доступ (члены семьи, лимиты)
- ✅ Экспорт/импорт конфигов

## Технологии

- **Frontend**: React + TypeScript + TailwindCSS
- **Backend**: Node.js/Express или Python/FastAPI
- **Database**: PostgreSQL или MongoDB
- **Auth**: JWT + OAuth2

## Структура проекта

```
web-panel/
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Profiles.tsx
│   │   │   ├── Statistics.tsx
│   │   │   └── Settings.tsx
│   │   ├── pages/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── vite.config.ts
├── backend/
│   ├── src/
│   │   ├── routes/
│   │   │   ├── profiles.ts
│   │   │   ├── stats.ts
│   │   │   └── auth.ts
│   │   ├── models/
│   │   └── index.ts
│   ├── package.json
│   └── tsconfig.json
└── README.md
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` — вход
- `POST /api/v1/auth/logout` — выход
- `POST /api/v1/auth/refresh` — обновление токена

### Profiles
- `GET /api/v1/profiles` — список профилей
- `POST /api/v1/profiles` — создать профиль
- `PUT /api/v1/profiles/:id` — обновить профиль
- `DELETE /api/v1/profiles/:id` — удалить профиль

### Statistics
- `GET /api/v1/stats/usage` — статистика использования
- `GET /api/v1/stats/traffic` — трафик по периодам
- `GET /api/v1/stats/sessions` — история сессий

### Account
- `GET /api/v1/account` — информация об аккаунте
- `PUT /api/v1/account` — обновление настроек
- `GET /api/v1/account/subscription` — статус подписки

## Установка

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend

```bash
cd backend
npm install
npm run dev
```

## Деплой

- **Frontend**: Vercel, Netlify, или статический хостинг
- **Backend**: Heroku, Railway, VPS
- **Database**: Supabase, MongoDB Atlas

## Скриншоты

- Dashboard с графиками трафика
- Список профилей с быстрым редактированием
- Настройки аккаунта и подписки
- Мониторинг серверов в реальном времени
