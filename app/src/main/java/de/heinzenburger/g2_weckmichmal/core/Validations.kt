package de.heinzenburger.g2_weckmichmal.core

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.PowerManager
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.api.routes.DBRoutePlanner
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import java.net.URL

class Validations(val core: Core) {
    fun isInternetAvailable(): Boolean {
        core.log(Logger.Level.INFO, "isInternetAvailable called")
        val connectivityManager = core.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: run {
            core.log(Logger.Level.INFO, "No active network")
            return false
        }
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
            core.log(Logger.Level.INFO, "No network capabilities")
            return false
        }
        val result = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return result

        core.log(Logger.Level.INFO, "isInternetAvailable result: $result")
        return result
    }

    fun isApplicationOpenedFirstTime(): Boolean {
        core.log(Logger.Level.INFO, "isApplicationOpenedFirstTime called")
        val settings = ApplicationSettingsHandler(core.context)
        val result = settings.isApplicationOpenedFirstTime()
        core.log(Logger.Level.INFO, "isApplicationOpenedFirstTime result: $result")
        return result
    }

    fun isValidCourseURL(urlString: String): Boolean {
        core.log(Logger.Level.INFO, "isValidCourseURL called with urlString: $urlString")
        if (!URLUtil.isValidUrl(urlString)) {
            core.log(Logger.Level.SEVERE, "Invalid URL format: $urlString")
            return false
        }
        try {
            RaplaFetcher(URL(urlString)).throwIfInvalidCourseURL()
        } catch (e: CourseFetcherException) {
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
            return false
        }
        core.log(Logger.Level.INFO, "Valid Course URL: $urlString")
        return true
    }

    fun validateConfiguration(configuration: Configuration): Boolean {
        core.log(Logger.Level.INFO, "validateConfiguration called with configuration: $configuration")
        var validation = true
        // Days should not be empty
        if(configuration.days.isEmpty()){
            core.log(Logger.Level.INFO, "Configuration days are empty")
            validation = false
        }
        if(configuration.startStation != null || configuration.endStation != null){
            // If one station is set but one is null, error
            if(configuration.startStation == null || configuration.endStation == null){
                core.log(Logger.Level.INFO, "One of the stations is null")
                validation = false
            }
            // Station exists if can be found in List of DB similar station names
            else if (!DBRoutePlanner().deriveValidStationNames(configuration.startStation!!).contains(configuration.startStation!!)
                || !DBRoutePlanner().deriveValidStationNames(configuration.endStation!!).contains(configuration.endStation!!)){
                core.log(Logger.Level.INFO, "Station names are not valid")
                validation = false
            }
        }
        core.log(Logger.Level.INFO, "validateConfiguration result: $validation")
        return validation
    }

    fun getGrantedPermissions(): List<String>{
        val result = mutableListOf<String>()
        val alarmManager = core.context.getSystemService(ALARM_SERVICE) as AlarmManager
        val powerManager = core.context.getSystemService(POWER_SERVICE) as PowerManager

        if(alarmManager.canScheduleExactAlarms()) result.add("Alarm")
        if(powerManager.isIgnoringBatteryOptimizations(core.context.packageName)) result.add("Battery")
        var allowNotifications = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allowNotifications = ContextCompat.checkSelfPermission(core.context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        if(allowNotifications) result.add("Notifications")

        return result
    }
}