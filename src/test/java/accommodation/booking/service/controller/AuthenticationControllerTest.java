package accommodation.booking.service.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {
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
                    new ClassPathResource("database/user/remove-all-users.sql")
            );
        }
    }

    @Test
    @DisplayName("Register a new user with valid input should succeed")
    void register_ValidInput_ReturnsCreatedUser() throws Exception {
        // Arrange
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newuser@example.com");
        requestDto.setPassword("SecurePassword123");
        requestDto.setConfirmPassword("SecurePassword123");
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Doe");

        UserResponseDto expected = new UserResponseDto();
        expected.setId(10L);
        expected.setEmail("newuser@example.com");
        expected.setFirstName("Jane");
        expected.setLastName("Doe");

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        assertNotNull(actual);
        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @Test
    @DisplayName("Register a new user with mismatched passwords should fail")
    void register_MismatchedPasswords_ReturnsBadRequest() throws Exception {
        // Arrange
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newuser@example.com");
        requestDto.setPassword("SecurePassword123");
        requestDto.setConfirmPassword("DifferentPassword123");
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Doe");

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Passwords must match"));
    }
}
