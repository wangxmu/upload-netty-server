package com.cnc.qoss.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ThreadIDConverter extends ClassicConverter {

  @Override
  public String convert(ILoggingEvent event) {
    return String.valueOf(Thread.currentThread().getId());
  }
}
