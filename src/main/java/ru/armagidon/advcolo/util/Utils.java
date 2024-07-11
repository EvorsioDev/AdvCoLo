package ru.armagidon.advcolo.util;

import io.leangen.geantyref.TypeToken;

public class Utils {

  public static <T>TypeToken<T> token() {
    return new TypeToken<>() {
    };
  }
}
