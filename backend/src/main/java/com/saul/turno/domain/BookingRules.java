package com.saul.turno.domain;
import java.time.*;
import java.util.*;
public final class BookingRules {
 private final Clock clock;
 public BookingRules(Clock clock) { this.clock=clock; }
 public Draft validate(Draft d){
  if(d==null||!Set.of("norte","sur").contains(d.room()==null?"":d.room()))throw new IllegalArgumentException("Sala inválida.");
  LocalDate day;try{day=LocalDate.parse(d.date());if(!day.toString().equals(d.date()))throw new IllegalArgumentException();}catch(Exception e){throw new IllegalArgumentException("Fecha inválida.");}
  if(d.start()==null||d.duration()==null||d.start()<540||d.start()%30!=0||!Set.of(30,60,90).contains(d.duration())||d.start()>1080-d.duration())throw new IllegalArgumentException("Elige un horario entre las 09:00 y las 18:00.");
  Instant first=day.atStartOfDay(ZoneId.of("America/Mexico_City")).plusMinutes(d.start()).toInstant(),now=clock.instant();
  if(!first.isAfter(now)||first.isAfter(now.plusSeconds(90L*86400)))throw new IllegalArgumentException("Reserva dentro de los próximos 90 días.");
  if(d.title()==null||d.title().strip().length()<3||d.title().strip().length()>80||d.name()==null||d.name().strip().length()<2||d.name().strip().length()>60)throw new IllegalArgumentException("Revisa el motivo y el nombre de la reserva.");
  return new Draft(d.room(),d.date(),d.start(),d.duration(),d.title().strip(),d.name().strip());
 }
}
