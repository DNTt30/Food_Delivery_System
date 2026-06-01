package com.duong.salesmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentViewController {

    @GetMapping("/payment-result")
    public String paymentResult(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orderId,
            Model model) {

        model.addAttribute("status", status);
        model.addAttribute("orderId", orderId);

        return "payment_result";
    }

    @GetMapping("/payment-failed")
    public String paymentFailed() {

        return "payment_failed";
    }
}