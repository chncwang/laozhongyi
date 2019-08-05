老中医是基于网格搜索的自动调参程序
# 为什么用老中医调参
* 调参中的一些步骤高度机械化，比如分别为超参数设置一堆不同的值，命名一堆log文件，起一堆后台进程，查看、比较log文件的结果等，手动操作既费时，又容易出错。
* 充分利用计算资源，睡觉时也在调参
* 老中医要求被调参的程序在训练集数据表现足够好时停止运行，养成不浪费计算资源的好习惯
* 现有的一些自动调参程序基于python，老中医基于字符串（比如对log文件的解析）
# 使用说明
本项目基于Java 8和Maven，可以从源代码运行`mvn clean package`生成`target`文件夹，也可以往[releases](https://github.com/chncwang/laozhongyi/releases)页面下载。
## 调参范围配置文件样例
**laozhongyi.config**：
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

**超参数配置文件样例：**
**hyper.config**

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
## 运行参数：
```
usage: laozhonghi
 -c <arg>          cmd
 -rt <arg>         program runtime upper bound in minutes
 -s <arg>          scope file path
 -sar <arg>        simulated annealing ratio
 -sat <arg>        simulated annealing initial temperature
 -strategy <arg>   base or sa or vsa
 -wd <arg>         working directory
 -pc <arg>         number of parallel processe
 ```
* `-c`表示运行程序时的命令，需要加引号，如`"python3 train.py -train train.txt -dev dev.txt -test test.txt -hyper {}"`，`{}`将在老中医运行时被替换成超参数配置文件`hyper.config`的路径
 
 * `-rt`表示每个进程运行时间的上限，单位是分钟，比如-rt 20表示一个进程如果在运行20分钟后，仍没有结束运行（比如因为训练集仍然没有足够好的拟合），则强制结束
 
 * `-s`表示调参范围的配置文件的路径
 
 * `-strategy`表示搜索策略，取值为`base`时是坐标下降法，`sa`时是模拟退火，`sar`是模拟退火算法中温度衰减的比率，`sat`是初始温度，`vsa`是一种模拟退火的变种策略
 
 * `-wd`表示进程的工作目录，这允许被调参程序内使用相对路径，可选

 * `-pc`表示并行的进程数

## 运行

```Bash
git clone https://github.com/chncwang/laozhongyi.git
cd laozhongyi
mvn clean package
cd target
java -cp "*:lib/*" com.hljunlp.laozhongyi.Laozhongyi -s /home/user/laozhongyi.config\
 -c "python3 train.py -train train.txt -dev dev.txt -test test.txt -hyper {}" -sar 0.9 -sat 1 -strategy sa -rt 5 -pc 4
```

**推荐在screen下跑，不推荐用类似 `> log 2>&1 &`这样的命令跑**（容易跑着跑着进程就没了，原因不明，感觉是跑jar包都有的问题，知道原因的大神请告诉我^_^）

程序启动时会在home目录生成带有时间戳后缀的log目录和超参数配置文件目录
# 功能介绍
## 进程管理
* 老中医支持多进程调参，目前最多8进程
* 进程需要自己能停止运行，在达到运行时间上限时会被kill
## 搜索策略
### 坐标下降法
每次对某种超参数，取一组候选值中表现最好的值，往复循环，直至收敛
### 模拟退火
为了解决坐标下降法太容易收敛到局部最优解，引入了模拟退火的策略
### 变种模拟退火
模拟退火策略每次只会移动到表现最好的点，这个策略允许以一定概率移动到所有的点，但是会随着温度的降低收敛于最好的点，详见VariantSimulatedAnnealing.java

这两种模拟退火都会在收敛时，将历史表现最好的点作为起点再次搜索，但不恢复当时的温度。
# Question?
Email: chncwang@gmail.com

[黑龙江大学NLP实验室](https://nlp.heida.me/)
