package com.hust.indoorlocation.ui.main.collector.consumer


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.hust.indoorlocation.ui.main.collector.model.BleEvent
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import com.hust.indoorlocation.ui.main.collector.model.GeneratedEvent
import com.hust.indoorlocation.ui.main.collector.model.GeneratedType
import com.hust.indoorlocation.tools.util.SensorUtil.generated2misc
import com.hust.indoorlocation.tools.util.SensorUtil.sensor2misc
import okio.buffer
import okio.sink
import java.io.File

class WriteToFileConsumer(private val dataDir: File): ISensorEventConsumer {

    override fun consume(event: DataEvent) {
        writeDataEventToFile(event, dataDir)
    }

    companion object {

        fun createDataDir(dir: File) {
            if(!dir.exists())
                dir.mkdirs()
        }

        fun createFiles(sensorList: List<Sensor>, createBleFile: Boolean, dataDir: File) {
            for(misc in sensor2misc) {

                sensorList.forEach{
                    s->
                    if(misc.key == s.type) {
                    val file = File(dataDir, "${misc.value.name}.csv")
                    file.sink(append = true).buffer().use { sink ->
                        sink.writeUtf8(misc.value.firstLine)
                        sink.writeUtf8("\n")
                    }
                }
                }
            }

            // create file for ble scan result
            if(createBleFile) {
                val file = File(dataDir, "ble_ibeacon.csv")
                file.sink(append = true).buffer().use { sink ->
                    sink.writeUtf8("major,minor,rssi,timestamp\n")
                }
            }
        }

        fun createMetadata(meta: List<Pair<String, String>>, dataDir: File) {
            val fileMetadata = File(dataDir, "metadata.csv")
            fileMetadata.sink(append = true).buffer().use { sink ->
                for((idx, key) in meta.map { p -> p.first }.withIndex()) {
                    if(idx != 0) sink.writeUtf8(",")
                    sink.writeUtf8(key)
                }
                sink.writeUtf8("\n")
                for((idx, value) in meta.map { p -> p.second }.withIndex()) {
                    if(idx != 0) sink.writeUtf8(",")
                    sink.writeUtf8(value)
                }
            }
        }

        fun createGeneratedFiles(generatedList: List<GeneratedType>, dataDir: File) {
            val dir = File(dataDir, "generated")
            createDataDir(dir)

            for(gen in generatedList) {
                val misc = generated2misc[gen] ?: continue
                val file = File(dir, "${misc.name}.csv")
                if(file.exists()) continue
                file.sink(append = true).buffer().use { sink ->
                    sink.writeUtf8(misc.firstLine)
                    sink.writeUtf8("\n")
                }
            }
        }

        fun writeDataEventToFile(event: DataEvent, dataDir: File) {
//    Log.v("consumer", "got a sensor event")
            when(event.event) {
                is SensorEvent -> {
                    writeSensorEventToFile(event.event, dataDir)
                }
                is BleEvent -> {
                    writeBleEventToFile(event.event, dataDir)
                }
                is GeneratedEvent -> {
                    writeGeneratedEventToFile(event.event, dataDir)
                }
            }
        }


        fun writeSensorEventToFile(event: SensorEvent, dataDir: File) {
            if(event.sensor.type in sensor2misc.keys) {
                val misc = sensor2misc[event.sensor.type] ?: return
                val file = File(dataDir, "${misc.name}.csv")
                file.sink(append = true).buffer().use { sink ->
                    if(misc.size > 0) {
                        repeat(misc.size) { i ->
                            sink.writeUtf8("${event.values[i]}")
                            sink.writeUtf8(",")
                        }
                    }

                    sink.writeUtf8("${event.timestamp}")
                    sink.writeUtf8("\n")
                }
            } else {
                Log.w("writeToFile", "got a strange sensor event with type: ${event.sensor.type}(${event.sensor.name})")
            }
        }
        fun writeBleEventToFile(event: BleEvent, dataDir: File) {
            val file = File(dataDir, "ble_ibeacon.csv")
            file.sink(append = true).buffer().use { sink ->
                sink.writeUtf8("${event.major}")
                sink.writeUtf8(",")
                sink.writeUtf8("${event.minor}")
                sink.writeUtf8(",")
                sink.writeUtf8("${event.scanResult.rssi}")
                sink.writeUtf8(",")
                sink.writeUtf8("${event.scanResult.timestampNanos}")
                sink.writeUtf8("\n")
            }
        }
        fun writeGeneratedEventToFile(event: GeneratedEvent, dataDir: File) {
            if(event.genType in generated2misc.keys) {
                val misc = generated2misc[event.genType]?:return
                val file = File(dataDir, "generated/${misc.name}.csv")
                file.sink(append = true).buffer().use { sink ->
                    if(misc.size > 0) {
                        repeat(misc.size) { i ->
                            sink.writeUtf8("${event.data[i]}")
                            sink.writeUtf8(",")
                        }
                    }

                    sink.writeUtf8("${event.timestamp}")
                    sink.writeUtf8("\n")
                }
            } else {
                Log.w("writeToFile", "strange generated event type")
            }
        }
    }
}