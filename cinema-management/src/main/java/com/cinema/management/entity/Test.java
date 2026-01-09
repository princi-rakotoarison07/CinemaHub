package com.cinema.management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test") // correspond exactement à ta table en minuscules
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false)
    private Integer port;

    // Constructeur vide obligatoire pour JPA
    public Test() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
