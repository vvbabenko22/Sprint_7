package models;

public class Courier {
    private String login;
    private String password;
    private String firstName;

    // Конструктор для создания курьера
    public Courier(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    // Конструктор для авторизации
    public Courier(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Конструктор для теста, когда передано только одно поле — login
    public Courier(String login) {
        this.login = login;
    }

    // Геттеры
    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }
}