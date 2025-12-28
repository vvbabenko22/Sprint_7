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
import test.models.LoginCourierRequest;
import static io.restassured.RestAssured.given;
import static io.restassured.config.SSLConfig.sslConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginCourierTest {

    // ENDPOINT запросов
    private static String BASE_URL;
    private final String COURIER_CREATE_ENDPOINT = "/courier"; // Эндпоинт для создания
    private final String COURIER_LOGIN_ENDPOINT = "/courier/login"; // Эндпоинт для авторизации
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
        InputStream input = LoginCourierTest.class.getClassLoader().getResourceAsStream("config.properties");
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

    // Курьер может авторизоваться и успешный запрос возвращает id
    @Test
    @Description("Курьер может авторизоваться и получает верный id")
    public void courrierCanLogInAndGetID() {
        String randomLogin = generateUniqueLogin();
        String randomPassword = faker.internet().password();
        String randomFirstName = faker.name().firstName();

        // Создаем курьера
        createCourier(randomLogin, randomPassword, randomFirstName);

        // Авторизуемся
        loginAsCourier(randomLogin, randomPassword);
    }

    // Создаем курьера
    @Step("Создаем курьера с логином, паролем и именем")
    private void createCourier(String randomLogin, String randomPassword, String randomFirstName) {
        LoginCourierRequest request = new LoginCourierRequest(); // Используем общий POJO-класс
        request.setLogin(randomLogin);
        request.setPassword(randomPassword);
        request.setFirstName(randomFirstName);

        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(request) // Используем общий POJO-класс
                .when()
                .post(COURIER_CREATE_ENDPOINT)
                .then()
                .statusCode(201) // Проверка на успешный статус-код
                .body("ok", equalTo(true)); // Проверяем значение "ok": true
    }

    // Авторизуемся
    @Step("Авторизуемся курьером с логином и паролем")
    private void loginAsCourier(String randomLogin, String randomPassword) {
        LoginCourierRequest request = new LoginCourierRequest(); // Используем общий POJO-класс
        request.setLogin(randomLogin);
        request.setPassword(randomPassword);

        LoginCourierRequest response = given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(request) // Используем общий POJO-класс
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(200) // Проверка на успешный статус-код
                .body("id", notNullValue()) // Проверяем, что в ответе присутствует id и оно не пустое
                .extract()
                .as(LoginCourierRequest.class);

        // Извлекаем идентификатор
        int authID = response.getId();
        this.lastUsedID = authID;
    }

    // Для авторизации нужно передать все обязательные поля
    @Test
    @Description("Авторизация невозможна без обязательных полей")
    public void requiredAllFieldsForAuth() {
        // Пробуем залогиниться с пустым паролем
        tryLoginWithMissingField("login", "");
    }

    // Пробуем залогиниться с одним обязательным полем, оставляем пароль пустым
    @Step("Пытаемся залогиниться с логином и пустым паролем")
    private void tryLoginWithMissingField(String login, String password) {
        LoginCourierRequest request = new LoginCourierRequest(); // Используем общий POJO-класс
        request.setLogin(login);
        request.setPassword(password); // Установим пустой пароль

        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(request) // Используем общий POJO-класс
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(400) // Проверяем статус-код "Bad request"
                .body("message", equalTo("Недостаточно данных для входа")); // Проверяем сообщение об ошибке
    }

    // Невозможно залогиниться под несуществующим пользователем, либо неправильным логином/паролем
    @Test
    @Description("Невозможно залогиниться с произвольными данными")
    public void invalidCredentialsReturnError() {
        // Пробуем залогиниться с произвольными данными
        tryLoginWithRandomData("invalid_login", "invalid_password");
    }

    // Пробуем залогиниться с произвольными данными
    @Step("Пытаемся залогиниться с произвольными данными: логин, пароль")
    private void tryLoginWithRandomData(String login, String password) {
        LoginCourierRequest request = new LoginCourierRequest(); // Используем общий POJO-класс
        request.setLogin(login);
        request.setPassword(password);

        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(request) // Используем общий POJO-класс
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(404) // Проверяем статус-код "Not found"
                .body("message", equalTo("Учетная запись не найдена")); // Проверяем сообщение об ошибке
    }

    // Если какого-то поля нет, запрос возвращает ошибку
    @Test
    @Description("Отсутствие обязательного поля возвращает ошибку")
    @Step("Тестируем отсутствие необходимого поля в запросе")
    public void missingFieldReturnsError() {
        // Отсутствие поля "пароль"
        LoginCourierRequest request = new LoginCourierRequest(); // Используем общий POJO-класс
        request.setLogin("some_login");

        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(request) // Используем общий POJO-класс
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(504) // Проверяем статус-код "Bad request"
                .body(equalTo("Service unavailable")); // Проверяем сообщение об ошибке
    }

    // Удаляем данные после теста courrierCanLogInAndGetID
    @AfterEach
    public void cleanup() {
        deleteCourierIfExists();
    }

    // Удаляем курьера, если он был создан
    @Step("Удаляем курьера с идентификатором '{lastUsedID}'")
    private void deleteCourierIfExists() {
        if (this.lastUsedID != null) {
            given()
                    .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                    .when()
                    .delete(String.format(COURIER_DELETE_ENDPOINT, lastUsedID))
                    .then()
                    .statusCode(200)
                    .body("ok", equalTo(true)); // Проверяем, что ответ содержит {"ok": true}
        }
    }
}