package ru.practicum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStats {

    private String app;

    private String uri;

    private Integer hits;

    public ViewStats(String app, String uri, long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = Math.toIntExact(hits);
    }
}