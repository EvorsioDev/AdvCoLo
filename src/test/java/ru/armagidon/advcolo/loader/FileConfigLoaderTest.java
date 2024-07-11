package ru.armagidon.advcolo.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

class FileConfigLoaderTest {

  @Test
  void testSimpleLoad(@TempDir File tmp) throws IOException {
    var listener = WatchServiceListener.create();
    var loader = new TestFileLoader(listener);
    TestInterface inter = loader.load(new File(tmp,"test.yml"), TestInterface.class,
        TestContainer.class);


    assertEquals(10, inter.getInt());
    assertEquals(10, inter.getNested().getInt());

    TestNestedInterface nestedInterface = inter.getNested();

    TestContainer testContainer = new TestContainer();
    TestNestedContainer testNestedContainer = new TestNestedContainer();
    testNestedContainer.setInt(50);
    testContainer.setNested(testNestedContainer);

    ProxyCallRouter<TestInterface> router = (ProxyCallRouter<TestInterface>) Proxy.getInvocationHandler(inter);
    router.update(testContainer);

    assertEquals(50, nestedInterface.getInt());
  }

  private static final class TestFileLoader extends FileConfigLoader {

    public TestFileLoader(WatchServiceListener listener) {
      super(listener);
    }

    @Override
    protected ConfigurationLoader<CommentedConfigurationNode> getLoader(Path path) {
      return YamlConfigurationLoader.builder().path(path)
          .indent(4)
          .nodeStyle(NodeStyle.BLOCK)
          .headerMode(HeaderMode.PRESERVE)
          .build();
    }
  }

  private interface TestInterface extends ReloadableConfig {

    int getInt();

    void setNested(TestNestedContainer newValue);

    @ReloadableGetter
    TestNestedInterface getNested();
  }

  private interface TestNestedInterface extends ReloadableConfig {

    int getInt();

    void setInt(int value);
  }

  @ConfigSerializable
  private static final class TestContainer implements TestInterface {

    private int value = 10;
    private TestNestedContainer nested;

    @Override
    public int getInt() {
      return value;
    }

    @Override
    public void setNested(TestNestedContainer newValue) {
      this.nested = newValue;
    }

    @Override
    public TestNestedInterface getNested() {
      return nested;
    }
  }

  @ConfigSerializable
  private static final class TestNestedContainer implements TestNestedInterface {

    private int value = 10;

    @Override
    public int getInt() {
      return value;
    }

    @Override
    public void setInt(int value) {
      this.value = value;
    }
  }

}