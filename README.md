老中医是基于网格搜索的自动调参程序
# 为什么用老中医调参
* 调参中的一些步骤高度机械化，比如分别为超参数设置一堆不同的值，命名一堆log文件，起一堆后台进程，查看、比较log文件的结果等，手动操作既费时，又容易出错。
* 充分利用计算资源，睡觉时也在调参
* 老中医要求被调参的程序在训练集数据表现足够好时停止运行，养成不浪费计算资源的好习惯
* 现有的一些自动调参程序基于python，老中医基于字符串（比如对log文件的解析）
# 使用方法
## 调参范围的配置文件例子params.config：
```
adaAlpha,0.01,0.001,0.0001
wordEmbFineTune,true,false
wordCutOff,0,1,2,3,4,5,6,7,8,9,10
dropProb,-1,0.1,0.2,0.3,0.4,0.5
batchSize,1,2,4,8,16,32,64,90
hiddenSize,50,60,100
maxIter,1000
maxInstance,-1
outBest,.debug
wordFile,/home/wqs/w2v.txt
wordEmbSize,100
```
每一行是参数名跟一串要调试的参数值，用逗号隔开。如果这个参数只有一个值，则它是固定值，不被调试。
## 对被调参程序的要求
因为老中医基于字符串，所以和被调参程序使用的编程语言、深度学习框架解耦，可以是基于pytorch，tensorflow的python程序，也可以是基于[N3LDG](https://github.com/zhangmeishan/N3LDG)的C++程序
* 程序需要在训练集表现足够好时退出，否则程序会达到运行时间上限后被中止，这会降低调参程序的效率
* 程序需要打log的标准输出（老中医会把标准输出重定向到合适的log文件），log中需要有类似laozhongyi_0.8这样的字段，其中laozhongyi_是为了便于定位，0.8是程序在开发集上的表现
* 程序需要解析老中医输出的超参数配置文件，并且支持把超参数配置文件的路径作为程序运行参数

超参数配置文件的格式如下：
```
adaAlpha = 0.0001
batchSize = 16
dropProb = 0.1
hiddenSize = 50
maxInstance = -1
maxIter = 10
outBest = .debug
wordCutOff = 7
wordEmbFineTune = true
wordEmbSize = 100
wordFile = /home/wqs/w2v.txt
```
## 运行
项目基于Java 8和Maven，可以从源代码运行mvn clean package生成target文件夹，也可以往[releases](https://github.com/chncwang/laozhongyi/releases)页面下载。
运行时有以下参数：
```
usage: laozhonghi
 -c <arg>          cmd
 -rt <arg>         program runtime upper bound in minutes
 -s <arg>          scope file path
 -sar <arg>        simulated annealing ratio
 -sat <arg>        simulated annealing initial temperature
 -strategy <arg>   base or sa
 -wd <arg>         working directory
 ```
 -c表示运行程序时的命令，需要加引号，如"python3 train.py -train train.txt -dev dev.txt -test test.txt -hyper {}"，{}将在老中医运行时被替换成超参数配置文件的路径
 
 -rt表示每个进程运行时间的上限，单位是分钟，比如-rt 20表示一个进程如果在运行20分钟后，仍没有结束运行（比如因为训练集仍然没有足够好的拟合），则强制结束
 
 -s表示调参范围的配置文件的路径
 
 -strategy表示搜索策略，取值为base时是坐标下降法，sa时是模拟退火，sar是模拟退火算法中温度衰减的比率，sat是初始温度
 
 -wd表示进程的工作目录，这允许被调参程序内使用相对路径，可选
