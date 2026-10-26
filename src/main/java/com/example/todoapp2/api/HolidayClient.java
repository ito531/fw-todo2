package com.example.todoapp2.api;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HolidayClient {

    private final RestClient restClient = RestClient.create(createRestTemplate());

    public HolidayResult getHolidays(LocalDate from, LocalDate to) {
        try {
            Map<String, String> holidays = restClient.get()
                    .uri("https://holidays-jp.github.io/api/v1/date.json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });

            if (from == null && to == null) {
                return new HolidayResult(holidays, false);
            }

            Map<String, String> filteredHolidays = new LinkedHashMap<>();
            for (Map.Entry<String, String> holiday : holidays.entrySet()) {
                LocalDate date = LocalDate.parse(holiday.getKey());
                if ((from == null || !date.isBefore(from))
                        && (to == null || !date.isAfter(to))) {
                    filteredHolidays.put(holiday.getKey(), holiday.getValue());
                }
            }
            return new HolidayResult(filteredHolidays, false);
        } catch (RestClientException exception) {
            return new HolidayResult(Collections.emptyMap(), true);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return new RestTemplate(requestFactory);
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
