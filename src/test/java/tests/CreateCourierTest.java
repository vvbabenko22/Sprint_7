package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import test.models.CreateCourierRequest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateCourierTest {

    // ENDPOINT запросов
    private static String BASE_URL;
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

    // Загружаем baseURI из config.properties
    @BeforeAll
    public static void setup() throws IOException {
        Properties props = new Properties();
        InputStream input = CreateCourierTest.class.getClassLoader().getResourceAsStream("config.properties");
        if (input != null) {
            props.load(input);
            BASE_URL = props.getProperty("base.url");
            RestAssured.baseURI = BASE_URL;
        } else {
            throw new RuntimeException("Файл config.properties не найден.");
        }
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
                    .when()
                    .delete(String.format(COURIER_DELETE_ENDPOINT, lastUsedID)) // Удаляем по ID
                    .then()
                    .statusCode(200)
                    .body("ok", equalTo(true));
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
        CreateCourierRequest request = new CreateCourierRequest(); // POJO-класс
        request.setLogin(randomLogin);
        request.setPassword(randomPassword);
        request.setFirstName(randomFirstName);

        given()
                .contentType("application/json")
                .body(request) // Используем POJO-класс
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
        CreateCourierRequest request = new CreateCourierRequest(); // Используем POJO-класс
        request.setLogin(randomLogin);
        request.setPassword(randomPassword);
        request.setFirstName(randomFirstName);

        given()
                .contentType("application/json")
                .body(request) // Используем POJO-класс
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(409) // Ошибка конфликта
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой.")); // Проверяем сообщение об ошибке
    }

    // Нужно передавать все обязательные поля
    @Test
    @Description("Нужно передавать все обязательные поля")
    public void missingFieldReturnsError() {
        String randomLogin = generateUniqueLogin();

        // Создаём курьера с отсутствием обязательных полей
        missingRequiredField(randomLogin);
    }

    // Создаём курьера с отсутствием обязательных полей
    @Step("Создаём курьера с логином и отсутствующим паролем")
    private void missingRequiredField(String randomLogin) {
        CreateCourierRequest request = new CreateCourierRequest(); // Используем POJO-класс
        request.setLogin(randomLogin); // Устанавливаем только логин

        given()
                .contentType("application/json")
                .body(request) // Используем POJO-класс
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(400) // Ожидаемая ошибка
                .body("message", equalTo("Недостаточно данных для создания учетной записи")); // Проверяем сообщение об ошибке
    }
}