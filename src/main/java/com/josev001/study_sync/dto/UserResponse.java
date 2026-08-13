package com.josev001.study_sync.dto;

// DTO que representa a resposta do usuário retornada pela API.
public class UserResponse {

    private String id;
    private String activeWorkspace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveWorkspace() {
        return activeWorkspace;
    }

    public void setActiveWorkspace(String activeWorkspace) {
        this.activeWorkspace = activeWorkspace;
    }
}
