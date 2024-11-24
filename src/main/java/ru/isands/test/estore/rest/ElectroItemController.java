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
import ru.isands.test.estore.dao.entity.ElectroItem;
import ru.isands.test.estore.dto.ElectroItemDTO;
import ru.isands.test.estore.service.ElectroItemService;

import java.io.IOException;

@RestController
@Tag(name = "ElectroItem", description = "Сервис для управления электротоварами")
@RequestMapping("/estore/api/electroItem")
public class ElectroItemController {

    private final ElectroItemService electroItemService;


    @Autowired
    public ElectroItemController(ElectroItemService electroItemService) {
        this.electroItemService = electroItemService;
    }

    /**
     * Получить список электротоваров с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить электротовары с постраничным выводом", responses = {
            @ApiResponse(description = "Список электротоваров на указанной странице")
    })
    public ResponseEntity<Page<ElectroItem>> getAllElectroItems(Pageable pageable) {
        Page<ElectroItem> electroItems = electroItemService.getAllElectroItems(pageable);
        return ResponseEntity.ok(electroItems);
    }

    /**
     * Получить электротовар по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить электротовар по ID", responses = {
            @ApiResponse(description = "Информация об электротоваре по ID")
    })
    public ResponseEntity<ElectroItem> getElectroItemById(@PathVariable Long id) {
        ElectroItem electroItem = electroItemService.getElectroItemById(id);
        return electroItem != null ? ResponseEntity.ok(electroItem) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новый электротовар
     */
    @PostMapping
    @Operation(summary = "Создать новый электротовар", responses = {
            @ApiResponse(description = "Данные созданного электротовара")
    })
    public ResponseEntity<ElectroItem> createElectroItem(@RequestBody ElectroItemDTO electroItemDTO) {
        ElectroItem createdElectroItem = electroItemService.createElectroItem(electroItemDTO);
        return ResponseEntity.status(201).body(createdElectroItem);
    }

    /**
     * Обновить данные электротовара
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные электротовара", responses = {
            @ApiResponse(description = "Данные обновленного электротовара")
    })
    public ResponseEntity<ElectroItem> updateElectroItem(@PathVariable Long id, @RequestBody ElectroItem electroItem) {
        ElectroItem updatedElectroItem = electroItemService.updateElectroItem(id, electroItem);
        return updatedElectroItem != null ? ResponseEntity.ok(updatedElectroItem) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить электротовар
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить электротовар", responses = {
            @ApiResponse(description = "Результат удаления электротовара")
    })
    public ResponseEntity<Void> deleteElectroItem(@PathVariable Long id) {
        electroItemService.deleteElectroItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Загрузить электротовары из csv
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить электротовары из csv", responses = {
            @ApiResponse(description = "Электротовары загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            electroItemService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
