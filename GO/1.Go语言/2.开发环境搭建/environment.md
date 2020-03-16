<center>Go 开发环境</center>
####Go本地环境设置
+ 文本编辑器
+ Go编译器
####文本编辑器
Windows记事本，OS编辑命令，Brief，Epsilon，EMACS和vim(或vi)
使用编辑器创建的文件称为源文件，源文件中包含程序的源代码。Go程序的源文件通常使用扩展名“.go”来命名。
####Go编辑器
Go发行版本是FreeBSD(版本8及更高版本)，Linux，Mac OS X(Snow Leopard及更高版本)和具有32位(386)和64位(amd64)x86处理器架构的Windows操作系统的二进制安装版本。
####下载Go存档文件
从链接【[Go下载](https://golang.org/dl/ "Go存档文件")】中下载最新版本的Go可安装的归档文件。
操作系统    |    存档名称
-|-:
Windows     |   go1.7.windows-amd64.msi
Linux       |   go1.7.linux-amd64.tar.gz
Mac         |   go1.7.4.darwin-amd64.pkg
FreeBSD     |   go1.7.freebsd-amd64.tar.gz
####在UNIX/Linux/Mac OS X和FreeBSD上安装
将下载归档文件解压缩到/usr/local目录中，在/usr/local/go目录创建一个Go树。 例如：
`tar -C /usr/local -xzf go1.7.4.linux-amd64.tar.gz`
将/usr/local/go/bin添加到PATH环境变量。
操作系统    |    输出
-|-:
Linux       |   export PATH=$PATH:/usr/local/go/bin
Mac         |   export PATH=$PATH:/usr/local/go/bin
FreeBSD     |   export PATH=$PATH:/usr/local/go/bin
####在Windows上安装
使用MSI文件并按照提示安装Go工具。 默认情况下，安装程序使用C:\Go目录。安装程序应该在窗口的PATH环境变量中设置C:\Go\bin目录。重新启动后，打开的命令提示验证更改是否生效。
######验证安装效果
在F:\worksp\golang中创建一个test.go的go文件。编写并保存以下代码到 test.go 文件中。
```
package main

import "fmt"

func main() {
   fmt.Println("Hello, World!")
}
```
现在运行test.go查看结果并验证输出结果如下：
```
F:\worksp\golang>go run test.go
Hello, World!
```
