package test;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.AfterAll;
import static io.restassured.RestAssured.given;
import static io.restassured.config.SSLConfig.sslConfig;
import static org.hamcrest.Matchers.notNullValue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CreateOrderTest {

    // API URL
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1";
    private final String ORDER_ENDPOINT = "/orders";
    private static final String CANCEL_ORDER_ENDPOINT = "/orders/cancel";

    // Список для хранения номеров заказов
    private static List<Integer> trackNumbers = new ArrayList<>();

    // Метод для генерации аргументов
    private static Stream<Arguments> credentialsProvider() {
        return Stream.of(
                Arguments.of("Иван", "Иванов", "Омск, 11 квартира", "1", "+7 999 999 99 99", 1, "2025-12-27", "1 самокат", "BLACK"),
                Arguments.of("Николай", "Петров", "Томск, 22 квартира", "2", "+7 888 888 88 88", 2, "2025-12-28", "2 самоката", "GREY"),
                Arguments.of("Семён", "Сидоров", "Красноярск, 23 квартира", "3", "+7 777 777 77 77", 3, "2025-12-29", "3 самоката", "BLACK,GREY"),
                Arguments.of("John", "Doe", "Новосибирск, 24 квартира", "4", "+7 666 666 66 66", 4, "2025-12-30", "4 самоката", "")
        );
    }

    // Тест создания заказа с разными наборами данных
    @ParameterizedTest(name = "Создание заказа с данными")
    @MethodSource("credentialsProvider")
    @Description("Проверка создания заказа с различными параметрами")
    public void createOrder(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, String color) {
        // Готовим тело запроса
        String requestBody = prepareRequestBody(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);

        // Создаем заказ
        int trackNumber = sendPostRequest(requestBody);

        // Сохраняем номер заказа
        trackNumbers.add(trackNumber);
    }

    // Готовим тело запроса
    @Step("Подготовка тела запроса с именем '{firstName}', фамилией '{lastName}' и адресом '{address}'")
    private String prepareRequestBody(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, String color) {
        String colorsJson = "";
        if (!color.equals("")) {
            colorsJson = ", \"color\": [\"" + color.replace(",", "\",\"") + "\"]";
        }
        return "{" +
                "    \"firstName\": \"" + firstName + "\"," +
                "    \"lastName\": \"" + lastName + "\"," +
                "    \"address\": \"" + address + "\"," +
                "    \"metroStation\": " + metroStation + "," +
                "    \"phone\": \"" + phone + "\"," +
                "    \"rentTime\": " + rentTime + "," +
                "    \"deliveryDate\": \"" + deliveryDate + "\"," +
                "    \"comment\": \"" + comment + "\"" +
                colorsJson +
                "}";
    }

    // Отправляем POST-запрос на создание заказа
    @Step("Отправка POST-запроса на создание заказа")
    private int sendPostRequest(String requestBody) {
        return given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames())) // Отключаем проверку hostname, иначе ловил ошибку javax.net.ssl.SSLException: Certificate for <qa-scooter.praktikum-Services.ru> doesn't match any of the subject alternative names: [teacher.yandex.ru]
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(ORDER_ENDPOINT)
                .then()
                .statusCode(201)
                .body("track", notNullValue())
                .extract()
                .path("track");
    }

    // Отмена заказов после завершения тестов
    @AfterAll
    public static void cancelOrders() {
        for (Integer track : trackNumbers) {
            cancelOrder(track);
        }
    }

    // Отмена заказа
    @Step("Отмена заказа с номером '{track}'")
    private static void cancelOrder(Integer track) {
        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .queryParam("track", track)
                .when()
                .put(CANCEL_ORDER_ENDPOINT)
                .then()
                .statusCode(200);
    }
}