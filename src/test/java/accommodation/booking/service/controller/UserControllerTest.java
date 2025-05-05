package accommodation.booking.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.service.dto.user.request.UserUpdateRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.util.AuthenticationTestUtil;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
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
    @WithMockUser(username = "admin", roles = {"MANAGER"})
    @DisplayName("Get current user profile with valid authentication should succeed")
    void getCurrentUserProfile_Authenticated_ReturnsUserProfile() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(
                2L, "test@example.com", Role.RoleName.ROLE_MANAGER);
        UserResponseDto expected = new UserResponseDto();
        expected.setId(2L);
        expected.setEmail("test@example.com");
        expected.setFirstName("Test User");
        expected.setLastName("Last Name");

        // Act
        MvcResult result = mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Get current user profile without authentication should fail")
    void getCurrentUserProfile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MANAGER"})
    @DisplayName("Update user profile with valid input and MANAGER role should succeed")
    void updateUserProfile_AuthenticatedWithManagerRole_ReturnsUpdatedProfile() throws Exception {
        // Arrange
        AuthenticationTestUtil.authenticateUser(2L, "test@example.com", Role.RoleName.ROLE_MANAGER);
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setFirstName("Updated First");
        requestDto.setLastName("Updated Last");

        UserResponseDto expected = new UserResponseDto();
        expected.setId(2L);
        expected.setEmail("test@example.com");
        expected.setFirstName("Updated First");
        expected.setLastName("Updated Last");

        String requestJson = objectMapper.writeValueAsString(requestDto);
        // Act
        MvcResult result = mockMvc.perform(
                        patch("/users/me/profile")
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}
