package com.sesac.solbid.service.wish;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.Wish;
import com.sesac.solbid.dto.wish.response.WishActionResponse;
import com.sesac.solbid.dto.wish.response.WishResponse;
import com.sesac.solbid.repository.ProductRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.repository.wish.WishRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishServiceImpl implements WishService {

        private final WishRepository wishRepository;

        private final UserRepository userRepository;

        private final ProductRepository productRepository;

        @Override
        @Transactional
        public List<ProductResponse> getWishes(Long userId) {
                User user = userRepository
                                .findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                return wishRepository
                                .findByUser(user)
                                .stream()
                                .map(Wish::getProduct)
                                .map(ProductResponse::fromEntity)
                                .toList();
        }

        @Override
        @Transactional
        public void addWish(Long userId, Long productId) {
                User user = userRepository
                                .findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                Product product = productRepository
                                .findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

                wishRepository
                                .findByUserAndProduct(user, product)
                                .ifPresent(wish -> {
                                        throw new IllegalArgumentException("Wish already exists");
                                });

                Wish wish = Wish.builder()
                                .user(user)
                                .product(product)
                                .build();

                Wish savedWish = wishRepository.save(wish);
                return WishActionResponse.added(savedWish.getId(), productId);
        }

        @Override
        @Transactional
        public WishActionResponse removeWish(Long userId, Long productId) {
                User user = userRepository
                                .findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                Product product = productRepository
                                .findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

                Wish wish = wishRepository
                                .findByUserAndProduct(user, product)
                                .orElseThrow(() -> new IllegalArgumentException("Wish not found"));

                wishRepository.delete(wish);
                return WishActionResponse.removed(productId);
        }
}
