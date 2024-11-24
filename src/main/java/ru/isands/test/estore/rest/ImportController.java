package ru.isands.test.estore.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.isands.test.estore.service.ImportService;

@RestController
@RequestMapping("/estore/api/upload")
public class ImportController {
    private final ImportService importService;

    @Autowired
    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/zip")
    @Operation(summary = "Загрузить zip архив", responses = {
            @ApiResponse(description = "Zip архив загружен", responseCode = "200"),
            @ApiResponse(description = "Ошибка при импорте ZIP: ", responseCode = "500")
    })
    public ResponseEntity<?> uploadZip(@RequestParam("file") MultipartFile file, @RequestParam(value = "encoding", defaultValue = "Windows-1251") String encoding) {
        importService.uploadZip(file, encoding);
        return ResponseEntity.ok("Zip импортирован успешно!");
    }
}

