package com.riskmonitor.controller;

import com.riskmonitor.dto.SupplierRiskDTO;
import com.riskmonitor.service.SupplierService;
import com.riskmonitor.dto.SupplierRiskDetailDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}) // Allow frontend to connect from Vite and Create React App
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/risk")
    public List<SupplierRiskDTO> getTopRiskSuppliers() {
        return supplierService.getTopRiskSuppliers();
    }

    @GetMapping("/{supplierId}/risk-detail")
    public SupplierRiskDetailDTO getSupplierRiskDetail(@PathVariable UUID supplierId) {
        return supplierService.getSupplierRiskDetail(supplierId);
    }
}
