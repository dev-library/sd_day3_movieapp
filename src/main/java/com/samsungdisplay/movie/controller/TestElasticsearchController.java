package com.samsungdisplay.movie.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;

@RestController
public class TestElasticsearchController {


    private final ElasticsearchClient client;

    public TestElasticsearchController(ElasticsearchClient client) {
        this.client = client;
    }

    @GetMapping("/es-info")
    public String getInfo() throws Exception {
        InfoResponse info = client.info();
        return "Cluster name: " + info.clusterName() + ", version: " + info.version().number();
    }
}
