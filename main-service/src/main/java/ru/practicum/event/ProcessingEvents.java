package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.client.StatsClient;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.event.model.Event;
import ru.practicum.request.RequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessingEvents {
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public List<Event> addViewsInEventsList(List<Event> events, HttpServletRequest request) {
        List<String> uris = events.stream().map(e -> request.getRequestURI() + "/" + e.getId()).collect(Collectors.toList());
        LocalDateTime start = findStartDateTime(events);
        LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<ViewStats> stats = statsClient.getStats(start, end, uris, true);
        fillEventViews(events, stats, request.getRequestURI());
        return events;
    }

    private void fillEventViews(List<Event> events, List<ViewStats> stats, String baseUri) {
        if (!stats.isEmpty()) {
            Map<String, Long> statsByUri = stats.stream().collect(Collectors.groupingBy(
                    ViewStats::getUri, Collectors.summingLong(ViewStats::getHits)));
            events.forEach(e -> {
                Long views = statsByUri.get(baseUri + "/" + e.getId());
                if (views != null) {
                    e.setViews(views);
                }
            });
        } else {
            events.forEach(e -> e.setViews(0L));
        }
    }

    public List<Event> confirmRequests(List<Event> events) {
        Map<Event, Long> requestsPerEvent = requestRepository.findAllByEventInAndStatus(events, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(Request::getEvent, Collectors.counting()));
        List<Event> newEvents = new ArrayList<>();
        if (!requestsPerEvent.isEmpty()) {
            for (Event e : events) {
                long count = requestsPerEvent.get(e) == null ? 0 : requestsPerEvent.get(e);
                e.setConfirmedRequests(count);
                newEvents.add(e);
            }
        } else {
            newEvents.addAll(events);
            newEvents.forEach(e -> e.setConfirmedRequests(0L));
        }
        return newEvents;
    }

    public long confirmedRequestsForOneEvent(Event event, RequestStatus status) {
        return requestRepository.countRequestByEventAndStatus(event, status);
    }

    public long searchViews(Event event, HttpServletRequest request) {
        LocalDateTime date = LocalDateTime.of(LocalDate.of(1900, 1, 1), LocalTime.of(0, 0, 1));
        LocalDateTime start = event.getPublishedOn() == null ? date : event.getPublishedOn();
        List<ViewStats> stats = statsClient.getStats(start, LocalDateTime.now(), List.of(request.getRequestURI()), true);
        return stats.stream().map(ViewStats::getHits).reduce(0L, Long::sum);
    }

    private LocalDateTime findStartDateTime(List<Event> events) {
        LocalDateTime start;
        Event event = events.stream().sorted(Comparator.comparing(Event::getCreatedOn)).collect(Collectors.toList()).get(0);
        if (event.getPublishedOn() == null) {
            start = LocalDateTime.of(LocalDate.of(1900, 1, 1), LocalTime.of(0, 0, 1));
        } else {
            start = event.getPublishedOn();
        }
        return start;
    }
}