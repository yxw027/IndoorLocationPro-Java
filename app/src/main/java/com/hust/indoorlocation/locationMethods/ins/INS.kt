package com.hust.indoorlocation.locationMethods.ins


/********
 * 惯性导航系统（Inertial Navigation System，INS）
 * 惯性导航系统基于经典动力学，通过对加速度进行两次积分来计算行人的位移。加速度传感器的数据带有误差，
 * 导致INS的定位误差随时间以三次方的速度积累，在实践中无法使用。
 * */
interface INS {
}