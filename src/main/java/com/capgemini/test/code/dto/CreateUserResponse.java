package com.capgemini.test.code.dto;

public class CreateUserResponse {

    private Long id;

    public CreateUserResponse() {
    }

    public CreateUserResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}