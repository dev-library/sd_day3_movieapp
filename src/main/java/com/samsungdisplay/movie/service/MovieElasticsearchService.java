package com.samsungdisplay.movie.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.samsungdisplay.movie.document.MovieDocument;
import com.samsungdisplay.movie.entity.Movie;
import com.samsungdisplay.movie.mapper.MovieMapper;

import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieElasticsearchService {

    private final ElasticsearchClient client;
    private final String INDEX_NAME = "movies";

    /** 저장 */
    public void save(Movie movie) throws IOException {
        MovieDocument doc = MovieMapper.toDocument(movie);

        client.index(i -> i
            .index(INDEX_NAME)
            .id(doc.getId()) // doc.getId()가 null이면 ES가 UUID 자동 생성
            .document(doc)
            .refresh(Refresh.True)
        );
    }

    /** ID로 조회 */
    public MovieDocument findById(String id) throws IOException {
        GetResponse<MovieDocument> response = client.get(g -> g
            .index(INDEX_NAME)
            .id(id),
            MovieDocument.class
        );

        MovieDocument doc = response.source();
        doc.setId(id); // 직접 주입
        return doc;
    }

    /** 전체 조회 (match_all) */
    public List<MovieDocument> findAll() throws IOException {
        SearchResponse<MovieDocument> response = client.search(s -> s
            .index(INDEX_NAME)
            .size(100) // 전체조회시 조회되는 데이터 100개로 제한
            .query(q -> q.matchAll(m -> m)),
            MovieDocument.class
        );

        return response.hits().hits().stream()
            .map(hit -> {
                MovieDocument doc = hit.source();
                doc.setId(hit.id()); // _id 수동 주입
                return doc;
            })
            .collect(Collectors.toList());
    }

    /** 제목 또는 감독 이름으로 Full-text 검색 */
    public List<MovieDocument> searchByTitleOrDirector(String keyword) throws IOException {
        SearchResponse<MovieDocument> response = client.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q
                .multiMatch(m -> m
                    .fields("title", "director")
                    .query(keyword)
                )
            ),
            MovieDocument.class
        );

        return response.hits().hits().stream()
            .map(hit -> {
                MovieDocument doc = hit.source();
                doc.setId(hit.id()); // _id 수동 주입
                return doc;
            })
            .collect(Collectors.toList());
    }

    /** 날짜 범위 검색 */
    public List<MovieDocument> searchByDateRange(LocalDate from, LocalDate to) throws IOException {
        DateRangeQuery.Builder dateRange = new DateRangeQuery.Builder().field("releaseDate");
        dateRange.gte(from.toString());
        dateRange.lte(to.toString());

        RangeQuery rangeQuery = RangeQuery.of(r -> r.date(dateRange.build()));
        Query query = Query.of(q -> q.range(rangeQuery));

        SearchResponse<MovieDocument> response = client.search(s -> s
            .index(INDEX_NAME)
            .query(query),
            MovieDocument.class
        );

        return response.hits().hits().stream()
            .map(hit -> {
                MovieDocument doc = hit.source();
                doc.setId(hit.id()); // _id 수동 주입
                return doc;
            })
            .collect(Collectors.toList());
    }

    /** 삭제 */
    public void delete(String id) throws IOException {
        client.delete(d -> d
            .index(INDEX_NAME)
            .id(id)
            .refresh(Refresh.True) // 바로 검색 반영
        );
    }
}
