package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Category;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // Aktif ve admin tarafından silinmemiş ürünleri döndürmek
    public List<Product> getActiveAndNotDeletedProducts() {
        return productRepository.findByActiveTrueAndDeletedByAdminFalse();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product addProduct(Product product, Long sellerId, Long categoryId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!seller.getRole().name().equals("SELLER")) {
            throw new RuntimeException("Only sellers can add products");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setSeller(seller);
        product.setCategory(category);
        product.setActive(true);

        return productRepository.save(product);
    }

    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }

    public void deleteProductByAdmin(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ürünün "deletedByAdmin" kısmını true yapıyoruz
        product.setDeletedByAdmin(true);
        productRepository.save(product);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        return productRepository.findBySeller(seller);
    }

    public void deleteProductBySeller(Long productId, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Bu ürünü silmeye yetkiniz yok.");
        }

        productRepository.delete(product);
    }

}
