package ru.practicum;

import ru.practicum.model.Application;

import java.util.Optional;

public interface ApplicationService {
    Optional<Application> getByName(String appMame);

    Application save(Application application);
}