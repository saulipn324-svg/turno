package com.saul.turno;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service public class BookingService {
 public record Draft(String room,String date,Integer start,Integer duration,String title,String name){}
 public record Booking(String id,String room,String date,int start,int duration,String title,String name,String status){}
 private final JdbcTemplate db;
 public BookingService(JdbcTemplate db){this.db=db;}
 public Draft validate(Draft d){
  if(d==null||!Set.of("norte","sur").contains(d.room()==null?"":d.room()))throw new IllegalArgumentException("Sala inválida.");
  LocalDate day;try{day=LocalDate.parse(d.date());if(!day.toString().equals(d.date()))throw new IllegalArgumentException();}catch(Exception e){throw new IllegalArgumentException("Fecha inválida.");}
  if(d.start()==null||d.duration()==null||d.start()<540||d.start()%30!=0||!Set.of(30,60,90).contains(d.duration())||d.start()>1080-d.duration())throw new IllegalArgumentException("Elige un horario entre las 09:00 y las 18:00.");
  Instant first=day.atStartOfDay(ZoneId.of("America/Mexico_City")).plusMinutes(d.start()).toInstant(),now=Instant.now();
  if(!first.isAfter(now)||first.isAfter(now.plusSeconds(90L*86400)))throw new IllegalArgumentException("Reserva dentro de los próximos 90 días.");
  if(d.title()==null||d.title().strip().length()<3||d.title().strip().length()>80||d.name()==null||d.name().strip().length()<2||d.name().strip().length()>60)throw new IllegalArgumentException("Revisa el motivo y el nombre de la reserva.");
  return new Draft(d.room(),d.date(),d.start(),d.duration(),d.title().strip(),d.name().strip());
 }
 public List<Booking> list(String date){try{if(!LocalDate.parse(date).toString().equals(date))throw new IllegalArgumentException();}catch(Exception e){throw new IllegalArgumentException("Fecha inválida.");}
  return db.query("SELECT * FROM bookings WHERE booking_date=? ORDER BY start_minute,id LIMIT 500",(rs,n)->new Booking(rs.getString("id"),rs.getString("room"),rs.getString("booking_date"),rs.getInt("start_minute"),rs.getInt("duration"),rs.getString("title"),rs.getString("name"),rs.getString("status")),date);
 }
 @Transactional public Booking create(Draft raw){Draft d=validate(raw);String id=UUID.randomUUID().toString();
  db.update("INSERT INTO bookings(id,room,booking_date,start_minute,duration,title,name,status) VALUES(?,?,?,?,?,?,?,?)",id,d.room(),d.date(),d.start(),d.duration(),d.title(),d.name(),"CONFIRMED");
  // The unique database constraint arbitrates concurrent requests; the entire reservation rolls back on conflict.
  for(int slot=d.start();slot<d.start()+d.duration();slot+=30)db.update("INSERT INTO slots(id,booking_id,room,booking_date,start_minute) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),id,d.room(),d.date(),slot);
  return new Booking(id,d.room(),d.date(),d.start(),d.duration(),d.title(),d.name(),"CONFIRMED");
 }
 @Transactional public void cancel(String id){List<String> rows=db.queryForList("SELECT status FROM bookings WHERE id=? FOR UPDATE",String.class,id);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Reserva no encontrada.");
  db.update("UPDATE bookings SET status='CANCELLED' WHERE id=?",id);db.update("DELETE FROM slots WHERE booking_id=?",id);
 }
}
