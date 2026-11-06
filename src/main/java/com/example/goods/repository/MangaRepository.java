package com.example.goods.repository;

import com.example.goods.model.Manga;
import com.example.goods.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MangaRepository extends JpaRepository<Manga, Long> {
    List<Manga> findByUser(User user);
}