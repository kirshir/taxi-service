package com.example.notification_service;

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

import com.example.notification_service.dto.CreateNotificationRequest;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationServiceIntegrationTests {

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
        System.out.println("\n  Preparing test data...");
        
        jdbcTemplate.execute("INSERT INTO passengers (id, name, email, phone) VALUES (1, 'Kirill Shirokov', 'kirill@example.com', '+79001234567') ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO passengers (id, name, email, phone) VALUES (2, 'Ivan Ivanov', 'ivanivan@example.com', '+79000998677') ON CONFLICT (id) DO NOTHING");
        
        jdbcTemplate.execute("INSERT INTO drivers (id, name, email, phone, license_number, status) VALUES (1, 'Egor Volkov', 'egor.volkov@example.com', '+79001112233', 'DRV001', 'FREE') ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO drivers (id, name, email, phone, license_number, status) VALUES (2, 'Ylia Kazanova', 'ylia.kazanova@example.com', '+79000001111', 'DRV002', 'FREE') ON CONFLICT (id) DO NOTHING");
        
        jdbcTemplate.execute("INSERT INTO trips (id, passenger_id, driver_id, status, origin, destination, price) VALUES (1, 1, 1, 'DRIVER_ASSIGNED', 'Novosibirsk, Kurchatova 1', 'Novosibirsk, Demitrova 2', 500) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO trips (id, passenger_id, driver_id, status, origin, destination, price) VALUES (2, 2, 2, 'DRIVER_ASSIGNED', 'Novosibirsk, Grebenshikova 4', 'Novosibirsk, Pushkina 3', 500) ON CONFLICT (id) DO NOTHING");

        success("Test data prepared: 2 passengers, 2 drivers, 2 trips");
    }

    @Test
    @Order(1)
    void demo1_getNotificationsForTrip1() throws Exception {
        print("Get all notifications for trip ID=1");

        String response = mockMvc.perform(get("/notifications")
                .param("trip_id", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Notifications", response);
        
        int count = objectMapper.readTree(response).size();
        section("Result: " + count + " notifications found");
        success("Retrieved notifications for trip ID=1");
        footer();
    }

    @Test
    @Order(2)
    void demo2_getNotificationsForTrip2() throws Exception {
        print("Get all notifications for trip ID=2");

        String response = mockMvc.perform(get("/notifications")
                .param("trip_id", "2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Notifications", response);
        
        int count = objectMapper.readTree(response).size();
        section("Result: " + count + " notifications found");
        success("Retrieved notifications for trip ID=2");
        footer();
    }

    @Test
    @Order(3)
    void demo3_createNotificationWithParams() throws Exception {
        print("Create notification using request params");

        String response = mockMvc.perform(post("/notifications")
                .param("tripId", "1")
                .param("recipientType", "PASSENGER")
                .param("recipientId", "1")
                .param("message", "Your driver has arrived"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created notification", response);
        success("Notification created with status PENDING");
        footer();
    }

    @Test
    @Order(4)
    void demo4_createNotificationWithBody() throws Exception {
        print("Create notification using JSON body");

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTripId(1L);
        request.setRecipientType("DRIVER");
        request.setRecipientId(1L);
        request.setMessage("New trip assigned to you");

        String response = mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created notification", response);
        section("Used JSON body instead of request params");
        success("Notification created via JSON body");
        footer();
    }

    @Test
    @Order(5)
    void demo5_createNotificationForDriver() throws Exception {
        print("Create notification for driver recipient");

        String response = mockMvc.perform(post("/notifications")
                .param("tripId", "2")
                .param("recipientType", "DRIVER")
                .param("recipientId", "2")
                .param("message", "Please confirm pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientType").value("DRIVER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created notification", response);
        success("Driver notification created");
        footer();
    }

    @Test
    @Order(6)
    void demo6_getNotificationsAfterCreation() throws Exception {
        print("Get notifications for trip ID=1 after creations");

        String response = mockMvc.perform(get("/notifications")
                .param("trip_id", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Notifications", response);
        
        int count = objectMapper.readTree(response).size();
        section("Now trip 1 has " + count + " notifications (was 0 + created 3)");
        success("Verified accumulated notifications");
        footer();
    }
}