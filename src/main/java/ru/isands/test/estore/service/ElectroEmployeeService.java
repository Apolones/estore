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
import ru.isands.test.estore.dao.entity.ElectroEmployee;
import ru.isands.test.estore.dao.entity.ElectroEmployeePK;
import ru.isands.test.estore.dao.repo.ElectroEmployeeRepository;
import ru.isands.test.estore.dto.ElectroEmployeeDTO;
import ru.isands.test.estore.exeption.CsvProcessingException;
import ru.isands.test.estore.exeption.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ElectroEmployeeService {

    private final ElectroEmployeeRepository electroEmployeeRepository;
    private final EmployeeService employeeService;
    private final ElectroTypeService electroTypeService;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public ElectroEmployeeService(ElectroEmployeeRepository electroEmployeeRepository, EmployeeService employeeService, ElectroTypeService electroTypeService) {
        this.electroEmployeeRepository = electroEmployeeRepository;
        this.employeeService = employeeService;
        this.electroTypeService = electroTypeService;
    }

    /**
     * Получить список связей сотрудников и типов электроники с постраничным выводом
     */
    public Page<ElectroEmployee> getAllElectroEmployees(Pageable pageable) {
        return electroEmployeeRepository.findAll(pageable);
    }

    /**
     * Получить связь сотрудника и типа электроники по составному ключу
     */
    public ElectroEmployee getElectroEmployeeById(ElectroEmployeePK id) {
        return electroEmployeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ElectroEmployee not found for ID: " + id));
    }

    /**
     * Создать новую связь сотрудника и типа электроники
     */
    public ElectroEmployee createElectroEmployee(ElectroEmployeeDTO electroEmployeeDTO) {
        ElectroEmployeePK id = new ElectroEmployeePK(electroEmployeeDTO.getEmployeeId(), electroEmployeeDTO.getElectroTypeId());
        ElectroEmployee electroEmployee = new ElectroEmployee();
        electroEmployee.setId(id);
        electroEmployee.setElectroType(electroTypeService.getElectroTypeById(electroEmployeeDTO.getElectroTypeId()));
        electroEmployee.setEmployee(employeeService.getEmployeeById(electroEmployeeDTO.getEmployeeId()));
        return electroEmployeeRepository.save(electroEmployee);
    }

    /**
     * Удалить связь сотрудника и типа электроники по составному ключу
     */
    public void deleteElectroEmployee(ElectroEmployeePK id) {
        if (electroEmployeeRepository.existsById(id)) {
            electroEmployeeRepository.deleteById(id);
        }
    }

    /**
     * Создать новые связи сотрудников и типов электроники из файла .csv
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
     * Создать новые связи сотрудников и типов электроники из пути к файлу .csv
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
            List<ElectroEmployee> electroEmployees = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    ElectroEmployee electroEmployee = new ElectroEmployee();
                    electroEmployee.setId(new ElectroEmployeePK(
                            Long.valueOf(values[0]),
                            Long.valueOf(values[1])
                    ));
                    electroEmployee.setEmployee(employeeService.getEmployeeById(Long.valueOf(values[0])));
                    electroEmployee.setElectroType(electroTypeService.getElectroTypeById(Long.valueOf(values[1])));
                    electroEmployees.add(electroEmployee);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            electroEmployeeRepository.saveAll(electroEmployees);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }
}
