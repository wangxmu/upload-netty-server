package com.cnc.qoss.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.lang.management.ManagementFactory;

public class ProcessIDConverter extends ClassicConverter {

  @Override
  public String convert(ILoggingEvent event) {
    String name =  ManagementFactory.getRuntimeMXBean().getName();
    return name.substring(0, name.indexOf("@"));
  }
}
