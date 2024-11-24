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
import ru.isands.test.estore.dao.entity.PositionType;
import ru.isands.test.estore.service.PositionTypeService;

import java.io.IOException;

@RestController
@Tag(name = "PositionType", description = "Сервис для управления должностями сотрудников")
@RequestMapping("/estore/api/positiontype")
public class PositionTypeController {

    private final PositionTypeService positionTypeService;

    @Autowired
    public PositionTypeController(PositionTypeService positionTypeService) {
        this.positionTypeService = positionTypeService;
    }

    /**
     * Получить список должностей с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить должности с постраничным выводом", responses = {
            @ApiResponse(description = "Список должностей на указанной странице")
    })
    public ResponseEntity<Page<PositionType>> getAllPositionTypes(Pageable pageable) {
        Page<PositionType> positionTypes = positionTypeService.getAllPositionTypes(pageable);
        return ResponseEntity.ok(positionTypes);
    }

    /**
     * Получить должность по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить должность по ID", responses = {
            @ApiResponse(description = "Информация о должности по ID")
    })
    public ResponseEntity<PositionType> getPositionTypeById(@PathVariable Long id) {
        PositionType positionType = positionTypeService.getPositionTypeById(id);
        return positionType != null ? ResponseEntity.ok(positionType) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новую должность
     */
    @PostMapping
    @Operation(summary = "Создать новую должность", responses = {
            @ApiResponse(description = "Данные созданной должности")
    })
    public ResponseEntity<PositionType> createPositionType(@RequestBody PositionType positionType) {
        PositionType createdPositionType = positionTypeService.createPositionType(positionType);
        return ResponseEntity.status(201).body(createdPositionType);
    }

    /**
     * Обновить данные должности
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные должности", responses = {
            @ApiResponse(description = "Данные обновленной должности")
    })
    public ResponseEntity<PositionType> updatePositionType(@PathVariable Long id, @RequestBody PositionType positionType) {
        PositionType updatedPositionType = positionTypeService.updatePositionType(id, positionType);
        return updatedPositionType != null ? ResponseEntity.ok(updatedPositionType) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить должность
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить должность", responses = {
            @ApiResponse(description = "Результат удаления должности")
    })
    public ResponseEntity<Void> deletePositionType(@PathVariable Long id) {
        positionTypeService.deletePositionType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Загрузить должности из CSV
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить должности из csv", responses = {
            @ApiResponse(description = "Должности загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            positionTypeService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }
}
