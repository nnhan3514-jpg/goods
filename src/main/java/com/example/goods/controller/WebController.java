package com.example.goods.controller;

import com.example.goods.model.Item;
import com.example.goods.model.User;
import com.example.goods.repository.ItemRepository;
import com.example.goods.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    // Thư mục lưu trữ file ảnh upload nằm ở thư mục gốc project
    private static final String UPLOAD_DIR = "uploads/";

    // --- ĐIỀU HƯỚNG TRANG CHỦ ---
    @GetMapping("/")
    public String home(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/collection";
        }
        return "redirect:/login";
    }

    // --- HIỂN THỊ CÁC TRANG AUTH ---
    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  Model model) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "register";
        }
        User newUser = new User(username, password);
        userRepository.save(newUser);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/collection";
        } else {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            return "login";
        }
    }

    // --- ĐĂNG XUẤT ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- QUẢN LÝ BỘ SƯU TẬP (GOODS) ---
    @GetMapping("/collection")
    public String showCollectionPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Item> itemList = itemRepository.findByUser(loggedInUser);
        model.addAttribute("items", itemList);
        model.addAttribute("newItem", new Item());
        return "collection";
    }

    @PostMapping("/collection/add")
    public String addItem(@ModelAttribute Item item,
                          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                          HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        item.setUser(loggedInUser);

        // --- THUẬT TOÁN QUÉT TOÀN BỘ CSDL ĐỂ TÌM ID BỊ TRỐNG NHỎ NHẤT ---
        List<Item> allItems = itemRepository.findAll();
        long nextId = 1;
        while (true) {
            long currentId = nextId;
            boolean exists = allItems.stream().anyMatch(i -> i.getId() != null && i.getId().equals(currentId));
            if (!exists) {
                break; // Tìm thấy ID còn trống nhỏ nhất
            }
            nextId++;
        }
        item.setId(nextId);
        // -----------------------------------------------------------------

        // Xử lý upload file ảnh Goods khi thêm mới
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, imageFile.getBytes());

                item.setImageUrl("/uploads/" + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Xử lý link shop khi thêm mới
        if (item.getShopLink() == null || item.getShopLink().trim().isEmpty()) {
            item.setShopLink(null);
        } else {
            item.setShopLink(item.getShopLink().trim());
        }

        itemRepository.save(item);
        return "redirect:/collection";
    }

    @GetMapping("/collection/delete/{id}")
    public String deleteItem(@PathVariable("id") Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Item item = itemRepository.findById(id).orElse(null);
        if (item != null && item.getUser().getId().equals(loggedInUser.getId())) {
            itemRepository.deleteById(id);
        }
        return "redirect:/collection";
    }

    @GetMapping("/collection/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item Id:" + id));

        if (!item.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/collection";
        }

        model.addAttribute("item", item);
        return "edit-item";
    }

    @PostMapping("/collection/update/{id}")
    public String updateItem(@PathVariable("id") Long id,
                             @ModelAttribute("item") Item itemDetails,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item Id:" + id));

        if (!item.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/collection";
        }

        item.setName(itemDetails.getName());
        item.setCharacterName(itemDetails.getCharacterName());
        item.setCategory(itemDetails.getCategory());
        item.setStatus(itemDetails.getStatus());

        // Xử lý upload file ảnh mới nếu người dùng chọn tải lên
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, imageFile.getBytes());

                item.setImageUrl("/uploads/" + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Xử lý cập nhật hoặc xóa trắng link shop
        if (itemDetails.getShopLink() == null || itemDetails.getShopLink().trim().isEmpty()) {
            item.setShopLink(null);
        } else {
            item.setShopLink(itemDetails.getShopLink().trim());
        }

        itemRepository.save(item);
        return "redirect:/collection";
    }

    // --- TRANG CÀI ĐẶT TÀI KHOẢN ---
    @GetMapping("/collection/settings")
    public String showSettingsPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        return "settings";
    }

    // --- XỬ LÝ ĐỔI AVATAR (Lưu vào thư mục uploads/ ngoài root) ---
    @PostMapping("/collection/update-avatar")
    public String updateAvatar(@RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                               HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, avatarFile.getBytes());

                loggedInUser.setAvatar("/uploads/" + fileName);
            }
            else if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                loggedInUser.setAvatar(avatarUrl.trim());
            }

            userRepository.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/collection/settings?success=avatar";
    }

    // --- ENDPOINT ĐỌC FILE ẢNH TRỰC TIẾP (Chống cache, hiện ảnh ngay lập tức không cần restart) ---
    @ResponseBody
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getUploadedFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache()) // Không lưu cache trình duyệt
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    // --- XỬ LÝ ĐỔI MẬT KHẨU ---
    @PostMapping("/collection/change-password")
    public String processChangePassword(@RequestParam("oldPassword") String oldPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        HttpSession session,
                                        Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!loggedInUser.getPassword().equals(oldPassword)) {
            model.addAttribute("error", "Mật khẩu cũ không chính xác!");
            return "settings";
        }

        loggedInUser.setPassword(newPassword);
        userRepository.save(loggedInUser);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "settings";
    }
}