package com.hust.indoorlocation.locationMethods.pdr.stride

class FixedStrideLengthStrategy(val length: Float): IStrideStrategy {
    override fun getStrideLength(): Float {
        return length
    }
}