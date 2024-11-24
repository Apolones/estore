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
import ru.isands.test.estore.dao.entity.PositionType;
import ru.isands.test.estore.dao.repo.PositionTypeRepository;
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
public class PositionTypeService {

    private final PositionTypeRepository positionTypeRepository;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public PositionTypeService(PositionTypeRepository positionTypeRepository) {
        this.positionTypeRepository = positionTypeRepository;
    }

    /**
     * Получить должности с постраничным выводом
     */
    public Page<PositionType> getAllPositionTypes(Pageable pageable) {
        return positionTypeRepository.findAll(pageable);
    }

    /**
     * Получить должность по ID
     */
    public PositionType getPositionTypeById(Long id) {
        return positionTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PositionType not found for ID: " + id));
    }

    /**
     * Создать новую должность
     */
    public PositionType createPositionType(PositionType positionType) {
        return positionTypeRepository.save(positionType);
    }

    /**
     * Обновить существующую должность
     */
    public PositionType updatePositionType(Long id, PositionType updatedPositionType) {
        if (positionTypeRepository.existsById(id)) {
            updatedPositionType.setId(id);
            return positionTypeRepository.save(updatedPositionType);
        }
        throw new ResourceNotFoundException("PositionType not found for ID: " + id);
    }

    /**
     * Удалить должность по ID
     */
    public void deletePositionType(Long id) {
        if (positionTypeRepository.existsById(id)) {
            positionTypeRepository.deleteById(id);
        }
    }

    /**
     * Создать новые должности из файла .cvs
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
     * Создать новые должности из пути к файлу .csv
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
            List<PositionType> positionTypes = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    PositionType positionType = new PositionType();
                    positionType.setId(Long.valueOf(values[0]));
                    positionType.setName(values[1]);
                    positionTypes.add(positionType);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            positionTypeRepository.saveAll(positionTypes);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }


}
