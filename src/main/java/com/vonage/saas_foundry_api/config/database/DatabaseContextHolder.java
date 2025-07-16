package com.vonage.saas_foundry_api.config.database;

public class DatabaseContextHolder {

  private DatabaseContextHolder() {
  }

  private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

  public static void setCurrentDb(String dbName) {
    contextHolder.set(dbName);
  }

  public static String getCurrentDb() {
    return contextHolder.get();
  }

  public static void clear() {
    contextHolder.remove();
  }
}
