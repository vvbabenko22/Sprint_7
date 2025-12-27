package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ResponseBody {

    // Список заказов
    @JsonProperty("orders")
    private List<Order> orders;

    // Информации о постраничной навигации
    @JsonProperty("pageInfo")
    private PageInfo pageInfo;

    // Список доступных станций метро
    @JsonProperty("availableStations")
    private List<AvailableStation> availableStations;

    // Геттеры и сеттеры
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<AvailableStation> getAvailableStations() {
        return availableStations;
    }

    public void setAvailableStations(List<AvailableStation> availableStations) {
        this.availableStations = availableStations;
    }

    // Внутренний класс для хранения информации о странице
    public static class PageInfo {
        @JsonProperty("page")
        private Integer page;

        @JsonProperty("total")
        private Integer total;

        @JsonProperty("limit")
        private Integer limit;

        // Геттеры и сеттеры
        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }
    }

    // Внутренний класс для хранения информации о станции метро
    public static class AvailableStation {
        @JsonProperty("name")
        private String name;

        @JsonProperty("number")
        private String number;

        @JsonProperty("color")
        private String color;

        // Геттеры и сеттеры
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}