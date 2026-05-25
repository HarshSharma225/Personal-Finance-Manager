package com.finance.manager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GoalDto {

    public static class CreateGoalRequest {

        @NotBlank(message = "Goal name is mandatory")
        private String goalName;

        @NotNull(message = "Target amount is mandatory")
        @DecimalMin(value = "0.01", message = "Target amount must be a positive value")
        private BigDecimal targetAmount;

        @NotNull(message = "Target date is mandatory")
        private LocalDate targetDate;

        private LocalDate startDate; // Optional, will default to today if null

        public CreateGoalRequest() {
        }

        public String getGoalName() {
            return goalName;
        }

        public void setGoalName(String goalName) {
            this.goalName = goalName;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }
    }

    public static class UpdateGoalRequest {

        @DecimalMin(value = "0.01", message = "Target amount must be a positive value")
        private BigDecimal targetAmount;

        private LocalDate targetDate;

        public UpdateGoalRequest() {
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
        }
    }

    public static class GoalResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private LocalDate startDate;
        private BigDecimal currentProgress;
        private double progressPercentage;
        private BigDecimal remainingAmount;

        public GoalResponse(Long id, String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate,
                            BigDecimal currentProgress, double progressPercentage, BigDecimal remainingAmount) {
            this.id = id;
            this.goalName = goalName;
            this.targetAmount = targetAmount;
            this.targetDate = targetDate;
            this.startDate = startDate;
            this.currentProgress = currentProgress;
            this.progressPercentage = progressPercentage;
            this.remainingAmount = remainingAmount;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getGoalName() {
            return goalName;
        }

        public void setGoalName(String goalName) {
            this.goalName = goalName;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public BigDecimal getCurrentProgress() {
            return currentProgress;
        }

        public void setCurrentProgress(BigDecimal currentProgress) {
            this.currentProgress = currentProgress;
        }

        public double getProgressPercentage() {
            return progressPercentage;
        }

        public void setProgressPercentage(double progressPercentage) {
            this.progressPercentage = progressPercentage;
        }

        public BigDecimal getRemainingAmount() {
            return remainingAmount;
        }

        public void setRemainingAmount(BigDecimal remainingAmount) {
            this.remainingAmount = remainingAmount;
        }
    }

    public static class GoalListResponse {
        private List<GoalResponse> goals;

        public GoalListResponse(List<GoalResponse> goals) {
            this.goals = goals;
        }

        public List<GoalResponse> getGoals() {
            return goals;
        }

        public void setGoals(List<GoalResponse> goals) {
            this.goals = goals;
        }
    }
}
