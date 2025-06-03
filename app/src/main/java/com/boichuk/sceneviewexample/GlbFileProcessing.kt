package com.boichuk.sceneviewexample

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GlbFileProcessing {
    private val fileName = "Schwimmhalle.glb"
    private val decodedJsonFileName = "glb_output_Schwimmhalle.json"

    fun getGlbFile(context: Context): File = File(context.filesDir, fileName)

    // TODO MB extract extras
    fun getFileJson(context: Context): JsonObject {
        val bytes = getGlbFile(context).readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val json = parseGlbBufferWithGson(buffer)
        saveParsedJson(context, json)
        return json
    }


    private fun saveParsedJson(context: Context, json: JsonObject) {
        Log.d(this::class.simpleName, json.toString())
        val gblOutput = File(context.filesDir, decodedJsonFileName)
        gblOutput.writeText(json.toString())
    }

    private fun parseGlbBufferWithGson(buffer: ByteBuffer): JsonObject {
        val gson = Gson()
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(12) // Skip GLB header: magic(4) + version(4) + length(4)
        // Read JSON chunk
        val jsonLength = buffer.int
        val jsonType = buffer.int
        require(jsonType == 0x4E4F534A) { "Expected JSON chunk (type 'JSON' in ASCII)" }
        val jsonBytes = ByteArray(jsonLength)
        buffer.get(jsonBytes)
        val jsonString = String(jsonBytes, Charsets.UTF_8)
        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
        return jsonObject
    }
}




