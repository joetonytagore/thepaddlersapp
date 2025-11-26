package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}

