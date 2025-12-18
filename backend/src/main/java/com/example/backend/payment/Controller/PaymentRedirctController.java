package com.example.backend.payment.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// just for static resources page to avoid errors

@Controller
public class PaymentRedirctController {

    @GetMapping("/payment/success")
    public String success() {
        // forwards to the static resource success.html
        return "forward:/payment/success.html";
    }
}
