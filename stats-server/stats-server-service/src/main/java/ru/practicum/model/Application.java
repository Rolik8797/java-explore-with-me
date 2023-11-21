package ru.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId;

    @Column(name = "app_name", nullable = false)
    private String appName;

    public Application(String appName) {
        this.appName = appName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(appId, that.appId) && Objects.equals(appName, that.appName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, appName);
    }

}