package ru.armagidon.advcolo.loader;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import ru.armagidon.advcolo.util.Unchecked;

public class ProxyCallRouter<T> implements InvocationHandler {

  private final Map<Method, Object> valueProxies = new ConcurrentHashMap<>();
  private final Map<Method, ProxyCallRouter<Object>> routers = new ConcurrentHashMap<>();
  private final AtomicReference<T> reference;

  public ProxyCallRouter(T initial) {
    reference = new AtomicReference<>(
        Preconditions.checkNotNull(initial, "Initial container must be not null"));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Class<?> returnType = method.getReturnType();
    if (!returnType.isInterface() || !method.isAnnotationPresent(ReloadableGetter.class)
        || method.getParameterCount() > 0) {
      return method.invoke(reference.get(), args);
    }

    Object initial = method.invoke(reference.get(), args);
    if (!valueProxies.containsKey(method)) {
      ProxyCallRouter<Object> router = new ProxyCallRouter<>(initial);
      routers.put(method, router);
      Object valueProxy = Proxy.newProxyInstance(
          getClass().getClassLoader(),
          new Class[]{returnType}, router);
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
    routers.forEach(Unchecked.biConsumer((method, router) ->
        router.update(method.invoke(newValue))));
  }
}
