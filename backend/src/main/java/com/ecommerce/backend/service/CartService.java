package com.ecommerce.backend.service;

import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Optional<Cart> optionalCart = cartRepository.findByUser(user);
        if (optionalCart.isPresent()) {
            return optionalCart.get();
        }

        Cart newCart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();

        return cartRepository.save(newCart);
    }
    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity) {
        
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
                System.out.println("STOK DURUMU: " + product.getStock());
                System.out.println("İSTENEN MİKTAR: " + quantity);
        if (product.getStock() < quantity) {

            throw new RuntimeException("Yeterli stok yok.");
        }
    
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
                return cartRepository.save(cart);
            }
        }
    
        CartItem newItem = CartItem.builder()
                .product(product)
                .quantity(quantity)
                .build();
    
        cart.getItems().add(newItem);
    
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    
        return cartRepository.save(cart);
    }
    

    public Cart removeFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCart(Long userId) {
        return getOrCreateCart(userId);
    }
}
