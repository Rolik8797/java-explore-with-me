package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatsServiceClient extends HttpRequestClient {

    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_START = "/stats";

    @Autowired
    public StatsServiceClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postEndpointHit(EndpointHit hitRequestDto) {
        return post(API_PREFIX_HIT, hitRequestDto);
    }

    public List<ViewStats> getStatistic(
            LocalDateTime start, LocalDateTime end,
            List<String> uris, Boolean unique
    ) {

        Map<String, Object> parameters = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        parameters.put("start", start.format(formatter));
        parameters.put("end", end.format(formatter));
        parameters.put("uris", String.join(",", uris));
        parameters.put("unique", unique);
        var query = "?start={start}&end={end}&uris={uris}&unique={unique}";
        var view = get(API_PREFIX_START + query, parameters);
        ResponseEntity<List<ViewStats>> response = rest.exchange(API_PREFIX_START + query,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {

                }, parameters
        );

        return response.getBody();

    }
}