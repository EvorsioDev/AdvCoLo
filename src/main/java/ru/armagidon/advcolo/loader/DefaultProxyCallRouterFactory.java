package ru.armagidon.advcolo.loader;

public class DefaultProxyCallRouterFactory implements ProxyCallRouterFactory {


  @Override
  public <T> ProxyCallRouter<T> create(T initial) {
    return new ProxyCallRouter<>(initial);
  }
}
