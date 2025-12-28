package test.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateOrderRequest {
    private String firstName;
    private String lastName;
    private String address;
    private Integer metroStation;
    private String phone;
    private Integer rentTime;
    private String deliveryDate;
    private String comment;
    private String[] color;
}