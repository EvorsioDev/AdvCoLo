package ru.armagidon.advcolo.loader;


import java.io.File;
import java.io.IOException;

public interface ConfigLoader {

  <T extends ReloadableConfig, C extends T> T load(File file, Class<T> interfaceType, Class<C> containerType)
      throws IOException;

}
