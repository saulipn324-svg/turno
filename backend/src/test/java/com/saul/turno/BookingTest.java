package com.saul.turno;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:turnotest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"})
class BookingTest {
 @Autowired BookingService service; @Autowired JdbcTemplate db;
 String date=LocalDate.now(ZoneId.of("America/Mexico_City")).plusDays(2).toString();
 @BeforeEach void reset(){db.update("DELETE FROM slots");db.update("DELETE FROM bookings");}
 BookingService.Draft draft(String room,int start,int duration){return new BookingService.Draft(room,date,start,duration,"Planeación","Equipo");}
 @Test void createsAndLists(){var b=service.create(draft("norte",600,60));assertEquals(b,service.list(date).getFirst());assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM slots",Integer.class));}
 @Test void rejectsOverlapAndRollsBack(){service.create(draft("norte",600,60));assertThrows(DataIntegrityViolationException.class,()->service.create(draft("norte",570,60)));assertEquals(1,service.list(date).size());assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM slots",Integer.class));}
 @Test void adjacentAndOtherRoomAllowed(){service.create(draft("norte",600,60));service.create(draft("norte",660,30));service.create(draft("sur",600,60));assertEquals(3,service.list(date).size());}
 @Test void cancellationReleasesSlots(){var b=service.create(draft("norte",600,90));service.cancel(b.id());service.cancel(b.id());service.create(draft("norte",600,90));assertEquals(3,db.queryForObject("SELECT COUNT(*) FROM slots",Integer.class));assertEquals(1,service.list(date).stream().filter(x->x.status().equals("CANCELLED")).count());}
 @Test void validatesBounds(){for(int n:new int[]{500,541,1070,1080,Integer.MAX_VALUE})assertThrows(IllegalArgumentException.class,()->service.create(draft("norte",n,60)));assertThrows(IllegalArgumentException.class,()->service.create(draft("x",600,60)));assertThrows(IllegalArgumentException.class,()->service.create(draft("norte",600,45)));}
 @Test void rejectsPastAndBadDate(){assertThrows(IllegalArgumentException.class,()->service.create(new BookingService.Draft("norte","2020-01-01",600,60,"Test","Equipo")));assertThrows(IllegalArgumentException.class,()->service.create(new BookingService.Draft("norte","2027-02-30",600,60,"Test","Equipo")));}
 @Test void simultaneousReservationsHaveOneWinner()throws Exception{try(var pool=Executors.newFixedThreadPool(2)){var gate=new CountDownLatch(1);Callable<Boolean> request=()->{gate.await();try{service.create(draft("norte",600,90));return true;}catch(DataIntegrityViolationException e){return false;}};var a=pool.submit(request);var b=pool.submit(request);gate.countDown();assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));assertEquals(1,service.list(date).size());assertEquals(3,db.queryForObject("SELECT COUNT(*) FROM slots",Integer.class));}}
}
