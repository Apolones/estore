package ru.isands.test.estore.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.isands.test.estore.dao.entity.Purchase;
import ru.isands.test.estore.dto.PurchaseDTO;
import ru.isands.test.estore.service.PurchaseService;

import java.io.IOException;

@RestController
@Tag(name = "Purchase", description = "Сервис для управления покупками")
@RequestMapping("/estore/api/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Получить список покупок с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить список покупок с постраничным выводом", responses = {
            @ApiResponse(description = "Список покупок на указанной странице")
    })
    public ResponseEntity<Page<Purchase>> getAllPurchases(Pageable pageable) {
        Page<Purchase> purchases = purchaseService.getAllPurchases(pageable);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Получить покупку по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить покупку по ID", responses = {
            @ApiResponse(description = "Информация о покупке по ID")
    })
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable Long id) {
        Purchase purchase = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(purchase);
    }

    /**
     * Создать новую покупку
     */
    @PostMapping
    @Operation(summary = "Создать новую покупку", responses = {
            @ApiResponse(description = "Данные созданной покупки")
    })
    public ResponseEntity<Purchase> createPurchase(@RequestBody PurchaseDTO purchaseDTO) {
        Purchase createdPurchase = purchaseService.createPurchase(purchaseDTO);
        return ResponseEntity.status(201).body(createdPurchase);
    }

    /**
     * Удалить покупку
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить покупку", responses = {
            @ApiResponse(description = "Результат удаления покупки")
    })
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновить данные покупки
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные покупки", responses = {
            @ApiResponse(description = "Данные обновленной покупки")
    })
    public ResponseEntity<Purchase> updatePurchase(@PathVariable Long id, @RequestBody Purchase purchase) {
        Purchase updatedPurchase = purchaseService.updatePurchase(id, purchase);
        return ResponseEntity.ok(updatedPurchase);
    }

    /**
     * Загрузить покупки из csv
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить покупки из csv", responses = {
            @ApiResponse(description = "Покупки загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            purchaseService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
