package com.saul.turno;
import java.time.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:turnohttp;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class BookingHttpTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired JdbcTemplate db;
 @BeforeEach void clean() { db.update("DELETE FROM slots"); db.update("DELETE FROM bookings"); }
 @Test void preservesCreateConflictAndCancelContract() throws Exception {
  var body=json.writeValueAsString(Map.of("room","norte","date",LocalDate.now(ZoneId.of("America/Mexico_City")).plusDays(2).toString(),"start",600,"duration",60,"title","Revisión","name","Equipo"));
  var result=mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("CONFIRMED")).andReturn();
  String id=json.readTree(result.getResponse().getContentAsString()).get("id").asText();
  mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
  mvc.perform(post("/api/bookings/"+id+"/cancel")).andExpect(status().isOk());
  mvc.perform(post("/api/bookings/"+id+"/cancel")).andExpect(status().isOk());
 }
 @Test void translatesMissingAndInvalidRequests() throws Exception {
  mvc.perform(post("/api/bookings/00000000-0000-0000-0000-000000000000/cancel")).andExpect(status().isNotFound());
  mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
 }
}
