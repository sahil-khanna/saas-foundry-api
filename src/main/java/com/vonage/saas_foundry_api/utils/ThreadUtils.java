package com.vonage.saas_foundry_api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {

  private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

  private ThreadUtils() {
    // Do nothing
  }
  
  public static void sleep(long millis, String logMessage) {
    if (logMessage != null) {
      logger.info(logMessage);
    }
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      logger.error("Thread was interrupted: {}", e.getLocalizedMessage());
      Thread.currentThread().interrupt();
    }
  }
}
