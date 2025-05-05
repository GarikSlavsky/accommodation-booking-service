package accommodation.booking.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.util.AccommodationTestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AccommodationControllerTest {
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
                    new ClassPathResource("database/accommodation/add-accommodations.sql")
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
                    new ClassPathResource("database/accommodation/remove-all-accommodations.sql")
            );
        }
    }

    @WithMockUser(username = "admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Save new accommodation.")
    void addAccommodation_ValidDto_Ok() throws Exception {
        AccommodationRequestDto requestDto =
                AccommodationTestUtil.initializeAccommodationRequestDto();
        Accommodation accommodation = AccommodationTestUtil.initializeAccommodation(requestDto);
        AccommodationResponseDto expected =
                AccommodationTestUtil.initializeAccommodationResponseDto(accommodation);

        String json = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                post("/accommodations")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        AccommodationResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationResponseDto.class);

        assertNotNull(actual);
        assertEquals(0, expected.getDailyRate().compareTo(actual.getDailyRate()));
        assertTrue(reflectionEquals(expected, actual, "id", "dailyRate"));
    }

    @Test
    @DisplayName("Get list of all accommodations.")
    void getAllAccommodations_ReturnListOfAccommodationResponseDto() throws Exception {
        List<AccommodationResponseDto> expected =
                AccommodationTestUtil.getAccommodationResponseDtoList();

        Pageable pageable = Pageable.ofSize(4);
        String jsonRequest = objectMapper.writeValueAsString(pageable);
        MvcResult result = mockMvc.perform(
                get("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<AccommodationResponseDto> actual = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<>(){});

        assertThat(new HashSet<>(actual))
                .usingRecursiveComparison()
                .isEqualTo(new HashSet<>(expected));
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(0,
                    expected.get(i).getDailyRate()
                            .compareTo(actual.get(i).getDailyRate()));
            assertTrue(reflectionEquals(
                    expected.get(i), actual.get(i),"id", "dailyRate"));
        }
    }

    @WithMockUser(username = "admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Update accommodation.")
    void updateAccommodation_ValidDto_Ok() throws Exception {
        AccommodationRequestDto requestDto =
                AccommodationTestUtil.initializeAccommodationRequestDto();
        requestDto.setDailyRate(BigDecimal.valueOf(55.00));
        requestDto.getAmenities().add("New Amenity");
        Accommodation accommodation = AccommodationTestUtil.initializeAccommodation(requestDto);
        AccommodationResponseDto expected =
                AccommodationTestUtil.initializeAccommodationResponseDto(accommodation);

        String json = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                        put("/accommodations/{id}", 15L)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        AccommodationResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationResponseDto.class);

        assertNotNull(actual);
        assertEquals(0, expected.getDailyRate().compareTo(actual.getDailyRate()));
        assertTrue(reflectionEquals(expected, actual, "id", "dailyRate"));
    }

    @WithMockUser(username = "admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Delete accommodation.")
    void deleteAccommodation_SwitchIsDeletedFieldToTrue_Ok() throws Exception {
        mockMvc.perform(delete("/accommodations/{id}", 15L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
