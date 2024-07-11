package ru.armagidon.advcolo.loader;

public class DefaultProxyUpdaterFactory implements ProxyUpdaterFactory{

  @Override
  public <T> ProxyUpdater<T> createProxy(ProxyCallRouter<T> router,
      Class<? extends T> containerType) {
    return new ProxyUpdater<>(router, containerType);
  }
}
