import os
import re
import json
import queue
import socket
import struct
import secrets
import subprocess
import threading
import collections
from functools import wraps
from pathlib import Path

from flask import Flask, render_template, request, Response, session, redirect, jsonify, send_file

APP_DIR = Path(__file__).resolve().parent
REPO_DIR = APP_DIR.parent.parent
SERVER_DIR = REPO_DIR / "server"

app = Flask(__name__)
app.secret_key = os.environ.get("FLASK_SECRET_KEY", os.urandom(24))

PASSWORD_FILE = SERVER_DIR / "data" / ".console-password.txt"
CONSOLE_PASSWORD = os.environ.get("CONSOLE_PASSWORD")
if not CONSOLE_PASSWORD:
    if PASSWORD_FILE.exists():
        CONSOLE_PASSWORD = PASSWORD_FILE.read_text().strip()
    else:
        CONSOLE_PASSWORD = secrets.token_urlsafe(8)
        PASSWORD_FILE.parent.mkdir(parents=True, exist_ok=True)
        PASSWORD_FILE.write_text(CONSOLE_PASSWORD)

CONTAINER_NAME = "minecraft-paper-1.21.8"
RCON_HOST = "localhost"
RCON_PORT = 25575

log_deque = collections.deque(maxlen=500)
clients = []
log_lock = threading.Lock()


def get_rcon_password():
    props = SERVER_DIR / "data" / "server.properties"
    if not props.exists():
        return None
    for line in props.read_text().splitlines():
        if line.startswith("rcon.password="):
            return line.split("=", 1)[1].strip()
    return None


def rcon_packet(req_id, pkt_type, payload):
    body = struct.pack("<ii", req_id, pkt_type) + payload.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(body)) + body


def recv_exact(sock, n, timeout=5):
    sock.settimeout(timeout)
    data = b""
    while len(data) < n:
        chunk = sock.recv(n - len(data))
        if not chunk:
            break
        data += chunk
    return data


def rcon_send(command):
    password = get_rcon_password()
    if not password:
        raise RuntimeError("RCON password not found")
    sock = socket.create_connection((RCON_HOST, RCON_PORT), timeout=5)
    try:
        # auth
        sock.sendall(rcon_packet(1, 3, password))
        size = struct.unpack("<i", recv_exact(sock, 4))[0]
        resp = recv_exact(sock, size)
        if len(resp) < 10:
            raise RuntimeError("RCON auth response too short")
        req_id_back, pkt_type = struct.unpack("<ii", resp[:8])
        if req_id_back == -1:
            raise RuntimeError("RCON auth failed")
        # command
        sock.sendall(rcon_packet(2, 2, command))
        size = struct.unpack("<i", recv_exact(sock, 4))[0]
        resp = recv_exact(sock, size)
        if len(resp) < 10:
            return ""
        return resp[8:-2].decode("utf-8", errors="replace")
    finally:
        sock.close()


def tail_logs():
    try:
        proc = subprocess.Popen(
            ["docker", "compose", "-f", str(SERVER_DIR / "docker-compose.yml"), "logs", "-f", "minecraft"],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
        )
        for line in proc.stdout:
            with log_lock:
                log_deque.append(line.rstrip("\n"))
            with log_lock:
                for q in list(clients):
                        try:
                            q.put(line.rstrip("\n"), block=False)
                        except queue.Full:
                            pass
    except Exception as e:
        with log_lock:
            log_deque.append(f"[console] log tailer error: {e}")


threading.Thread(target=tail_logs, daemon=True).start()


def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not session.get("logged_in"):
            return jsonify({"error": "not logged in"}), 401
        return f(*args, **kwargs)
    return decorated


@app.route("/")
def index():
    if not session.get("logged_in"):
        return render_template("login.html")
    return render_template("console.html", password=CONSOLE_PASSWORD)


@app.route("/login", methods=["POST"])
def login():
    password = request.form.get("password", "").strip()
    if password == CONSOLE_PASSWORD:
        session["logged_in"] = True
        return redirect("/")
    return render_template("login.html", error="Senha incorreta"), 401


@app.route("/logout")
def logout():
    session.clear()
    return redirect("/")


@app.route("/api/status")
@login_required
def status():
    online = False
    try:
        out = subprocess.check_output(
            ["docker", "ps", "--filter", f"name={CONTAINER_NAME}", "--format", "{{.Status}}"],
            text=True,
            timeout=5,
        ).strip()
        online = out.startswith("Up")
    except Exception:
        pass

    player_text = ""
    if online:
        try:
            player_text = rcon_send("list")
        except Exception as e:
            player_text = str(e)

    return jsonify({
        "online": online,
        "address": "bore.digital:55001",
        "players": player_text,
    })


@app.route("/api/logs")
@login_required
def logs():
    def gen():
        q = queue.Queue(maxsize=200)
        with log_lock:
            clients.append(q)
            for line in log_deque:
                yield f"data: {json.dumps({'line': line})}\n\n"
        try:
            while True:
                line = q.get(timeout=30)
                yield f"data: {json.dumps({'line': line})}\n\n"
        except queue.Empty:
            pass
        finally:
            with log_lock:
                try:
                    clients.remove(q)
                except ValueError:
                    pass
    return Response(gen(), mimetype="text/event-stream")


@app.route("/api/command", methods=["POST"])
@login_required
def run_command():
    cmd = request.json.get("cmd", "").strip()
    if not cmd:
        return jsonify({"error": "empty command"}), 400
    try:
        output = rcon_send(cmd)
        return jsonify({"output": output})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/op/<player>", methods=["POST"])
@login_required
def op_player(player):
    try:
        output = rcon_send(f"op {player}")
        return jsonify({"output": output})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/deop/<player>", methods=["POST"])
@login_required
def deop_player(player):
    try:
        output = rcon_send(f"deop {player}")
        return jsonify({"output": output})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/kick/<player>", methods=["POST"])
@login_required
def kick_player(player):
    reason = request.json.get("reason", "") if request.json else ""
    try:
        output = rcon_send(f"kick {player} {reason}".strip())
        return jsonify({"output": output})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/restart", methods=["POST"])
@login_required
def restart_server():
    try:
        subprocess.Popen(
            ["docker", "compose", "-f", str(SERVER_DIR / "docker-compose.yml"), "restart"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        return jsonify({"output": "Reinicio solicitado"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/stop", methods=["POST"])
@login_required
def stop_server():
    try:
        output = rcon_send("stop")
        return jsonify({"output": output})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/tab-resourcepack.zip")
def tab_resourcepack():
    pack = SERVER_DIR / "data" / "plugins" / "oTalentzTab" / "tab-resourcepack.zip"
    if not pack.exists():
        return jsonify({"error": "resource pack not found"}), 404
    return send_file(pack, mimetype="application/zip", as_attachment=True, download_name="tab-resourcepack.zip")


if __name__ == "__main__":
    print(f"Console password: {CONSOLE_PASSWORD}")
    app.run(host="0.0.0.0", port=5000)
