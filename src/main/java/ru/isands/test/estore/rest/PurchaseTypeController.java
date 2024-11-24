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
import ru.isands.test.estore.dao.entity.PurchaseType;
import ru.isands.test.estore.service.PurchaseTypeService;

import java.io.IOException;

@RestController
@Tag(name = "PurchaseType", description = "Сервис для управления типами покупок")
@RequestMapping("/estore/api/purchaseType")
public class PurchaseTypeController {

    private final PurchaseTypeService purchaseTypeService;

    @Autowired
    public PurchaseTypeController(PurchaseTypeService purchaseTypeService) {
        this.purchaseTypeService = purchaseTypeService;
    }

    /**
     * Получить список типов покупок с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить список типов покупок с постраничным выводом", responses = {
            @ApiResponse(description = "Список типов покупок на указанной странице")
    })
    public ResponseEntity<Page<PurchaseType>> getAllPurchaseTypes(Pageable pageable) {
        Page<PurchaseType> purchaseTypes = purchaseTypeService.getAllPurchaseTypes(pageable);
        return ResponseEntity.ok(purchaseTypes);
    }

    /**
     * Получить тип покупки по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить тип покупки по ID", responses = {
            @ApiResponse(description = "Информация о типе покупки по ID")
    })
    public ResponseEntity<PurchaseType> getPurchaseTypeById(@PathVariable Long id) {
        PurchaseType purchaseType = purchaseTypeService.getPurchaseTypeById(id);
        return purchaseType != null ? ResponseEntity.ok(purchaseType) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новый тип покупки
     */
    @PostMapping
    @Operation(summary = "Создать новый тип покупки", responses = {
            @ApiResponse(description = "Данные созданного типа покупки")
    })
    public ResponseEntity<PurchaseType> createPurchaseType(@RequestBody PurchaseType purchaseType) {
        PurchaseType createdPurchaseType = purchaseTypeService.createPurchaseType(purchaseType);
        return ResponseEntity.status(201).body(createdPurchaseType);
    }

    /**
     * Обновить тип покупки
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить тип покупки", responses = {
            @ApiResponse(description = "Данные обновленного типа покупки")
    })
    public ResponseEntity<PurchaseType> updatePurchaseType(@PathVariable Long id, @RequestBody PurchaseType purchaseType) {
        PurchaseType updatedPurchaseType = purchaseTypeService.updatePurchaseType(id, purchaseType);
        return updatedPurchaseType != null ? ResponseEntity.ok(updatedPurchaseType) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить тип покупки
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип покупки", responses = {
            @ApiResponse(description = "Результат удаления типа покупки")
    })
    public ResponseEntity<Void> deletePurchaseType(@PathVariable Long id) {
        purchaseTypeService.deletePurchaseType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Загрузить типы покупок из CSV
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить типы покупок из csv", responses = {
            @ApiResponse(description = "Типы покупок загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            purchaseTypeService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
