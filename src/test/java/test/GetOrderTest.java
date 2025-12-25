package test;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

class GetOrderTest {

    // API URL
    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1";
    private final String ORDERS_ENDPOINT = "/orders";

    // Тест получения заказов
    @Test
    @Description("Проверка получения списка заказов")
    public void getOrdersTest() {
        makeGetRequest(); // Выделяем отправку запроса в отдельный метод
    }

    // Отправка запроса на получение заказов
    @Step("Делаем запрос на получение заказов с лимитом 2 и первой страницей")
    private void makeGetRequest() {
        given()
                .baseUri(BASE_URL)
                .queryParam("limit", 2) // Лимит: 2 заказа
                .queryParam("page", 0)  // Страница: первая страница
                .when()
                .get(ORDERS_ENDPOINT)
                .then()
                .statusCode(200) // Проверяем успешный статус
                .body("orders", hasSize(greaterThanOrEqualTo(1))); // Проверяем, что список заказов не пустой
    }
}