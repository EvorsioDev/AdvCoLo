package ru.armagidon.advcolo.loader;

public interface ProxyCallRouterFactory {

  <T extends ReloadableConfig> ProxyCallRouter<T> create();

}
