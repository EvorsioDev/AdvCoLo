package ru.armagidon.advcolo.loader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import ru.armagidon.advcolo.util.Unchecked;

public class ProxyCallRouter<T extends ReloadableConfig> implements InvocationHandler {

  private final Map<Method, Object> valueProxies = new ConcurrentHashMap<>();
  private final Map<Method, ProxyCallRouter<ReloadableConfig>> routers = new ConcurrentHashMap<>();
  private final AtomicReference<T> reference;

  public ProxyCallRouter(T initial) {
    reference = new AtomicReference<>(initial);
  }

  public ProxyCallRouter() {
    this(null);
  }


  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Class<?> returnType = method.getReturnType();
    if (!method.isAnnotationPresent(ReloadableGetter.class)) {
      return method.invoke(reference.get(), args);
    } else {
      if (!returnType.isInterface() || method.getParameterCount() > 0 || !ReloadableConfig.class.isAssignableFrom(returnType)) {
        throw new RuntimeException("Reloadable getter must be a property with return type that is interface extending ReloadableConfig");
      }
    }

    if (reference.get() == null)
      throw new IllegalAccessException("Value is not loaded");

    ReloadableConfig initial = (ReloadableConfig) method.invoke(reference.get(), args);
    if (!valueProxies.containsKey(method)) {
      ProxyCallRouter<ReloadableConfig> router = new ProxyCallRouter<>(initial);
      routers.put(method, router);
      Object valueProxy = Proxy.newProxyInstance(getClass().getClassLoader(),
          new Class[]{ returnType }, router);
      valueProxies.put(method, valueProxy);
      return valueProxy;
    } else {
      return valueProxies.get(method);
    }
  }

  public void update(T newValue) {
    if (newValue == null) {
      return;
    }
    reference.set(newValue);
    newValue.onReload();
    routers.forEach(Unchecked.biConsumer((method, router) -> {
      ReloadableConfig reloadableConfig = (ReloadableConfig) method.invoke(newValue);
      router.update(reloadableConfig);
    }));
  }
}
