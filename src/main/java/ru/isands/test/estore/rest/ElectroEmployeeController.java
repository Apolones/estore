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
import ru.isands.test.estore.dao.entity.ElectroEmployee;
import ru.isands.test.estore.dao.entity.ElectroEmployeePK;
import ru.isands.test.estore.dto.ElectroEmployeeDTO;
import ru.isands.test.estore.service.ElectroEmployeeService;

import java.io.IOException;

@RestController
@Tag(name = "ElectroEmployee", description = "Сервис для управления связями сотрудников и типов электроники")
@RequestMapping("/estore/api/electroemployee")
public class ElectroEmployeeController {

    private final ElectroEmployeeService electroEmployeeService;

    @Autowired
    public ElectroEmployeeController(ElectroEmployeeService electroEmployeeService) {
        this.electroEmployeeService = electroEmployeeService;
    }

    /**
     * Получить список связей сотрудников и типов электроники с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить связи с постраничным выводом", responses = {
            @ApiResponse(description = "Список связей сотрудников и типов электроники на указанной странице")
    })
    public ResponseEntity<Page<ElectroEmployee>> getAllElectroEmployees(Pageable pageable) {
        Page<ElectroEmployee> electroEmployees = electroEmployeeService.getAllElectroEmployees(pageable);
        return ResponseEntity.ok(electroEmployees);
    }

    /**
     * Получить связь сотрудника и типа электроники по составному ключу
     */
    @GetMapping("/{employeeId}/{electroTypeId}")
    @Operation(summary = "Получить связь по составному ключу", responses = {
            @ApiResponse(description = "Информация о связи по составному ключу")
    })
    public ResponseEntity<ElectroEmployee> getElectroEmployeeById(@PathVariable Long employeeId, @PathVariable Long electroTypeId) {
        ElectroEmployeePK id = new ElectroEmployeePK(employeeId, electroTypeId);
        ElectroEmployee electroEmployee = electroEmployeeService.getElectroEmployeeById(id);
        return electroEmployee != null ? ResponseEntity.ok(electroEmployee) : ResponseEntity.notFound().build();
    }

    /**
     * Создать новую связь
     */
    @PostMapping
    @Operation(summary = "Создать новую связь", responses = {
            @ApiResponse(description = "Данные созданной связи")
    })
    public ResponseEntity<ElectroEmployee> createElectroEmployee(@RequestBody ElectroEmployeeDTO electroEmployeeDTO) {
        ElectroEmployee createdElectroEmployee = electroEmployeeService.createElectroEmployee(electroEmployeeDTO);
        return ResponseEntity.status(201).body(createdElectroEmployee);
    }

    /**
     * Удалить связь по составному ключу
     */
    @DeleteMapping("/{employeeId}/{electroTypeId}")
    @Operation(summary = "Удалить связь по составному ключу", responses = {
            @ApiResponse(description = "Результат удаления связи")
    })
    public ResponseEntity<Void> deleteElectroEmployee(@PathVariable Long employeeId, @PathVariable Long electroTypeId) {
        ElectroEmployeePK id = new ElectroEmployeePK(employeeId, electroTypeId);
        electroEmployeeService.deleteElectroEmployee(id);
        return ResponseEntity.noContent().build();
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
            electroEmployeeService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }
}
