package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.StatsDtoForSave;
import ru.practicum.StatsDtoForView;
import ru.practicum.dto.StatWithHits;

import ru.practicum.model.Application;
import ru.practicum.model.Stat;

@Component
public class StatMapper {

    public Stat mapFromSaveToModel(StatsDtoForSave statsDtoForSave) {
        Stat stat = new Stat();
        stat.setApp(new Application(statsDtoForSave.getApp()));
        return stat;
    }

    public StatsDtoForSave mapToDtoForSave(Stat stat) {
        StatsDtoForSave statsDtoForSave = new StatsDtoForSave();
        statsDtoForSave.setApp(stat.getApp().getApp());
        return statsDtoForSave;
    }

    public StatWithHits mapFromViewToStatDto(StatsDtoForView statsDtoForView) {
        StatWithHits statWithHits = new StatWithHits();
        statWithHits.setApp(statsDtoForView.getApp());
        return statWithHits;
    }

    public StatsDtoForView mapToDtoForView(StatWithHits statWithHits) {
        StatsDtoForView statsDtoForView = new StatsDtoForView();
        statsDtoForView.setApp(statWithHits.getApp());
        return statsDtoForView;
    }
}