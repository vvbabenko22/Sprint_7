package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import test.models.CreateOrderRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static io.restassured.RestAssured.given;
import static io.restassured.config.SSLConfig.sslConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateOrderTest {

    // ENDPOINT запросов
    private static String BASE_URL;
    private final String ORDER_ENDPOINT = "/orders";
    private static final String CANCEL_ORDER_ENDPOINT = "/orders/cancel";

    // Список для хранения номеров заказов
    private static List<Integer> trackNumbers = new ArrayList<>();

    // Загружаем baseURI из config.properties
    @BeforeAll
    public static void setup() throws IOException {
        Properties props = new Properties();
        InputStream input = CreateOrderTest.class.getClassLoader().getResourceAsStream("config.properties");
        if (input != null) {
            props.load(input);
            BASE_URL = props.getProperty("base.url");
            RestAssured.baseURI = BASE_URL;
        } else {
            throw new RuntimeException("Файл config.properties не найден.");
        }
    }

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
    @ParameterizedTest(name = "Создание заказа с данными: {arguments}")
    @MethodSource("credentialsProvider")
    @Description("Проверка создания заказа с различными параметрами")
    public void createOrder(String firstName, String lastName, String address, String metroStationStr,
                            String phone, int rentTime, String deliveryDate, String comment, String colorStr) {
        // Создаем объект заказа
        CreateOrderRequest order = new CreateOrderRequest(); // Новый POJO-класс
        order.setFirstName(firstName);
        order.setLastName(lastName);
        order.setAddress(address);
        order.setMetroStation(Integer.parseInt(metroStationStr));
        order.setPhone(phone);
        order.setRentTime(rentTime);
        order.setDeliveryDate(deliveryDate);
        order.setComment(comment);

        // Цвета могли прийти через запятую
        if (!colorStr.isEmpty()) {
            order.setColor(colorStr.split(","));
        }

        // Отправляем запрос на создание заказа
        int trackNumber = sendPostRequest(order);

        // Сохраняем номер заказа
        trackNumbers.add(trackNumber);
    }

    // Отправляем POST-запрос на создание заказа
    @Step("Отправка POST-запроса на создание заказа")
    private int sendPostRequest(CreateOrderRequest order) { // Параметр — POJO-класс
        return given()
                .config(RestAssured.config().sslConfig(sslConfig().allowAllHostnames()))
                .contentType("application/json")
                .body(order) // Передаётся объект POJO-класса
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
                .contentType("application/json")
                .queryParam("track", track)
                .when()
                .put(CANCEL_ORDER_ENDPOINT)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true)); // Проверяем значение "ok": true
    }
}