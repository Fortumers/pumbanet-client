# Keystore для подписи APK

## Создание keystore

### Вариант 1: Android Studio

1. Откройте Android Studio
2. Build → Generate Signed Bundle / APK
3. Выберите APK
4. Нажмите "Create new..."
5. Заполните:
   - Key store path: `keystore/release-key.jks`
   - Password: ваш пароль
   - Alias: `pumbanet`
   - Validity: 25 лет
6. Сохраните в папку `keystore/`

### Вариант 2: Командная строка

```bash
cd keystore
keytool -genkey -v -keystore release-key.jks -alias pumbanet -keyalg RSA -keysize 2048 -validity 9125
```

Вам предложат ввести:
- Пароль keystore
- Имя, фамилию
- Название организации
- Пароль ключа

## Переменные окружения

Для автоматической сборки настройте переменные:

```bash
export KEYSTORE_PASSWORD="ваш_пароль_keystore"
export KEY_ALIAS="pumbanet"
export KEY_PASSWORD="ваш_пароль_ключа"
```

## Безопасность

⚠️ **ВАЖНО**: Никогда не коммитьте keystore в Git!

Добавлено в `.gitignore`:
```
keystore/*.jks
keystore/*.keystore
keystore/*.properties
```

## Сборка релизного APK

```bash
./gradlew assembleRelease
```

APK появится в:
```
app/build/outputs/apk/release/app-release.apk
```

## Universal APK

Для создания универсального APK (все архитектуры):

```bash
./gradlew assembleReleaseUniversal
```

APK появится в:
```
app/build/outputs/apk/release/app-release-universal.apk
```
