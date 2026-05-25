package com.finance.manager;

import com.finance.manager.dto.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.CustomException;
import com.finance.manager.repository.*;
import com.finance.manager.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PersonalFinanceManagerApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CategoryService categoryService;
    private TransactionService transactionService;
    private SavingsGoalService savingsGoalService;
    private ReportService reportService;

    private User testUser;
    private Category defaultIncomeCategory;
    private Category customExpenseCategory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        categoryService = new CategoryService(categoryRepository, userService, transactionRepository);
        transactionService = new TransactionService(transactionRepository, userService, categoryService);
        savingsGoalService = new SavingsGoalService(savingsGoalRepository, transactionRepository, userService);
        reportService = new ReportService(transactionRepository, userService);

        testUser = new User("john.doe@example.com", "encodedPassword", "John Doe", "+1234567890");
        testUser.setId(1L);

        defaultIncomeCategory = new Category("Salary", "INCOME", false, null);
        defaultIncomeCategory.setId(1L);

        customExpenseCategory = new Category("Gym", "EXPENSE", true, testUser);
        customExpenseCategory.setId(2L);
    }

    private void mockAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testUserRegistration_Success() {
        UserDto.RegisterRequest request = new UserDto.RegisterRequest();
        request.setUsername("new.user@example.com");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setPhoneNumber("+1111111111");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registered = userService.register(request);

        assertNotNull(registered);
        assertEquals(testUser.getUsername(), registered.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUserRegistration_Conflict() {
        UserDto.RegisterRequest request = new UserDto.RegisterRequest();
        request.setUsername("john.doe@example.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(CustomException.ConflictException.class, () -> userService.register(request));
    }

    @Test
    public void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("john.doe@example.com");

        assertNotNull(userDetails);
        assertEquals("john.doe@example.com", userDetails.getUsername());
    }

    @Test
    public void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("none@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("none@example.com"));
    }

    @Test
    public void testGetCurrentUser_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        User current = userService.getCurrentUser();

        assertNotNull(current);
        assertEquals(testUser.getUsername(), current.getUsername());
    }

    @Test
    public void testGetCurrentUser_Unauthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(CustomException.UnauthorizedException.class, () -> userService.getCurrentUser());
    }

    @Test
    public void testGetAllCategories_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByUserIsNull()).thenReturn(Collections.singletonList(defaultIncomeCategory));
        when(categoryRepository.findByUser(testUser)).thenReturn(Collections.singletonList(customExpenseCategory));

        CategoryDto.CategoryListResponse response = categoryService.getAllCategories();

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
    }

    @Test
    public void testCreateCategory_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));
        
        CategoryDto.CreateCategoryRequest request = new CategoryDto.CreateCategoryRequest();
        request.setName("Freelance");
        request.setType("INCOME");

        when(categoryRepository.existsByNameAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", testUser)).thenReturn(false);
        
        Category savedCat = new Category("Freelance", "INCOME", true, testUser);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCat);

        CategoryDto.CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Freelance", response.getName());
        assertTrue(response.getIsCustom());
    }

    @Test
    public void testDeleteCategory_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByNameAndUserIsNull("Gym")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Gym", testUser)).thenReturn(Optional.of(customExpenseCategory));
        when(transactionRepository.existsByCategory(customExpenseCategory)).thenReturn(false);

        categoryService.deleteCategory("Gym");

        verify(categoryRepository, times(1)).delete(customExpenseCategory);
    }

    @Test
    public void testDeleteCategory_Predefined() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultIncomeCategory));

        assertThrows(CustomException.BadRequestException.class, () -> categoryService.deleteCategory("Salary"));
    }

    @Test
    public void testCreateTransaction_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        TransactionDto.CreateTransactionRequest request = new TransactionDto.CreateTransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setDate(LocalDate.of(2024, 1, 15));
        request.setCategory("Salary");
        request.setDescription("Salary payment");

        when(categoryRepository.findByNameAndUser("Salary", testUser)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultIncomeCategory));

        Transaction savedTx = new Transaction(new BigDecimal("500.00"), LocalDate.of(2024, 1, 15), defaultIncomeCategory, "Salary payment", "INCOME", testUser);
        savedTx.setId(10L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionDto.TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("INCOME", response.getType());
    }

    @Test
    public void testUpdateTransaction_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        Transaction tx = new Transaction(new BigDecimal("100.00"), LocalDate.of(2024, 1, 15), customExpenseCategory, "Gym bill", "EXPENSE", testUser);
        tx.setId(15L);
        when(transactionRepository.findById(15L)).thenReturn(Optional.of(tx));

        TransactionDto.UpdateTransactionRequest request = new TransactionDto.UpdateTransactionRequest();
        request.setAmount(new BigDecimal("120.00"));
        request.setDescription("Updated Gym bill");

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto.TransactionResponse response = transactionService.updateTransaction(15L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("120.00"), response.getAmount());
        assertEquals("Updated Gym bill", response.getDescription());
    }

    @Test
    public void testCreateGoal_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        GoalDto.CreateGoalRequest request = new GoalDto.CreateGoalRequest();
        request.setGoalName("Car savings");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));
        request.setStartDate(LocalDate.now());

        SavingsGoal savedGoal = new SavingsGoal("Car savings", new BigDecimal("10000.00"), LocalDate.now().plusYears(1), LocalDate.now(), testUser);
        savedGoal.setId(1L);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savedGoal);
        when(transactionRepository.findByUser(testUser)).thenReturn(new ArrayList<>());

        GoalDto.GoalResponse response = savingsGoalService.createGoal(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(0.0, response.getProgressPercentage());
    }

    @Test
    public void testMonthlyReport_Success() {
        mockAuthentication("john.doe@example.com");
        when(userRepository.findByUsername("john.doe@example.com")).thenReturn(Optional.of(testUser));

        Transaction tx1 = new Transaction(new BigDecimal("2000.00"), LocalDate.of(2024, 1, 10), defaultIncomeCategory, "Salary", "INCOME", testUser);
        Transaction tx2 = new Transaction(new BigDecimal("500.00"), LocalDate.of(2024, 1, 20), customExpenseCategory, "Gym", "EXPENSE", testUser);

        when(transactionRepository.findByUser(testUser)).thenReturn(List.of(tx1, tx2));

        ReportDto.MonthlyReportResponse response = reportService.getMonthlyReport(2024, 1);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("1500.00"), response.getNetSavings());
        assertEquals(new BigDecimal("2000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("500.00"), response.getTotalExpenses().get("Gym"));
    }
}
