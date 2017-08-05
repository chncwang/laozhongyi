老中医是基于网格搜索的自动调参程序
# 为什么用老中医调参
* 调参中的一些步骤高度机械化，比如分别为超参数设置一堆不同的值，命名一堆log文件，起一堆后台进程，查看、比较log文件的结果等，手动操作既费时，又容易出错。
* 充分利用计算资源，睡觉时也在调参
* 老中医要求被调参的程序在训练集数据表现足够好时停止运行，养成不浪费计算资源的好习惯
* 现有的一些自动调参程序基于python，老中医基于字符串（比如对log文件的解析）
# 使用方法
## 调参范围的配置文件
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
