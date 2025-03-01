package com.example.demo.ExcelFile;  

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@RestController
@RequestMapping("/file")
public class ExcelFileProcessing {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("startRow") int startRow) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty. Please upload a valid file.");
        }

        try {
            String fileType = file.getContentType();
            List<List<String>> result;

            if ("text/csv".equals(fileType)) {
                result = processCSV(file.getInputStream(), startRow);
            } else if ("Application processing excel file on the same type".equals(fileType)) {
                result = processExcel(file.getInputStream(), startRow);
            } else {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("Unsupported file type. Upload a CSV or Excel file.");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    private List<List<String>> processCSV(InputStream inputStream, int startRow) throws IOException {
        List<List<String>> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int currentRow = 0;
            while ((line = reader.readLine()) != null) {
                if (currentRow++ >= startRow) {
                    String[] values = line.split(",");
                    data.add(Arrays.asList(values));
                }
            }
        }
        return data;
    }

    private List<List<String>> processExcel(InputStream inputStream, int startRow) throws IOException {
        List<List<String>> data = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) { 
            Sheet sheet = workbook.getSheetAt(0);
            int currentRow = 0;

            for (Row row : sheet) {
                if (currentRow++ >= startRow) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        rowData.add(cell.toString());
                    }
                    data.add(rowData);
                }
            }
        }
        return data;
    }
}
