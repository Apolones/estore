package ru.isands.test.estore.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.isands.test.estore.dao.entity.Employee;
import ru.isands.test.estore.dao.repo.EmployeeRepository;
import ru.isands.test.estore.dto.BestEmployeeDTO;
import ru.isands.test.estore.dto.EmployeeDTO;
import ru.isands.test.estore.exeption.CsvProcessingException;
import ru.isands.test.estore.exeption.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionTypeService positionTypeService;
    private final ShopService shopService;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;


    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PositionTypeService positionTypeService, ShopService shopService) {
        this.employeeRepository = employeeRepository;
        this.positionTypeService = positionTypeService;
        this.shopService = shopService;
    }

    /**
     * Получить сотрудников с постраничным выводом
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Получить сотрудника по ID
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found for ID: " + id));
    }

    /**
     * Создать нового сотрудника
     */
    public Employee createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setLastName(employeeDTO.getLastName());
        employee.setFirstName(employeeDTO.getLastName());
        employee.setPatronymic(employeeDTO.getPatronymic());
        employee.setBirthDate(employeeDTO.getBirthDate());
        employee.setShop(shopService.getShopById(employeeDTO.getShopId()));
        employee.setPosition(positionTypeService.getPositionTypeById(employeeDTO.getPositionId()));
        employee.setGender(employeeDTO.isGender());
        return employeeRepository.save(employee);
    }

    /**
     * Обновить существующего сотрудника
     */
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        if (employeeRepository.existsById(id)) {
            updatedEmployee.setId(id);
            return employeeRepository.save(updatedEmployee);
        }
        throw new ResourceNotFoundException("Employee not found for ID: " + id);
    }

    /**
     * Удалить сотрудника по ID
     */
    public void deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
        }
    }

    /**
     * Получить лучших сотрудников в зависимости от должности по сумме продаж
     */
    public List<BestEmployeeDTO> getBestEmployeesByTotalSales(int years, Long positionId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -years);
        Date date = calendar.getTime();

        return employeeRepository.findBestEmployeesByTotalSales(date, positionId);
    }

    /**
     * Получить лучших сотрудников в зависимости от должности по количеству продаж
     */
    public List<BestEmployeeDTO> getBestEmployeesByItemSold(int years, Long positionId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -years);
        Date date = calendar.getTime();

        return employeeRepository.findBestEmployeesByItemSold(date, positionId);
    }

    /**
     * Вывод лучшего младшего продавца-консультанта, продавшего больше всех умных часов
     */
    public BestEmployeeDTO getBestJuniorConsultantBySmartWatches(String employeePosition, String electroItem) {
        List<BestEmployeeDTO> result = employeeRepository.findBestJuniorConsultantBySmartWatches(employeePosition, electroItem);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Создать новых сотрудников из файла .csv
     */
    @Transactional
    public void importCsv(MultipartFile file, String encoding) throws IOException {
        if (file.getSize() > maxFileSize * 1024 * 1024)
            throw new CsvProcessingException("Превышен максимальный размер .CSV: текущий: " + file.getSize() / (1024 * 1024) + "mb, максимальный:" + maxFileSize + "mb");
        try (InputStream inputStream = file.getInputStream()) {
            processCsv(inputStream, encoding);
        }
    }

    /**
     * Создать новых сотрудников из пути к файлу .csv
     */
    @Transactional
    public void importCsv(Path filePath, String encoding) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            processCsv(inputStream, encoding);
        }
    }

    private void processCsv(InputStream inputStream, String encoding) throws IOException {
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream, encoding))
                .withSkipLines(1)
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';')
                        .build())
                .build()) {
            List<Employee> employees = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    Employee employee = new Employee();

                    employee.setId(Long.valueOf(values[0]));
                    employee.setLastName(values[1]);
                    employee.setFirstName(values[2]);
                    employee.setPatronymic(values[3]);
                    employee.setBirthDate(new SimpleDateFormat("dd.MM.yyyy").parse(values[4]));
                    employee.setPosition(positionTypeService.getPositionTypeById(Long.valueOf(values[5])));
                    employee.setShop(shopService.getShopById(Long.valueOf(values[6])));
                    employee.setGender(Boolean.parseBoolean(values[7]));
                    employees.add(employee);
                } catch (ResourceNotFoundException | ParseException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            employeeRepository.saveAll(employees);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }


}
