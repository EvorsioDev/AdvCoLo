package ru.armagidon.advcolo.loader;

public interface ProxyUpdaterFactory {

  <T extends ReloadableConfig> ProxyUpdater<T> createProxy(ProxyCallRouter<T> router, Class<? extends T> containerType);

}
