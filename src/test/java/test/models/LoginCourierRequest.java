package test.models;

import lombok.Data;

@Data
public class LoginCourierRequest {
    private String login;
    private String password;
    private String firstName;
    private boolean ok;
    private int id;
}