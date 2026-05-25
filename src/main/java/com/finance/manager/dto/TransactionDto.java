package com.finance.manager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDto {

    public static class CreateTransactionRequest {

        @NotNull(message = "Amount is mandatory")
        @DecimalMin(value = "0.01", message = "Amount must be a positive value")
        private BigDecimal amount;

        @NotNull(message = "Date is mandatory")
        @PastOrPresent(message = "Date cannot be in the future")
        private LocalDate date;

        @NotBlank(message = "Category is mandatory")
        private String category;

        private String description;

        public CreateTransactionRequest() {
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class UpdateTransactionRequest {

        @DecimalMin(value = "0.01", message = "Amount must be a positive value")
        private BigDecimal amount;

        private String category;

        private String description;

        private LocalDate date; // even if passed, will be ignored during update

        public UpdateTransactionRequest() {
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public static class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private LocalDate date;
        private String category;
        private String description;
        private String type;

        public TransactionResponse(Long id, BigDecimal amount, LocalDate date, String category, String description, String type) {
            this.id = id;
            this.amount = amount;
            this.date = date;
            this.category = category;
            this.description = description;
            this.type = type;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class TransactionListResponse {
        private List<TransactionResponse> transactions;

        public TransactionListResponse(List<TransactionResponse> transactions) {
            this.transactions = transactions;
        }

        public List<TransactionResponse> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<TransactionResponse> transactions) {
            this.transactions = transactions;
        }
    }
}
