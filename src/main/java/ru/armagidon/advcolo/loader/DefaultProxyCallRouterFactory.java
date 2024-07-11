package ru.armagidon.advcolo.loader;

public class DefaultProxyCallRouterFactory implements ProxyCallRouterFactory {


  @Override
  public <T extends ReloadableConfig> ProxyCallRouter<T> create() {
    return new ProxyCallRouter<>();
  }
}
