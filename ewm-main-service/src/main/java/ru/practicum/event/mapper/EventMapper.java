package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventDate", source = " eventDate")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "createdOn", expression = "java(ru.practicum.utils.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "eventDate", expression = "java(ru.practicum.utils.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "publishedOn", expression = "java(ru.practicum.utils.ExploreDateTimeFormatter.localDateTimeToString(event.getPublishedOn()))")
    @Mapping(target = "views", source = "views")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "eventDate", expression = "java(ru.practicum.utils.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "rangeStart", source = "start")
    @Mapping(target = "rangeEnd", source = "end")
    EventFilterParams toEventFilterParams(EventFilterParamsDto filterDto, LocalDateTime start, LocalDateTime end);

    List<EventShortDto> toEventShortDtoListForEvents(List<Event> events);
}