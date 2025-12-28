package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;
import test.models.GetOrderTestRequest;
import test.models.GetOrderTestResponseBody;

public class GetOrderTest {

    // ENDPOINT запроса получения списка заказов
    private static final String ORDERS_ENDPOINT = "/orders";

    // Загружаем baseURI из config.properties
    @BeforeAll
    public static void setup() throws IOException {
        Properties props = new Properties();
        InputStream input = GetOrderTest.class.getClassLoader().getResourceAsStream("config.properties");
        if (input != null) {
            props.load(input);
            String baseUrl = props.getProperty("base.url");
            RestAssured.baseURI = baseUrl;
        } else {
            throw new RuntimeException("Файл config.properties не найден.");
        }
    }

    @Test
    @Description("Проверка, что список заказов не пустой")
    @Step("Получение списка заказов")
    public void simpleGetOrdersTest() {
        ValidatableResponse response =
                RestAssured.given()
                        .queryParam("limit", 2)
                        .queryParam("page", 0)
                        .when()
                        .get(ORDERS_ENDPOINT)
                        .then()
                        .statusCode(200);

        GetOrderTestResponseBody responseBody = response.extract().as(GetOrderTestResponseBody.class);
        assertThat(responseBody.getOrders()).isNotNull().isNotEmpty();
    }
}