# G2-WeckMichMal
In einer zunehmend digitalisierten Welt, in der sich Mobilität und Flexibilität stetig weiterentwickeln, wird Zeitmanagement immer wichtiger – insbesondere für Studierende, die täglich zwischen Vorlesungen, Terminen und dem Pendelverkehr jonglieren müssen. Hier setzt die Android-App WeckMichMal an, die klassische Weckfunktionen mit intelligenten, nutzerorientierten Datenquellen kombiniert, um den Alltag von Studierenden effizienter zu gestalten.
## Zieldefinition
Ziel des Projekts ist die Entwicklung einer Android-basierten Wecker-App, die klassische Weckfunktionen mit zusätzlichen, auf den Nutzer abgestimmten Datenquellen kombiniert. Dazu zählen insbesondere der Stundenplan der DHBW Karlsruhe (Rapla) und die entsprechenden Bahnverbindungen (DB Navigator/DB API). Die Anwendung soll es Studierenden ermöglichen, basierend auf ihrem individuellen Stundenplan und den relevanten Bahnverbindungen automatisch zu einem optimalen Zeitpunkt geweckt zu werden.
## Softwareanforderungen (Lastenheft)
### 🔴 Primäranforderungen
- Muss die Funktionalitäten eines Weckers abbilden:
    - Weckton an einem Aufwachzeitpunkt
    - Die aktuelle Uhrzeit anzeigen können
    - Applikation soll im Hintergrund des Android Gerätes laufen können
- Der Aufwachzeitpunkt soll anhand folgender Faktoren bestimmt werden:
    - Vorlesungsbeginn oder manuell gesetzte Uhrzeit
    - Die vom Benutzer eingestellte Bahnverbindungsstrecke im Regionalverkehr
    - Ein vom Benutzer eingestellter Puffer
- Der Benutzer muss folgende Präferenzen einstellen können:
    - Vorlesungsplan (Rapla):
        - URL
    - Bahnverbindungsstrecke (DB Navigator):
        - Start- und Endstation
    - Persönlicher Puffer (vor Fahrtantritt und vor dem Vorlesungbeginn)
### 🟡 Sekundäranforderungen
- Der Benutzer soll folgende zusätzliche Präferenzen einstellen können:
    - Vorlesungplan (Rapla):
        - Studiengangsleiter + Kursbezeichnung
    - Bahnverbindungsstrecke (DB Navigator):
        - Puffer zwischen Umstiegen
        - Es können mehrere Start- und Enstationen ausgewählt werden
- Der Benutzer soll folgende Informationen angezeigt bekommen können:
    - Die zwei besten Bahnverbindungen
    - Die für den Tag vorgesehenen Vorlesungen
### 🟢 Tertiäranforderungen
- Es wird ein entsprechender Hardware-Bausatz (Rasberry-Pi + Bauanleitung) entwickelt
- Der Benutzer kann folgende zusätzliche Informationen angezeigt bekommen:
    - Das aktuelle Kantinenmenü
### § Annahmen und Zusagen
Die Applikation sagt zu einen idealen Aufwachzeitpunkt zu bestimmen, unter der Prämisse, dass die von der DB Navigator zur Verfügung gestellten Daten korrekt sind.
## Abnahmekriterien
1. Erstmaliges in Betrieb nehmen\
    *Ausgangssituation*:\
    Der Nutzer hat die App erfolgreich installiert.\
    *Ereignis:*\
    Der Nutzer öffnet die App zum ersten mal.\
    *Reaktion:*\
    Der Nutzer wird auf eine Einrichtungsseite/Einstellungsseite geleitet und muss dort seine Präferenzen initial festlegen. Zwingend erforderlich ist die Verknüpfung mit seinem Vorlesungsplan (Rapla). 
2. Erstellen der Weckers\
    *Ausgangssituation*\
    Der Nutzer hat die App bereits eingerichtet.\
    *Ereignis*\
    Der Nutzer klickt auf ein + um einen neuen Wecker zu erstellen.\
    *Reaktion*\
    Der Nutzer muss folgendes einstellen:
    - spezifischen Puffer (standardmäßig auf 30 min)
    - Start- und Endstation (Endstation standardmäßig auf DHBW Kalrsruhe gesetzt)
    - die Wochentage, an denen der Wecker aktiv sein soll
    - Puffer vor dem Vorlesungsbeginn
3. Reguläre Nutzung\
    *Ausgangssituation*\
    Die App wurde bereits eingerichtet und alle Präferenzen, sowie mindestens ein Wecker für den aktuellen Tag ist gesetzt.\
    *Ereignis*\
    Berechnete Weckuhrzeit wurde erreicht.\
    *Reaktion*\
    Der Wecker klingelt und zeigt folgende Informationen an:
    - Aktuelle Uhrzeit
    - Die selektierte(n) Bahnverbindung(en)
    - Die Vorlesungen des Tages
    Zusätzlich kann der Nutzer über eine Interaktion den Wecker beenden.
4. Wecker löschen\
    *Ausgangssituation*\
    Die App wurde bereits eingerichtet und es liegt mindesten ein Wecker vor.\
    *Ereignsi*\
    Der Nutzer wählt das Löschen eines Weckers aus.\
    *Reaktion*\
    Der Nutzer wird aufgefordert erneut zu bestätigen, dass er den Wecker löschen möchte. Danach wird der Wecker mitsamt seinen Präferenzen gelöscht.


## TODO
- Titel überarbeiten? "WakeMeUpBeforeYouNeedToGoGo" oder "WMUBY-N-TGG"?
- Workflows und statische Mock-Ups erstellen
- Pflichtenheft erstellen/verfeinern