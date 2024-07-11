package ru.armagidon.advcolo.loader;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.reactive.Subscriber;

public class ProxyUpdater<T> implements Subscriber<CommentedConfigurationNode> {

  private final ProxyCallRouter<T> router;
  private final Class<? extends T> containerType;

  public ProxyUpdater(ProxyCallRouter<T> router, Class<? extends T> containerType) {
    this.router = router;
    this.containerType = containerType;
  }

  @Override
  public void submit(CommentedConfigurationNode node) {
    try {
      router.update(node.get(containerType));
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
