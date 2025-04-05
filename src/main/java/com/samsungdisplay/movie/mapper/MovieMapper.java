package com.samsungdisplay.movie.mapper;

import com.samsungdisplay.movie.document.MovieDocument;
import com.samsungdisplay.movie.entity.Movie;

public class MovieMapper {

    public static MovieDocument toDocument(Movie movie) {
        return new MovieDocument(
            movie.getId(),
            movie.getTitle(),
            movie.getDirector(),
            movie.getReleaseDate()
        );
    }

    public static Movie toEntity(MovieDocument doc) {
        Movie movie = new Movie();

        movie.setId(doc.getId());
        movie.setTitle(doc.getTitle());
        movie.setDirector(doc.getDirector());
        movie.setReleaseDate(doc.getReleaseDate());
        return movie;
    }
}