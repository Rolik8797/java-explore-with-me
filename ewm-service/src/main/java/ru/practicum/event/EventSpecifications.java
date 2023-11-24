package ru.practicum.event;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.model.Event;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventSpecifications {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Specification<Event> getByAdminSpec(Integer[] users, String[] states, Integer[] categories,
                                                      String rangeStart, String rangeEnd) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && users[0] != 0) {
                predicates.add(criteriaBuilder.in(root.get("initiator").get("id")).value(Arrays.asList(users)));
            }

            if (states != null) {
                List<State> stateEnums = Arrays.stream(states)
                        .map(State::valueOf)  // Convert string to State enum
                        .collect(Collectors.toList());

                predicates.add(root.get("state").in(stateEnums));
            }

            if (categories != null && categories[0] != 0) {
                predicates.add(criteriaBuilder.in(root.get("category").get("id")).value(Arrays.asList(categories)));
            }

            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.parse(rangeStart, formatter)));
            }

            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), LocalDateTime.parse(rangeEnd, formatter)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> getPubliclySpec(String text, Integer[] categories, Boolean paid,
                                                       String rangeStart, String rangeEnd, Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (text != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("annotation"), "%" + text + "%"),
                        criteriaBuilder.like(root.get("description"), "%" + text + "%")
                ));
            }
            if (categories != null) {
                predicates.add(criteriaBuilder.in(root.get("category").get("id")).value(Arrays.asList(categories)));
            }
            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.parse(rangeStart, formatter)));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), LocalDateTime.parse(rangeEnd, formatter)));
            }
            if (rangeStart == null && rangeEnd == null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            }

            if (onlyAvailable) {
                predicates.add(criteriaBuilder.notEqual(root.get("participantLimit"), root.get("confirmedRequests")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}