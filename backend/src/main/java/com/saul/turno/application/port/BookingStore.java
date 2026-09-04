package com.saul.turno.application.port;
import com.saul.turno.domain.*;
import java.util.List;
/** Each write is atomic. reserve must reject conflicting slots without partial writes. */
public interface BookingStore {
 List<Booking> list(String date);
 Booking reserve(Draft draft);
 void cancel(String id);
}
