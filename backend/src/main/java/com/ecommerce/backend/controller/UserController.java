package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.UpdateAddressRequest;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.model.Role;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PutMapping("/update-address")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> updateAddress(@RequestBody UpdateAddressRequest request, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean changed = false;

        if (!request.getAddressLine().equals(user.getAddressLine())) {
            user.setAddressLine(request.getAddressLine());
            changed = true;
        }
        if (!request.getCity().equals(user.getCity())) {
            user.setCity(request.getCity());
            changed = true;
        }
        if (!request.getPostalCode().equals(user.getPostalCode())) {
            user.setPostalCode(request.getPostalCode());
            changed = true;
        }
        if (!request.getCountry().equals(user.getCountry())) {
            user.setCountry(request.getCountry());
            changed = true;
        }
        if (!request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
            return ResponseEntity.ok("Adres ve iletişim bilgileri başarıyla güncellendi.");
        } else {
            return ResponseEntity.ok("Adres bilgileri zaten güncel.");
        }
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
