# exchangeQuantity


````
公共线程{volume=3.845004252643309, number=0, lastNum=7, minNumber=6, totalCpu=0.04300000000000004, buyIfOk=false, time=1619337327019}

合格: 最近15分钟,三根5分k都是阳线并且有15根1分k中有10根阳线,并且15分钟涨幅大于0.5%,并且最近2小时k和三小时k都是阳线

volume: 最近这15根1分钟k线,成交量最大的这根阳线成交量是多少,单位人民币千万,美元汇率按6.4(不算正在发生这根)

number: 最近8小时内合格的次数

minNumber: 8小时内,连续的15根1分k中出现10根阳线的次数

lastNum: 最近这15根1分k出现了几根阳线

buyIfOk: 当前是否合格
````




#### 介绍
创建基础springboot项目

本程序为数字时代项目后端的第二部分
程序部署后先运行公共变量buyObject线程,然后再运行量化策略

#### 运行环境 本项目采用 jdk15.0.2    maven 3.6.3 进行开发
运行环境：   
jdk版本11以上  spring boot版本2.3.3.RELEASE
(2021年开发时用的jdk版本是15.0.2)

IDEA
 
File→Project Structure→Project→Project SDK 此处两个地方版本号  选择java11

File→Project Structure→Modules 此处一个地方的版本号：java11

File→Settings→Build,Execution,Deployment→Complier→Java Complier 此处两个地方版本号：java11

File→Settings→Build,Execution,Deployment→Build Tools→Maven→绑定Maven安装的根目录

注意：如果初次导入没有运行按钮，点File→Project Structure→Modules
然后点+号。选Import module from...选maven 一直点下一步完成

IDEA开启自动导包：
File→Settings→Editor→General→Auto Import→
把Add unambiguous imports on the fly
和Optimize imports on the fly 这两项打上对勾

# **运行逻辑/包含的部分**

1. /fileThread/FileThread{}类项目启动后运行 → 启动定时器方法获取行情和行情判断

2. /fileThread/FileThread{}类lineStop变量为线程存储器

3. 量化策略程序 /bankDancer/BankDancer{}类

4. /severiceThread/RunbankDancer{}类 具体的线程运行层,控制线程关闭

5. /controller层

    ① BuyIfObjectTest{}类 打印趋势判断函数    http://localhost:端口号/buIf
    
    ② StartPage{}类 传入参数,运行一个线程      http://localhost:端口号/start
    
    ③ CloseThresdPage{}类 传入参数,关闭一个线程    http://localhost:端口号/closeLine
    
    ④ login{} 登录类
    
    ⑤ register{} 注册类 

6. 数据库,一共四张表:用户表;  潜力币种表; solana钱包余额表(暂未开发) 

   ① 数据库地址  /resources/application.yml

   ② 数据库方法  /dao/目录下接口都是
   
   ③ 数据库方法的实现 /resources/mapper.coinMapper.xml 包含创建表格
   
   ④ 德鲁伊连接池配置 /config/DruidConfig
   
   ⑤ 数据库表对应的类 /domain

7. solana公链钱包相关 /core/  

# **运行逻辑**

用户注册页面 → 存储用户信息至数据库

绑定交易所key页面 → 存储到数据库

用户登录 → 判断uuid策略是否在运行 
        
          否 → 填写参数运行策略 
          
          是 →  显示停止策略按钮


数据库内潜力币种,后台添加


每个币种的现货状态,管理员手动在后台更高
    
    ① buy 检测用户有无持仓,如果没有则买入
    
    ② stop 停止检测
    
    ③ sell 检测用户有没有持仓,如果又则卖出


暂时先不进行日志信息的打印

#### 作者微信:  yjkhaoyun

#### 加微进交流群

作者承接各种前后端需求项目
