package com.example.livraison.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeGenerator {

    private final AtomicInteger sequence = new AtomicInteger(1);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Ex: CMD-20260908-0001 */
    public String genererNumeroCommande() {
        String jour = LocalDate.now().format(FORMAT);
        int n = sequence.getAndIncrement();
        return "CMD-" + jour + "-" + String.format("%04d", n);
    }

    /** Code de confirmation a 6 chiffres pour la preuve de livraison */
    public String genererCodeConfirmation() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}
