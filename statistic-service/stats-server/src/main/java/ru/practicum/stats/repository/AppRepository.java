package ru.practicum.stats.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.stats.model.App;

import java.util.Optional;

public interface AppRepository extends JpaRepository<App, Long> {
    Optional<App> findByName(String app);
}