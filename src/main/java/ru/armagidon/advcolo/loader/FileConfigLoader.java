package ru.armagidon.advcolo.loader;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

public abstract class FileConfigLoader implements ConfigLoader {

  private final WatchServiceListener listener;
  private final ProxyCallRouterFactory routerFactory;
  private final ProxyUpdaterFactory proxyUpdaterFactory;

  @Inject
  protected FileConfigLoader(WatchServiceListener listener,
      ProxyCallRouterFactory routerFactory, ProxyUpdaterFactory proxyUpdaterFactory) {

    this.listener = listener;
    this.routerFactory = routerFactory;
    this.proxyUpdaterFactory = proxyUpdaterFactory;
  }

  @Inject
  protected FileConfigLoader(WatchServiceListener listener) {
    this(listener, new DefaultProxyCallRouterFactory(), new DefaultProxyUpdaterFactory());
  }

  @Override
  public <T, C extends T> T load(File file, Class<T> interfaceType, Class<C> containerType)
      throws IOException {
    if (!interfaceType.isInterface()) {
      throw new IllegalArgumentException("You must provide interface to use loader");
    }
    return createProxy(file, interfaceType, containerType);
  }

  protected abstract ConfigurationLoader<CommentedConfigurationNode> getLoader(Path path);

  private <T, C extends T> T createProxy(File file, Class<T> interfaceType, Class<C> containerType)
      throws IOException {

    ConfigurationReference<CommentedConfigurationNode> reference = ConfigurationReference.watching(
        this::getLoader, file.toPath(), listener);

    ProxyCallRouter<T> router = routerFactory.create();
    ProxyUpdater<T> proxyUpdater = proxyUpdaterFactory.createProxy(router, containerType);
    reference.updates().subscribe(proxyUpdater);

    if (!file.exists()) {
      reference.save();
    }

    proxyUpdater.submit(reference.node());

    //noinspection unchecked
    return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{interfaceType},
        router);

  }
}
