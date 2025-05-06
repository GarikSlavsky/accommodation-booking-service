package accommodation.booking.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.service.dto.payment.PaymentRequestDto;
import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.service.payment.StripeService;
import accommodation.booking.service.util.AuthenticationTestUtil;
import accommodation.booking.service.util.PaymentServiceTestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeMethod(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext webApplicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/add-users.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/add-additional-users.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/accommodation/add-accommodations.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/booking/add-bookings.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/booking/add-additional-bookings.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/payment/add-payments.sql")
            );
        }
    }

    @AfterEach
    void afterMethod(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/payment/remove-all-payments.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/booking/remove-all-bookings.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/accommodation/remove-all-accommodations.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/remove-all-users.sql")
            );
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Initiate payment for valid booking should succeed")
    void initiatePayment_ValidBookingAuthenticatedUser_ReturnsPaymentResponse() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                4L, "john.smith@example.com", Role.RoleName.ROLE_CUSTOMER);
        Long bookingId = 59L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(bookingId);

        PaymentResponseDto expected = new PaymentResponseDto();
        expected.setBookingId(bookingId);
        expected.setStatus(Payment.PaymentStatus.PENDING);
        expected.setSessionUrl("http://localhost:8081/payments/success?session_id=session_123");
        expected.setSessionId("session_123");
        expected.setAmountToPay(new BigDecimal("210.00"));

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        PaymentResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentResponseDto.class
        );
        assertNotNull(actual.getId());
        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Initiate payment for booking with existing payment should fail")
    void initiatePayment_BookingWithExistingPayment_ReturnsBadRequest() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                2L, "test@example.com", Role.RoleName.ROLE_MANAGER);
        Long bookingId = 36L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(bookingId);

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        mockMvc.perform(
                        post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(containsString("""
                                Payment already exists for this booking"""
                        ))
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Get payments for current user should return paginated list")
    void getPayments_CurrentUser_ReturnsPaginatedPayments() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                3L, "jane.doe@example.com", Role.RoleName.ROLE_CUSTOMER);
        PaymentResponseDto dto1 = new PaymentResponseDto();
        dto1.setId(40L);
        dto1.setBookingId(58L);
        dto1.setSessionUrl("http://example.com/session456");
        dto1.setSessionId("session456");
        dto1.setStatus(Payment.PaymentStatus.PAID);
        dto1.setAmountToPay(new BigDecimal("210.0"));

        PaymentResponseDto dto2 = new PaymentResponseDto();
        dto2.setId(42L);
        dto2.setBookingId(56L);
        dto2.setSessionUrl("http://localhost:8081/payments/success?session_id=session_123");
        dto2.setSessionId("session7890");
        dto2.setStatus(Payment.PaymentStatus.EXPIRED);
        dto2.setAmountToPay(new BigDecimal("210.0"));

        List<PaymentResponseDto> expected = List.of(dto1, dto2);

        Pageable pageable = Pageable.ofSize(4);
        String jsonRequest = objectMapper.writeValueAsString(pageable);
        // Act
        MvcResult result = mockMvc.perform(
                        get("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<PaymentResponseDto> actual = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<>() {}
        );
        assertThat(new HashSet<>(actual))
                .usingRecursiveComparison()
                .isEqualTo(new HashSet<>(expected));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Renew expired payment session should succeed")
    void renewPaymentSession_ValidExpiredPayment_ReturnsUpdatedPaymentResponse() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                3L, "jane.doe@example.com", Role.RoleName.ROLE_CUSTOMER);
        Long paymentId = 42L;
        PaymentResponseDto expected = new PaymentResponseDto();
        expected.setId(paymentId);
        expected.setBookingId(56L);
        expected.setStatus(Payment.PaymentStatus.PENDING);
        expected.setSessionUrl("http://localhost:8081/payments/success?session_id=session_123");
        expected.setSessionId("session_123");
        expected.setAmountToPay(new BigDecimal("210.00"));

        // Act
        MvcResult result = mockMvc.perform(
                        post("/payments/renew/{paymentId}", paymentId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        PaymentResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentResponseDto.class
        );
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @TestConfiguration
    static class PaymentTestConfig {
        @Bean
        @Primary
        public StripeService stripeService() {
            StripeService stripeService = Mockito.mock(StripeService.class);
            Session stripeSession = PaymentServiceTestUtil.initializeStripeSession();
            when(stripeService.createSession(any(), any())).thenReturn(stripeSession);
            return stripeService;
        }
    }
}
