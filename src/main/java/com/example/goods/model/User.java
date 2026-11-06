package com.example.goods.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Trường lưu đường dẫn ảnh đại diện (Avatar)
    private String avatar;

    // Trường lưu đường dẫn ảnh banner riêng cho trang Quản lý Truyện
    @Column(name = "manga_banner")
    private String mangaBanner;

    // Một user có thể sở hữu nhiều món goods
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.avatar = "https://i.imgur.com/6VBx3io.png"; // Ảnh đại diện mặc định ban đầu
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getMangaBanner() { return mangaBanner; }
    public void setMangaBanner(String mangaBanner) { this.mangaBanner = mangaBanner; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}