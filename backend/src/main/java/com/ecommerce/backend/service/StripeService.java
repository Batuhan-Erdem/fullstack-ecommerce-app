package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository; 

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

public String createCheckoutSession(List<SessionCreateParams.LineItem> lineItems, String orderId) throws Exception {
    Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new RuntimeException("Order not found"));

    User user = order.getCustomer(); // ✅ Doğru user buradan alınmalı

    SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("http://localhost:4200/payment-success")
            .setCancelUrl("http://localhost:4200/payment-cancel")
            .addAllLineItem(lineItems)
            .putMetadata("orderId", orderId)
            .build();

    Session session = Session.create(params);

    String stripeCustomerId = session.getCustomer(); // Stripe müşteri ID

    // Kullanıcının Stripe müşteri ID'si daha önce kaydedilmediyse set et
    if (stripeCustomerId != null && user.getStripeCustomerId() == null) {
        user.setStripeCustomerId(stripeCustomerId);
        userRepository.save(user);
    }

    return session.getUrl();
}


    public void refundPayment(String paymentIntentId) throws Exception {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        Refund.create(params);
    }
}