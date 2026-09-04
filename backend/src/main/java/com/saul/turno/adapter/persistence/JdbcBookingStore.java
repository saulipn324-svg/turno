package com.saul.turno.adapter.persistence;
import com.saul.turno.domain.*;
import com.saul.turno.application.port.BookingStore;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository public class JdbcBookingStore implements BookingStore {
 private final JdbcTemplate db;
 public JdbcBookingStore(JdbcTemplate db) { this.db=db; }
 public List<Booking> list(String date){
  return db.query("SELECT * FROM bookings WHERE booking_date=? ORDER BY start_minute,id LIMIT 500",(rs,n)->new Booking(rs.getString("id"),rs.getString("room"),rs.getString("booking_date"),rs.getInt("start_minute"),rs.getInt("duration"),rs.getString("title"),rs.getString("name"),rs.getString("status")),date);
 }
 @Transactional public Booking reserve(Draft d){try { String id=UUID.randomUUID().toString();
  db.update("INSERT INTO bookings(id,room,booking_date,start_minute,duration,title,name,status) VALUES(?,?,?,?,?,?,?,?)",id,d.room(),d.date(),d.start(),d.duration(),d.title(),d.name(),"CONFIRMED");
  // The unique database constraint arbitrates concurrent requests; the entire reservation rolls back on conflict.
  for(int slot=d.start();slot<d.start()+d.duration();slot+=30)db.update("INSERT INTO slots(id,booking_id,room,booking_date,start_minute) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),id,d.room(),d.date(),slot);
  return new Booking(id,d.room(),d.date(),d.start(),d.duration(),d.title(),d.name(),"CONFIRMED");
 } catch (org.springframework.dao.DataIntegrityViolationException e) { throw new BookingConflict(); }
 }
 @Transactional public void cancel(String id){List<String> rows=db.queryForList("SELECT status FROM bookings WHERE id=? FOR UPDATE",String.class,id);if(rows.isEmpty())throw new BookingNotFound();
  db.update("UPDATE bookings SET status='CANCELLED' WHERE id=?",id);db.update("DELETE FROM slots WHERE booking_id=?",id);
 }
}
