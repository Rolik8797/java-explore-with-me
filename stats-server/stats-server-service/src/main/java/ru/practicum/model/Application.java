package ru.practicum.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app", nullable = false, updatable = false)
    private Long id;

    @Column(name = "app_name", nullable = false)
    private String name;

    public Application(String name) {
        this.name = name;
    }
}