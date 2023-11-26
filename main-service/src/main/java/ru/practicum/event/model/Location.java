package ru.practicum.event.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Location {
    private float lat;
    private float lon;
}