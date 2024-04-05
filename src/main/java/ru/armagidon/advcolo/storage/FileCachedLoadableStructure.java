package ru.armagidon.advcolo.storage;

import io.leangen.geantyref.TypeToken;
import java.io.File;
import java.io.IOException;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader.Builder;
import org.spongepowered.configurate.loader.HeaderMode;

public abstract class FileCachedLoadableStructure<T> implements CachedLoadableStructure<T> {

  private final TypeToken<T> structureClass;
  private final File file;
  private T structure;

  protected FileCachedLoadableStructure(TypeToken<T> structureClass, File file) {
    this.structureClass = structureClass;
    this.file = file;
  }

  protected abstract <V extends Builder<V, L>, L extends AbstractConfigurationLoader<?>> AbstractConfigurationLoader.Builder<V, L> getLoader();

  private AbstractConfigurationLoader<?> configureLoader() {
    return getLoader().file(file).headerMode(HeaderMode.PRESERVE).build();
  }

  @Override
  public synchronized T getLoaded() {
    if (structure == null) {
      try {
        load();
      } catch (IOException e) {
        throw new IllegalStateException("Structure could not be loaded because of the underlying error.", e);
      }
    }
    return structure;
  }

  @Override
  public synchronized void load() throws IOException {
    var loader = configureLoader();
    var rootNode = loader.load();
    structure = rootNode.get(structureClass);
  }

  @Override
  public void save() throws IOException {
    var loader = configureLoader();
    var rootNode = loader.createNode();
    rootNode.set(structureClass, structure);
  }
}
