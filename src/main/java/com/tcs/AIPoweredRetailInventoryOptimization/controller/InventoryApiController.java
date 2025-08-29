
package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Inventory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InventoryApiController {
    private final InventoryRepository inventoryRepository;

    @GetMapping("/api/inventories")
    public List<Inventory> list() {
        return inventoryRepository.findAll();
    }
}
