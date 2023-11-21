package ru.practicum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStats {

    private String appName;

    private String uri;

    private Integer hits;

    public ViewStats(String appName, String uri, long hits) {
        this.appName = appName;
        this.uri = uri;
        this.hits = Math.toIntExact(hits);
    }
}