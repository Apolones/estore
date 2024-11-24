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
import ru.isands.test.estore.dao.entity.ElectroShop;
import ru.isands.test.estore.dao.entity.ElectroShopPK;
import ru.isands.test.estore.dao.repo.ElectroShopRepository;
import ru.isands.test.estore.dto.ElectroShopDTO;
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
public class ElectroShopService {

    private final ElectroShopRepository electroShopRepository;
    private final ShopService shopService;
    private final ElectroItemService electroItemService;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public ElectroShopService(ElectroShopRepository electroShopRepository, ShopService shopService, ElectroItemService electroItemService) {
        this.electroShopRepository = electroShopRepository;
        this.shopService = shopService;
        this.electroItemService = electroItemService;
    }

    /**
     * Получить список связей электротоваров и магазинов с постраничным выводом
     */
    public Page<ElectroShop> getAllElectroShops(Pageable pageable) {
        return electroShopRepository.findAll(pageable);
    }

    /**
     * Получить связь электротовара и магазина по составному ключу
     */
    public ElectroShop getElectroShopById(ElectroShopPK id) {
        return electroShopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Товар с ID: " + id.getElectroItem() + " не доступен в магазине с ID: " + id.getShop()));
    }

    /**
     * Создать новую связь электротовара и магазина
     */
    public ElectroShop createElectroShop(ElectroShopDTO electroShopDTO) {
        ElectroShopPK id = new ElectroShopPK(electroShopDTO.getElectroItemId(), electroShopDTO.getShopId());
        ElectroShop electroShop = new ElectroShop();
        electroShop.setId(id);
        electroShop.setShop(shopService.getShopById(electroShopDTO.getShopId()));
        electroShop.setElectroItem(electroItemService.getElectroItemById(electroShopDTO.getElectroItemId()));
        electroShop.setCount(electroShopDTO.getCount());
        return electroShopRepository.save(electroShop);
    }

    /**
     * Обновить связь электротовара и магазина
     */
    public ElectroShop updateElectroShop(ElectroShopPK id, ElectroShop updatedEelectroShop) {
        if (electroShopRepository.existsById(id)) {
            updatedEelectroShop.setId(id);
            return electroShopRepository.save(updatedEelectroShop);
        }
        throw new ResourceNotFoundException("Purchase not found for ID: " + id);
    }

    /**
     * Удалить связь электротовара и магазина по составному ключу
     */
    public void deleteElectroShop(ElectroShopPK id) {
        if (electroShopRepository.existsById(id)) {
            electroShopRepository.deleteById(id);
        }
    }

    /**
     * Проверка на наличие товара в магазине
     */
    public boolean checkItemAvailability(Long shopId, Long itemId) {
        return electroShopRepository.isItemAvailable(shopId, itemId);
    }

    /**
     * Создать новые связи электротоваров и магазинов из файла .cvs
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
     * Создать новые связи электротоваров и магазинов из пути к файлу .csv
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
            List<ElectroShop> electroShops = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    ElectroShop electroShop = new ElectroShop();

                    electroShop.setId(new ElectroShopPK(
                            Long.valueOf(values[0]),
                            Long.valueOf(values[1])
                    ));
                    electroShop.setShop(shopService.getShopById(Long.valueOf(values[0])));
                    electroShop.setElectroItem(electroItemService.getElectroItemById(Long.valueOf(values[1])));
                    electroShop.setCount(Integer.valueOf(values[2]));
                    electroShops.add(electroShop);
                } catch (ResourceNotFoundException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            electroShopRepository.saveAll(electroShops);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }


}
