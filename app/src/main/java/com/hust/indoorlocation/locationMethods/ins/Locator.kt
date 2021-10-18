package com.hust.indoorlocation.locationMethods.ins

/**
 * xDistance :x方向的移动距离，单位为 cm
 * time :该移动发送经过的时间，单位为 ms
 * */
data class Locator(
    var xDistanceMm: Float,
    var yDistanceMm: Float,
    var timeMills: Float,
) {
    private var xyDes="m"
    private var tDes="s"
    private var type =1000F

    fun addDistance(aLocator: Locator){
        this.timeMills+=aLocator.timeMills
        this.xDistanceMm +=aLocator.xDistanceMm
        this.yDistanceMm+=aLocator.yDistanceMm
        //LogUtil.d(" x=$xDistanceCm , plus x=${aLocator.xDistanceCm}")
    }

    override fun toString(): String {
        return "Locator:\n"+
                "xDistance = ${xDistanceMm/type} $xyDes\n"+
                "yDistance = ${yDistanceMm/type} $xyDes\n"+
                "total = ${timeMills/type} $tDes"

    }
}