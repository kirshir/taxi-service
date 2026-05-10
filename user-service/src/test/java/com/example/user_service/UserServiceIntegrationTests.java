package com.example.user_service;

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

import com.example.user_service.entity.Driver;
import com.example.user_service.entity.Passenger;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceIntegrationTests {

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


    private void cleanDatabase() {
        System.out.println("\n  Cleaning database...");
        jdbcTemplate.execute("TRUNCATE TABLE notification_tasks CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE trips CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE drivers CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE passengers CASCADE");
        jdbcTemplate.execute("ALTER SEQUENCE passengers_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE drivers_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE trips_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE notification_tasks_id_seq RESTART WITH 1");
        success("Database cleaned and sequences reset");
    }

    @Test
    @Order(1)
    void demo1_createPassenger() throws Exception {
        print("Create new passenger");
        cleanDatabase();

        Passenger passenger = new Passenger();
        passenger.setName("Kirill Shirokov");
        passenger.setEmail("kirill@example.com");
        passenger.setPhone("+79001234567");

        String response = mockMvc.perform(post("/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Kirill Shirokov"))
                .andExpect(jsonPath("$.email").value("kirill@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created passenger", response);
        success("Passenger 'Kirill Shirokov' created with ID");
        footer();
    }

    @Test
    @Order(2)
    void demo2_createSecondPassenger() throws Exception {
        print("Create second passenger");

        Passenger passenger = new Passenger();
        passenger.setName("Ivan Ivanov");
        passenger.setEmail("ivanivan@example.com");
        passenger.setPhone("+79000998677");

        String response = mockMvc.perform(post("/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created passenger", response);
        success("Second passenger 'Ivan Ivanov' created");
        footer();
    }

    @Test
    @Order(3)
    void demo3_createDriver() throws Exception {
        print("Create new driver");

        Driver driver = new Driver();
        driver.setName("Egor Volkov");
        driver.setEmail("egor.volvkov@example.com");
        driver.setPhone("+79001112233");
        driver.setLicenseNumber("DRV001");

        String response = mockMvc.perform(post("/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(driver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("FREE"))
                .andExpect(jsonPath("$.name").value("Egor Volkov"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created driver", response);
        success("Driver 'Egor Volkov' created with status FREE");
        footer();
    }

    @Test
    @Order(4)
    void demo4_createSecondDriver() throws Exception {
        print("Create second driver");

        Driver driver = new Driver();
        driver.setName("Ylia Kazanova");
        driver.setEmail("ylia.kazanova@example.com");
        driver.setPhone("+79000001111");
        driver.setLicenseNumber("DRV002");

        String response = mockMvc.perform(post("/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(driver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("FREE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Created driver", response);
        success("Second driver 'Ylia Kazanova' created");
        footer();
    }

    @Test
    @Order(5)
    void demo5_updateDriverStatusToBusy() throws Exception {
        print("Update driver status to BUSY");

        String response = mockMvc.perform(patch("/drivers/1/status?status=BUSY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BUSY"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Updated driver", response);
        success("Driver ID=1 status changed to BUSY");
        footer();
    }

    @Test
    @Order(6)
    void demo6_getFreeDrivers() throws Exception {
        print("Get all free drivers");

        String response = mockMvc.perform(get("/drivers/free"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Free drivers", response);
        section("Result: Only 1 free driver (ID=2) because driver 1 is BUSY");
        success("Free drivers list retrieved");
        footer();
    }

    @Test
    @Order(7)
    void demo7_getDriverById() throws Exception {
        print("Get driver by ID=1");

        String response = mockMvc.perform(get("/drivers/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Driver details", response);
        success("Driver ID=1 details retrieved");
        footer();
    }

    @Test
    @Order(8)
    void demo8_getDriverById2() throws Exception {
        print("Get driver by ID=2");

        String response = mockMvc.perform(get("/drivers/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Driver details", response);
        success("Driver ID=2 details retrieved");
        footer();
    }

    @Test
    @Order(9)
    void demo9_getPassengerById() throws Exception {
        print("Get passenger by ID=1");

        String response = mockMvc.perform(get("/passengers/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Passenger details", response);
        success("Passenger ID=1 details retrieved");
        footer();
    }

    @Test
    @Order(10)
    void demo10_updateDriverStatusToFree() throws Exception {
        print("Update driver status to FREE");

        String response = mockMvc.perform(patch("/drivers/1/status?status=FREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FREE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        printJson("Updated driver", response);
        success("Driver ID=1 status changed to FREE");
        footer();
    }

    }