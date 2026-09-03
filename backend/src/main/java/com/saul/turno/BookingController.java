package com.saul.turno;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.*;
@RestController @RequestMapping("/api") public class BookingController {
 private final BookingService service;
 public BookingController(BookingService service){this.service=service;}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP","mode","java");}
 @GetMapping("/bookings") public List<BookingService.Booking> list(@RequestParam String date){return service.list(date);}
 @PostMapping("/bookings") @ResponseStatus(HttpStatus.CREATED) public BookingService.Booking create(@RequestBody BookingService.Draft d){return service.create(d);}
 @PostMapping("/bookings/{id}/cancel") public Map<String,String> cancel(@PathVariable UUID id){service.cancel(id.toString());return Map.of("status","CANCELLED");}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<?> invalid(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
 @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<?> malformed(){return ResponseEntity.badRequest().body(Map.of("message","Solicitud inválida."));}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(){return ResponseEntity.status(409).body(Map.of("message","Ese horario acaba de ocuparse. Elige otro."));}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> missing(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.requireNonNullElse(e.getReason(),"Reserva no encontrada.")));}
}
