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
import ru.isands.test.estore.dao.entity.Shop;
import ru.isands.test.estore.dao.repo.ShopRepository;
import ru.isands.test.estore.dto.ShopDto;
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
public class ShopService {

    private final ShopRepository shopRepository;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    /**
     * Получить все магазины с постраничным выводом
     */
    public Page<Shop> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable);
    }

    /**
     * Получить магазин по ID
     */
    public Shop getShopById(Long id) {
        return shopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shop not found for ID: " + id));
    }

    /**
     * Создать новый магазин
     */
    public Shop createShop(Shop shop) {
        return shopRepository.save(shop);
    }

    /**
     * Обновить магазин
     */
    public Shop updateShop(Long id, Shop shop) {
        if (shopRepository.existsById(id)) {
            shop.setId(id);
            return shopRepository.save(shop);
        }
        throw new ResourceNotFoundException("Shop not found for ID: " + id);
    }

    /**
     * Удалить магазин по ID
     */
    public void deleteShop(Long id) {
        if (shopRepository.existsById(id)) {
            shopRepository.deleteById(id);
        }
    }

    /**
     * Вывод суммы денежных средств, полученной магазином через оплату "Наличныме"
     */
    public List<ShopDto> getShopsByPurchaseType(Long id, String purchaseType) {
        return shopRepository.findShopsByPurchaseType(id, purchaseType);
    }


    /**
     * Создать новые магазины из файла .csv
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
     * Создать новые магазины из пути к файлу .csv
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
            List<Shop> shops = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    Shop shop = new Shop();
                    shop.setId(Long.valueOf(values[0]));
                    shop.setName(values[1]);
                    shop.setAddress(values[2]);
                    shops.add(shop);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            shopRepository.saveAll(shops);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }
}
