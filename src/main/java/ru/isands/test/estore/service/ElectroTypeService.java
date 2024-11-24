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
import ru.isands.test.estore.dao.entity.ElectroType;
import ru.isands.test.estore.dao.repo.ElectroTypeRepository;
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
public class ElectroTypeService {

    private final ElectroTypeRepository electroTypeRepository;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;


    @Autowired
    public ElectroTypeService(ElectroTypeRepository electroTypeRepository) {
        this.electroTypeRepository = electroTypeRepository;
    }

    /**
     * Получить типы электроники с постраничным выводом
     */
    public Page<ElectroType> getAllElectroTypes(Pageable pageable) {
        return electroTypeRepository.findAll(pageable);
    }

    /**
     * Получить тип электроники по ID
     */
    public ElectroType getElectroTypeById(Long id) {
        return electroTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ElectroType not found for ID: " + id));
    }

    /**
     * Создать новый тип электроники
     */
    public ElectroType createElectroType(ElectroType electroType) {
        return electroTypeRepository.save(electroType);
    }

    /**
     * Обновить существующий тип электроники
     */
    public ElectroType updateElectroType(Long id, ElectroType updatedElectroType) {
        if (electroTypeRepository.existsById(id)) {
            updatedElectroType.setId(id);
            return electroTypeRepository.save(updatedElectroType);
        }
        throw new ResourceNotFoundException("ElectroType not found for ID: " + id);
    }

    /**
     * Удалить тип электроники по ID
     */
    public void deleteElectroType(Long id) {
        if (electroTypeRepository.existsById(id)) {
            electroTypeRepository.deleteById(id);
        }
    }

    /**
     * Создать новые типы электроники из файла .csv
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
     * Создать новые типы электроники из пути к файлу .csv
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
            List<ElectroType> electroTypes = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    ElectroType electroType = new ElectroType();

                    electroType.setId(Long.valueOf(values[0]));
                    electroType.setName(values[1]);
                    electroTypes.add(electroType);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            electroTypeRepository.saveAll(electroTypes);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }


}
