package hu.hamburger.backend;

import hu.hamburger.backend.model.RestaurantTable;
import hu.hamburger.backend.model.Role;
import hu.hamburger.backend.model.User;
import hu.hamburger.backend.repository.RestaurantTableRepository;
import hu.hamburger.backend.repository.UserRepository;
import hu.hamburger.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String authHeader;
    private Long existingTableId; //a ténylegesen generált id-t használjuk, nem hardcode-olt értéket

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Teszt Elek");
        user.setEmail("teszt@teszt.hu");
        user.setPassword(passwordEncoder.encode("jelszo123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        authHeader = "Bearer " + token;

        RestaurantTable table = new RestaurantTable();
        table.setTableNumber((int) (System.nanoTime() % 1_000_000));
        table.setCapacity(4);
        table = tableRepository.save(table);
        existingTableId = table.getId();
    }

    @Test
    void sikeresFoglalasLetrehozasa() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"table":{"id":%d},"date":"2026-09-20","startTime":"18:00:00","guestCount":2}
                                """.formatted(existingTableId)))
                .andExpect(status().isOk());
    }

    @Test
    void nemLehetKetszerFoglalniUgyanaztAzAsztalt() throws Exception {
        String requestBody = """
                {"table":{"id":%d},"date":"2026-09-20","startTime":"18:00:00","guestCount":2}
                """.formatted(existingTableId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nemLetezoAsztalraNemLehetFoglalni() throws Exception {
        //ez a szám garantáltan nem létezik, mert az IDENTITY számláló sosem fut vissza idáig
        long nonExistentTableId = existingTableId + 1_000_000L;

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"table":{"id":%d},"date":"2026-09-20","startTime":"18:00:00","guestCount":2}
                                """.formatted(nonExistentTableId)))
                .andExpect(status().isNotFound());
    }
}