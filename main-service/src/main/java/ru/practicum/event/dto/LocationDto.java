package ru.practicum.event.dto;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class LocationDto {
    float lat;
    float lon;
}