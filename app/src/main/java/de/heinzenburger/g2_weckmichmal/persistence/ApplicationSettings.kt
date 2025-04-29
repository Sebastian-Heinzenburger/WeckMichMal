package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.specifications.I_ApplicationSettings
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import org.json.JSONObject
import java.io.File

data class ApplicationSettings (
    val context: Context
) : I_ApplicationSettings{
    private fun toJson(settingsEntity: SettingsEntity) : JSONObject{
        var json = JSONObject();
        json.put("rapla", settingsEntity.raplaURL);
        return json
    }
    private fun fromJson(jsonObject: JSONObject) : SettingsEntity{
        var settingsEntity = SettingsEntity(
            raplaURL = jsonObject.get("rapla").toString()
        );
        return settingsEntity
    }
    override fun saveOrUpdateApplicationSettings(settings: SettingsEntity): Boolean {
        try {
            var json = toJson(settings)
            File(context.filesDir, "settings.json").writeText(json.toString())
            return true
        }
        catch (e : Exception){
            MainActivity.log.severe(e.toString())
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
        return !File(context.filesDir, "settings.json").exists()
    }
}