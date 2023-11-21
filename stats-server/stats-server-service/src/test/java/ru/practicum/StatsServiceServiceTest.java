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
}