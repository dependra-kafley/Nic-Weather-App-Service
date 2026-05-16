package com.example.weathermap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WeatherMapApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void awsObservationsEndpointReturnsGroupedArrays() throws Exception {
        mockMvc.perform(get("/api/weather/aws-observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.org").isArray())
                .andExpect(jsonPath("$.aws").isArray())
                .andExpect(jsonPath("$.arg").isArray());
    }

    @Test
    void nowcastMapEndpointReturnsDistricts() throws Exception {
        mockMvc.perform(get("/api/weather/nowcast/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].districtKey").exists());
    }

    @Test
    void rainfallMapEndpointReturnsDistricts() throws Exception {
        mockMvc.perform(get("/api/weather/rainfall/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].districtKey").exists())
                .andExpect(jsonPath("$[0].dailyRainfallCategory").exists());
    }

    @Test
    void districtWarningMapEndpointReturnsDistricts() throws Exception {
        mockMvc.perform(get("/api/weather/district-warning/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].districtKey").exists())
                .andExpect(jsonPath("$[0].day1Color").exists());
    }

    @Test
    void cityWeatherEndpointReturnsStations() throws Exception {
        mockMvc.perform(get("/api/weather/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stationName").exists());
    }

    @Test
    void syncStatusEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/weather/sync-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nowcast").exists());
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
