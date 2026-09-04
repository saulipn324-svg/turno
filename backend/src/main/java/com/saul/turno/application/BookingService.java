package com.saul.turno.application;
import com.saul.turno.domain.*;
import com.saul.turno.application.port.BookingStore;
import java.time.*;
import java.util.*;
public final class BookingService {
 private final BookingStore store;
 private final BookingRules rules;
 public BookingService(BookingStore store, Clock clock) { this.store=store; this.rules=new BookingRules(clock); }
 public Booking create(Draft draft) { return store.reserve(rules.validate(draft)); }
 public List<Booking> list(String date) {
  try { if(!LocalDate.parse(date).toString().equals(date)) throw new IllegalArgumentException(); }
  catch(Exception e) { throw new IllegalArgumentException("Fecha inválida."); }
  return store.list(date);
 }
 public void cancel(String id) { store.cancel(id); }
}
