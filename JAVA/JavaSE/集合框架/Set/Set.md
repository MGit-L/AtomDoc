<center>Java Set集合</center>

一、Set  
&emsp;&emsp;Set：不会存储重复的元素；用于存储无序（存入和取出的顺序不一定相同）元素
+ Itreable 接口 实现该接口可以使用增强for循环
  + Collection 描述所有集合共性的接口
    + List接口 可以有重复元素的集合
      + ArrayList 底层数据结构是数组Array，线程不安全
      + LinkedList 底层数据结构是链表，线程不安全
      + Vector 底层数据结构是数组Array，线程安全
    + Set接口 不可以有重复元素的集合
      + HashSet 底层数据结构是哈希表，无序；线程不安全
        + LinkedHashSet 底层数据结构是双向链表和哈希表，有序
      + TreeSet 底层数据结构是红黑树，内部实现排序，也可以自定义排序规则
      + ArraySet 底层数据结构是双数组，有序

```java
/**
  * 增加元素
  **/
public abstract boolean add(E paramE);
public abstract boolean addAll(Collection<? extends E> paramCollection);
/**
  * 从此集合中删除所有元素
  **/
public abstract void clear();
/**
  * 如果此集合包含指定的元素，则返回 true
  **/
public abstract boolean contains(Object paramObject);
/**
  * 如果此集合包含所有指定集合的元素，则返回 true
  **/
public abstract boolean containsAll(Collection<?> paramCollection);
public abstract boolean equals(Object paramObject);
public abstract int hashCode();
/**
  * 如果此集合不包含元素，则返回 true
  **/
public abstract boolean isEmpty();
/**
  * 返回此集合中元素的迭代器
  **/
public abstract Iterator<E> iterator();
/**
  * 删除元素
  **/
public abstract boolean remove(Object paramObject);
public abstract boolean removeAll(Collection<?> paramCollection);
/**
  * 仅保留该集合中包含在指定集合中的元素
  **/
public abstract boolean retainAll(Collection<?> paramCollection);
/**
  * 返回此集合中的元素数
  **/
public abstract int size();
/**
  * 返回一个包含此集合中所有元素的数组
  **/
public abstract Object[] toArray();
/**
  * 返回一个包含此集合中所有元素的数组; 返回的数组的运行时类型是指定数组的运行时类型
  */
public abstract <T> T[] toArray(T[] paramArrayOfT);
```

二、HashSet  
&emsp;&emsp;调用原理:先判断hashcode 方法的值,如果相同才会去判断equals 如果不相同,是不会调用equals方法的。
&emsp;&emsp;判断两个元素是否相同，先要判断元素的hashCode值是否一致，只有在该值一致的情况下，才会判断equals方法，如果存储在HashSet中的两个对象hashCode方法的值相同equals方法返回的结果是true，那么HashSet认为这两个元素是相同元素，只存储一个（重复元素无法存入）。
<font color=red>&emsp;&emsp;注意：</font>HashSet集合在判断元素是否相同先判断hashCode方法，如果相同才会判断equals。如果不相同，是不会调用equals方法的。
<font color=red>
HashSet 和ArrayList集合都有判断元素是否相同的方法
```
boolean contains(Object o)
```
HashSet使用hashCode和equals方法，ArrayList使用了equals方法
</font>
三、TreeSet  
&emsp;&emsp;存入TreeSet集合中的元素要具备比较性.
&emsp;&emsp;红黑树算法的规则: 左小右大。
&emsp;&emsp;TreeSet的排序规则：
&emsp;&emsp;&emsp;&emsp;1:让存入的元素自定义比较规则。
&emsp;&emsp;&emsp;&emsp;2:给TreeSet指定排序规则。
&emsp;&emsp;方式一：元素自身具备比较性
&emsp;&emsp;&emsp;&emsp;元素自身具备比较性，需要元素实现Comparable接口，重写compareTo方法，也就是让元素自身具备比较性，这种方式叫做元素的自然排序也叫做默认排序。
&emsp;&emsp;方式二：容器具备比较性
&emsp;&emsp;&emsp;&emsp;当元素自身不具备比较性，或者自身具备的比较性不是所需要的。那么此时可以让容器自身具备。需要定义一个类实现接口Comparator，重写compare方法，并将该接口的子类实例对象作为参数传递给TreeSet集合的构造方法。
<font color=red>&emsp;&emsp;注意：</font>当Comparable比较方式和Comparator比较方式同时存在时，以Comparator的比较方式为主；
<font color=red>&emsp;&emsp;注意：</font>在重写compareTo或者compare方法时，必须要明确比较的主要条件相等时要比较次要条件。此时就需要进行次要条件判断。
&emsp;&emsp;问题:为什么使用TreeSet存入字符串,字符串默认输出是按升序排列的?
&emsp;&emsp;因为字符串实现了一个接口,叫做Comparable 接口.字符串重写了该接口的compareTo 方法,所以String对象具备了比较性.
&emsp;&emsp;那么同样道理,我的自定义元素(例如Person类,Book类)想要存入TreeSet集合,就需要实现该接口,也就是要让自定义对象具备比较性.
&emsp;&emsp;TreeSet集合排序的两种方式：
&emsp;&emsp;一，让元素自身具备比较性。
&emsp;&emsp;元素需要实现Comparable接口，覆盖compareTo 方法。这种方式也作为元素的自然排序，也可称为默认排序。
&emsp;&emsp;年龄按照搜要条件，年龄相同再比姓名。
```
public class Demo4 {
    public static void main(String[] args) {
        TreeSet ts = new TreeSet();
        ts.add(new Person("aa", 20, "男"));
        ts.add(new Person("bb", 18, "女"));
        ts.add(new Person("cc", 17, "男"));
        ts.add(new Person("dd", 17, "女"));
        ts.add(new Person("dd", 15, "女"));
        ts.add(new Person("dd", 15, "女"));
        System.out.println(ts);
        System.out.println(ts.size()); // 5
    }
}
class Person implements Comparable {
    private String name;
    private int age;
    private String gender;
    public Person() {
    }
    public Person(String name, int age, String gender) {
this.name = name;
        this.age = age;
        this.gender = gender;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    @Override
    public int hashCode() {
        return name.hashCode() + age * 37;
    }
    public boolean equals(Object obj) {
        System.err.println(this + "equals :" + obj);
        if (!(obj instanceof Person)) {
            return false;
        }
        Person p = (Person) obj;
        return this.name.equals(p.name) && this.age == p.age;
    }
    public String toString() {
        return "Person [name=" + name + ", age=" + age + ", gender=" + gender
                + "]";
    }
    @Override
    public int compareTo(Object obj) {
        Person p = (Person) obj;
        System.out.println(this+" compareTo:"+p);
        if (this.age > p.age) {
            return 1;
        }
        if (this.age < p.age) {
            return -1;
        }
        return this.name.compareTo(p.name);
    }
}
```
&emsp;&emsp;二，让容器自身具备比较性，自定义比较器。
&emsp;&emsp;需求：当元素自身不具备比较性，或者元素自身具备的比较性不是所需的。那么这时只能让容器自身具备。
<font color=red>&emsp;&emsp;定义一个类实现Comparator 接口，覆盖compare方法。并将该接口的子类对象作为参数传递给TreeSet集合的构造函数。</font>
&emsp;&emsp;当Comparable比较方式，及Comparator比较方式同时存在，以Comparator比较方式为主。
```
public class Demo5 {
    public static void main(String[] args) {
        TreeSet ts = new TreeSet(new MyComparator());
        ts.add(new Book("think in java", 100));
        ts.add(new Book("java 核心技术", 75));
        ts.add(new Book("现代操作系统", 50));
        ts.add(new Book("java就业教程", 35));
        ts.add(new Book("think in java", 100));
        ts.add(new Book("ccc in java", 100));
        System.out.println(ts);
    }
}
class MyComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        Book b1 = (Book) o1;
        Book b2 = (Book) o2;
        System.out.println(b1+" comparator "+b2);
        if (b1.getPrice() > b2.getPrice()) {
            return 1;
        }
        if (b1.getPrice() < b2.getPrice()) {
            return -1;
        }
        return b1.getName().compareTo(b2.getName());
    }
}
class Book {
    private String name;
    private double price;
    public Book() {
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public Book(String name, double price) {
this.name = name;
        this.price = price;
    }
    @Override
    public String toString() {
        return "Book [name=" + name + ", price=" + price + "]";
    }
}
```
四、LinkedHashSet  
&emsp;&emsp;会保存插入的顺序
