package com.saul.turno;
import java.nio.file.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ArchitectureTest {
 @Test void dependencyDirection() throws IOException {
 check("domain", "com.saul.turno.application");
 check("domain", "com.saul.turno.adapter");
 check("domain", "com.saul.turno.configuration");
 check("domain", "org.springframework");
 check("domain", "java.sql");
 check("domain", "javax.sql");
 check("domain", "jakarta.persistence");
 check("application", "com.saul.turno.adapter");
 check("application", "com.saul.turno.configuration");
 check("application", "org.springframework");
 check("application", "java.sql");
 check("application", "javax.sql");
 check("application", "jakarta.persistence");
 }
 private void check(String layer, String forbidden) throws IOException {
  Path base = Path.of("src/main/java/com/saul/turno",layer);
  assertTrue(Files.isDirectory(base), "Missing layer " + base);
  try(var files = Files.walk(base)) {
   for(Path file : files.filter(p -> p.toString().endsWith(".java")).toList())
    assertFalse(Files.readString(file).contains(forbidden), file + " depends on " + forbidden);
  }
 }
}
