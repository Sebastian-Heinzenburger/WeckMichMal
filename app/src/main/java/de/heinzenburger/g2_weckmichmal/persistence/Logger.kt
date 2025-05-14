package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
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

    val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(ConfigurationHandler::class.java.name)
    @Throws(PersistenceException.CreateLogFileException::class, PersistenceException.WriteLogException::class)
    fun log(level: Level, text: String) {
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
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
                    throw PersistenceException.CreateLogFileException(e)
                }
            }
            try {
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(modifiedText)
                buf.newLine()
                buf.close()
            }
            catch (e: IOException) {
                throw PersistenceException.WriteLogException(e)
            }
        }
    }
    @Throws(PersistenceException.ReadLogException::class)
    fun getLogs() : String{
        try{
            if(context != null){
                var text = File(context.filesDir,"log").readText().split("\n").reversed().joinToString("\n")
                return text
            } else{
                return "Called getLogs without context"
            }
        }
        catch (e: Exception){
            throw PersistenceException.ReadLogException(e)
        }
    }
}