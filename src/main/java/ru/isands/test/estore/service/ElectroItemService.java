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
import ru.isands.test.estore.dao.entity.ElectroItem;
import ru.isands.test.estore.dao.repo.ElectroItemRepository;
import ru.isands.test.estore.dto.ElectroItemDTO;
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
public class ElectroItemService {

    private final ElectroItemRepository electroItemRepository;
    private final ElectroTypeService electroTypeService;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public ElectroItemService(ElectroItemRepository electroItemRepository, ElectroTypeService electroTypeService) {
        this.electroItemRepository = electroItemRepository;
        this.electroTypeService = electroTypeService;
    }

    /**
     * Получить электротовары с постраничным выводом
     */
    public Page<ElectroItem> getAllElectroItems(Pageable pageable) {
        return electroItemRepository.findAll(pageable);
    }

    /**
     * Получить электротовар по ID
     */
    public ElectroItem getElectroItemById(Long id) {
        return electroItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ElectroItem not found for ID: " + id));
    }

    /**
     * Создать новый электротовар
     */
    public ElectroItem createElectroItem(ElectroItemDTO electroItemDTO) {
        ElectroItem electroItem = new ElectroItem();
        System.out.println(electroItemDTO.getETypeId());
        electroItem.setName(electroItemDTO.getName());
        electroItem.setEType(electroTypeService.getElectroTypeById(electroItemDTO.getETypeId()));
        electroItem.setPrice(electroItemDTO.getPrice());
        electroItem.setCount(electroItemDTO.getCount());
        electroItem.setArchive(false);
        electroItem.setDescription(electroItemDTO.getDescription());
        return electroItemRepository.save(electroItem);
    }

    /**
     * Обновить существующий электротовар
     */
    public ElectroItem updateElectroItem(Long id, ElectroItem updatedElectroItem) {
        if (electroItemRepository.existsById(id)) {
            updatedElectroItem.setId(id);
            return electroItemRepository.save(updatedElectroItem);
        }
        throw new ResourceNotFoundException("ElectroItem not found for ID: " + id);
    }

    /**
     * Удалить электротовар по ID
     */
    public void deleteElectroItem(Long id) {
        if (electroItemRepository.existsById(id)) {
            electroItemRepository.deleteById(id);
        }
    }

    /**
     * Создать новые электротовары из файла .csv
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
     * Создать новые электротовары из пути к файлу .csv
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
            List<ElectroItem> electroItems = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    ElectroItem electroItem = new ElectroItem();
                    electroItem.setId(Long.valueOf(values[0]));
                    electroItem.setName(values[1]);
                    electroItem.setEType(electroTypeService.getElectroTypeById(Long.valueOf(values[2])));
                    electroItem.setPrice(Long.valueOf(values[3]));
                    electroItem.setCount(Integer.valueOf(values[4]));
                    electroItem.setArchive(Boolean.parseBoolean(values[5]));
                    electroItem.setDescription(values[6]);
                    electroItems.add(electroItem);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            electroItemRepository.saveAll(electroItems);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }


}
