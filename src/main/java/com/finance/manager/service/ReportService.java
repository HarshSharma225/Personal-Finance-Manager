package com.finance.manager.service;

import com.finance.manager.dto.ReportDto;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.CustomException;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ReportService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public ReportDto.MonthlyReportResponse getMonthlyReport(int year, int month) {
        if (month < 1 || month > 12) {
            throw new CustomException.BadRequestException("Month must be between 1 and 12");
        }

        User currentUser = userService.getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getDate().getYear() == year && t.getDate().getMonthValue() == month) {
                String categoryName = t.getCategory().getName();
                BigDecimal amount = t.getAmount().setScale(2, RoundingMode.HALF_UP);

                if ("INCOME".equals(t.getType())) {
                    totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                    sumIncome = sumIncome.add(amount);
                } else if ("EXPENSE".equals(t.getType())) {
                    totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                    sumExpense = sumExpense.add(amount);
                }
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpense).setScale(2, RoundingMode.HALF_UP);

        return new ReportDto.MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }

    @Transactional(readOnly = true)
    public ReportDto.YearlyReportResponse getYearlyReport(int year) {
        User currentUser = userService.getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getDate().getYear() == year) {
                String categoryName = t.getCategory().getName();
                BigDecimal amount = t.getAmount().setScale(2, RoundingMode.HALF_UP);

                if ("INCOME".equals(t.getType())) {
                    totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                    sumIncome = sumIncome.add(amount);
                } else if ("EXPENSE".equals(t.getType())) {
                    totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                    sumExpense = sumExpense.add(amount);
                }
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpense).setScale(2, RoundingMode.HALF_UP);

        return new ReportDto.YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
