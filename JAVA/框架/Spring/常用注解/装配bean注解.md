<center>装配bean时常用的注解</center>
####介绍
<strong>@Autowired：</strong>属于Spring 的org.springframework.beans.factory.annotation包下,可用于为类的属性、构造器、方法进行注值
<strong>@Resource：</strong>不属于spring的注解，而是来自于JSR-250位于java.annotation包下，使用该annotation为目标bean指定协作者Bean。
@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作

####举例详解
**（1）：@Autowired**
```java
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    boolean required() default true;
}
```
```java
@Controller
public class HappyController {
    @Autowired //默认依赖的ClubDao 对象（Bean）必须存在
    //@Autowired(required = false) 改变默认方式
    @Qualifier("goodClubService")
    private ClubService clubService;

    // Control the people entering the Club
    // do something
}
```
**（2）：@Resource**
```java
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface Resource {

	/**
	 * 资源的JNDI名称
	 * 字段注解：名称为默认字段名
	 * 方法注解：该方法的JavaBean名
	 * 类注解：没有默认，必须命名
	 * @return
	 */
    String name() default "";

    /**
     * The name of the resource that the reference points to. It can
     * link to any compatible resource using the global JNDI names.
     * 资源名称的参考点，它可以使用全局JNDI名称链接到任何兼容的资源
     * @since Common Annotations 1.1
     */

    String lookup() default "";

    /**
     * 资源的Java类型
     * 字段注解：字段对应的类型
	 * 方法注解：JavaBean
	 * 类注解：没有默认，必须命名
     */
    Class<?> type() default java.lang.Object.class;

    /**
     * 资源的验证类型（两种）
     */
    enum AuthenticationType {
	    CONTAINER,
	    APPLICATION
    }

    /**
     * 表示任何受支持类型的连接工厂的资源指定此方法，不得为其他类型的资源指定
     * javax.annotation.Resource.AuthenticationType.CONTAINER
     */
    AuthenticationType authenticationType() default AuthenticationType.CONTAINER;

    /**
     * 两个组件之间是否共享此资源
     * 任何受支持类型的连接工厂的资源指定此方法，不得为其他类型的资源制定
     */
    boolean shareable() default true;

    /**
     * 资源映射到的特定产品的名称
     * 资源的名称使用name元素或默认定义，则该名称是本地应用组件的使用名称（命名空间：java:comp/env）
     * 许多应用程序服务器都提供一种方式将这些本地名称映射到应用程序服务器已知的资源名称。
     * 此映射的名称通常是全局 JNDI名称，但也可以是任何形式的名称。
     *
     * 应用程序服务器不需要支持任何特殊形式或类型的映射名称，也不需要具有使用映射名称的能力。
     */
    String mappedName() default "";

    /**
     * 资源的描述（应用程序的系统的默认语言）
     */
    String description() default "";
}
```
```java
public class AnotationExp {

    @Resource(name = "HappyClient")
    private HappyClient happyClient;

    @Resource(type = HappyPlayAno .class)
    private HappyPlayAno happyPlayAno;
}
```

####总结
（1）：相同点
@Resource的作用相当于@Autowired，均可标注在字段或属性的setter方法上。
（2）：不同点
+ a：提供方
    @Autowired是Spring的注解，
    @Resource是javax.annotation注解，而是来自于JSR-250，J2EE提供，需要JDK1.6及以上。
+ b ：注入方式
    @Autowired只按照Type 注入；
    @Resource默认按Name自动注入，也提供按照Type 注入；
+ c：属性
    @Autowired注解可用于为类的**属性、构造器、方法**进行注值。默认情况下，其依赖的对象必须存在（bean可用），如果需要改变这种默认方式，可以设置其required属性为false。
    <strong>还有一个比较重要的点就是</strong>：@Autowired注解默认按照类型装配，如果容器中包含多个同一类型的Bean，那么启动容器时会报找不到指定类型bean的异常，解决办法是结合`@Qualifier`注解进行限定，指定注入的bean名称。
    @Resource有两个中重要的属性：name和type。name属性指定byName，如果没有指定name属性，当注解标注在字段上，即默认取字段的名称作为bean名称寻找依赖对象，当注解标注在属性的setter方法上，即默认取属性名作为bean名称寻找依赖对象。
    需要注意的是，@Resource如果没有指定name属性，并且按照默认的名称仍然找不到依赖对象时， @Resource注解会回退到按类型装配。但一旦指定了name属性，就只能按名称装配了。
+ d：
    @Resource注解的使用性更为灵活，可指定名称，也可以指定类型 ；
    @Autowired注解进行装配容易抛出异常，特别是装配的bean类型有多个的时候，而解决的办法是需要在增加@Qualifier进行限定。

<strong><span style="font-size: 15px; color: rgb(122, 68, 66);">Spring中 @Autowired注解与@Resource注解的区别</span></strong>
注意点：使用@Resource也要注意添加配置文件到Spring，如果没有配置component-scan
```xml
<context:component-scan>
<!--<context:component-scan>的使用，是默认激活<context:annotation-config>功能-->
```
则一定要配置 annotation-config
```xml
<context:annotation-config/>
```
