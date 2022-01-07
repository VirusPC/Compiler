# Compiler

## 目录

- [概述](#概述)
- [语法支持](#语法支持)
- [组成部分](#组成部分)
- [目录结构](#目录结构)
- [使用方式](#使用方式)
- [随便聊聊](#随便聊聊)
- [使用教材](#使用教材)

---

## 概述

编译原理课程作业. 基于LR1文法的编译器, 将简单C语言语法转为X86汇编.

## 语法支持

1. 数字/字符串定义
2. if-else条件语句
3. while循环
4. 加减乘除数学运算
5. 输入输出

可参考`/src/resource/code/code.txt`

## 组成部分

由以下几部分组成:

* 词法分析: 普通自动机
* 语法分析: LR(1)语法分析
* 语义分析: 语法制导的语义分析, 语义子程序
* 中间代码生成: 四元式
* 目标代码生成: X86汇编
* 由正规文法构建确定的有穷自动机的算法: 用的求闭包的方法来通过正规文法来构建Action表和Goto表, 用这两个表来表达自动机. 有求Forward集.

## 目录结构

```
|-src
  |-assembly  中间代码生成X86汇编代码
  |-lexis  词法分析相关
    |- Lexer  词法分析器
    |- Word  分析后的存储单词数据结构
    |- 其他 保留字操作符等识别的单词种类
  |-syntax  语法分析, 语义分析, 中间代码生成相关
    |- Parser 语法分析器
    |- FourElement 四元式数据结构
    |- SematicNode 语义分析用的数据结构
    |- 其他
  |-resource  一些资源文件
    |-assembly  运行MainTest后生成的汇编的结果
    |-code  c语言源代码
    |-syntax  一些正规文法. 文件名后面的数字代表该文法出自课本上的第几章.
|-test  测试
  |-lexis  词法分析相关测试
  |-syntax  语法分析相关测试
  |-MainTest.java  通过运行它可以让整个项目跑起来 

```


## 使用方式

可以直接运行`/test/MainTest.java`来将整个编译器跑起来.

主要方法:

```java
// 词法分析. 需传入源代码文件的路径.
Lexer lexer = new Lexer("src/resource/code/semanticTestCode.txt");

// 由正规文法构建自动机. 第一个变量是路径, 第二个变量是正规文法的起始符. 注意正规文法要符合LR1文法的要求.
Parser parser = new Parser("src/resource/syntax/semanticTest.txt", "program");  

// 将分析好的单词流传给parser.
parser.parseWordStream(lexer.getWordStream());

// 将parser分析好的的四元式流和符号表传给asm, 用中间代码来生成汇编代码.
Asm asm = new Asm(parser.getFourElementList(), parser.getSymbolTable());

// 结果写入指定目录的指定文件中
asm.writeAsmToFile("src/resource/assembly", "objectCode.asm");
```

部分辅助方法:

```
// 打印分析后的单词流
lexer.printWordStream();

// 打印符号表
parser.printSymbolTable();

// 打印四元式列表(中间代码)
parser.printFourElementList();

// 打印结果        
asm.printtAsm();
```

## 随便聊聊

现在回过头来看, 这个项目的价值可能就在于实现了一些书上的算法, 并成功的它们硬凑成了一个编译器 :joy: ?

作为赶出来的作业, 这个项目存在很多问题, 包括但不限于:

1. 项目架构**非常**不合理 
2. LR1语法分析虽然看起来比LL系列高级, 但占用内存太高基本只用于学术界
3. 没有写出/找到合适的c语言的正规文法
4. 缺少代码优化部分
5. 支持语句过少
6. 不支持函数
7. 没考虑寄存器分配问题
8. ......

## 使用教材

- [编译原理（第3版）](http://www.tup.tsinghua.edu.cn/booksCenter/book_02631501.html)
