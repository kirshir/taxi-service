package com.example.trip_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.example.trip_service.dto.CreateTripRequest;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TripServiceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static int counter = 1;

    private void print(String message) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  TEST " + counter + ": " + message);
        System.out.println("=".repeat(70));
        counter++;
    }

    private void printJson(String label, String json) {
        System.out.println("  " + label + ": ");
        try {
            String formatted = formatJson(json);
            System.out.println("    " + formatted.replace("\n", "\n    "));
        } catch (Exception e) {
            System.out.println("    " + json);
        }
    }

    private String formatJson(String json) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inString = !inString;
                sb.append(c);
            } else if (!inString) {
                if (c == '{' || c == '[') {
                    sb.append(c).append("\n");
                    indent += 2;
                    sb.append(" ".repeat(indent));
                } else if (c == '}' || c == ']') {
                    sb.append("\n");
                    indent -= 2;
                    sb.append(" ".repeat(indent)).append(c);
                } else if (c == ',') {
                    sb.append(c).append("\n").append(" ".repeat(indent));
                } else if (c == ':') {
                    sb.append(c).append(" ");
                } else if (c != ' ') {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void success(String message) {
        System.out.println("  [OK] " + message);
    }

    private void section(String message) {
        System.out.println("\n  --- " + message + " ---");
    }

    private void footer() {
        System.out.println("  " + "-".repeat(70));
    }

    @BeforeEach
    void setUp() {
        System.out.println("\n  Preparing test data: passengers and drivers...");
        
        jdbcTemplate.execute("INSERT INTO passengers (id, name, email, phone) VALUES (1, 'Kirill Shirokov', 'kirill@example.com', '+79001234567') ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO passengers (id, name, email, phone) VALUES (2, 'Ivan Ivanov', 'ivanivan@example.com', '+79000998677') ON CONFLICT (id) DO NOTHING");
        
        jdbcTemplate.execute("INSERT INTO drivers (id, name, email, phone, license_number, status) VALUES (1, 'Egor Volkov', 'egor.volvkov@example.com', '+79001112233', 'DRV001', 'FREE') ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO drivers (id, name, email, phone, license_number, status) VALUES (2, 'Ylia Kazanova', 'ylia.kazanova@example.com', '+79000001111', 'DRV002', 'FREE') ON CONFLICT (id) DO NOTHING");
        
        success("Setup complete: passengers ID=1,2 and drivers ID=1,2 (status FREE)");
    }

    @Test
    @Order(1)
    void demo1_createTrip() throws Exception {
        print("Create new trip");

        CreateTripRequest request = new CreateTripRequest();
        request.setPassengerId(1L);
        request.setOrigin("Novosibirsk, Kurchatova 1");
        request.setDestination("Novosibirsk, Demitrova 2");

        String response = mockMvc.perform(post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRIVER_ASSIGNED"))
                .andExpect(jsonPath("$.price").value(100.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created trip", response);
        section("Auto-assigned driver from FREE pool");
        success("Trip created, driver assigned automatically");
        footer();
    }

    @Test
    @Order(2)
    void demo2_createSecondTrip() throws Exception {
        print("Create second trip");

        CreateTripRequest request = new CreateTripRequest();
        request.setPassengerId(2L);
        request.setOrigin("Novosibirsk, Grebenshikova 4");
        request.setDestination("Novosibirsk, Pushkina 3");

        String response = mockMvc.perform(post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRIVER_ASSIGNED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created trip", response);
        success("Second trip created successfully");
        footer();
    }

    @Test
    @Order(3)
    void demo3_getTripById() throws Exception {
        print("Get trip by ID=1");

        String response = mockMvc.perform(get("/trips/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Trip details", response);
        success("Trip details retrieved");
        footer();
    }

    @Test
    @Order(4)
    void demo4_getPassengerTrips() throws Exception {
        print("Get all trips for passenger ID=1");

        String response = mockMvc.perform(get("/trips?passenger_id=1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Passenger trips", response);
        section("Passenger 1 has 1 trip");
        success("Passenger trip history retrieved");
        footer();
    }

    @Test
    @Order(5)
    void demo5_getPassenger2Trips() throws Exception {
        print("Get all trips for passenger ID=2");

        String response = mockMvc.perform(get("/trips?passenger_id=2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Passenger trips", response);
        section("Passenger 2 has 1 trip");
        success("Second passenger trip history retrieved");
        footer();
    }

    @Test
    @Order(6)
    void demo6_completeTrip() throws Exception {
        print("Complete trip ID=1");

        String response = mockMvc.perform(patch("/trips/1/status?status=COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Updated trip", response);
        success("Trip status changed to COMPLETED");
        footer();
    }

    @Test
    @Order(7)
    void demo7_addRating() throws Exception {
        print("Add rating 5 stars to trip ID=1");

        String response = mockMvc.perform(patch("/trips/1/rating?rating=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Rated trip", response);
        success("Rating (5 stars) added to trip");
        footer();
    }

    @Test
    @Order(8)
    void demo8_getCompletedTripDetails() throws Exception {
        print("Get completed trip details");

        String response = mockMvc.perform(get("/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.rating").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Completed trip", response);
        section("Verified: status=COMPLETED, rating=5");
        success("Completed trip with rating retrieved");
        footer();
    }

    @Test
    @Order(9)
    void demo9_cancelSecondTrip() throws Exception {
        print("Cancel trip ID=2");

        String response = mockMvc.perform(patch("/trips/2/status?status=CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Cancelled trip", response);
        success("Trip cancelled successfully");
        footer();
    }

    @Test
    @Order(10)
    void demo10_createTripWithNonExistentPassenger() throws Exception {
        print("Create trip with non-existent passenger (404)");

        CreateTripRequest request = new CreateTripRequest();
        request.setPassengerId(999L);
        request.setOrigin("Novosibirsk, Kurchatova 1");
        request.setDestination("Novosibirsk, Demitrova 2");

        String response = mockMvc.perform(post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Error response", response);
        section("Expected: 404 Not Found - passenger does not exist");
        success("Correctly returns 404 for non-existent passenger");
        footer();
    }

    @Test
    @Order(11)
    void demo11_createTripWithNoFreeDrivers() throws Exception {
        print("Create trip when no free drivers available (400)");

        jdbcTemplate.execute("UPDATE drivers SET status = 'BUSY' WHERE id = 1");
        jdbcTemplate.execute("UPDATE drivers SET status = 'BUSY' WHERE id = 2");

        try {
            CreateTripRequest request = new CreateTripRequest();
            request.setPassengerId(1L);
            request.setOrigin("Novosibirsk, Kurchatova 1");
            request.setDestination("Novosibirsk, Demitrova 2");

            String response = mockMvc.perform(post("/trips")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            printJson("Error response", response);
            section("Expected: 400 Bad Request - no free drivers");
            success("Correctly returns 400 when no free drivers");
            footer();
        } finally {
            jdbcTemplate.execute("UPDATE drivers SET status = 'FREE' WHERE id = 1");
            jdbcTemplate.execute("UPDATE drivers SET status = 'FREE' WHERE id = 2");
        }
    }
}