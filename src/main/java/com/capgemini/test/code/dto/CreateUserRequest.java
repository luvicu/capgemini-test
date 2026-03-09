package com.capgemini.test.code.dto;

public class CreateUserRequest {

    private String name;
    private String email;
    private String phone;
    private String rol;
    private String dni;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String email, String phone, String rol, String dni) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.rol = rol;
        this.dni = dni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}