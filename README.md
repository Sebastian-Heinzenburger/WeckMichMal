# G2-WeckMichMal

Dieses Android-Projekt wurde modular aufgebaut und folgt einer komponentenbasierten Architektur.

## 📁 Projektstruktur

Das Projekt sollte mit **Android Studio** geöffnet werden, um von der strukturierten “App”-Ansicht zu profitieren.

### 🧾 Dokumentation

Die Dokumentationsdateien befinden sich unter:
👉 [→ documents](./documents)
### 🧑‍💻 Anwendungscode

Der zentrale Anwendungscode liegt im folgenden Verzeichnis:
👉 [→ app/src/main/java/de/heinzenburger/g2\_weckmichmal](./app/src/main/java/de/heinzenburger/g2_weckmichmal)
Die dortige Ordnerstruktur bildet die funktional unabhängigen Komponenten ab, wie sie im [Komponentendiagramm](https://gitlab.com/dhbw-se/se-tinf23b2/G2-WeckMichMal/g2-weckmichmal/-/wikis/home/Architektur/Komponentendiagramm) definiert sind.

### 📐 Spezifikationen

Im folgenden Pfad befinden sich die Schnittstellen und Datenstrukturen, die zur Kommunikation zwischen den Komponenten dienen:
👉 [→ app/src/main/java/de/heinzenburger/g2\_weckmichmal/specifications](./app/src/main/java/de/heinzenburger/g2_weckmichmal/specifications)
Dort:
* definieren **Interfaces** die angebotenen Funktionalitäten,
* beschreiben **Datenstrukturen** die Kommunikationsdatentypen zwischen Komponenten.

Implementierungen dieser Spezifikationen befinden sich in den jeweiligen Komponentenordnern. Bei Änderungen der Anforderungen können die Specifications entsprechend angepasst werden.

### 🧪 Tests

Das Testverzeichnis findet sich unter:
👉 [→ app/src/test/java/de/heinzenburger/g2\_weckmichmal](./app/src/test/java/de/heinzenburger/g2_weckmichmal)
Es bildet die Struktur des Anwendungscodes ab und enthält Unit- und Funktionstests für testbare Komponenten.

### 🚀 Einstiegspunkt

Der Einstiegspunkt der App ist:
👉 [→ MainActivity.kt](./app/src/main/java/de/heinzenburger/g2_weckmichmal/MainActivity.kt)
Von hier wird die App gestartet und über den App Context wird vom UI aus das Backend aufgerufen.
