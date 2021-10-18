package com.hust.indoorlocation.locationMethods.pdr

import android.util.Log
import com.hust.indoorlocation.ui.main.collector.consumer.ISensorEventConsumer
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CyclicBarrier

/**
 * 处理接收到的传感器事件
 * */
class SensorEventConsumerThread(
    val queue: BlockingQueue<DataEvent>,
    val barrier: CyclicBarrier,
    val consumers: List<ISensorEventConsumer>
) : Runnable {
    override fun run() {
        barrier.await()

        try {
            while(!Thread.currentThread().isInterrupted) {
                val event = queue.take()
                for(consumer in consumers)
                    consumer.consume(event)
            }

        } catch (e : InterruptedException) {
            Log.d("ConsumerThread", "consumer interrupted and will exit")
            e.printStackTrace()
        } finally {
            Log.d("ConsumerThread", "consumer is exiting")
            queue.clear()
        }
    }
}