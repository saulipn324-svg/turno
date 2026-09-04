package com.saul.turno.domain;
public class BookingConflict extends RuntimeException { public BookingConflict() { super("Ese horario acaba de ocuparse. Elige otro."); } }
