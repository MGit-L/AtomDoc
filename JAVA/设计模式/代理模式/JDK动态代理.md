<center>JDK动态代理</center>
##一、必要性
###1.1代理模式
代理模式是指给某一个对象提供一个代理对象，并由代理对象控制对原对象的引用。通俗的来讲代理模式就是我们生活中常见的中介。
作用：它可以在原对象的基础上增强原对象的功能，比如在原对象调用一个方法的前后进行日志、事务操作等。Spring AOP就使用了代理模式。
###1.2代理模式----静态代理
静态代理是指在程序运行前就已经存在的编译好的代理类是为静态代理。实现静态代理有四个步骤：
①定义业务接口；
②被代理类实现业务接口；
③定义代理类并实现业务接口；
④最后便可通过客户端进行调用。（这里可以理解成程序的main方法里的内容）
我们按照这个步骤去实现静态代理。需求：在向数据库添加一个用户时前后打印日志。
####1.2.1业务接口
IUserService.java
```
package com.zhb.jdk.proxy;

/**
* @author live
* @date 2020年3月23日下午02:24:01
* @todo TODO
*/
public interface IUserService {

 void add(String name);
}
```
####1.2.2被代理类实现业务接口
UserServiceImpl.java
```
package com.zhb.jdk.proxy;

/**
* @author live
* @date 2020年3月23日下午02:27:23
* @todo TODO
*/
public class UserServiceImpl implements IUserService {

 @Override
 public void add(String name) {
   System.out.println("向数据库中插入名为： "+name+" 的用户");
 }
}
```
####1.2.3定义代理类并实现业务接口
因为代理对象和被代理对象需要实现相同的接口。所以代理类源文件UserServiceProxy.java这么写：
```
package com.zhb.jdk.proxy;

/**
* @author live
* @date 2020年3月23日下午02:29:39
* @todo TODO
*/
public class UserServiceProxy implements IUserService {

 // 被代理对象
 private IUserService target;

 // 通过构造方法传入被代理对象
 public UserServiceProxy(IUserService target) {
   this.target = target;
 }

 @Override
 public void add(String name) {
   System.out.println("准备向数据库中插入数据");
   target.add(name);
   System.out.println("插入数据库成功");
 }
}
```
由于代理类(UserServiceProxy )和被代理类(UserServiceImpl )都实现了IUserService接口，所以都有add方法，在代理类的add方法中调用了被代理类的add方法，并在其前后各打印一条日志。
####1.2.4客户端调用
```
package com.zhb.jdk.proxy;

/**
* @author live
* @date 2020年3月23日下午02:30:09
* @todo TODO
*/
public class StaticProxyTest {

 public static void main(String[] args) {

   IUserService target = new UserServiceImpl();
   UserServiceProxy proxy = new UserServiceProxy(target);
   proxy.add("陈粒");
 }
}
```
###1.3 代理模式----动态代理
静态代理的一些缺点：
①代理类和被代理类实现了相同的接口，导致代码的重复，如果接口增加一个方法，那么除了被代理类需要实现这个方法外，代理类也要实现这个方法，增加了代码维护的难度。
②代理对象只服务于一种类型的对象，如果要服务多类型的对象。势必要为每一种对象都进行代理，静态代理在程序规模稍大时就无法胜任了。比如上面的例子，只是对用户的业务功能（IUserService）进行代理，如果是商品（IItemService）的业务功能那就无法代理，需要去编写商品服务的代理类。
于是乎，动态代理的出现就能帮助我们解决静态代理的不足。所谓动态代理是指：在程序运行期间根据需要动态创建代理类及其实例来完成具体的功能。
动态代理主要分为JDK动态代理和cglib动态代理两大类，本文主要对JDK动态代理进行探讨。
##二、动态代理实例
###2.1 使用JDK动态代理步骤：
①创建被代理的接口和类；
②创建InvocationHandler接口的实现类，在invoke方法中实现代理逻辑；
③通过Proxy的静态方法newProxyInstance( ClassLoaderloader, Class[] interfaces, InvocationHandler h)创建一个代理对象
④使用代理对象。
###2.2 Demo
还是我们刚才的需求，这次换用动态代理实现：
####2.2.1 创建被代理的接口和类
这个和静态代理的源码相同，还是使用上面的IUserService.java和UserServiceImpl.java
####2.2.2 创建InvocationHandler接口的实现类
```
package com.zhb.jdk.dynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
* @author live
* @date 2020年3月23日下午02:37:53
* @todo TODO
*/
public class MyInvocationHandler implements InvocationHandler {

 //被代理对象，Object类型
 private Object target;

 public MyInvocationHandler(Object target) {
   this.target = target;
 }

 @Override
 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

   System.out.println("准备向数据库中插入数据");
   Object returnvalue = method.invoke(target, args); System.out.println("插入数据库成功");
   return returnvalue;
 }
}
```
###2.2.3 通过Proxy的静态方法创建代理对象并使用代理对象
代码中有注释：
```
package com.zhb.jdk.dynamicProxy;

import java.lang.reflect.Proxy;

/**
* @todo TODO
*/
public class DynamicProxyTest {

 public static void main(String[] args) {

   IUserService target = new UserServiceImpl();
   MyInvocationHandler handler = new MyInvocationHandler(target);
   //第一个参数是指定代理类的类加载器（我们传入当前测试类的类加载器）
   //第二个参数是代理类需要实现的接口（我们传入被代理类实现的接口，这样生成的代理类和被代理类就实现了相同的接口）
   //第三个参数是invocation handler，用来处理方法的调用。这里传入我们自己实现的handler
   IUserService proxyObject = (IUserService) Proxy.newProxyInstance(DynamicProxyTest.class.getClassLoader(),
       target.getClass().getInterfaces(), handler);
   proxyObject.add("陈粒");
 }
}
```
运行结果和静态代理一样，说明成功了。但是，我们注意到，我们并没有像静态代理那样去自己定义一个代理类，并实例化代理对象。实际上，动态代理的代理对象是在内存中的，是JDK根据我们传入的参数生成好的。
##三、动态代理源码深入分析
这部分如果想要更快更好的理解，建议一边对着源码(本文JDK 1.8)，一边看着博客。毕竟自己亲身实践效果才好嘛。
Proxy.newProxyInstance( ClassLoaderloader, Class[] interfaces, InvocationHandler h)产生了代理对象，所以我们进到newProxyInstance的实现：
```
public static Object newProxyInstance(ClassLoader loader,
                                         Class<?>[] interfaces,
                                         InvocationHandler h)
       throws IllegalArgumentException
   {
       //检验h不为空，h为空抛异常
       Objects.requireNonNull(h);
       //接口的类对象拷贝一份
       final Class<?>[] intfs = interfaces.clone();
       //进行一些安全性检查
       final SecurityManager sm = System.getSecurityManager();
       if (sm != null) {
           checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
       }

       /*
        * Look up or generate the designated proxy class.
        *  查询（在缓存中已经有）或生成指定的代理类的class对象。
        */
       Class<?> cl = getProxyClass0(loader, intfs);

       /*
        * Invoke its constructor with the designated invocation handler.
        */
       try {
           if (sm != null) {
               checkNewProxyPermission(Reflection.getCallerClass(), cl);
           }
           //得到代理类对象的构造函数，这个构造函数的参数由constructorParams指定
           //参数constructorParames为常量值：private static final Class<?>[] constructorParams = { InvocationHandler.class };
           final Constructor<?> cons = cl.getConstructor(constructorParams);
           final InvocationHandler ih = h;
           if (!Modifier.isPublic(cl.getModifiers())) {
               AccessController.doPrivileged(new PrivilegedAction<Void>() {
                   public Void run() {
                       cons.setAccessible(true);
                       return null;
                   }
               });
           }
           //这里生成代理对象，传入的参数new Object[]{h}后面讲
           return cons.newInstance(new Object[]{h});
       } catch (IllegalAccessException|InstantiationException e) {
           throw new InternalError(e.toString(), e);
       } catch (InvocationTargetException e) {
           Throwable t = e.getCause();
           if (t instanceof RuntimeException) {
               throw (RuntimeException) t;
           } else {
               throw new InternalError(t.toString(), t);
           }
       } catch (NoSuchMethodException e) {
           throw new InternalError(e.toString(), e);
       }
   }
```
这段代码核心就是通过getProxyClass0(loader, intfs)得到代理类的Class对象，然后通过Class对象得到构造方法，进而创建代理对象。下一步看getProxyClass0这个方法。
```
//此方法也是Proxy类下的方法
   private static Class<?> getProxyClass0(ClassLoader loader,
                                          Class<?>... interfaces) {
       if (interfaces.length > 65535) {
           throw new IllegalArgumentException("interface limit exceeded");
       }

       // If the proxy class defined by the given loader implementing
       // the given interfaces exists, this will simply return the cached copy;
       // otherwise, it will create the proxy class via the ProxyClassFactory
       //意思是：如果代理类被指定的类加载器loader定义了，并实现了给定的接口interfaces，
       //那么就返回缓存的代理类对象，否则使用ProxyClassFactory创建代理类。
       return proxyClassCache.get(loader, interfaces);
   }
```
这里看到proxyClassCache，有Cache便知道是缓存的意思，正好呼应了前面Look up or generate the designated proxy class。查询（在缓存中已经有）或生成指定的代理类的class对象这段注释。
在进入get方法之前，我们看下 proxyClassCache是什么？高能预警，前方代码看起来可能有乱，但我们只需要关注重点即可。
```
/**
    * a cache of proxy classes
    */
   private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
       proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
```
proxyClassCache是个WeakCache类的对象，调用proxyClassCache.get(loader, interfaces); 可以得到缓存的代理类或创建代理类（没有缓存的情况）。说明WeakCache中有get这个方法。先看下WeakCache类的定义（这里先只给出变量的定义和构造函数）：
```
//K代表key的类型，P代表参数的类型，V代表value的类型。
// WeakCache<ClassLoader, Class<?>[], Class<?>>  proxyClassCache  说明proxyClassCache存的值是Class<?>对象，正是我们需要的代理类对象。
final class WeakCache<K, P, V> {

   private final ReferenceQueue<K> refQueue
       = new ReferenceQueue<>();
   // the key type is Object for supporting null key
   private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map
       = new ConcurrentHashMap<>();
   private final ConcurrentMap<Supplier<V>, Boolean> reverseMap
       = new ConcurrentHashMap<>();
   private final BiFunction<K, P, ?> subKeyFactory;
   private final BiFunction<K, P, V> valueFactory;


   public WeakCache(BiFunction<K, P, ?> subKeyFactory,
                    BiFunction<K, P, V> valueFactory) {
       this.subKeyFactory = Objects.requireNonNull(subKeyFactory);
       this.valueFactory = Objects.requireNonNull(valueFactory);
   }
```
其中map变量是实现缓存的核心变量，他是一个双重的Map结构: (key, sub-key) -> value。其中key是传进来的Classloader进行包装后的对象，sub-key是由WeakCache构造函数传人的KeyFactory()生成的。value就是产生代理类的对象，是由WeakCache构造函数传人的ProxyClassFactory()生成的。如下，回顾一下:
```
private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
       proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
```
产生sub-key的KeyFactory代码如下，这个我们不去深究，只要知道他是根据传入的ClassLoader和接口类生成sub-key即可。
```
private static final class KeyFactory
       implements BiFunction<ClassLoader, Class<?>[], Object>
   {
       @Override
       public Object apply(ClassLoader classLoader, Class<?>[] interfaces) {
           switch (interfaces.length) {
               case 1: return new Key1(interfaces[0]); // the most frequent
               case 2: return new Key2(interfaces[0], interfaces[1]);
               case 0: return key0;
               default: return new KeyX(interfaces);
           }
       }
   }
```
通过sub-key拿到一个Supplier<Class<?>>对象，然后调用这个对象的get方法，最终得到代理类的Class对象。
好，大体上说完WeakCache这个类的作用，我们回到刚才proxyClassCache.get(loader, interfaces);这句代码。get是WeakCache里的方法。源码如下：
```
public V get(K key, P parameter) {
       //检查parameter不为空
       Objects.requireNonNull(parameter);
        //清除无效的缓存
       expungeStaleEntries();
       // cacheKey就是(key, sub-key) -> value里的一级key，
       Object cacheKey = CacheKey.valueOf(key, refQueue);

       // lazily install the 2nd level valuesMap for the particular cacheKey
       //根据一级key得到 ConcurrentMap<Object, Supplier<V>>对象。如果之前不存在，则新建一个ConcurrentMap<Object, Supplier<V>>和cacheKey（一级key）一起放到map中。
       ConcurrentMap<Object, Supplier<V>> valuesMap = map.get(cacheKey);
       if (valuesMap == null) {
           ConcurrentMap<Object, Supplier<V>> oldValuesMap
               = map.putIfAbsent(cacheKey,
                                 valuesMap = new ConcurrentHashMap<>());
           if (oldValuesMap != null) {
               valuesMap = oldValuesMap;
           }
       }

       // create subKey and retrieve the possible Supplier<V> stored by that
       // subKey from valuesMap
       //这部分就是调用生成sub-key的代码，上面我们已经看过怎么生成的了
       Object subKey = Objects.requireNonNull(subKeyFactory.apply(key, parameter));
       //通过sub-key得到supplier
       Supplier<V> supplier = valuesMap.get(subKey);
       //supplier实际上就是这个factory
       Factory factory = null;

       while (true) {
           //如果缓存里有supplier ，那就直接通过get方法，得到代理类对象，返回，就结束了，一会儿分析get方法。
           if (supplier != null) {
               // supplier might be a Factory or a CacheValue<V> instance
               V value = supplier.get();
               if (value != null) {
                   return value;
               }
           }
           // else no supplier in cache
           // or a supplier that returned null (could be a cleared CacheValue
           // or a Factory that wasn't successful in installing the CacheValue)
           // lazily construct a Factory
           //下面的所有代码目的就是：如果缓存中没有supplier，则创建一个Factory对象，把factory对象在多线程的环境下安全的赋给supplier。
           //因为是在while（true）中，赋值成功后又回到上面去调get方法，返回才结束。
           if (factory == null) {
               factory = new Factory(key, parameter, subKey, valuesMap);
           }

           if (supplier == null) {
               supplier = valuesMap.putIfAbsent(subKey, factory);
               if (supplier == null) {
                   // successfully installed Factory
                   supplier = factory;
               }
               // else retry with winning supplier
           } else {
               if (valuesMap.replace(subKey, supplier, factory)) {
                   // successfully replaced
                   // cleared CacheEntry / unsuccessful Factory
                   // with our Factory
                   supplier = factory;
               } else {
                   // retry with current supplier
                   supplier = valuesMap.get(subKey);
               }
           }
       }
   }
```
所以接下来我们看Factory类中的get方法。
```
public synchronized V get() { // serialize access
    // re-check
    Supplier<V> supplier = valuesMap.get(subKey);
    /重新检查得到的supplier是不是当前对象
           if (supplier != this) {
               // something changed while we were waiting:
               // might be that we were replaced by a CacheValue
               // or were removed because of failure ->
               // return null to signal WeakCache.get() to retry
               // the loop
               return null;
           }
           // else still us (supplier == this)

           // create new value
           V value = null;
           try {
                //代理类就是在这个位置调用valueFactory生成的
                //valueFactory就是我们传入的 new ProxyClassFactory()
               //一会我们分析ProxyClassFactory()的apply方法
               value = Objects.requireNonNull(valueFactory.apply(key, parameter));
           } finally {
               if (value == null) { // remove us on failure
                   valuesMap.remove(subKey, this);
               }
           }
           // the only path to reach here is with non-null value
           assert value != null;

           // wrap value with CacheValue (WeakReference)
           //把value包装成弱引用
           CacheValue<V> cacheValue = new CacheValue<>(value);

           // put into reverseMap
           // reverseMap是用来实现缓存的有效性
           reverseMap.put(cacheValue, Boolean.TRUE);

           // try replacing us with CacheValue (this should always succeed)
           if (!valuesMap.replace(subKey, this, cacheValue)) {
               throw new AssertionError("Should not reach here");
           }

           // successfully replaced us with new CacheValue -> return the value
           // wrapped by it
           return value;
       }
   }
```
拨云见日，来到ProxyClassFactory的apply方法，代理类就是在这里生成的。
```
//这里的BiFunction<T, U, R>是个函数式接口，可以理解为用T，U两种类型做参数，得到R类型的返回值
private static final class ProxyClassFactory
       implements BiFunction<ClassLoader, Class<?>[], Class<?>>
   {
       // prefix for all proxy class names
       //所有代理类名字的前缀
       private static final String proxyClassNamePrefix = "$Proxy";

       // next number to use for generation of unique proxy class names
       //用于生成代理类名字的计数器
       private static final AtomicLong nextUniqueNumber = new AtomicLong();

       @Override
       public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {

           Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
           //验证代理接口，可不看
           for (Class<?> intf : interfaces) {
               /*
                * Verify that the class loader resolves the name of this
                * interface to the same Class object.
                */
               Class<?> interfaceClass = null;
               try {
                   interfaceClass = Class.forName(intf.getName(), false, loader);
               } catch (ClassNotFoundException e) {
               }
               if (interfaceClass != intf) {
                   throw new IllegalArgumentException(
                       intf + " is not visible from class loader");
               }
               /*
                * Verify that the Class object actually represents an
                * interface.
                */
               if (!interfaceClass.isInterface()) {
                   throw new IllegalArgumentException(
                       interfaceClass.getName() + " is not an interface");
               }
               /*
                * Verify that this interface is not a duplicate.
                */
               if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                   throw new IllegalArgumentException(
                       "repeated interface: " + interfaceClass.getName());
               }
           }
           //生成的代理类的包名
           String proxyPkg = null;     // package to define proxy class in
           //代理类访问控制符: public ,final
           int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

           /*
            * Record the package of a non-public proxy interface so that the
            * proxy class will be defined in the same package.  Verify that
            * all non-public proxy interfaces are in the same package.
            */
           //验证所有非公共的接口在同一个包内；公共的就无需处理
           //生成包名和类名的逻辑，包名默认是com.sun.proxy，类名默认是$Proxy 加上一个自增的整数值
           //如果被代理类是 non-public proxy interface ，则用和被代理类接口一样的包名
           for (Class<?> intf : interfaces) {
               int flags = intf.getModifiers();
               if (!Modifier.isPublic(flags)) {
                   accessFlags = Modifier.FINAL;
                   String name = intf.getName();
                   int n = name.lastIndexOf('.');
                   String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                   if (proxyPkg == null) {
                       proxyPkg = pkg;
                   } else if (!pkg.equals(proxyPkg)) {
                       throw new IllegalArgumentException(
                           "non-public interfaces from different packages");
                   }
               }
           }

           if (proxyPkg == null) {
               // if no non-public proxy interfaces, use com.sun.proxy package
               proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
           }

           /*
            * Choose a name for the proxy class to generate.
            */
           long num = nextUniqueNumber.getAndIncrement();
           //代理类的完全限定名，如com.sun.proxy.$Proxy0.calss
           String proxyName = proxyPkg + proxyClassNamePrefix + num;

           /*
            * Generate the specified proxy class.
            */
           //核心部分，生成代理类的字节码
           byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
               proxyName, interfaces, accessFlags);
           try {
               //把代理类加载到JVM中，至此动态代理过程基本结束了
               return defineClass0(loader, proxyName,
                                   proxyClassFile, 0, proxyClassFile.length);
           } catch (ClassFormatError e) {
               /*
                * A ClassFormatError here means that (barring bugs in the
                * proxy class generation code) there was some other
                * invalid aspect of the arguments supplied to the proxy
                * class creation (such as virtual machine limitations
                * exceeded).
                */
               throw new IllegalArgumentException(e.toString());
           }
       }
   }
```
到这里其实已经分析完了，但是本着深究的态度，决定看看JDK生成的动态代理字节码是什么，于是我们将字节码保存到磁盘上的class文件中。代码如下：
```
package com.zhb.jdk.proxy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;

import com.zhb.jdk.dynamicProxy.HelloworldImpl;

import sun.misc.ProxyGenerator;

/**
* @todo TODO
*/
public class DynamicProxyTest {

   public static void main(String[] args) {

       IUserService target = new UserServiceImpl();
       MyInvocationHandler handler = new MyInvocationHandler(target);
       //第一个参数是指定代理类的类加载器（我们传入当前测试类的类加载器）
       //第二个参数是代理类需要实现的接口（我们传入被代理类实现的接口，这样生成的代理类和被代理类就实现了相同的接口）
       //第三个参数是invocation handler，用来处理方法的调用。这里传入我们自己实现的handler
       IUserService proxyObject = (IUserService) Proxy.newProxyInstance(DynamicProxyTest.class.getClassLoader(),
               target.getClass().getInterfaces(), handler);
       proxyObject.add("陈粒");

       String path = "D:/$Proxy0.class";
       byte[] classFile = ProxyGenerator.generateProxyClass("$Proxy0", HelloworldImpl.class.getInterfaces());
       FileOutputStream out = null;

       try {
           out = new FileOutputStream(path);
           out.write(classFile);
           out.flush();
       } catch (Exception e) {
           e.printStackTrace();
       } finally {
           try {
               out.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

   }
}
```
运行这段代码，会在D盘生成一个名为$Proxy0.class的文件。通过反编译工具,得到JDK为我们生成的代理类是这样的：
```
// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space

import com.zhb.jdk.proxy.IUserService;
import java.lang.reflect.*;

public final class $Proxy0 extends Proxy
   implements IUserService
{

   private static Method m1;
   private static Method m2;
   private static Method m3;
   private static Method m0;
   //代理类的构造函数，其参数正是是InvocationHandler实例，
   //Proxy.newInstance方法就是通过通过这个构造函数来创建代理实例的
   public $Proxy0(InvocationHandler invocationhandler)
   {
       super(invocationhandler);
   }
    // Object类中的三个方法，equals，toString， hashCode
   public final boolean equals(Object obj)
   {
       try
       {
           return ((Boolean)super.h.invoke(this, m1, new Object[] {
               obj
           })).booleanValue();
       }
       catch (Error ) { }
       catch (Throwable throwable)
       {
           throw new UndeclaredThrowableException(throwable);
       }
   }

   public final String toString()
   {
       try
       {
           return (String)super.h.invoke(this, m2, null);
       }
       catch (Error ) { }
       catch (Throwable throwable)
       {
           throw new UndeclaredThrowableException(throwable);
       }
   }
   //接口代理方法
   public final void add(String s)
   {
       try
       {
           // invocation handler的 invoke方法在这里被调用
           super.h.invoke(this, m3, new Object[] {
               s
           });
           return;
       }
       catch (Error ) { }
       catch (Throwable throwable)
       {
           throw new UndeclaredThrowableException(throwable);
       }
   }

   public final int hashCode()
   {
       try
       {
           // 在这里调用了invoke方法。
           return ((Integer)super.h.invoke(this, m0, null)).intValue();
       }
       catch (Error ) { }
       catch (Throwable throwable)
       {
           throw new UndeclaredThrowableException(throwable);
       }
   }

   // 静态代码块对变量进行一些初始化工作
   static
   {
       try
       {
           m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[] {
               Class.forName("java.lang.Object")
           });
           m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
           m3 = Class.forName("com.zhb.jdk.proxy.IUserService").getMethod("add", new Class[] {
               Class.forName("java.lang.String")
           });
           m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
       }
       catch (NoSuchMethodException nosuchmethodexception)
       {
           throw new NoSuchMethodError(nosuchmethodexception.getMessage());
       }
       catch (ClassNotFoundException classnotfoundexception)
       {
           throw new NoClassDefFoundError(classnotfoundexception.getMessage());
       }
   }
}
```
生成了Object类的三个方法：toString，hashCode，equals。还有我们需要被代理的方法。
