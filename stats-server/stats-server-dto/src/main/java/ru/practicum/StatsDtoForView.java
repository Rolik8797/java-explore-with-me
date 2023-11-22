package ru.practicum;

import lombok.*;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class StatsDtoForView {

    private String app;

    private String uri;

    private Long hits;


}