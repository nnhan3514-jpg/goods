package com.example.goods.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mangas")
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // Tên truyện / Artbook
    private String author;         // Tác giả / Họa sĩ
    private String imageUrl;       // Đường dẫn ảnh bìa (URL)
    private int ownedVolumes;      // Số tập đang có
    private int totalVolumes;      // Tổng số tập hiện phát hành (có thể để trống hoặc 0 nếu chưa rõ)
    private String status;         // Trạng thái

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Manga() {}

    public Manga(String title, String author, String imageUrl, int ownedVolumes, int totalVolumes, String status) {
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.ownedVolumes = ownedVolumes;
        this.totalVolumes = totalVolumes;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getOwnedVolumes() { return ownedVolumes; }
    public void setOwnedVolumes(int ownedVolumes) { this.ownedVolumes = ownedVolumes; }

    public int getTotalVolumes() { return totalVolumes; }
    public void setTotalVolumes(int totalVolumes) { this.totalVolumes = totalVolumes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}