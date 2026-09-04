package com.saul.turno;

import com.saul.turno.application.BookingService;
import com.saul.turno.application.port.BookingStore;
import com.saul.turno.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingDomainTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-03T15:00:00Z"), ZoneOffset.UTC);
    static class MemoryStore implements BookingStore {
        final List<Booking> rows = new ArrayList<>();
        public List<Booking> list(String date) { return rows.stream().filter(b -> b.date().equals(date)).toList(); }
        public Booking reserve(Draft d) {
            var b = new Booking("test", d.room(), d.date(), d.start(), d.duration(), d.title(), d.name(), "CONFIRMED");
            rows.add(b); return b;
        }
        public void cancel(String id) { if (rows.stream().noneMatch(b -> b.id().equals(id))) throw new BookingNotFound(); }
    }
    @Test void runsWithoutSpringOrDatabaseAndNormalizesInput() {
        var service = new BookingService(new MemoryStore(), clock);
        var booking = service.create(new Draft("norte", "2026-09-04", 600, 60, "  Revisión  ", "  Saul  "));
        assertEquals("Revisión", booking.title());
        assertEquals("Saul", booking.name());
        assertEquals(List.of(booking), service.list("2026-09-04"));
    }
    @Test void rejectsInvalidBookingBeforeCallingPersistence() {
        var store = new MemoryStore();
        var service = new BookingService(store, clock);
        assertThrows(IllegalArgumentException.class, () -> service.create(new Draft("norte", "2026-09-03", 540, 60, "Revisión", "Saul")));
        assertThrows(IllegalArgumentException.class, () -> service.create(new Draft("norte", "2027-01-04", 600, 60, "Revisión", "Saul")));
        assertTrue(store.rows.isEmpty());
    }
    @Test void missingBookingIsADomainFailure() {
        assertThrows(BookingNotFound.class, () -> new BookingService(new MemoryStore(), clock).cancel("missing"));
    }
}
