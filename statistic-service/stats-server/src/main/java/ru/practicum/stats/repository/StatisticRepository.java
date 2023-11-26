package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.common.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM EndpointHit as st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getAllStatisticByDistinctIp(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.common.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM EndpointHit as st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getAllStatistic(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.common.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM EndpointHit as st " +
            "WHERE st.uri IN (:uri) " +
            "AND st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getStatisticByUrisDistinctIps(
            @Param("uri") List<String> uri,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT NEW ru.practicum.common.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM EndpointHit as st " +
            "WHERE st.uri IN(:uri) " +
            "AND st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getStatisticByUris(
            @Param("uri") List<String> uri,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}