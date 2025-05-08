package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Logger(
    val context: Context?
){
    enum class Level(
    ) { INFO(), WARNING(), SEVERE() }

    val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(AlarmConfiguration::class.java.name)
    fun log(level: Level, text: String) {
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        var modifiedText = LocalDateTime.now().format(formatter) + " " + text
        if (level == Level.INFO){
            logger.info(text)
            modifiedText = "INFO: $modifiedText"
        }
        else if (level == Level.WARNING) {
            logger.warning(text)
            modifiedText = "WARNING: $modifiedText"
        }
        else if (level == Level.SEVERE) {
            logger.severe(text)
            modifiedText = "SEVERE: $modifiedText"
        }

        if(context != null){
            val logFile = File(context.filesDir, "log")
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                }
                catch (e: IOException) {
                    logger.severe(e.message)
                    e.printStackTrace()
                }
            }
            try {
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(modifiedText)
                buf.newLine()
                buf.close()
            }
            catch (e: IOException) {
                logger.severe(e.message)
                e.printStackTrace()
            }
        }
    }
    fun getLogs() : String{
        if(context != null){
            var text = File(context.filesDir,"log").readText().split("\n").reversed().joinToString("\n")
            return text
        } else{
            return "Called getLogs without context"
        }
    }
}