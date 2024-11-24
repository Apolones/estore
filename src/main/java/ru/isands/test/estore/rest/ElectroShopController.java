package ru.isands.test.estore.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.isands.test.estore.dao.entity.ElectroShop;
import ru.isands.test.estore.dao.entity.ElectroShopPK;
import ru.isands.test.estore.dto.ElectroShopDTO;
import ru.isands.test.estore.service.ElectroItemService;
import ru.isands.test.estore.service.ElectroShopService;
import ru.isands.test.estore.service.ShopService;

import java.io.IOException;

@RestController
@Tag(name = "ElectroShop", description = "Сервис для управления связями электротоваров и магазинов")
@RequestMapping("/estore/api/electroshop")
public class ElectroShopController {

    private final ElectroShopService electroShopService;

    @Autowired
    public ElectroShopController(ElectroShopService electroShopService) {
        this.electroShopService = electroShopService;
    }

    /**
     * Получить список связей электротоваров и магазинов с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить связи с постраничным выводом", responses = {
            @ApiResponse(description = "Список связей электротоваров и магазинов на указанной странице")
    })
    public ResponseEntity<Page<ElectroShop>> getAllElectroShops(Pageable pageable) {
        Page<ElectroShop> electroShops = electroShopService.getAllElectroShops(pageable);
        return ResponseEntity.ok(electroShops);
    }

    /**
     * Получить связь электротовара и магазина по составному ключу
     */
    @GetMapping("/{electroItemId}/{shopId}")
    @Operation(summary = "Получить связь по составному ключу", responses = {
            @ApiResponse(description = "Информация о связи по составному ключу")
    })
    public ResponseEntity<ElectroShop> getElectroShopById(@PathVariable Long electroItemId, @PathVariable Long shopId) {
        ElectroShopPK id = new ElectroShopPK(electroItemId, shopId);
        ElectroShop electroShop = electroShopService.getElectroShopById(id);
        return electroShop != null ? ResponseEntity.ok(electroShop) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новую связь
     */
    @PostMapping
    @Operation(summary = "Создать новую связь", responses = {
            @ApiResponse(description = "Данные созданной связи")
    })
    public ResponseEntity<ElectroShop> createElectroShop(@RequestBody ElectroShopDTO electroShopDTO) {
        ElectroShop createdElectroShop = electroShopService.createElectroShop(electroShopDTO);
        return ResponseEntity.status(201).body(createdElectroShop);
    }

    /**
     * Обновить связи
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить связи", responses = {
            @ApiResponse(description = "Связи обновленны")
    })
    public ResponseEntity<ElectroShop> updateElectroShop(@PathVariable ElectroShopPK id, @RequestBody ElectroShop electroShop) {
        ElectroShop updatedElectroShop = electroShopService.updateElectroShop(id, electroShop);
        return ResponseEntity.ok(updatedElectroShop);
    }

    /**
     * Удалить связь по составному ключу
     */
    @DeleteMapping("/{electroItemId}/{shopId}")
    @Operation(summary = "Удалить связь по составному ключу", responses = {
            @ApiResponse(description = "Результат удаления связи")
    })
    public ResponseEntity<Void> deleteElectroShop(@PathVariable Long electroItemId, @PathVariable Long shopId) {
        ElectroShopPK id = new ElectroShopPK(electroItemId, shopId);
        electroShopService.deleteElectroShop(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Проверить наличие товара в магазине (shopId, itemId)
     */
    @GetMapping("/availability")
    public ResponseEntity<String> isItemAvailable(@RequestParam("shopId") Long shopId, @RequestParam("itemId") Long itemId) {
        boolean available = electroShopService.checkItemAvailability(shopId, itemId);
        if (available) {
            return ResponseEntity.ok("The item is available in the shop.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The item is not available in the shop.");
        }
    }

    /**
     * Загрузить связи из csv
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить связи из csv", responses = {
            @ApiResponse(description = "Связи загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            electroShopService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
