package com.example.goods.repository;

import com.example.goods.model.Item;
import com.example.goods.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Lấy danh sách các món goods thuộc về một User cụ thể
    List<Item> findByUser(User user);
}