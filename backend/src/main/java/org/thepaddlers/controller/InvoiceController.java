package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.Invoice;
import org.thepaddlers.repository.InvoiceRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class InvoiceController {
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> list() {
        return ResponseEntity.ok(invoiceRepository.findAll());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Optional<Invoice> o = invoiceRepository.findById(id);
        if (o.isEmpty()) return ResponseEntity.status(404).body(null);
        return ResponseEntity.ok(o.get());
    }
}

