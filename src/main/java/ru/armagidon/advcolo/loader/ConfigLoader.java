package ru.armagidon.advcolo.loader;


import java.io.File;

public interface ConfigLoader {

  <T, C extends T> T load(File file, Class<T> interfaceType, Class<C> containerType);

}
