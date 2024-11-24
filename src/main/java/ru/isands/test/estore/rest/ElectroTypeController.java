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
import ru.isands.test.estore.dao.entity.ElectroType;
import ru.isands.test.estore.service.ElectroTypeService;

import java.io.IOException;

@RestController
@Tag(name = "ElectroType", description = "Сервис для управления типами электроники")
@RequestMapping("/estore/api/electrotype")
public class ElectroTypeController {

    private final ElectroTypeService electroTypeService;

    @Autowired
    public ElectroTypeController(ElectroTypeService electroTypeService) {
        this.electroTypeService = electroTypeService;
    }

    /**
     * Получить список типов электроники с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить типы электроники с постраничным выводом", responses = {
            @ApiResponse(description = "Список типов электроники на указанной странице")
    })
    public ResponseEntity<Page<ElectroType>> getAllElectroTypes(Pageable pageable) {
        Page<ElectroType> electroTypes = electroTypeService.getAllElectroTypes(pageable);
        return ResponseEntity.ok(electroTypes);
    }

    /**
     * Получить тип электроники по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить тип электроники по ID", responses = {
            @ApiResponse(description = "Информация о типе электроники по ID")
    })
    public ResponseEntity<ElectroType> getElectroTypeById(@PathVariable Long id) {
        ElectroType electroType = electroTypeService.getElectroTypeById(id);
        return electroType != null ? ResponseEntity.ok(electroType) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новый тип электроники
     */
    @PostMapping
    @Operation(summary = "Создать новый тип электроники", responses = {
            @ApiResponse(description = "Данные созданного типа электроники")
    })
    public ResponseEntity<ElectroType> createElectroType(@RequestBody ElectroType electroType) {
        ElectroType createdElectroType = electroTypeService.createElectroType(electroType);
        return ResponseEntity.status(201).body(createdElectroType);
    }

    /**
     * Обновить данные типа электроники
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные типа электроники", responses = {
            @ApiResponse(description = "Данные обновленного типа электроники")
    })
    public ResponseEntity<ElectroType> updateElectroType(@PathVariable Long id, @RequestBody ElectroType electroType) {
        ElectroType updatedElectroType = electroTypeService.updateElectroType(id, electroType);
        return updatedElectroType != null ? ResponseEntity.ok(updatedElectroType) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить тип электроники
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип электроники", responses = {
            @ApiResponse(description = "Результат удаления типа электроники")
    })
    public ResponseEntity<Void> deleteElectroType(@PathVariable Long id) {
        electroTypeService.deleteElectroType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Загрузить типы электроники из csv
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить типы электроники из csv", responses = {
            @ApiResponse(description = "Типы электроники загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            electroTypeService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
