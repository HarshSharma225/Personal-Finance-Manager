package com.finance.manager.controller;

import com.finance.manager.dto.TransactionDto;
import com.finance.manager.dto.UserDto;
import com.finance.manager.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDto.TransactionResponse> createTransaction(@Valid @RequestBody TransactionDto.CreateTransactionRequest request) {
        TransactionDto.TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<TransactionDto.TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type) {
        TransactionDto.TransactionListResponse response = transactionService.getTransactions(
                startDate,
                endDate,
                categoryId,
                category,
                type
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto.TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto.UpdateTransactionRequest request) {
        TransactionDto.TransactionResponse response = transactionService.updateTransaction(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto.MessageResponse> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(
                new UserDto.MessageResponse("Transaction deleted successfully"),
                HttpStatus.OK
        );
    }
}
