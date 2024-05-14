package com.example.elasticsearch;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.example.weatherstation.WeatherStatus;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticsearchHandler {

    private static final String SERVER_URL = "http://elastic-kibana:9200";
    private RestClient restClient;
    private ElasticsearchClient esClient;

    public ElasticsearchHandler() {
        // Create the low-level client
        restClient = RestClient
                .builder(HttpHost.create(SERVER_URL))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        esClient = new ElasticsearchClient(transport);
    }

    public void bulkIndex(List<WeatherStatus> weatherStatuses) throws ElasticsearchException, IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        long station_id = weatherStatuses.get(0).getStationId();

        for (WeatherStatus weatherStatus : weatherStatuses) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("station_" + station_id)
                            .id(String.valueOf(weatherStatus.getSerialNo()))
                            .document(weatherStatus)));
        }

        esClient.bulk(br.build());
    }

    public void close() throws IOException {
        restClient.close();
    }
}
