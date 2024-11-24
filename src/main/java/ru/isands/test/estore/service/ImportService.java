package ru.isands.test.estore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.isands.test.estore.exeption.ZipProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class ImportService {

    private final ElectroEmployeeService electroEmployeeService;
    private final ElectroItemService electroItemService;
    private final ElectroShopService electroShopService;
    private final ElectroTypeService electroTypeService;
    private final EmployeeService employeeService;
    private final PositionTypeService positionTypeService;
    private final PurchaseService purchaseService;
    private final PurchaseTypeService purchaseTypeService;
    private final ShopService shopService;

    @Value("${cvs.max.size.mb:32}")
    private long maxFileSize;

    @Autowired
    public ImportService(
            ElectroEmployeeService electroEmployeeService,
            ElectroItemService electroItemService,
            ElectroShopService electroShopService,
            ElectroTypeService electroTypeService,
            EmployeeService employeeService,
            PositionTypeService positionTypeService,
            PurchaseService purchaseService,
            PurchaseTypeService purchaseTypeService,
            ShopService shopService) {
        this.electroEmployeeService = electroEmployeeService;
        this.electroItemService = electroItemService;
        this.electroShopService = electroShopService;
        this.electroTypeService = electroTypeService;
        this.employeeService = employeeService;
        this.positionTypeService = positionTypeService;
        this.purchaseService = purchaseService;
        this.purchaseTypeService = purchaseTypeService;
        this.shopService = shopService;
    }

    /**
     * Заполнить БД файлами .csv из архива .zip
     */
    public void uploadZip(MultipartFile file, String encoding) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            throw new ZipProcessingException("Файл не является ZIP архивом");
        }

        if (file.getSize() > maxFileSize * 1024 * 1024) {
            throw new ZipProcessingException("Превышен максимальный размер ZIP файла: текущий: "
                    + file.getSize() / (1024 * 1024) + "mb, максимальный: " + maxFileSize + "mb");
        }

        try {
            Path tempDir = Files.createTempDirectory("unzipped-");
            List<Path> csvFiles = new ArrayList<>();

            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = tempDir.resolve(entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    } else {
                        Files.createDirectories(filePath.getParent());
                        try (OutputStream os = Files.newOutputStream(filePath)) {
                            zis.transferTo(os);
                        }
                        if (entry.getName().endsWith(".csv")) {
                            csvFiles.add(filePath);
                        }
                    }
                }
            }

            Map<String, Integer> priorityMap = new HashMap<>() {{
                put("ElectroType", 1);
                put("PositionType", 1);
                put("PurchaseType", 1);
                put("Shop", 1);
                put("ElectroItem", 2);
                put("Employee", 2);
                put("Purchase", 3);
                put("ElectroEmployee", 4);
                put("ElectroShop", 4);
            }};

            csvFiles.sort(Comparator.comparingInt(file1 ->
                    priorityMap.getOrDefault(getBaseName(file1.getFileName().toString()), Integer.MAX_VALUE)));

            for (Path csvFile : csvFiles) {
                sendDataToService(csvFile, encoding);
            }

            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        } catch (IOException e) {
            throw new ZipProcessingException("Ошибка распаковки ZIP архива " + e);
        }
    }

    private String getBaseName(String fileName) {
        return fileName.replaceFirst("\\.csv$", "");
    }

    private void sendDataToService(Path path, String encoding) {
        String fileName = path.getFileName().toString();
        try {
            if (fileName.contains("ElectroEmployee")) {
                electroEmployeeService.importCsv(path, encoding);
            } else if (fileName.contains("ElectroItem")) {
                electroItemService.importCsv(path, encoding);
            } else if (fileName.contains("ElectroShop")) {
                electroShopService.importCsv(path, encoding);
            } else if (fileName.contains("ElectroType")) {
                electroTypeService.importCsv(path, encoding);
            } else if (fileName.contains("Employee")) {
                employeeService.importCsv(path, encoding);
            } else if (fileName.contains("PositionType")) {
                positionTypeService.importCsv(path, encoding);
            } else if (fileName.contains("PurchaseType")) {
                purchaseTypeService.importCsv(path, encoding);
            } else if (fileName.contains("Purchase")) {
                purchaseService.importCsv(path, encoding);
            } else if (fileName.contains("Shop")) {
                shopService.importCsv(path, encoding);
            } else {
                throw new ZipProcessingException("Неизвестный тип файла: " + fileName);
            }
        } catch (IOException e) {
            throw new ZipProcessingException("Ошибка обработки файла: " + fileName + " " + e);
        }
    }
}
