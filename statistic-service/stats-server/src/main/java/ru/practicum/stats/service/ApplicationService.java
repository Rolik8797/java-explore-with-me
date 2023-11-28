package ru.practicum.stats.service;

import ru.practicum.stats.model.Application;

import java.util.Optional;

public interface ApplicationService {

    Optional<Application> getByName(String appMame);

    Application save(Application application);
}