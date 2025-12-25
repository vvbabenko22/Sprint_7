package test;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateCourierTest {

    // API URL
    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1";
    private final String COURIER_CREATE_ENDPOINT = "/courier"; // Эндпоинт для создания
    private final String COURIER_LOGIN_ENDPOINT = "/courier/login"; // Эндпоинт для логина
    private final String COURIER_DELETE_ENDPOINT = "/courier/%d"; // Эндпоинт для удаления по id

    // Генератор фейковых данных
    private final Faker faker = new Faker();

    // Переменная для хранения идентификатора последнего созданного курьера
    private Integer lastUsedID;

    // Генерируем гарантированно уникальный логин
    private String generateUniqueLogin() {
        return faker.internet().emailAddress() + "_" + UUID.randomUUID().toString();
    }

    // Перед каждым тестом очищаем состояние
    @BeforeEach
    public void beforeEach() {
        this.lastUsedID = null;
    }

    // После каждого теста удаляем созданный аккаунт
    @AfterEach
    public void afterEach() {
        if (lastUsedID != null) {
            given()
                    .baseUri(BASE_URL)
                    .when()
                    .delete(String.format(COURIER_DELETE_ENDPOINT, lastUsedID)) // Удаляем по ID
                    .then()
                    .statusCode(200);
        }
    }

    // Курьера можно создать
    @Test
    @Description("Курьера можно создать")
    public void createCourierSuccessfully() {
        String randomLogin = generateUniqueLogin();
        String randomPassword = faker.internet().password();
        String randomFirstName = faker.name().firstName();

        // Создаём курьера
        createCourier(randomLogin, randomPassword, randomFirstName);

        // Получаем идентификатор курьера
        getCourierID(randomLogin, randomPassword);
    }

    // Создаём курьера
    @Step("Создаём курьера")
    private void createCourier(String randomLogin, String randomPassword, String randomFirstName) {
        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + randomLogin + "\"," +
                        "    \"password\": \"" + randomPassword + "\"," +
                        "    \"firstName\": \"" + randomFirstName + "\"" +
                        "}")
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true)); // Проверяем значение "ok": true
    }

    // Получаем идентификатор курьера
    @Step("Получаем идентификатор курьера")
    private void getCourierID(String randomLogin, String randomPassword) {
        var response = given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + randomLogin + "\"," +
                        "    \"password\": \"" + randomPassword + "\"" +
                        "}")
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Извлекаем идентификатор
        int createdID = response.path("id");
        this.lastUsedID = createdID;
    }

    // Нельзя создать двух одинаковых курьеров, при попытке создать существующего курьера возвращается ошибка
    @Test
    @Description("Нельзя создать двух одинаковых курьеров")
    public void cannotCreateDuplicateCourier() {
        String randomLogin = generateUniqueLogin();
        String randomPassword = faker.internet().password();
        String randomFirstName = faker.name().firstName();

        // Первый раз создание проходит успешно
        createCourier(randomLogin, randomPassword, randomFirstName);

        // Повторное создание
        duplicateCourierCreation(randomLogin, randomPassword, randomFirstName);

        // Получаем идентификатор курьера
        getCourierID(randomLogin, randomPassword);
    }

    // Повторное создание курьеров
    @Step("Повторное создание курьера")
    private void duplicateCourierCreation(String randomLogin, String randomPassword, String randomFirstName) {
        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + randomLogin + "\"," +
                        "    \"password\": \"" + randomPassword + "\"," +
                        "    \"firstName\": \"" + randomFirstName + "\"" +
                        "}")
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(409); // Ошибка конфликта
    }

    // Нужно передавать все обязательные поля
    @Test
    @Description("Нужно передавать все обязательные поля")
    public void missingFieldReturnsError() {
        String randomLogin = generateUniqueLogin();

        // Создаём курьера с отсутствующими обязательными полями
        missingRequiredField(randomLogin);
    }

    // Создаём курьера с отсутствием обязательных полей
    @Step("Создаём курьера с логином '{randomLogin}' и отсутствующим паролем")
    private void missingRequiredField(String randomLogin) {
        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + randomLogin + "\"" +
                        "}") // Пропущен пароль
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(400); // Ожидаемая ошибка
    }
}