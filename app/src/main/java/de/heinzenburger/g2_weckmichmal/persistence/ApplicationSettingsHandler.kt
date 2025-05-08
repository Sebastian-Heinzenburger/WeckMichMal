package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceApplicationSettings
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import org.json.JSONObject
import java.io.File

data class ApplicationSettingsHandler (
    val context: Context
) : InterfaceApplicationSettings{
    val logger = Logger(context)
    private fun toJson(settingsEntity: SettingsEntity) : JSONObject{
        var json = JSONObject()
        json.put("rapla", settingsEntity.raplaURL) //Only configuration so far
        return json
    }
    private fun fromJson(jsonObject: JSONObject) : SettingsEntity{
        var settingsEntity = SettingsEntity(
            raplaURL = jsonObject.get("rapla").toString()
        )
        return settingsEntity
    }
    override fun saveOrUpdateApplicationSettings(settings: SettingsEntity): Boolean {
        try {
            var json = toJson(settings)
            //context.filesDir is the apps personal data folder
            File(context.filesDir, "settings.json").writeText(json.toString())
            return true
        }
        catch (e : Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            return false
        }
    }
    override fun getApplicationSettings() : SettingsEntity{
        val json = if(isApplicationOpenedFirstTime()){
            toJson(SettingsEntity(raplaURL = ""))
        } else{
            JSONObject(File(context.filesDir,"settings.json").readText())
        }
        return fromJson(json)
    }
    override fun isApplicationOpenedFirstTime(): Boolean{
        //Application is considered opened first time if the configuration file doesn't yet exist
        return !File(context.filesDir, "settings.json").exists()
    }
}