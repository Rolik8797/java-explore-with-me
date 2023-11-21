package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage extends JpaRepository<Stats, Integer> {

    @Query("SELECT new ru.practicum.ViewStats(s.application.appName, s.uri, COUNT(s.ip))" +
            " FROM Stats s" +
            " WHERE s.timestamp >= :start" +
            " AND s.timestamp <= :end" +
            " GROUP BY s.application.appName, s.uri" +
            " ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> findStatsAll(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.ViewStats(s.application.appName, s.uri, COUNT(DISTINCT s.ip))" +
            " FROM Stats s" +
            " WHERE s.timestamp >= :start" +
            " AND s.timestamp <= :end" +
            " GROUP BY s.application.appName, s.uri" +
            " ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ViewStats> findStatsAllWithUniqueIp(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.ViewStats(s.application.appName, s.uri, COUNT(DISTINCT s.ip))" +
            " FROM Stats s" +
            " WHERE s.uri IN :uris" +
            " AND s.timestamp >= :start" +
            " AND s.timestamp <= :end" +
            " GROUP BY s.application.appName, s.uri" +
            " ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ViewStats> findStatsForUrisWithUniqueIp(@Param("uris") String[] uris,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.ViewStats(s.application.appName, s.uri, COUNT(s.ip))" +
            " FROM Stats s" +
            " WHERE s.uri IN :uris" +
            " AND s.timestamp >= :start" +
            " AND s.timestamp <= :end" +
            " GROUP BY s.application.appName, s.uri" +
            " ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> findStatsForUris(@Param("uris") String[] uris,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);


}
