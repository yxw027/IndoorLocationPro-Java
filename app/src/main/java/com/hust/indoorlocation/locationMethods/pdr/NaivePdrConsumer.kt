package com.hust.indoorlocation.locationMethods.pdr


import com.hust.indoorlocation.locationMethods.pdr.orientation.IOrientationDetector

import com.hust.indoorlocation.locationMethods.pdr.stride.IStrideStrategy
import com.hust.indoorlocation.locationMethods.pdr.step.IStepDetector
import com.hust.indoorlocation.ui.main.collector.consumer.ISensorEventConsumer
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import com.hust.indoorlocation.ui.main.collector.model.DataEvent.Companion.TYPE_GENERATED_EVENT
import com.hust.indoorlocation.ui.main.collector.model.DataEvent.Companion.TYPE_SENSOR_EVENT
import com.hust.indoorlocation.ui.main.collector.model.GeneratedEvent
import com.hust.indoorlocation.ui.main.collector.model.GeneratedType
import kotlin.math.cos
import kotlin.math.sin

class NaivePdrConsumer(
    //步数
    val stepDetector: IStepDetector,
    //方向
    val orientationDetector: IOrientationDetector,
    //跨步长度策略
    val strideLengthStrategy: IStrideStrategy,
    //传感器事件处理者
    val followingConsumers: List<ISensorEventConsumer>,
    val pdrCallback:PDRcallback,
): ISensorEventConsumer {
    private var x: Double = 0.0
    private var y: Double = 0.0

    override fun consume(event: DataEvent) {
        when(event.type) {
            TYPE_SENSOR_EVENT -> {

                consumeGeneratedEvent(stepDetector.updateWithDataEvent(event))
                consumeGeneratedEvent(orientationDetector.updateWithDataEvent(event))

                if(stepDetector.isStepDetected()) {
                    // update
                    val strideLength = strideLengthStrategy.getStrideLength()
                    val orientation = orientationDetector.getOrientation()
                    if(orientation.isEmpty()) return
                    // remap
                    val angle = Math.PI/2 - orientation[0]
                    val deltaX = strideLength * cos(angle)
                    val deltaY = strideLength * sin(angle)
                    x += deltaX
                    y += deltaY

                    // consume this movement
                    val genEvent = DataEvent(
                        TYPE_GENERATED_EVENT,
                        GeneratedEvent(
                            GeneratedType.Gen_Trajectory,
                            floatArrayOf(x.toFloat(), y.toFloat()),
                            stepDetector.lastDetectedTimestamp()
                        )
                    )
                    pdrCallback.movement(x,y)
                    consumeGeneratedEvent(genEvent)
                }
            }
        }
    }

    private fun consumeGeneratedEvent(event: DataEvent?) {
        if(event == null) return
        for(consumer in followingConsumers)
            consumer.consume(event)
    }

    companion object {
        val generatedList = listOf(
            GeneratedType.Gen_Step_Detector,
            GeneratedType.Gen_Rotation_Angles,
            GeneratedType.Gen_Trajectory
        )
    }

}