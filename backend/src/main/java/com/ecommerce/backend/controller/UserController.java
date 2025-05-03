package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.UpdateAddressRequest;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PutMapping("/update-address")
    public ResponseEntity<String> updateAddress(@RequestBody UpdateAddressRequest request, Principal principal) {
        String email = principal.getName(); // JWT'den kullanıcıyı al
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAddressLine(request.getAddressLine());
        user.setCity(request.getCity());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());
        user.setPhoneNumber(request.getPhoneNumber()); // ✅

        userRepository.save(user);

        return ResponseEntity.ok("Adres ve iletişim bilgileri başarıyla güncellendi.");
    }
    @PutMapping("/ban/{userId}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> banUser(@PathVariable Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

    if (user.getRole() == Role.ADMIN) {
        return ResponseEntity.badRequest().body("Admin kullanıcı banlanamaz.");
    }

    user.setBanned(true);
    userRepository.save(user);

    return ResponseEntity.ok("Kullanıcı başarıyla banlandı.");
}
@PutMapping("/unban/{userId}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> unbanUser(@PathVariable Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

    user.setBanned(false);
    userRepository.save(user);

    return ResponseEntity.ok("Kullanıcının ban'ı kaldırıldı.");
}

}
