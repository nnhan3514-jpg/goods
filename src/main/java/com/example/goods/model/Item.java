package com.example.goods.model;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    private Long id; // Tự quản lý thủ công để lấp đầy ID trống khi xóa

    private String name;          // Chủ đề Goods
    private String characterName; // Tên nhân vật
    private String category;      // Loại Goods
    private String status;        // Trạng thái (Đã có, Pre-order, Đang tìm)
    private String shopLink;      // Link shop / bài viết Facebook
    private String imageUrl;      // Đường dẫn file ảnh upload

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;            // Thuộc về user nào

    // --- Constructors ---
    public Item() {
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShopLink() { return shopLink; }
    public void setShopLink(String shopLink) { this.shopLink = shopLink; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}