# Blessing config

This library is a library for serializing and deserializing configuration files. 

It is designed to be as lightweight and simple as possible. 

It doesn't have a lot of bells and whistles. 
But you (should) be able to convert to a different configuration file library by implementing the required interfaces.

When you need a specific ConfigType. You need to compile a specific library.

# Usage (用法):

> ⚠️ Written from chinese. If necessary. Please use a translator.

---

## 创建属于你的配置文件类:

> ⚠️ 目前不支持通过路径和值来配置配置文件. 
> 你可以扩展AbstractConfig然后自动将解析的值放在List中. 
> 尽管不推荐这么做.

要开始第一步 请创建第一个类 并扩展AbstractConfig. 就像:

```java
import net.miaomoe.blessing.config.parser.AbstractConfig;
public class FooConfig extends AbstractConfig {}
```

让我们继续前进!

## 选择一个类型

您可以实现一些可以作为配置文件的类作为字段的type. 支持以下类型:

  - List<支持的类>
  - Enum (枚举类)
  - AbstractConfig (其实现的类)
  - 除了`char`外的所有基元类型 (例如`boolean`, `int` 等等)
  - String (字符串)

但在选择类型的时候请注意:

  - 不支持List的原始类型: Blessing会由于无法处理原始类型而出错. 
  原始类型也不应该在任何一个不算过时的项目中.
  - Map不被支持: 
  如果你想创建一个子Map用于存储在一个路径下面的值 请自行实现AbstractConfig
  - 枚举类作为字符串处理: 
  不支持直接存储枚举类类型的对象. 
  (默认情况下)祝福会在读取枚举时自动获得同名的枚举类.
  如果键入了错误的名字则会抛出异常.
  - 不支持非全大写的枚举对象: 在引用枚举类型时 请确保名称是全大写的.

当您选择好类型之后 我们便可以继续下一步了.

## 添加字段

该步骤决定了您打算写入或读取哪些东西.

例如 (hocon示例):
```hocon
foo="Foo!"
debug=true
```

要达到上面的效果 你可以实现字段并在字段上方添加`@ConfigValue`, 就像这样:

```java
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;

public class FooConfig extends AbstractConfig {
  @ConfigValue
  public @NotNull /* 可选 */ String foo = "Foo!";
  @ConfigValue
  public boolean debug = true;
}
```

我们明明没有修改`@ConfigValue`中的默认值! 让我们使用`@ParseAllField`来避免重复编写无意义的注解.

```java
import net.miaomoe.blessing.config.annotation.ParseAllField;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;

@ParseAllField
public class FooConfig extends AbstractConfig {
  public @NotNull String foo = "Foo!";
  public boolean debug = true;
}
```

至此就完成了一个可以解析和设置的配置文件. 当然还有一些进阶操作:

### 使用Getter/Setter

尽管祝福会自动选择可用的Getter和Setter. 
(除非在ConfigValue中将useGetter和useSetter设置为false)  
但您也可以自己实现那些方法.

1. 使用[Lombok](https://projectlombok.org/)的 [@Getter和@Setter](https://projectlombok.org/features/GetterSetter)

```java
import lombok.Getter;
import lombok.Setter;
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class FooConfig extends AbstractConfig {
  @ConfigValue
  private @NotNull String foo = "Foo!";
}
```

这是一个很好的实现. Lombok也会在Setter中实现空检查. 也可以手动实现它:

```java
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FooConfig extends AbstractConfig {
  @ConfigValue
  private @NotNull String foo = "Foo!";

  public @NotNull String getFoo() {
    return foo;
  }

  public void setFoo(@NotNull String foo) {
    this.foo = Objects.requireNonNull(foo);
  }
}
```

### 使用驼峰命名法并自动格式化

> ⚠️`autoFormat`为false或`path`不为空都会导致祝福跳过格式化. 即传递原始输入

祝福会自动处理驼峰命名法 例如: `thisIsFoo` 会被处理成 `this-is-foo`

### 忽略指定的字段 (仅限`@ParseAllField`)

只需在ParseAllField的exempt中填入需要忽略的字段名即可. 例如:

```java
import net.miaomoe.blessing.config.annotation.ParseAllField;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;

@ParseAllField(ignore = {"exempt"})
public class FooConfig extends AbstractConfig {
  private @NotNull String exempt = "This is exempted field!";
  public @NotNull String foo = "Foo!";
}
```

这样. 祝福就不会再去处理`exempt`字段.

## 保存并读取配置文件

`SimpleConfigUtil` 能够快速帮助你读取或新建配置文件. 有两种方法:

  - `saveAndRead(文件夹, 文件名, 配置文件对象, 配置文件的属性)`
  - `saveAndRead(目标文件, 配置文件对象, 配置文件的属性)`

属性将决定它以什么格式来读取或写入配置文件. 例如`Hocon`或`Json`

对于第一个方法. 
它的文件名并不是直接采用的, 
而是取决于属性的后缀 
例如:  
`SimpleConfigUtil.saveAndRead(folder, "config", config, ConfigType.HOCON)`  
文件名将会是`config.conf`.
反之,如果使用`JSON`则会是`config.json`.

第二个方法会直接根据目标文件来读取或存储. 不会自动应用后缀.

使用了saveAndRead方法过后 除非加载时出现了异常 否则值将是对应于配置文件中的内容的.

### 不存在的路径

当指定配置文件已经存在的时候 
所有不在文件中存在(亦或者没有被解析成功或被忽略)的路径对应的字段将保持默认的值(即字段默认赋予的值)

你可以在解析配置文件过后删除文件然后重新生成 
尽管这会丢失所有(非默认)的注释. 
以及被忽略的内容(即类不包含对应的那个值的路径的字段)

## 在配置文件中添加注释

添加注释非常简单. 只需在字段上添加`@Comment`注解

例如:

```java
import net.miaomoe.blessing.config.annotation.Comment;
import net.miaomoe.blessing.config.annotation.ParseAllField;
import net.miaomoe.blessing.config.parser.AbstractConfig;

@ParseAllField
public class Foo extends AbstractConfig {
  @Comment(description = "这是什么? Foo! 碰一下")
  public String foo = "Foo!";
  @Comment(description = {
    "这是另一个Foo!",
    "碰这一个Foo并不会导致你失忆."
  })
  public String anotherFoo = "This is a foo!";
}
```

生成的hocon配置文件将是这样的:

```hocon
# 这是什么? Foo! 碰一下
foo="Foo!"
# 这是另一个Foo!
# 碰这一个Foo并不会导致你失忆.
another-foo="This is a foo!"
```

## 嵌套`AbstractConfig`

你可以在AbstractConfig中声明另一个AbstractConfig对象  
它实际上没有太复杂. 本文来解释一些可能的简单的用例

实际上 AbstractConfig是可以被无限嵌套和正确解析的  
> 虽然不建议这么做 这可能会增加代码的复杂度并降低可读性

> ⚠️可能无法解析其重写的类的字段 例如:
> ```java
> import net.miaomoe.blessing.config.annotation.ConfigValue;
> import net.miaomoe.blessing.config.annotation.ParseAllField;
> import net.miaomoe.blessing.config.parser.AbstractConfig;
>
> @ParseAllField
> public class Example extends AbstractConfig {
>   public final Foo foo = new Foo();
>   public final AnotherFoo anotherFoo = new AnotherFoo();
>
>   public class Foo extends AbstractConfig {
>     @ConfigValue
>     public String foo = "Foo!";
>   }
>
>   public class AnotherFoo extends Foo {
>     public String anotherFoo = "I am foo!";
>   }
> }
> ```
> `AnotherFoo`在实际应用中实际上不持有`foo`, 而是仅持有`anotherFoo`.

### 引用单个`AbstractConfig`对象

例如:

```java
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;

public class SimpleConfig extends AbstractConfig {
    
    @ConfigValue
    public final SubConfig subConfig = new SubConfig("Foo!");

    public static class SubConfig extends AbstractConfig {
        public SubConfig(final String value) { this.subString=value; }
        @ConfigValue
        public String subString;
    }

}
```

> ✅建议对嵌套的单个AbstractConfig标记为final. 
> 因为祝福会在类的内部来设置需要设置的值 而不是新建一个相同的类.

默认的Hocon配置打印出来将是:

```hocon
sub-config {
  sub-string="Foo!";
}
```

这在设计上于`sub-config.sub-string`这个路径相同. 
但前者很显然提供了更大的可能.
在实际应用中对于减少样板代码也非常有帮助.

### `List<AbstractConfig>`对象

更多..更多..更多的规则!

```java
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;

import java.util.Arrays;

public class MoreFoo extends AbstractConfig {

    @ConfigValue
    public List<Foo> manyFoo = Arrays.asList(new Foo(), new Foo("1"));

    public static class Foo extends AbstractConfig {
        
        public Foo(final String suffix) {
            this.foo = "Foo" + suffix;
        }
        
        public Foo() { this.foo = "Foo!"; }
        
        @ConfigValue
        public String foo;
    }

}
```

> ⚠️目标配置文件必须有一个无形参构造器 使其可以类似于像这样(`new Foo()`)创建一个`Foo`.

> ⚠️每次重载都会新建所需的对象. 因此请确保应用程序可以处理这种变动.

> ⚠️不要将List设置成final. 祝福通过直接设置解析过后的List而不是往原有的List中添加或删除内容.

转换为hocon应该是这样的:
```hocon
many-foo=[
  {
    foo="Foo!"
  },
  {
    foo="Foo1"
  }
]
```

别忘了祝福可以读取配置文件. 你可以随意添加和删除Foo.

编辑配置文件:

```hocon
many-foo=[
  {
    foo="This is foo1!";
  }
  {
    foo="I am foo2!";
  }
  {
    foo="Who am i?";
  }
]
```

在重载之后 List会有3个新的Foo.

 - 第一个Foo的foo对象的文本: "This is foo!"  
 - 第二个Foo的foo对象的文本: "I am foo2!"  
 - 第三个Foo的foo对象的文本: "Who am i?"

以此类推. 理论上根据配置文件变动 可以创造出无限的AbstractConfig.

---