package com.financetracker.api.model;

import java.time.Instant;

public class ExampleEntity {

    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public ExampleEntity() {}

    public ExampleEntity(Long id, String name, String email, boolean active,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
