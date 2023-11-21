package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.Application;
import ru.practicum.model.Stats;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceServiceTest {


    private final EntityManager em;
    private final StatsService statsService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ApplicationRepository applicationRepository;


    @Test
    void shouldCreateEndpointHit() {
        Application application = new Application("ewm-main-service");
        EndpointHit endpointHit = EndpointHit.builder()
                .app(application.getAppName())
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp("2022-09-06 11:00:23")
                .build();

        statsService.create(endpointHit);

        TypedQuery<Stats> queryForItem = em.createQuery("SELECT s FROM Stats s WHERE s.id = :id", Stats.class);
        queryForItem.setParameter("id", 1);

        try {
            Stats stats = queryForItem.getSingleResult();

            assertThat(stats.getId(), is(notNullValue()));
            assertThat(stats.getId(), is(equalTo(1)));
            assertThat(stats.getApplication().getAppName(), is(equalTo(endpointHit.getApp())));
            assertThat(stats.getUri(), is(equalTo(endpointHit.getUri())));
            assertThat(stats.getIp(), is(equalTo(endpointHit.getIp())));
            assertThat(stats.getTimestamp().format(DATE_FORMATTER), is(equalTo(endpointHit.getTimestamp())));

        } catch (NoResultException e) {

            return;
        }
    }

    @Test
    void shouldCreateEndpointHitWithoutTimestamp() {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.2")
                .build();
        LocalDateTime timeNow = LocalDateTime.now();
        statsService.create(endpointHit);

        TypedQuery<Stats> queryForItem = em.createQuery("SELECT s FROM Stats s WHERE s.ip = :ip", Stats.class);
        queryForItem.setParameter("ip", "192.163.0.2");

        try {
            Stats stats = queryForItem.getSingleResult();

            assertThat(stats.getId(), is(notNullValue()));
            assertThat(stats.getId(), is(equalTo(1)));
            assertThat(stats.getApplication().getAppName(), is(equalTo(endpointHit.getApp())));
            assertThat(stats.getUri(), is(equalTo(endpointHit.getUri())));
            assertThat(stats.getIp(), is(equalTo(endpointHit.getIp())));
            assertThat(stats.getTimestamp(), is(notNullValue()));
            assertThat(stats.getTimestamp().format(DATE_FORMATTER), is(equalTo(timeNow.format(DATE_FORMATTER))));

        } catch (NoResultException e) {
           return;
        }
    }

    @Test
    void shouldGetStats() {
        // Sample endpoint hits
        EndpointHit endpointHit1 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp("2022-09-06 11:00:23")
                .build();

        EndpointHit endpointHit2 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip("192.163.0.1")
                .timestamp("2023-02-06 11:00:23")
                .build();

        EndpointHit endpointHit3 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp("2023-03-06 11:00:23")
                .build();

        EndpointHit endpointHit4 = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.2")
                .timestamp("2023-05-06 11:00:23")
                .build();

        // Create endpoint hits
        statsService.create(endpointHit1);
        statsService.create(endpointHit2);
        statsService.create(endpointHit3);
        statsService.create(endpointHit4);

        // Вывод статистики при отсутствии критериев к uri
        List<ViewStats> resultWithoutUris = statsService.get(
                LocalDateTime.parse("2021-05-06 11:00:23", DATE_FORMATTER),
                LocalDateTime.parse("2023-07-06 11:00:23", DATE_FORMATTER),
                new String[]{"all"},
                false);

        assertThat(resultWithoutUris, is(notNullValue()));
        assertThat(resultWithoutUris.size(), is(2));
        assertThat(resultWithoutUris.get(0).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithoutUris.get(0).getUri(), is(equalTo("/events/1")));
        assertThat(resultWithoutUris.get(0).getHits(), is(3));
        assertThat(resultWithoutUris.get(1).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithoutUris.get(1).getUri(), is(equalTo("/events")));
        assertThat(resultWithoutUris.get(1).getHits(), is(1));

        // Вывод статистики с списком uri
        List<ViewStats> resultWithAllUris = statsService.get(
                LocalDateTime.parse("2021-05-06 11:00:23", DATE_FORMATTER),
                LocalDateTime.parse("2023-07-06 11:00:23", DATE_FORMATTER),
                new String[]{"/events", "/events/1"},
                false);

        assertThat(resultWithAllUris, is(notNullValue()));
        assertThat(resultWithAllUris.size(), is(2));
        assertThat(resultWithAllUris, is(equalTo(resultWithoutUris)));

        // Вывод статистики с 1 значением uri
        List<ViewStats> resultWithOneUris = statsService.get(
                LocalDateTime.parse("2021-05-06 11:00:23", DATE_FORMATTER),
                LocalDateTime.parse("2023-07-06 11:00:23", DATE_FORMATTER),
                new String[]{"/events/1"},
                false);

        assertThat(resultWithOneUris, is(notNullValue()));
        assertThat(resultWithOneUris.size(), is(1));
        assertThat(resultWithOneUris.get(0).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithOneUris.get(0).getUri(), is(equalTo("/events/1")));
        assertThat(resultWithOneUris.get(0).getHits(), is(3));

        // Вывод статистики с уникальными значениями ip
        List<ViewStats> resultWithUniqueIp = statsService.get(
                LocalDateTime.parse("2021-05-06 11:00:23", DATE_FORMATTER),
                LocalDateTime.parse("2023-07-06 11:00:23", DATE_FORMATTER),
                new String[]{"/events/1"},
                true);

        assertThat(resultWithUniqueIp, is(notNullValue()));
        assertThat(resultWithUniqueIp.size(), is(1));
        assertThat(resultWithUniqueIp.get(0).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithUniqueIp.get(0).getUri(), is(equalTo("/events/1")));
        assertThat(resultWithUniqueIp.get(0).getHits(), is(2));

        // Вывод статистики при ограниченном диапазоне timestamp
        List<ViewStats> resultWithRange = statsService.get(
                LocalDateTime.parse("2023-01-06 11:00:23", DATE_FORMATTER),
                LocalDateTime.parse("2023-04-06 11:00:23", DATE_FORMATTER),
                new String[]{"/events", "/events/1"},
                false);

        assertThat(resultWithRange, is(notNullValue()));
        assertThat(resultWithRange.size(), is(2));
        assertThat(resultWithRange.get(0).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithRange.get(0).getUri(), is(equalTo("/events")));
        assertThat(resultWithRange.get(0).getHits(), is(1));
        assertThat(resultWithRange.get(1).getAppName(), is(equalTo("ewm-main-service")));
        assertThat(resultWithRange.get(1).getUri(), is(equalTo("/events/1")));
        assertThat(resultWithRange.get(1).getHits(), is(1));
    }
}
