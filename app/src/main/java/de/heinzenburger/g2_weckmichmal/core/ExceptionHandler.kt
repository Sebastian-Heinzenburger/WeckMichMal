package de.heinzenburger.g2_weckmichmal.core

import android.content.Intent
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.ui.screens.LogScreen

class ExceptionHandler(val core: Core) {
    fun unexpectedException(e: Throwable, toastMessage: String, snitch: Boolean){
        core.log(Logger.Level.SEVERE, toastMessage)
        core.log(Logger.Level.SEVERE, e.message.toString())
        core.log(Logger.Level.SEVERE, e.cause?.message.toString())
        core.log(Logger.Level.SEVERE, e.stackTraceToString())
        core.showToast(toastMessage)
        if(snitch){
            try {
                val intent = Intent(core.context, LogScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                core.context.startActivity(intent)
            }
            catch (e: Exception){
                core.log(Logger.Level.SEVERE, "Kann nicht mal die Logs öffnen :(")
                core.log(Logger.Level.SEVERE, e.message.toString())
                core.log(Logger.Level.SEVERE, e.stackTraceToString())
                core.showToast("Kann nicht mal die Logs öffnen :(")
            }
        }
    }
    inline fun <T> runWithUnexpectedExceptionHandler(
        errorMessage: String = "Fehler",
        snitch: Boolean = false,
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            unexpectedException(e, errorMessage, snitch)
            null
        }
    }
}