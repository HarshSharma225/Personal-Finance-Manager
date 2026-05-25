package com.finance.manager.service;

import com.finance.manager.dto.GoalDto;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.CustomException;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, TransactionRepository transactionRepository, UserService userService) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @Transactional
    public GoalDto.GoalResponse createGoal(GoalDto.CreateGoalRequest request) {
        User currentUser = userService.getCurrentUser();

        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException.BadRequestException("Target amount must be a positive value");
        }
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new CustomException.BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        if (startDate.isAfter(request.getTargetDate())) {
            throw new CustomException.BadRequestException("Start date cannot be after target date");
        }

        SavingsGoal goal = new SavingsGoal(
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                startDate,
                currentUser
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return calculateProgress(saved, currentUser);
    }

    @Transactional(readOnly = true)
    public GoalDto.GoalListResponse getAllGoals() {
        User currentUser = userService.getCurrentUser();
        List<SavingsGoal> goals = savingsGoalRepository.findByUser(currentUser);

        List<GoalDto.GoalResponse> responses = goals.stream()
                .map(goal -> calculateProgress(goal, currentUser))
                .collect(Collectors.toList());

        return new GoalDto.GoalListResponse(responses);
    }

    @Transactional(readOnly = true)
    public GoalDto.GoalResponse getGoalById(Long id) {
        User currentUser = userService.getCurrentUser();

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Savings goal not found"));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException.ForbiddenException("Access to other user's data is forbidden");
        }

        return calculateProgress(goal, currentUser);
    }

    @Transactional
    public GoalDto.GoalResponse updateGoal(Long id, GoalDto.UpdateGoalRequest request) {
        User currentUser = userService.getCurrentUser();

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Savings goal not found"));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException.ForbiddenException("Access to other user's data is forbidden");
        }

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException.BadRequestException("Target amount must be a positive value");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new CustomException.BadRequestException("Target date must be in the future");
            }
            if (goal.getStartDate().isAfter(request.getTargetDate())) {
                throw new CustomException.BadRequestException("Start date cannot be after target date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return calculateProgress(saved, currentUser);
    }

    @Transactional
    public void deleteGoal(Long id) {
        User currentUser = userService.getCurrentUser();

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new CustomException.ResourceNotFoundException("Savings goal not found"));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException.ForbiddenException("Access to other user's data is forbidden");
        }

        savingsGoalRepository.delete(goal);
    }

    private GoalDto.GoalResponse calculateProgress(SavingsGoal goal, User user) {
        List<Transaction> transactions = transactionRepository.findByUser(user);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(goal.getStartDate())) {
                if ("INCOME".equals(t.getType())) {
                    totalIncome = totalIncome.add(t.getAmount());
                } else if ("EXPENSE".equals(t.getType())) {
                    totalExpense = totalExpense.add(t.getAmount());
                }
            }
        }

        BigDecimal currentProgress = totalIncome.subtract(totalExpense);
        
        // Progress towards a savings goal cannot logically be negative
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal targetAmount = goal.getTargetAmount();
        double progressPercentage = 0.0;
        
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = (currentProgress.doubleValue() / targetAmount.doubleValue()) * 100.0;
            // Round to 2 decimal places
            progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;
        }

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return new GoalDto.GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                targetAmount.setScale(2, RoundingMode.HALF_UP),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress.setScale(2, RoundingMode.HALF_UP),
                progressPercentage,
                remainingAmount.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
