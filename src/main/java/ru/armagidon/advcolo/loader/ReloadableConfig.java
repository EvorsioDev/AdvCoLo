package ru.armagidon.advcolo.loader;

public interface ReloadableConfig {

  default void onReload(){
   // no-op
  }

}
