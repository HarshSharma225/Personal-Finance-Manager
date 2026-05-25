package com.finance.manager.controller;

import com.finance.manager.dto.GoalDto;
import com.finance.manager.dto.UserDto;
import com.finance.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<GoalDto.GoalResponse> createGoal(@Valid @RequestBody GoalDto.CreateGoalRequest request) {
        GoalDto.GoalResponse response = savingsGoalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<GoalDto.GoalListResponse> getAllGoals() {
        GoalDto.GoalListResponse response = savingsGoalService.getAllGoals();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto.GoalResponse> getGoalById(@PathVariable Long id) {
        GoalDto.GoalResponse response = savingsGoalService.getGoalById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto.GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalDto.UpdateGoalRequest request) {
        GoalDto.GoalResponse response = savingsGoalService.updateGoal(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto.MessageResponse> deleteGoal(@PathVariable Long id) {
        savingsGoalService.deleteGoal(id);
        return new ResponseEntity<>(
                new UserDto.MessageResponse("Goal deleted successfully"),
                HttpStatus.OK
        );
    }
}
