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
import ru.isands.test.estore.dao.entity.Purchase;
import ru.isands.test.estore.dao.repo.PurchaseRepository;
import ru.isands.test.estore.dto.PurchaseDTO;
import ru.isands.test.estore.exeption.CsvProcessingException;
import ru.isands.test.estore.exeption.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ElectroItemService electroItemService;
    private final EmployeeService employeeService;
    private final PurchaseTypeService purchaseTypeService;
    private final ShopService shopService;
    private final ElectroShopService electroShopService;

    @Value("${cvs.max.size.mb: 32}")
    private long maxFileSize;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, ElectroItemService electroItemService, EmployeeService employeeService, PurchaseTypeService purchaseTypeService, ShopService shopService, ElectroShopService electroShopService) {
        this.purchaseRepository = purchaseRepository;
        this.electroItemService = electroItemService;
        this.employeeService = employeeService;
        this.purchaseTypeService = purchaseTypeService;
        this.shopService = shopService;
        this.electroShopService = electroShopService;
    }

    /**
     * Получить список покупок с постраничным выводом
     */
    public Page<Purchase> getAllPurchases(Pageable pageable) {
        return purchaseRepository.findAll(pageable);
    }

    /**
     * Получить покупку по ID
     */
    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase not found for ID: " + id));
    }

    /**
     * Обновить существующую покупку
     */
    public Purchase updatePurchase(Long id, Purchase updatedPurchase) {
        if (purchaseRepository.existsById(id)) {
            updatedPurchase.setId(id);
            return purchaseRepository.save(updatedPurchase);
        }
        throw new ResourceNotFoundException("Purchase not found for ID: " + id);
    }

    /**
     * Создать новую покупку
     */
    @Transactional
    public Purchase createPurchase(PurchaseDTO purchaseDTO) {
        Purchase purchase = new Purchase();
        purchase.setElectroItem(electroItemService.getElectroItemById(purchaseDTO.getElectroItemId()));
        purchase.setEmployee(employeeService.getEmployeeById(purchaseDTO.getEmployeeId()));
        purchase.setShop(shopService.getShopById(purchaseDTO.getShopId()));
        purchase.setPurchaseDate(new Date());
        purchase.setPurchaseType(purchaseTypeService.getPurchaseTypeById(purchaseDTO.getPurchaseTypeId()));

        ElectroShop electroShop = electroShopService.getElectroShopById(new ElectroShopPK(purchaseDTO.getElectroItemId(), purchaseDTO.getShopId()));

        if (electroShop.getCount() > 0) {
            electroShop.setCount(electroShop.getCount() - 1);
            electroShopService.updateElectroShop(electroShop.getId(), electroShop);

            return purchaseRepository.save(purchase);
        } else {
            throw new ResourceNotFoundException("Товар с ID: " + purchaseDTO.getElectroItemId() + " не доступен в магазине с ID: " + purchaseDTO.getShopId());
        }
    }

    /**
     * Удалить покупку по ID
     */
    public void deletePurchase(Long id) {
        if (purchaseRepository.existsById(id)) {
            purchaseRepository.deleteById(id);
        }
    }

    /**
     * Создать новые покупки из файла .csv
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
     * Создать новые покупки из пути к файлу .csv
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
            List<Purchase> purchases = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                try {
                    Purchase purchase = new Purchase();
                    purchase.setId(Long.valueOf(values[0]));
                    purchase.setElectroItem(electroItemService.getElectroItemById(Long.valueOf(values[1])));
                    purchase.setEmployee(employeeService.getEmployeeById(Long.valueOf(values[2])));
                    purchase.setPurchaseDate(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(values[3]));
                    purchase.setPurchaseType(purchaseTypeService.getPurchaseTypeById(Long.valueOf(values[4])));
                    purchase.setShop(shopService.getShopById(Long.valueOf(values[5])));
                    purchases.add(purchase);
                } catch (ResourceNotFoundException | ParseException | NumberFormatException e) {
                    throw new CsvProcessingException("Ошибка обработки строки CSV: " + Arrays.toString(values) + ". " + e.getMessage());
                }
            }
            purchaseRepository.saveAll(purchases);
        } catch (CsvValidationException e) {
            throw new CsvProcessingException("Ошибка обработки строки CSV: " + e.getMessage());
        }
    }
}
