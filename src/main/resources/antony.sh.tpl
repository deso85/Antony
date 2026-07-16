#!/bin/bash

# Antony Discord Bot - Start/Stop/Restart Script
# Dieses Script verwaltet den Bot-Prozess in einer Screen-Session.
#
# Verwendung:
#   ~/bin/antony.sh -c start -v <version>   # Startet den Bot
#   ~/bin/antony.sh -c stop                  # Stoppt den Bot
#   ~/bin/antony.sh -c restart -v <version> # Restartet den Bot
#   ~/bin/antony.sh -c status                # Zeigt Status an
#
# Konfiguration (umgebungsvariablen oder hier setzen):
#   ANTONY_SCREENNAME  - Name der Screen-Session (default: antonydiscordbot)
#   ANTONY_VERSION     - Version des Bots (default: 7.21.0)
#   ANTONY_CONFIG      - Pfad zur antony.cfg Konfigurationsdatei
#   ANTONY_JAVA_HOME   - Pfad zur Java-Installation
#   ANTONY_SRC_DIR     - Verzeichnis mit den Bot-JARs
#   ANTONY_LOG_DIR     - Verzeichnis für Logs
#   ANTONY_LOCK_FILE   - Pfad zur Lockfile
#
# Installation:
#   1. Dieses Script nach ~/bin/antony.sh kopieren
#   2. Ausführungsrechte setzen: chmod +x ~/bin/antony.sh
#   3. Pfade anpassen oder Umgebungsvariablen setzen

# ============================================================
# Konfiguration
# ============================================================
SCREENNAME="${ANTONY_SCREENNAME:-antonydiscordbot}"
VERSION="${ANTONY_VERSION:-7.21.0}"
CONFIGFILE="${ANTONY_CONFIG:-/home/antony/etc/antony.cfg}"
JAVA_HOME="${ANTONY_JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
SRC_DIR="${ANTONY_SRC_DIR:-/home/antony/src}"
LOG_DIR="${ANTONY_LOG_DIR:-/home/antony/log}"
LOCK_FILE="${ANTONY_LOCK_FILE:-/tmp/antonydiscordbot.lock}"

# ============================================================
# Hilfsfunktionen
# ============================================================
log() {
    local level="$1"
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    mkdir -p "$LOG_DIR"
    echo "[$timestamp] [$level] $message" >> "$LOG_DIR/antony-script.log"
    echo "[$level] $message"
}

check_java() {
    if [ ! -x "$JAVA_HOME/bin/java" ]; then
        log "ERROR" "Java nicht gefunden: $JAVA_HOME/bin/java"
        return 1
    fi
    log "INFO" "Java gefunden: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
    return 0
}

check_screen() {
    if ! command -v screen &> /dev/null; then
        log "ERROR" "screen ist nicht installiert"
        return 1
    fi
    return 0
}

is_running() {
    screen -list | grep -q "$SCREENNAME"
}

acquire_lock() {
    if [ -f "$LOCK_FILE" ]; then
        local pid=$(cat "$LOCK_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            log "ERROR" "Lockfile existiert und Prozess $pid läuft noch"
            return 1
        else
            log "WARN" "Stale Lockfile entfernt (PID $pid nicht aktiv)"
            rm -f "$LOCK_FILE"
        fi
    fi
    echo $$ > "$LOCK_FILE"
    return 0
}

release_lock() {
    rm -f "$LOCK_FILE"
}

# ============================================================
# Hauptlogik
# ============================================================
command=start

while getopts c:v: flag
do
    case "${flag}" in
        c) command=${OPTARG};;
        v) VERSION=${OPTARG};;
    esac
done

case "$command" in
    start)
        # Voraussetzungen prüfen
        check_java || exit 1
        check_screen || exit 1

        # Neueste JAR finden
        YOUNGESTFILE=$(ls -t "$SRC_DIR"/*.jar 2>/dev/null | head -1 | xargs basename)
        if [ -z "$YOUNGESTFILE" ]; then
            log "ERROR" "Keine JAR-Datei in $SRC_DIR gefunden"
            exit 1
        fi

        # Bereits läuft?
        if is_running; then
            log "INFO" "Antony ($SCREENNAME) läuft bereits"
            exit 0
        fi

        # Lockfile erstellen
        acquire_lock || exit 1

        # Starten
        log "INFO" "Starte $YOUNGESTFILE (Version: $VERSION)"
        screen -AmdS "$SCREENNAME" \
            "$JAVA_HOME/bin/java" \
            -jar "$SRC_DIR/$YOUNGESTFILE" \
            -config="$CONFIGFILE" \
            >> "$LOG_DIR/antony-app.log" 2>&1

        # Kurze Wartezeit und prüfen
        sleep 2
        if is_running; then
            log "INFO" "Antony erfolgreich gestartet (PID: $(screen -list | grep "$SCREENNAME" | awk '{print $1}'))"
        else
            log "ERROR" "Antony konnte nicht gestartet werden"
            release_lock
            exit 1
        fi
        ;;

    stop)
        if is_running; then
            log "INFO" "Stoppe Antony ($SCREENNAME)"

            PID=$(screen -list | grep "$SCREENNAME" | awk -F. '{print $1}')

            if [ -n "$PID" ]; then
                log "INFO" "Kille Screen PID $PID"
                kill "$PID" 2>/dev/null
                sleep 2

                if kill -0 "$PID" 2>/dev/null; then
                    log "WARN" "Prozess lebt noch, sende SIGKILL"
                    kill -9 "$PID" 2>/dev/null
                fi
            fi

            sleep 1

            if is_running; then
                log "ERROR" "Antony konnte nicht gestoppt werden"
                exit 1
            else
                log "INFO" "Antony gestoppt"
            fi
        else
            log "INFO" "Antony ($SCREENNAME) läuft nicht"
        fi

        release_lock
        ;;

    restart)
        log "INFO" "Restart für Antony ($SCREENNAME) gestartet"

        nohup bash -c "
            sleep 1
            \"$0\" -c stop
            sleep 2
            \"$0\" -c start -v \"$VERSION\"
        " >> /tmp/antony_restart.log 2>&1 &

        log "INFO" "Restart wurde vollständig entkoppelt"
        exit 0
        ;;

    status)
        if is_running; then
            log "INFO" "Antony ($SCREENNAME) läuft"
            screen -list | grep "$SCREENNAME"
        else
            log "INFO" "Antony ($SCREENNAME) läuft nicht"
        fi
        ;;

    *)
        echo "Verwendung: $0 -c {start|stop|restart|status} [-v version]"
        echo ""
        echo "Optionen:"
        echo "  -c <command>  Befehl: start, stop, restart, status"
        echo "  -v <version>  Bot-Version (optional)"
        exit 1
        ;;
esac