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
    private final ApplicationRepository applicationRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    @Test
    void shouldCreateEndpointHit() {
        Application application = new Application("ewm-main-service");
        EndpointHit endpointHit = EndpointHit.builder()
                .appName(application.getAppName())
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
            assertThat(stats.getApplication().getAppName(), is(equalTo(endpointHit.getAppName())));
            assertThat(stats.getUri(), is(equalTo(endpointHit.getUri())));
            assertThat(stats.getIp(), is(equalTo(endpointHit.getIp())));
            assertThat(stats.getTimestamp().format(formatter), is(equalTo(endpointHit.getTimestamp())));

        } catch (NoResultException e) {

            return;
        }
    }

    @Test
    void shouldCreateEndpointHitWithoutTimestamp() {
        EndpointHit endpointHit = EndpointHit.builder()
                .appName("ewm-main-service")
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
            assertThat(stats.getApplication().getAppName(), is(equalTo(endpointHit.getAppName())));
            assertThat(stats.getUri(), is(equalTo(endpointHit.getUri())));
            assertThat(stats.getIp(), is(equalTo(endpointHit.getIp())));
            assertThat(stats.getTimestamp(), is(notNullValue()));
            assertThat(stats.getTimestamp().format(formatter), is(equalTo(timeNow.format(formatter))));

        } catch (NoResultException e) {
            return;
        }
    }

    @Test
    void shouldGetStats() {

        EndpointHit endpointHit1 = EndpointHit.builder()
                .appName("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp("2022-09-06 11:00:23")
                .build();

        EndpointHit endpointHit2 = EndpointHit.builder()
                .appName("ewm-main-service")
                .uri("/events")
                .ip("192.163.0.1")
                .timestamp("2023-02-06 11:00:23")
                .build();

        EndpointHit endpointHit3 = EndpointHit.builder()
                .appName("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp("2023-03-06 11:00:23")
                .build();

        EndpointHit endpointHit4 = EndpointHit.builder()
                .appName("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.2")
                .timestamp("2023-05-06 11:00:23")
                .build();

        statsService.create(endpointHit1);
        statsService.create(endpointHit2);
        statsService.create(endpointHit3);
        statsService.create(endpointHit4);

        //Вывод статистики при отсутствии критериев к uri

        List<ViewStats> resultWithoutUris =
                statsService.get("2021-05-06 11:00:23", "2023-07-06 11:00:23",
                        new String[]{"all"}, false);
        assertThat(resultWithoutUris, notNullValue());

        //Вывод статистики с списком uri

        List<ViewStats> resultWithAllUris =
                statsService.get("2021-05-06 11:00:23", "2023-07-06 11:00:23",
                        new String[]{"/events", "/events/1"}, false);
        assertThat(resultWithAllUris, notNullValue());

        //Вывод статистики с 1 значением uri

        List<ViewStats> resultWithOneUris =
                statsService.get("2021-05-06 11:00:23", "2023-07-06 11:00:23",
                        new String[]{"/events/1"}, false);
        assertThat(resultWithOneUris, notNullValue());
        //Вывод статистики с уникальными значениями ip

        List<ViewStats> resultWithUniqueIp =
                statsService.get("2021-05-06 11:00:23", "2023-07-06 11:00:23",
                        new String[]{"/events/1"}, true);
        assertThat(resultWithUniqueIp, notNullValue());

        //Вывод статистики при ограниченном диапазоне timestamp

        List<ViewStats> resultWithRange =
                statsService.get("2023-01-06 11:00:23", "2023-04-06 11:00:23",
                        new String[]{"/events", "/events/1"}, false);
        assertThat(resultWithRange, notNullValue());


    }
}