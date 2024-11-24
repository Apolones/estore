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
import ru.isands.test.estore.dao.entity.Shop;
import ru.isands.test.estore.dto.ShopDto;
import ru.isands.test.estore.service.ShopService;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Shop", description = "Сервис для управления магазинами")
@RequestMapping("/estore/api/shop")
public class ShopController {

    private final ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * Получить список всех магазинов с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить список всех магазинов с постраничным выводом", responses = {
            @ApiResponse(description = "Список всех магазинов на указанной странице")
    })
    public ResponseEntity<Page<Shop>> getAllShops(Pageable pageable) {
        Page<Shop> shops = shopService.getAllShops(pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * Получить магазин по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить магазин по ID", responses = {
            @ApiResponse(description = "Информация о магазине по ID")
    })
    public ResponseEntity<Shop> getShopById(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        return shop != null ? ResponseEntity.ok(shop) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новый магазин
     */
    @PostMapping
    @Operation(summary = "Создать новый магазин", responses = {
            @ApiResponse(description = "Данные созданного магазина")
    })
    public ResponseEntity<Shop> createShop(@RequestBody Shop shop) {
        Shop createdShop = shopService.createShop(shop);
        return ResponseEntity.status(201).body(createdShop);
    }

    /**
     * Обновить данные магазина
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные магазина", responses = {
            @ApiResponse(description = "Данные обновленного магазина")
    })
    public ResponseEntity<Shop> updateShop(@PathVariable Long id, @RequestBody Shop shop) {
        Shop updatedShop = shopService.updateShop(id, shop);
        return updatedShop != null ? ResponseEntity.ok(updatedShop) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить магазин
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить магазин", responses = {
            @ApiResponse(description = "Результат удаления магазина")
    })
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Вывод суммы денежных средств, полученной магазином через оплату "Наличныме"
     */
    @GetMapping("/by-purchase-type/{id}")
    public List<ShopDto> getShopsByPurchaseType(@PathVariable Long id, @RequestParam(value = "purchaseType", defaultValue = "Наличные") String purchaseType) {
        return shopService.getShopsByPurchaseType(id, purchaseType);
    }

    /**
     * Загрузить магазины из CSV
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить магазины из csv", responses = {
            @ApiResponse(description = "Магазины загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            shopService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
