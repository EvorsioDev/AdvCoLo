package ru.armagidon.advcolo.loader;

public interface ProxyUpdaterFactory {

  <T> ProxyUpdater<T> createProxy(ProxyCallRouter<T> router, Class<? extends T> containerType);

}
