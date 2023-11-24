package ru.practicum.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.event.model.Event;

import java.util.List;

public interface EventStorage extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    Boolean existsByCategoryId(Integer catId);

    List<Event> findAllByInitiatorId(Integer userId, Pageable sortedAndPageable);

    Boolean existsByIdAndState(Integer eventId, State state);
}