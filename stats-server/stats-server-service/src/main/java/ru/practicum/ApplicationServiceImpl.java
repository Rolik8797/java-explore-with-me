package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.model.Application;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;

    @Override
    public Optional<Application> getByName(String appName) {
        return applicationRepository.findByAppName(appName);
    }

    @Override
    public Application save(Application application) {
        Application app = applicationRepository.save(application);
        log.info("Выполнено сохранение записи о новом приложении {}.", application);
        return app;
    }
}