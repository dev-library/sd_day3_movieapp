package com.samsungdisplay.movie;
import com.samsungdisplay.movie.document.MovieDocument;
import com.samsungdisplay.movie.service.MetricPushService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;

@SpringBootApplication
//@EnableScheduling // 주석처리시 메트릭 발송 중지
public class MovieApplication {

    private final MetricPushService metricPushService;

    MovieApplication(MetricPushService metricPushService) {
        this.metricPushService = metricPushService;
    }

	public static void main(String[] args) {
		SpringApplication.run(MovieApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner dummyMovieLoader(ElasticsearchClient client) {
	    return args -> {
	        String INDEX_NAME = "movies";

	        // 인덱스 존재 여부 확인
	        boolean indexExists = client.indices().exists(e -> e.index(INDEX_NAME)).value();

	        if (!indexExists) {
	            // 인덱스가 없으면 기본 매핑으로 생성 (필요시 매핑 지정 가능)
	            client.indices().create(c -> c.index(INDEX_NAME));
	            System.out.println("[movies 인덱스] 새로 생성함");
	        }

	        // 문서가 존재하는지 확인
	        SearchResponse<MovieDocument> response = client.search(s -> s
	                        .index(INDEX_NAME)
	                        .size(1)
	                        .query(q -> q.matchAll(m -> m)),
	                MovieDocument.class
	        );

	        long count = response.hits().total() != null ? response.hits().total().value() : 0;

	        if (count > 0) {
	            System.out.println("[movies 인덱스] 이미 " + count + "건의 데이터가 존재합니다.");
	            return;
	        }

	        List<MovieDocument> movies = List.of(
	                new MovieDocument(null, "기생충", "봉준호", LocalDate.of(2019, 5, 30)),
	                new MovieDocument(null, "괴물", "봉준호", LocalDate.of(2006, 7, 27)),
	                new MovieDocument(null, "올드보이", "박찬욱", LocalDate.of(2003, 11, 21)),
	                new MovieDocument(null, "범죄와의 전쟁", "윤종빈", LocalDate.of(2012, 2, 2)),
	                new MovieDocument(null, "도둑들", "최동훈", LocalDate.of(2012, 7, 25)),
	                new MovieDocument(null, "암살", "최동훈", LocalDate.of(2015, 7, 22)),
	                new MovieDocument(null, "택시운전사", "장훈", LocalDate.of(2017, 8, 2)),
	                new MovieDocument(null, "7번방의 선물", "이환경", LocalDate.of(2013, 1, 23)),
	                new MovieDocument(null, "극한직업", "이병헌", LocalDate.of(2019, 1, 23)),
	                new MovieDocument(null, "서울의 봄", "김성수", LocalDate.of(2023, 11, 22))
	        );

	        for (MovieDocument movie : movies) {
	            client.index(i -> i
	                    .index(INDEX_NAME)
	                    .document(movie)
	            );
	        }

	        System.out.println("[movies 인덱스] 더미 영화 데이터 10건 자동 등록 완료!");
	    };
	}
}
