<center>Go语言程序结构</center>
####示例
Go程序基本上由以下部分组成：
+ 软件包声明
+ 导入包
+ 函数(功能)
+ 变量
+ 语句与表达式
+ 注释
```
package main

import "fmt"

func main() {
   /* This is my first sample program. */
   fmt.Println("Hello, World!")
}
```
让我们来看看以上程序的各个部分：
+ 程序 `package main` 的第一行定义了程序应该包含的包名。它是一个<font color=red>必须</font>的语句，因为Go程序在包中运行。<font color=red>main包是运行程序的起点(入口点)</font>。每个包都有一个与之相关的路径和名称。
+ 下一行`import "fmt"`是一个预处理器命令，它告诉Go编译器包含位于包`fmt`中的文件。
+ 下一行`func main()`是程序执行开始的主函数。
+ 下一行`/*...*/`将被编译器忽略，并且已经在程序中添加了额外的注释。 所以这样的行称为程序中的注释。注释也使用`//`表示，类似于Java或C++注释。
+ 下一行`fmt.Println(...)`是Go中的另一个函数，它会产生消息“Hello，World！”。 以显示在屏幕上。这里`fmt`包已经导出`Println`方法，用于在屏幕上打印消息。
+ 注意`Println`方法的大写`P`。在Go语言中，如果以大写字母开头，则是导出的名称。导出意味着相应包装的输入者可以访问函数或变量/常数。
####执行Go程序
+ 打开文本编辑器并添加上述代码
+ 将文件另存为`hello.go`
+ 打开命令提示符，转到保存文件的目录
+ 键入`go run hello.go`，然后按Enter键运行代码
+ 如果代码中没有错误，那么将能够看到屏幕上打印的“Hello World”
```
$ go run hello.go
Hello, World!
```
####关键字
break    |    default   |   func    |   interface   |   select
-|:-:|:-:|:-:|-:
case     |    defer     |   go      |   map         |   struct
chan     |    else      |   goto    |   package     |   switch
const    |    fallthrought| if      |   range       |   type
continue |    for       |   import  |   return      |   var
