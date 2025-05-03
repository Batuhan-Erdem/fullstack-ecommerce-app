package com.ecommerce.backend.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
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

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Stripe Checkout oturumu oluşturur
     *
     * @param lineItems Stripe line item listesi (ürünler)
     * @param orderId   Sipariş ID'si (veritabanı ile eşleşme için metadata olarak yazılır)
     * @return Checkout URL'i (Stripe tarafından oluşturulan ödeme sayfası linki)
     */
    public String createCheckoutSession(List<SessionCreateParams.LineItem> lineItems, String orderId) throws Exception {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/payment-success")
                .setCancelUrl("http://localhost:4200/payment-cancel")
                .addAllLineItem(lineItems)
                .putMetadata("orderId", orderId) // METADATA EKLENDİ ✅
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
