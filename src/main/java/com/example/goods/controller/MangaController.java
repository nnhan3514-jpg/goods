package com.example.goods.controller;

import com.example.goods.model.Manga;
import com.example.goods.model.User;
import com.example.goods.repository.MangaRepository;
import com.example.goods.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/collection/manga")
public class MangaController {

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showMangaPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Manga> mangaList = mangaRepository.findByUser(loggedInUser);
        model.addAttribute("mangas", mangaList);
        model.addAttribute("newManga", new Manga());
        return "manga";
    }

    @PostMapping("/add")
    public String addManga(@ModelAttribute Manga manga, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        manga.setUser(loggedInUser);
        mangaRepository.save(manga);
        return "redirect:/collection/manga";
    }

    @GetMapping("/delete/{id}")
    public String deleteManga(@PathVariable("id") Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Manga manga = mangaRepository.findById(id).orElse(null);
        if (manga != null && manga.getUser().getId().equals(loggedInUser.getId())) {
            mangaRepository.deleteById(id);
        }
        return "redirect:/collection/manga";
    }

    @GetMapping("/edit/{id}")
    public String showEditMangaForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));

        if (!manga.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/collection/manga";
        }

        model.addAttribute("manga", manga);
        return "edit-manga";
    }

    @PostMapping("/update/{id}")
    public String updateManga(@PathVariable("id") Long id, @ModelAttribute("manga") Manga mangaDetails, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));

        if (!manga.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/collection/manga";
        }

        manga.setTitle(mangaDetails.getTitle());
        manga.setAuthor(mangaDetails.getAuthor());
        manga.setImageUrl(mangaDetails.getImageUrl());
        manga.setOwnedVolumes(mangaDetails.getOwnedVolumes());
        manga.setTotalVolumes(mangaDetails.getTotalVolumes());
        manga.setStatus(mangaDetails.getStatus());

        mangaRepository.save(manga);
        return "redirect:/collection/manga";
    }

    // --- CẬP NHẬT BANNER RIÊNG CHO TRANG TRUYỆN ---
    @PostMapping("/update-banner")
    public String updateMangaBanner(@RequestParam(value = "bannerUrl", required = false) String bannerUrl,
                                    HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (bannerUrl != null && !bannerUrl.trim().isEmpty()) {
            loggedInUser.setMangaBanner(bannerUrl.trim());
            userRepository.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
        }

        return "redirect:/collection/manga";
    }
}