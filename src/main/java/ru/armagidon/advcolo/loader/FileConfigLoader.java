package ru.armagidon.advcolo.loader;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.WatchServiceListener;
import ru.armagidon.advcolo.util.Unchecked;

public abstract class FileConfigLoader implements ConfigLoader {

  private final ClassToInstanceMap<Object> holders = MutableClassToInstanceMap.create();
  private final WatchServiceListener watchServiceListener;
  private final ProxyCallRouterFactory proxyCallRouterFactory;

  @Inject
  protected FileConfigLoader(WatchServiceListener watchServiceListener,
      ProxyCallRouterFactory proxyCallRouterFactory) {

    this.watchServiceListener = watchServiceListener;
    this.proxyCallRouterFactory = proxyCallRouterFactory;
  }

  protected FileConfigLoader(WatchServiceListener watchServiceListener) {
    this(watchServiceListener, new DefaultProxyCallRouterFactory());
  }

  @Override
  public <T, C extends T> T load(File file, Class<T> interfaceType, Class<C> containerType) {
    if (!interfaceType.isInterface())
      throw new IllegalArgumentException("You must provide interface to use loader");
    T value = holders.getInstance(interfaceType);
    if (value == null) {
      value = Unchecked.supplier(() -> createProxy(file, interfaceType, containerType)).get();
    }
    return value;
  }

  protected abstract ConfigurationLoader<CommentedConfigurationNode> getLoader(Path path);

  protected <T> void updateProxy(ProxyCallRouter<T> router, T newValue) {
    router.update(newValue);
  }

  @SuppressWarnings("unchecked")
  private <T, C extends T> T createProxy(File file, Class<T> interfaceType, Class<C> containerType)
      throws IOException {
    Files.createDirectories(file.toPath().getParent());
    ConfigurationReference<CommentedConfigurationNode> reference = ConfigurationReference.watching(
        this::getLoader, file.toPath(), watchServiceListener);

    T current = reference.node().get(containerType);

    reference.save();

    ProxyCallRouter<T> router =  proxyCallRouterFactory.create(current);

    reference.updates().subscribe(node -> {
      T newValue = Unchecked.<Class<T>, T>function(node::get).apply(interfaceType);
      updateProxy(router, newValue);
    });

    T proxy = (T) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class[]{ interfaceType }, router);

    holders.putInstance(interfaceType, proxy);

    return proxy;
  }
}
