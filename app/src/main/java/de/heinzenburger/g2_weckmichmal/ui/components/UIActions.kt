package de.heinzenburger.g2_weckmichmal.ui.components

import android.content.Context
import android.content.Intent
import de.heinzenburger.g2_weckmichmal.Core

/*
Schön wäre es gewesen, wenn man einmalig irgendwo den Core instanziiert und dann dauernd mit dieser Instanz arbeitet, aber
das stellt sich als schwer heraus.
Android UI funktioniert mit Context. Der Context beschreibt die momentan im Vordergrund stehende Komponente.
Soll eine neue Komponente im Vordergrund stehen, wird der Context weiter gereicht. Aus diesem Grund darf der Context
nie irgendwo hängen bleiben, wie zum Beispiel als statische Variable. Das Problem bei unserer Architektur ist, dass sogar die
tiefsten Komponenten wie die Persistenz einen Context brauchen um zu funktionieren. Deswegen braucht unser Core diesen Context.
Man kann den Core also nicht einmalig instanziieren und dann in eine statische variable klatschen.
Die zweite Option wäre die Instanz des Cores einfach immer an die Komponente im Vordergrund weiter zu reichen. Aber auch
das krieg ich nicht hin. Also instanziieren wir momentan den Core jedes Mal neu wenn ein neuer Screen aufgerufen wird. Vielleicht
ist das auch besser so
 */

data class UIActions(
    val context: Context
) {
    val core : Core = Core(context)

    fun setWelcomeScreen(){
        var intent = Intent(context, WelcomeScreen::class.java)
    }

    fun setRaplaURL(url : String) {
        core.saveRaplaURL(url)
    }
}