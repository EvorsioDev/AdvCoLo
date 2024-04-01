package ru.armagidon.advcolo.loader;

public interface ProxyCallRouterFactory {

  <T> ProxyCallRouter<T> create(T initial);

}
