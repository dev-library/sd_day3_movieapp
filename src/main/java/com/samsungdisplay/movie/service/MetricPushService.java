package com.samsungdisplay.movie.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MetricPushService {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public MetricPushService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Scheduled(fixedRate = 60000) // 1분마다
    public void collectAndPushMetrics() {
        try {
            // 1. 메트릭 이름 리스트 조회
            String metricsListJson = restTemplate.getForObject("http://localhost:8080/actuator/metrics", String.class);
            Map<String, Object> metricsList = objectMapper.readValue(metricsListJson, Map.class);
            List<String> names = (List<String>) metricsList.get("names");

            for (String metricName : names) {
                // 2. 개별 메트릭 조회
                String metricJson = restTemplate.getForObject("http://localhost:8080/actuator/metrics/" + metricName, String.class);
                Map<String, Object> metricData = objectMapper.readValue(metricJson, Map.class);

                // 3. timestamp 추가
                metricData.put("@timestamp", Instant.now().toString());

                // 4. Elasticsearch에 인덱싱
                elasticsearchClient.index(i -> i
                    .index("springboot-metrics")
                    .document(metricData)
                );
            }

            log.info("Metrics pushed to Elasticsearch");

        } catch (Exception e) {
            log.error("Error collecting or pushing metrics", e);
        }
    }
}
