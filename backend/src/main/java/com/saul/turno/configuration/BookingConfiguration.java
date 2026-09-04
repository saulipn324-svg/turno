package com.saul.turno.configuration;
import com.saul.turno.application.BookingService;
import com.saul.turno.application.port.BookingStore;
import java.time.Clock;
import org.springframework.context.annotation.*;
@Configuration public class BookingConfiguration {
 @Bean Clock bookingClock() { return Clock.systemUTC(); }
 @Bean BookingService bookingService(BookingStore store, Clock clock) { return new BookingService(store,clock); }
}
