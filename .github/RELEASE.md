# Как создать релиз с APK

## Автоматическая сборка через GitHub Actions

### Шаг 1: Создать тег

```bash
git tag v1.0.0
git push origin v1.0.0
```

Или через GitHub UI:
1. https://github.com/Fortumers/pumbanet-client/releases
2. "Create a new release"
3. Tag version: `v1.0.0`
4. Release name: `PumbaNET Client v1.0.0`
5. Нажмите "Publish release"

### Шаг 2: Дождаться сборки

GitHub Actions автоматически:
1. Соберёт APK
2. Создаст релиз
3. Прикрепит APK файл

### Шаг 3: Скачать APK

Перейдите на:  
https://github.com/Fortumers/pumbanet-client/releases/latest

Скачайте файл `PumbaNET-debug.apk`

## Ручная сборка

Если хотите собрать локально:

```bash
./gradlew assembleDebug
```

APK появится в:  
`app/build/outputs/apk/debug/app-debug.apk`

## CI/CD статус

Проверить статус сборок:  
https://github.com/Fortumers/pumbanet-client/actions
