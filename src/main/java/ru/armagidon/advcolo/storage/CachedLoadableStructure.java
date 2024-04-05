package ru.armagidon.advcolo.storage;

import java.io.IOException;

public interface CachedLoadableStructure<T> {

  void save() throws IOException;

  void load() throws IOException;

  T getLoaded();

}
