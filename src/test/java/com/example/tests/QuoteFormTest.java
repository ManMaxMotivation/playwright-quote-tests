package com.example.tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuoteFormTest {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private static final String CSV_FILE_PATH = "reports/test_results.csv";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
        page.setViewportSize(1920, 1080);
        page.navigate("https://qatest.datasub.com");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.evaluate("() => { document.querySelectorAll('.wow').forEach(el => el.classList.remove('wow')); }");
        page.locator("#name").scrollIntoViewIfNeeded();
        page.waitForSelector("#name", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
    }

    @AfterEach
    void tearDown() {
        browser.close();
        playwright.close();
    }

    @Test
    void happyPathSuccessfulFormSubmission() {
        boolean testPassed = false;
        try {
            // Собираем данные формы
            List<String> testData = new ArrayList<>();
            String name = "John Doe";
            String email = "test@example.com";
            String service = "Select B Service";
            String accountPurpose = "Business";
            String withdrawalOptions = "Cash";
            String message = "Test message for quote request.";

            // Заполняем форму
            page.fill("#name", name);
            page.fill("#email", email);
            page.selectOption("#service", new SelectOption().setLabel(service));
            page.check("#purposeBusiness");
            page.check("#withdrawCash");
            page.fill("#message", message);
            page.locator("button[type='submit']:has-text('Request A Quote')").click();

            // Проверки и сбор статуса
            Locator formStatus = page.locator("#formStatus");
            boolean isFormStatusVisible = formStatus.isVisible();
            boolean hasSuccessMessage = formStatus.textContent().contains("Форма отправлена.");

            // Добавляем данные в список
            testData.add(name);
            testData.add(email);
            testData.add(service);
            testData.add(accountPurpose);
            testData.add(withdrawalOptions);
            testData.add(message);
            testData.add(String.valueOf(isFormStatusVisible));
            testData.add(String.valueOf(hasSuccessMessage));
            testData.add(LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            // Записываем в CSV
            writeToCsv(testData);

            // Выполняем проверки
            assertTrue(isFormStatusVisible, "Form status should be visible");
            assertTrue(hasSuccessMessage, "Form status should contain success message");

            testPassed = true;
        } finally {
            // Логирование результата теста
            System.out.println("Test 'happyPathSuccessfulFormSubmission' " + (testPassed ? "PASSED" : "FAILED"));
        }
    }

    @Test
    void negativeCaseInvalidEmailAndEmptyField() {
        boolean testPassed = false;
        try {
            page.fill("#name", "John Doe");
            page.fill("#email", "invalid_email");
            page.selectOption("#service", new SelectOption().setLabel("Select B Service"));
            page.check("#withdrawCash");
            page.fill("#message", "Test message");
            Locator submitButton = page.locator("button[type='submit']:has-text('Request A Quote')");
            if (!submitButton.isDisabled()) {
                submitButton.click();
                try {
                    page.waitForSelector("#email.is-invalid", new Page.WaitForSelectorOptions().setTimeout(5000));
                } catch (TimeoutError e) {
                    System.out.println("Validation did not set error class in time");
                }
            }
            Locator emailField = page.locator("#email");
            assertTrue(emailField.getAttribute("class").contains("is-invalid"), "Email field should have error class");
            Locator formStatus = page.locator("#formStatus");
            assertFalse(formStatus.isVisible(), "Form status should not be visible");

            testPassed = true;
        } finally {
            // Логирование результата теста
            System.out.println("Test 'negativeCaseInvalidEmailAndEmptyField' " + (testPassed ? "PASSED" : "FAILED"));
        }
    }

    private void writeToCsv(List<String> testData) {
        try {
            // Создаём директорию, если она не существует
            Path reportsDir = Paths.get("reports");
            if (!Files.exists(reportsDir)) {
                Files.createDirectory(reportsDir);
            }

            // Проверяем, существует ли файл, чтобы добавить заголовок только при первом запуске
            boolean fileExists = Files.exists(Paths.get(CSV_FILE_PATH));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
                if (!fileExists) {
                    writer.write("Name,Email,Service,AccountPurpose,WithdrawalOptions,Message,FormStatusVisible,SuccessMessagePresent,Timestamp");
                    writer.newLine();
                }

                // Формируем строку CSV
                String csvLine = String.join(",", testData.stream()
                        .map(this::escapeCsv)
                        .collect(Collectors.toList()));
                writer.write(csvLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Экранируем кавычки и оборачиваем значение в кавычки, если оно содержит запятые или кавычки
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}