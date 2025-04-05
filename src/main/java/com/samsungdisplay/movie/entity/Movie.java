package com.samsungdisplay.movie.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Movie {
    private String id;
    private String title;
    private String director;
    private LocalDate releaseDate;
}