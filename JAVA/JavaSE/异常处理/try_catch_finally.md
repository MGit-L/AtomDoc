<center>try、catch、finally中的细节分析</center>
####例子one
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            return t;
        } catch (Exception e) {
            // result = "catch";
            t = "catch";
            return t;
        } finally {
            t = "finally";
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
首先程序执行try语句块，把变量t赋值为try，由于没有发现异常，接下来执行finally语句块，把变量t赋值为finally，然后return t，则t的值是finally，最后t的值就是finally，程序结果应该显示finally，但是实际结果为try。
用javap -verbose TryCatchFinally 来显示目标文件(.class文件)字节码信息。
编译出来的字节码部分信息，我们只看test方法，其他的先忽略掉
```
public static final java.lang.String test();
  Code:
   Stack=1, Locals=4, Args_size=0
   0:    ldc    #16; //String
   2:    astore_0
   3:    ldc    #18; //String try
   5:    astore_0
   6:    aload_0
   7:    astore_3
   8:    ldc    #20; //String finally
   10:    astore_0
   11:    aload_3
   12:    areturn
   13:    astore_1
   14:    ldc    #22; //String catch
   16:    astore_0
   17:    aload_0
   18:    astore_3
   19:    ldc    #20; //String finally
   21:    astore_0
   22:    aload_3
   23:    areturn
   24:    astore_2
   25:    ldc    #20; //String finally
   27:    astore_0
   28:    aload_2
   29:    athrow
  Exception table:
   from   to  target type
    8    13   Class java/lang/Exception
    8    24   any
   19    24   any
  LineNumberTable:
   line 5: 0
   line 8: 3
   line 9: 6
   line 15: 8
   line 9: 11
   line 10: 13
   line 12: 14
   line 13: 17
   line 15: 19
   line 13: 22
   line 14: 24
   line 15: 25
   line 16: 28

  LocalVariableTable:
   Start  Length  Slot  Name   Signature
     27      0    t       Ljava/lang/String;
     10      1    e       Ljava/lang/Exception;

  StackMapTable: number_of_entries = 2
   frame_type = 255 /* full_frame */
     offset_delta = 13
     locals = [ class java/lang/String ]
     stack = [ class java/lang/Exception ]
   frame_type = 74 /* same_locals_1_stack_item */
     stack = [ class java/lang/Throwable ]
```
首先看LocalVariableTable信息，这里面定义了两个变量 一个是t String类型,一个是e Exception 类型
接下来看Code部分
第[0-2]行，给第0个变量赋值“”，也就是String t=""；
第[3-6]行，也就是执行try语句块 赋值语句 ，也就是 t = "try";
第7行，重点是第7行，把第s对应的值"try"付给第三个变量，但是这里面第三个变量并没有定义,这个比较奇怪
第[8-10] 行，对第0个变量进行赋值操作，也就是t="finally"
第[11-12]行，把第三个变量对应的值返回
通过字节码，我们发现，在try语句的return块中，return 返回的引用变量（t 是引用类型）并不是try语句外定义的引用变量t，而是系统重新定义了一个局部引用t’，这个引用指向了引用t对应的值，也就是try ，即使在finally语句中把引用t指向了值finally，因为return的返回引用已经不是t ，所以引用t的对应的值和try语句中的返回值无关了。
####例子two
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            return t;
        } catch (Exception e) {
            // result = "catch";
            t = "catch";
            return t;
        } finally {
            t = "finally";
            return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这里稍微修改了 第一段代码，只是在finally语句块里面加入了一个 return t 的表达式。
按照第一段代码的解释，先进行try{}语句，然后在return之前把当前的t的值try保存到一个变量t'，然后执行finally语句块，修改了变量t的值，在返回变量t。
这里面有两个return语句，但是程序到底返回的是try 还是 finally。接下来我们还是看字节码信息
```
public static final java.lang.String test();
  Code:
   Stack=1, Locals=2, Args_size=0
   0:    ldc    #16; //String
   2:    astore_0
   3:    ldc    #18; //String try
   5:    astore_0
   6:    goto    17
   9:    astore_1
   10:    ldc    #20; //String catch
   12:    astore_0
   13:    goto    17
   16:    pop
   17:    ldc    #22; //String finally
   19:    astore_0
   20:    aload_0
   21:    areturn
  Exception table:
   from   to  target type
    9     9   Class java/lang/Exception
   16    16   any
  LineNumberTable:
   line 5: 0
   line 8: 3
   line 9: 6
   line 10: 9
   line 12: 10
   line 13: 13
   line 14: 16
   line 15: 17
   line 16: 20

  LocalVariableTable:
   Start  Length  Slot  Name   Signature
     19      0    t       Ljava/lang/String;
     6      1    e       Ljava/lang/Exception;

  StackMapTable: number_of_entries = 3
   frame_type = 255 /* full_frame */
     offset_delta = 9
     locals = [ class java/lang/String ]
     stack = [ class java/lang/Exception ]
   frame_type = 70 /* same_locals_1_stack_item */
     stack = [ class java/lang/Throwable ]
   frame_type = 0 /* same */
```
这段代码翻译出来的字节码和第一段代码完全不同，还是继续看code属性。
第[0-2]行、[3-5]行第一段代码逻辑类似，就是初始化t，把try中的t进行赋值try
第6行，这里面跳转到第17行，[17-19]就是执行finally里面的赋值语句，把变量t赋值为finally，然后返回t对应的值
我们发现try语句中的return语句给忽略。可能jvm认为一个方法里面有两个return语句并没有太大的意义，所以try中的return语句给忽略了，直接起作用的是finally中的return语句，所以这次返回的是finally。
####例子three
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = "catch";
            return t;
        } finally {
            t = "finally";
            // System.out.println(t);
            // return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这里面try语句里面会抛出 java.lang.NumberFormatException，所以程序会先执行catch语句中的逻辑，t赋值为catch，在执行return之前，会把返回值保存到一个临时变量里面t '，执行finally的逻辑，t赋值为finally，但是返回值和t'，所以变量t的值和返回值已经没有关系了，返回的是catch
####例子four
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = "catch";
            return t;
        } finally {
            t = "finally";
            return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这个和例2有点类似，由于try语句里面抛出异常，程序转入catch语句块，catch语句在执行return语句之前执行finally，而finally语句有return,则直接执行finally的语句值，返回finally
####例子five
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = "catch";
            Integer.parseInt(null);
            return t;
        } finally {
            t = "finally";
            //return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这个例子在catch语句块添加了Integer.parser(null)语句，强制抛出了一个异常。然后finally语句块里面没有return语句。继续分析一下，由于try语句抛出异常，程序进入catch语句块，catch语句块又抛出一个异常，说明catch语句要退出，则执行finally语句块，对t进行赋值。然后catch语句块里面抛出异常。结果是抛出java.lang.NumberFormatException异常。
####例子six
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = "catch";
            Integer.parseInt(null);
            return t;
        } finally {
            t = "finally";
            return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这个例子和上面例子中唯一不同的是，这个例子里面finally 语句里面有return语句块。try catch中运行的逻辑和上面例子一样，当catch语句块里面抛出异常之后，进入finally语句快，然后返回t。则程序忽略catch语句块里面抛出的异常信息，直接返回t对应的值 也就是finally。方法不会抛出异常。
####例子七
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (NullPointerException e) {
            t = "catch";
            return t;
        } finally {
            t = "finally";
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这个例子里面catch语句里面catch的是NPE异常，而不是java.lang.NumberFormatException异常，所以不会进入catch语句块，直接进入finally语句块，finally对s赋值之后，由try语句抛出java.lang.NumberFormatException异常。
#####例子eight
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";
            Integer.parseInt(null);
            return t;
        } catch (NullPointerException e) {
            t = "catch";
            return t;
        } finally {
            t = "finally";
            return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
和上面的例子中try catch的逻辑相同，try语句执行完成执行finally语句，finally赋值s 并且返回s ，最后程序结果返回finally。
####例子nine
```
public class TryCatchFinally {
    @SuppressWarnings("finally")
    public static final String test() {
        String t = "";

        try {
            t = "try";return t;
        } catch (Exception e) {
            t = "catch";
            return t;
        } finally {
            t = "finally";
            String.valueOf(null);
            return t;
        }
    }

    public static void main(String[] args) {
        System.out.print(TryCatchFinally.test());
    }
}
```
这个例子中，对finally语句中添加了String.valueOf(null), 强制抛出NPE异常。首先程序执行try语句，在返回执行，执行finally语句块，finally语句抛出NPE异常，整个结果返回NPE异常。
###总结
try、catch、finally语句中，在如果try语句有return语句，则返回的之后当前try中变量此时对应的值，此后对变量做任何的修改，都不影响try中return的返回值
1、如果finally块中有return 语句，则返回try或catch中的返回语句忽略。
2、如果finally块中抛出异常，则整个try、catch、finally块中抛出异常
**注意**
1、尽量在try或者catch中使用return语句。通过finally块中达到对try或者catch返回值修改是不可行的。
2、finally块中避免使用return语句，因为finally块中如果使用return语句，会显示的消化掉try、catch块中的异常信息，屏蔽了错误的发生。
3、finally块中避免再次抛出异常，否则整个包含try语句块的方法回抛出异常，并且会消化掉try、catch块中的异常。
