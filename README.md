# IndoorLocation
IndoorLocation 程序使用说明书

开发平台：Android studio 4.2.1 
运行环境：Android 11.0 系统
开发语言：java(50%)+kotlin(50%) ; 程序主题框架使用Java编写,部分类使用Kotlin编写。

主要功能：
1.手机传感器数据显示，蓝牙Beacon信息显示。
2.传感器数据和蓝牙信号强度收集，保存为本地.csv文件。
3.分析传感器数据，利用PDR定位方法推算行人移动轨迹并显示在地图上。

![图片](https://user-images.githubusercontent.com/67481255/137683648-73163698-d7ee-4134-842d-828e3f517485.png)
程序包说明：
顶级目录 com.hust.indoorlocation
	base 保存有一些自定义的基类信息，方便继承后统一修改
	locationMethods 定位方法
		pdr  行人航位推算定位方法
			orientation 方向策略
			step 步数策略
			stride 步长策略
	tools 工具包，辅助代码实现
	ui  ui界面类
		main 程序主界面
		setting 设置界面
		graphs 折线图界面

![图片](https://user-images.githubusercontent.com/67481255/137683690-b3e19a15-624d-4767-a7fb-ae876acb0d01.png)
CollecterService后台服务：运行期间收集传感器和蓝牙信息，保存为.csv文件。
PdrService后台服务：调用NaivePdrConsumer分析传感器信息，将计算结果通过PDRcallback接口传递给PdrActivity。

