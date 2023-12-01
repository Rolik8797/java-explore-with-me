package ru.practicum.stats.model;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ViewStats {

    private String app;
    private String uri;
    private Long hits;

}