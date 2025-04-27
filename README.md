
# Playwright Quote Form Tests

## Обзор

Этот проект содержит автоматизированные тесты для формы запроса котировок на сайте `https://qatest.datasub.com`. Тесты написаны с использованием **Playwright для Java** и **JUnit 5** для проверки функциональности формы, включая успешную отправку и валидацию некорректных данных.

Проект генерирует отчет в формате CSV (`reports/test_results.csv`) с данными, отправленными в положительном тестовом случае, и статусами проверок. Также создаются скриншоты для отладки.

## Требования

- **Java**: JDK 8 или выше (рекомендуется JDK 11 или 17 для совместимости).
- **Maven**: Для управления зависимостями и выполнения тестов.
- **Playwright**: Версия 1.47.0 (указана в `pom.xml`).
- **Операционная система**: Windows, macOS или Linux.

## Установка

1. **Клонируйте репозиторий**:
   ```bash
   git clone https://github.com/ManMaxMotivation/playwright-quote-tests
   cd playwright-quote-tests
   ```

2. **Установите зависимости**:
   Убедитесь, что Maven установлен, затем выполните:
   ```bash
   mvn clean install
   ```

3. **Проверьте версию Java**:
   Выполните команду, чтобы убедиться, что установлена Java 8+:
   ```bash
   java -version
   ```

4. **Настройка Playwright**:
   Зависимости Playwright автоматически устанавливаются через Maven. Дополнительная настройка не требуется.

## Запуск тестов

Для выполнения тестов используйте:
```bash
mvn test
```

Это действие:
- Запустит два теста: `happyPathSuccessfulFormSubmission` и `negativeCaseInvalidEmailAndEmptyField`.
- Сгенерирует CSV-файл (reports/test_results.csv) с данными теста и результатами.
- Сохранит скриншоты (setup_screenshot.png и error_screenshot.png) в папке reports для отладки.
- Выведет логи выполнения тестов в консоль, указывая, прошли тесты или нет.

Пример вывода в консоли при успешном выполнении:

```
Test 'happyPathSuccessfulFormSubmission' PASSED
Test 'negativeCaseInvalidEmailAndEmptyField' PASSED
```

## Описание тестов

1. **happyPathSuccessfulFormSubmission**

   **Цель**: Проверяет успешную отправку формы запроса котировок с корректными данными.

   **Действия**:
   - Заполняет форму корректными данными (имя, email, услуга, цель аккаунта, варианты вывода, сообщение).
   - Отправляет форму, нажимая кнопку "Request A Quote".
   - Проверяет, что сообщение об успехе (#formStatus) отображается и содержит текст "Форма отправлена.".

   **Результат**:
   - Выводит результат теста в консоль (например, Test 'happyPathSuccessfulFormSubmission' PASSED).
   - Сохраняет отправленные данные и результаты проверок в reports/test_results.csv.

2. **negativeCaseInvalidEmailAndEmptyField**

   **Цель**: Проверяет корректную обработку формы при вводе некорректного email и предотвращение отправки.

   **Действия**:
   - Заполняет форму с некорректным email (invalid_email) и корректными данными для остальных полей.
   - Пытается отправить форму (если кнопка отправки активна).
   - Проверяет, что поле email помечено как некорректное (имеет CSS-класс is-invalid).
   - Убедитесь, что сообщение об успехе (#formStatus) не отображается.

   **Результат**:
   - Выводит результат теста в консоль (например, Test 'negativeCaseInvalidEmailAndEmptyField' PASSED).
   - Сохраняет скриншот (error_screenshot.png) для отладки.

## Генерируемые артефакты

- **CSV-отчёт** (reports/test_results.csv):
  Содержит данные, отправленные в happyPathSuccessfulFormSubmission (например, Имя, Email, Услуга и т.д.).
  Включает результаты проверок (FormStatusVisible, SuccessMessagePresent) и временную метку.

  Пример:
  ```
  Name,Email,Service,AccountPurpose,WithdrawalOptions,Message,FormStatusVisible,SuccessMessagePresent,Timestamp
  John Doe,test@example.com,Select B Service,Business,Cash,"Test message for quote request.",true,true,2025-04-27 14:30:45
  ```

- **Скриншоты**:
   - reports/setup_screenshot.png: Снимается перед каждым тестом для проверки загрузки страницы.
   - reports/error_screenshot.png: Снимается в negativeCaseInvalidEmailAndEmptyField для отладки ошибок валидации.

## Устранение неполадок

- **Тесты не проходят из-за таймаутов**: Убедитесь, что сайт https://qatest.datasub.com доступен. Увеличьте таймаут в waitForSelector (например, с 60000 мс до 90000 мс), если страница загружается медленно.
- **CSV-файл не создаётся**: Проверьте права доступа для записи в папку reports. Убедитесь, что в консоли нет сообщений об IOException.
- **Логи не отображаются в консоли**: Убедитесь, что тесты запускаются через mvn test, и проверьте вывод в консоли. Логи выводятся в формате Test '<имя теста>' PASSED/FAILED.
