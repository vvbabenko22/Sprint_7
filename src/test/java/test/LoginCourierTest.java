package test;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static io.restassured.config.SSLConfig.sslConfig;

public class LoginCourierTest {

    // API URL
    private final String BASE_URL = "https://qa-scooter.praktikum-Services.ru/api/v1";
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
    @Step("Создаем курьера с логином '{randomLogin}', паролем '{randomPassword}' и именем '{randomFirstName}'")
    private void createCourier(String randomLogin, String randomPassword, String randomFirstName) {
        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
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
                .statusCode(201);
    }

    // Авторизуемся
    @Step("Авторизуемся курьером с логином '{randomLogin}' и паролем '{randomPassword}'")
    private void loginAsCourier(String randomLogin, String randomPassword) {
        var response = given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
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
        int authID = response.path("id");
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
    @Step("Пытаемся залогиниться с логином '{login}' и пустым паролем")
    private void tryLoginWithMissingField(String login, String password) {
        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + login + "\"," +
                        "    \"password\": \"\"" +
                        "}")
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(400); // ожидаем Bad Request
    }

    // Невозможно залогиниться под несуществующим пользователем, либо неправильным логином/паролем
    @Test
    @Description("Невозможно залогиниться с произвольными данными")
    public void invalidCredentialsReturnError() {
        // Пробуем залогиниться с произвольными данными
        tryLoginWithRandomData("invalid_login", "invalid_password");
    }

    // Пробуем залогиниться с произвольными данными
    @Step("Пытаемся залогиниться с произвольными данными: логин '{login}', пароль '{password}'")
    private void tryLoginWithRandomData(String login, String password) {
        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"" + login + "\"," +
                        "    \"password\": \"" + password + "\"" +
                        "}")
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(404); // ожидаем Not Found
    }

    /* Если какого-то поля нет, запрос возвращает ошибку
    @Test
    @Description("Отсутствие обязательного поля возвращает ошибку")
    public void missingFieldReturnsError() {
        // Отсутствие поля "пароль"
        given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{" +
                        "    \"login\": \"any_login\"" +
                        "}")
                .when()
                .post(COURIER_LOGIN_ENDPOINT)
                .then()
                .statusCode(400); // ожидаем Bad Request
    } */

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
                    .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
                    .baseUri(BASE_URL)
                    .when()
                    .delete(String.format(COURIER_DELETE_ENDPOINT, lastUsedID))
                    .then()
                    .statusCode(200);
        }
    }
}