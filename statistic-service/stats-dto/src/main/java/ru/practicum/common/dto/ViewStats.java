package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ViewStats {

    private String app;

    private String uri;

    private Long hits;
}