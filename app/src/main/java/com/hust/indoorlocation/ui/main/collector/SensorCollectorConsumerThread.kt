package com.hust.indoorlocation.ui.main.collector

import android.hardware.SensorEvent
import android.util.Log
import com.hust.indoorlocation.ui.main.collector.consumer.WriteToFileConsumer
import com.hust.indoorlocation.ui.main.collector.model.BleEvent
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import com.hust.indoorlocation.ui.main.collector.model.GeneratedEvent
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CyclicBarrier

class SensorCollectorConsumerThread(
    private val queue: BlockingQueue<DataEvent>,
    private val barrier: CyclicBarrier,
    private val dataDir: File,
) : Runnable {

    companion object {
        const val TAG = "SensorEventConsumer"
    }

    override fun run() {
        barrier.await()

        try {
            while (!Thread.currentThread().isInterrupted) {
                val event = queue.take()
                writeDataEventToFile(event, dataDir)
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "consumer interrupted and will exit")
            e.printStackTrace()
        } finally {
            Log.d(TAG, "consumer is exiting")
            queue.clear()
        }
    }

    fun writeDataEventToFile(event: DataEvent, dataDir: File) {
//    Log.v("consumer", "got a sensor event")
        when(event.event) {
            is SensorEvent -> {
                WriteToFileConsumer.writeSensorEventToFile(event.event, dataDir)
            }
            is BleEvent -> {
                WriteToFileConsumer.writeBleEventToFile(event.event, dataDir)
            }
            is GeneratedEvent -> {
                WriteToFileConsumer.writeGeneratedEventToFile(event.event, dataDir)
            }
        }
    }
}