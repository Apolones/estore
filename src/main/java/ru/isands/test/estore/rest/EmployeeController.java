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
import ru.isands.test.estore.dao.entity.Employee;
import ru.isands.test.estore.dto.BestEmployeeDTO;
import ru.isands.test.estore.dto.EmployeeDTO;
import ru.isands.test.estore.service.EmployeeService;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Employee", description = "Сервис для выполнения операций над сотрудниками магазина")
@RequestMapping("/estore/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;


    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Получить сотрудников с постраничным выводом
     */
    @GetMapping
    @Operation(summary = "Получить сотрудников с постраничным выводом", responses = {
            @ApiResponse(description = "Список сотрудников на указанной странице")
    })
    public ResponseEntity<Page<Employee>> getAllEmployees(Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    /**
     * Получить сотрудника по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить сотрудника по ID", responses = {
            @ApiResponse(description = "Информация о сотруднике по ID")
    })
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return employee != null ? ResponseEntity.ok(employee) : ResponseEntity.notFound().build();
    }

    /**
     * Создать нового сотрудника
     */
    @PostMapping
    @Operation(summary = "Создать нового сотрудника", responses = {
            @ApiResponse(description = "Данные созданного сотрудника")
    })
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(201).body(createdEmployee);
    }

    /**
     * Обновить данные сотрудника
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные сотрудника", responses = {
            @ApiResponse(description = "Данные обновленного сотрудника")
    })
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        return updatedEmployee != null ? ResponseEntity.ok(updatedEmployee) : ResponseEntity.notFound().build();
    }

    /**
     * Удалить сотрудника
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сотрудника", responses = {
            @ApiResponse(description = "Результат удаления сотрудника")
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Получить лучших сотрудников за год по id должности, отсорированный по сумме проданых товаров
     */
    @GetMapping("/totalsales/{positionId}")
    public ResponseEntity<List<BestEmployeeDTO>> getBestEmployeesByTotalSales(@PathVariable Long positionId, @RequestParam(value = "years", defaultValue = "1") int years) {
        List<BestEmployeeDTO> bestEmployees = employeeService.getBestEmployeesByTotalSales(years, positionId);
        return ResponseEntity.ok(bestEmployees);
    }

    /**
     * Получить лучших сотрудников за год по id должности, отсорированный по количеству проданых товаров
     */
    @GetMapping("/itemssold/{positionId}")
    public ResponseEntity<List<BestEmployeeDTO>> getBestEmployeesByItemSold(@PathVariable Long positionId, @RequestParam(value = "years", defaultValue = "1") int years) {
        List<BestEmployeeDTO> bestEmployees = employeeService.getBestEmployeesByItemSold(years, positionId);
        return ResponseEntity.ok(bestEmployees);
    }

    /**
     * Вывод лучшего младшего продавца-консультанта, продавшего больше всех умных часов
     */
    @GetMapping("/bestjuniorconsultantsmartwatches")
    public ResponseEntity<BestEmployeeDTO> getBestJuniorConsultantBySmartWatches(@RequestParam(value = "empoyeePosition", defaultValue = "Младший продавец-консультант") String employeePosition, @RequestParam(value = "electoItem", defaultValue = "Умные часы") String electoItem) {
        BestEmployeeDTO bestEmployee = employeeService.getBestJuniorConsultantBySmartWatches(employeePosition, electoItem);
        return bestEmployee != null ? ResponseEntity.ok(bestEmployee) : ResponseEntity.noContent().build();
    }

    /**
     * Загрузить сотрудников из csv
     */
    @PostMapping("/csv")
    @Operation(summary = "Загрузить сотрудников из csv", responses = {
            @ApiResponse(description = "Сотрудники загружены успешно", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте CSV: ", responseCode = "500")
    })
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        try {
            employeeService.importCsv(file, encoding);
            return ResponseEntity.ok("CSV импортирован успешно!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при импорте CSV: " + e.getMessage());
        }
    }

}
