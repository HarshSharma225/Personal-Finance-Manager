package com.finance.manager.service;

import com.finance.manager.dto.TransactionDto;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.CustomException;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Transactional
    public TransactionDto.TransactionResponse createTransaction(TransactionDto.CreateTransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException.BadRequestException("Amount must be a positive value");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new CustomException.BadRequestException("Date cannot be in the future");
        }

        Category category = categoryService.getValidCategory(request.getCategory(), currentUser);

        Transaction transaction = new Transaction(
                request.getAmount(),
                request.getDate(),
                category,
                request.getDescription(),
                category.getType(),
                currentUser
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionDto.TransactionListResponse getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            String categoryName,
            String type) {
        User currentUser = userService.getCurrentUser();

        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                currentUser,
                startDate,
                endDate,
                categoryId,
                categoryName,
                type
        );

        List<TransactionDto.TransactionResponse> responses = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new TransactionDto.TransactionListResponse(responses);
    }

    @Transactional
    public TransactionDto.TransactionResponse updateTransaction(Long id, TransactionDto.UpdateTransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException.ForbiddenException("Access to other user's data is forbidden");
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException.BadRequestException("Amount must be a positive value");
            }
            transaction.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            Category category = categoryService.getValidCategory(request.getCategory(), currentUser);
            transaction.setCategory(category);
            transaction.setType(category.getType());
        }

        // Date update is explicitly ignored/restricted as per specs

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException.ForbiddenException("Access to other user's data is forbidden");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionDto.TransactionResponse mapToResponse(Transaction t) {
        return new TransactionDto.TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getDate(),
                t.getCategory().getName(),
                t.getDescription(),
                t.getType()
        );
    }
}
