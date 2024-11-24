package ru.isands.test.estore.exeption;

public class CsvProcessingException extends RuntimeException {
    public CsvProcessingException(String message) {
        super(message);
    }
}