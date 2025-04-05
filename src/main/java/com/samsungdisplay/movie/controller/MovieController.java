package com.samsungdisplay.movie.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.samsungdisplay.movie.document.MovieDocument;
import com.samsungdisplay.movie.entity.Movie;
import com.samsungdisplay.movie.service.MovieElasticsearchService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieElasticsearchService esService;

    /** 목록 + 키워드 검색 */
    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       Model model) throws IOException {
        List<MovieDocument> movies;

        if (keyword != null && !keyword.isBlank()) {
            movies = esService.searchByTitleOrDirector(keyword);
        } else {
            movies = esService.findAll();
        }

        model.addAttribute("movies", movies);
        model.addAttribute("keyword", keyword);
        return "movies/list";
    }

    /** 새 폼 */
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("movie", new Movie());
        return "movies/form";
    }

    /** 저장 */
    @PostMapping
    public String create(@ModelAttribute Movie movie) throws IOException {
        esService.save(movie);
        return "redirect:/movies";
    }

    /** 상세보기 */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") String id, Model model) throws IOException {
        MovieDocument movie = esService.findById(id);
        model.addAttribute("movie", movie);
        return "movies/detail";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") String id, Model model) throws IOException {
        MovieDocument movie = esService.findById(id);
        model.addAttribute("movie", movie);
        return "movies/form";
    }

    /** 수정 저장 */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") String id,
                         @ModelAttribute Movie movie) throws IOException {
        movie.setId(id);
        esService.save(movie);
        return "redirect:/movies/" + id;
    }

    /** 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id) throws IOException {
        esService.delete(id);
        return "redirect:/movies";
    }

    /** 날짜 범위 검색 */
    @GetMapping("/search/by-date")
    public String searchByDateRange(@RequestParam(name = "from") String from,
                                    @RequestParam(name = "to") String to,
                                    Model model) throws IOException {
        List<MovieDocument> movies = esService.searchByDateRange(LocalDate.parse(from), LocalDate.parse(to));
        model.addAttribute("movies", movies);
        return "movies/list";
    }
}
