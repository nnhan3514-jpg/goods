package com.example.goods.repository;

import com.example.goods.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm kiếm user theo tên đăng nhập để phục vụ việc kiểm tra đăng nhập/đăng ký
    User findByUsername(String username);
}