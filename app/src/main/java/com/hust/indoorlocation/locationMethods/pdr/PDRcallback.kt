package com.hust.indoorlocation.locationMethods.pdr


/***
 * 行人航位推算（Pedestrian Dead Reckoning，PDR）
 * 行人航位推算是一类利用每次步行的方向和步长计算行人当前位置的方法。
 * 通常，行人航位推算包含三个部分：步行检测、方向估计和步长估计。行人迈步时，
 * 加速度计和陀螺仪的信号往往呈现出周期性的特征，可以利用这类特征进行步行检测。
 * */
interface PDRcallback{
    //移动信息回调接口
    fun movement(x:Double,y:Double){

    }
}