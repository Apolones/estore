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
import ru.isands.test.estore.dao.entity.PurchaseType;
import ru.isands.test.estore.dao.repo.PurchaseTypeRepository;
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
public class PurchaseTypeService {

    private final PurchaseTypeRepository purchaseTypeRepository;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public PurchaseTypeService(PurchaseTypeRepository purchaseTypeRepository) {
        this.purchaseTypeRepository = purchaseTypeRepository;
    }

    /**
     * Получить все типы покупок с постраничным выводом
     */
    public Page<PurchaseType> getAllPurchaseTypes(Pageable pageable) {
        return purchaseTypeRepository.findAll(pageable);
    }

    /**
     * Получить тип покупки по ID
     */
    public PurchaseType getPurchaseTypeById(Long id) {
        return purchaseTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PurchaseType not found for ID: " + id));
    }

    /**
     * Создать новый тип покупки
     */
    public PurchaseType createPurchaseType(PurchaseType purchaseType) {
        return purchaseTypeRepository.save(purchaseType);
    }

    /**
     * Обновить тип покупки
     */
    public PurchaseType updatePurchaseType(Long id, PurchaseType purchaseType) {
        if (purchaseTypeRepository.existsById(id)) {
            purchaseType.setId(id);
            return purchaseTypeRepository.save(purchaseType);
        }
        throw new ResourceNotFoundException("PurchaseType not found for ID: " + id);
    }

    /**
     * Удалить тип покупки по ID
     */
    public void deletePurchaseType(Long id) {
        if (purchaseTypeRepository.existsById(id)) {
            purchaseTypeRepository.deleteById(id);
        }
    }

    /**
     * Создать новые типы покупок из файла .csv
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
     * Создать новые типы покупок пути к файлу .csv
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
            List<PurchaseType> purchaseTypes = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    PurchaseType purchaseType = new PurchaseType();
                    purchaseType.setId(Long.valueOf(values[0]));
                    purchaseType.setName(values[1]);
                    purchaseTypes.add(purchaseType);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            purchaseTypeRepository.saveAll(purchaseTypes);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }
}
