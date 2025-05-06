package accommodation.booking.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.dto.booking.BookingStatusPatchRequestDto;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.util.AuthenticationTestUtil;
import accommodation.booking.service.util.BookingTestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class BookingControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(
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
    void afterEach(@Autowired DataSource dataSource) {
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
    @DisplayName("Create booking with valid input and no pending payments should succeed")
    void createBooking_ValidInputNoPendingPayments_ReturnsCreated() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                3L, "jane.doe@example.com", Role.RoleName.ROLE_CUSTOMER);
        BookingRequestDto requestDto = BookingTestUtil.initializeBookingRequestDto();

        BookingResponseDto expected =
                BookingTestUtil.initializeBookingResponseDtoDirectlyFromRequest(requestDto);
        expected.setUserId(3L);
        expected.setStatus(Booking.BookingStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        post("/bookings")
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        BookingResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponseDto.class
        );
        assertNotNull(actual.getId());
        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Create booking with pending payments should fail")
    void createBooking_WithPendingPayments_ReturnsBadRequest() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                2L, "test@example.com", Role.RoleName.ROLE_MANAGER);
        BookingRequestDto requestDto = BookingTestUtil.initializeBookingRequestDto();
        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        mockMvc.perform(post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(containsString("""
                                Cannot create new booking: You have 1 pending payment(s)"""
                        ))
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Get bookings for authenticated user should return paginated list")
    void getUserBookings_AuthenticatedUser_ReturnsPaginatedBookings() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                4L, "john.smith@example.com", Role.RoleName.ROLE_CUSTOMER);
        BookingResponseDto dto1 = new BookingResponseDto();
        dto1.setId(59L);
        dto1.setAccommodationId(15L);
        dto1.setUserId(4L);
        dto1.setCheckInDate(LocalDate.of(2026, 5, 8));
        dto1.setCheckOutDate(LocalDate.of(2026, 5, 15));
        dto1.setStatus(Booking.BookingStatus.PENDING);

        BookingResponseDto dto2 = new BookingResponseDto();
        dto2.setId(61L);
        dto2.setAccommodationId(15L);
        dto2.setUserId(4L);
        dto2.setCheckInDate(LocalDate.of(2026, 5, 10));
        dto2.setCheckOutDate(LocalDate.of(2026, 5, 20));
        dto2.setStatus(Booking.BookingStatus.PENDING);

        List<BookingResponseDto> expected = List.of(dto1, dto2);
        Pageable pageable = Pageable.ofSize(4);
        String jsonRequest = objectMapper.writeValueAsString(pageable);

        // Act
        MvcResult result = mockMvc.perform(get("/bookings/my")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<BookingResponseDto> actual = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<>() {}
        );

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        assertThat(new HashSet<>(actual))
                .usingRecursiveComparison()
                .isEqualTo(new HashSet<>(expected));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Get booking by ID for authenticated user should succeed")
    void getBookingById_ValidIdAuthenticatedUser_ReturnsBooking() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                4L, "john.smith@example.com", Role.RoleName.ROLE_CUSTOMER);
        Long bookingId = 59L;
        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(bookingId);
        expected.setAccommodationId(15L);
        expected.setUserId(4L);
        expected.setCheckInDate(LocalDate.of(2026, 5, 8));
        expected.setCheckOutDate(LocalDate.of(2026, 5, 15));
        expected.setStatus(Booking.BookingStatus.PENDING);

        // Act
        MvcResult result = mockMvc.perform(get("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        BookingResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponseDto.class
        );
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER", "CUSTOMER"})
    @DisplayName("Get booking by ID not owned by user should fail")
    void getBookingById_NotOwnedByUser_ReturnsForbidden() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                4L, "john.smith@example.com", Role.RoleName.ROLE_CUSTOMER);
        Long bookingId = 67L;

        // Act
        mockMvc.perform(get("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error")
                        .value(containsString("""
                                Access denied: You are not authorized to view this booking."""
                        ))
                );
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Update booking status with valid input by manager should succeed")
    void updateBookingStatus_ValidInputByManager_ReturnsUpdatedBooking() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                2L, "test@example.com", Role.RoleName.ROLE_MANAGER);
        Long bookingId = 59L;
        BookingStatusPatchRequestDto requestDto = new BookingStatusPatchRequestDto();
        requestDto.setStatus(Booking.BookingStatus.CONFIRMED);

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(bookingId);
        expected.setAccommodationId(15L);
        expected.setUserId(4L);
        expected.setCheckInDate(LocalDate.of(2026, 5, 8));
        expected.setCheckOutDate(LocalDate.of(2026, 5, 15));
        expected.setStatus(Booking.BookingStatus.CONFIRMED);

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        patch("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        BookingResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponseDto.class
        );
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Update booking status by non-manager should fail")
    void updateBookingStatus_NonManagerRole_ReturnsForbidden() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                4L, "john.smith@example.com", Role.RoleName.ROLE_CUSTOMER);
        Long bookingId = 59L;
        BookingStatusPatchRequestDto requestDto = new BookingStatusPatchRequestDto();
        requestDto.setStatus(Booking.BookingStatus.CONFIRMED);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // Act
        mockMvc.perform(
                        patch("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isForbidden());
    }
}
