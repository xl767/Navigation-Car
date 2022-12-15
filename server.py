import socket
import time
import RPi.GPIO as GPIO
import io
import picamera
import logging
import socketserver
from threading import Condition
from http import server
import threading

PAGE="""\
<html>
<header>
<title>Raspberry Pi Camera Streaming To Phone</title>
</header>
<body>
<h4>Raspberry Pi Live Streaming</h4>
<img src="stream.mjpg" width="320" height="240" />
</body>
</html>
"""

class StreamingOutput(object):
    def __init__(self):
        self.frame = None
        self.buffer = io.BytesIO()
        self.condition = Condition()

    def write(self, buf):
        if buf.startswith(b'\xff\xd8'):
            # New frame, copy the existing buffer's content and notify all
            # clients it's available
            self.buffer.truncate()
            with self.condition:
                self.frame = self.buffer.getvalue()
                self.condition.notify_all()
            self.buffer.seek(0)
        return self.buffer.write(buf)

class StreamingHandler(server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/':
            self.send_response(301)
            self.send_header('Location', '/index.html')
            self.end_headers()
        elif self.path == '/index.html':
            content = PAGE.encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'text/html')
            self.send_header('Content-Length', len(content))
            self.end_headers()
            self.wfile.write(content)
        elif self.path == '/stream.mjpg':
            self.send_response(200)
            self.send_header('Age', 0)
            self.send_header('Cache-Control', 'no-cache, private')
            self.send_header('Pragma', 'no-cache')
            self.send_header('Content-Type', 'multipart/x-mixed-replace; boundary=FRAME')
            self.end_headers()
            try:
                while True:
                    with output.condition:
                        output.condition.wait()
                        frame = output.frame
                    self.wfile.write(b'--FRAME\r\n')
                    self.send_header('Content-Type', 'image/jpeg')
                    self.send_header('Content-Length', len(frame))
                    self.end_headers()
                    self.wfile.write(frame)
                    self.wfile.write(b'\r\n')
            except Exception as e:
                logging.warning(
                    'Removed streaming client %s: %s',
                    self.client_address, str(e))
        else:
            self.send_error(404)
            self.end_headers()

class StreamingServer(socketserver.ThreadingMixIn, server.HTTPServer):
    allow_reuse_address = True
    daemon_threads = True

GPIO.setmode(GPIO.BCM)
GPIO.setup(6, GPIO.OUT)
GPIO.setup(5, GPIO.OUT)
GPIO.setup(20, GPIO.OUT)
GPIO.setup(12, GPIO.OUT)
GPIO.setup(21, GPIO.OUT)
GPIO.setup(18, GPIO.OUT)

GPIO.output(5, GPIO.LOW)
GPIO.output(20, GPIO.LOW)
GPIO.output(12, GPIO.LOW)
GPIO.output(21, GPIO.LOW)

f = 50
f2 = 50
dc = 78
dc2 = 60
p = GPIO.PWM(6,f)
p2 = GPIO.PWM(18, f2)
p.start(dc)
p2.start(dc2)
i = 0

def run():
    s = socket.socket()
    port = 5000
    s.bind(('', port))

    # put the socket into listening mode
    s.listen(5)
    print ("socket is listening")

    while True:
        c, addr = s.accept()
        print('Got connection from', addr)
        data = c.recv(1024).decode()
        if not data:
            continue
        else:
            msg = str(data)[0]
            print("from connected user: " + msg)
            if msg == "0":
                stop()
            elif msg == "1":
                forward()
            elif msg == "2":
                left()
            elif msg == "3":
                right()
            elif msg == "4":
                backward()
            else:
                print(msg)
                print("Unknown message")
            c.send('1'.encode())
        c.close()


def left():
    print("Turning left")
    GPIO.output(20, GPIO.HIGH)
    GPIO.output(5, GPIO.LOW)
    GPIO.output(12, GPIO.HIGH)
    GPIO.output(21, GPIO.LOW)

    p.ChangeDutyCycle(dc)
    p2.ChangeDutyCycle(dc2)

def right():
    print("Turning right")
    GPIO.output(20, GPIO.LOW)
    GPIO.output(5, GPIO.HIGH)
    GPIO.output(12, GPIO.LOW)
    GPIO.output(21, GPIO.HIGH)

    p.ChangeDutyCycle(dc)
    p2.ChangeDutyCycle(dc2)

def forward():
    print("Moving forward")
    GPIO.output(20, GPIO.HIGH)
    GPIO.output(5, GPIO.LOW)
    GPIO.output(12, GPIO.LOW)
    GPIO.output(21, GPIO.HIGH)

    p.ChangeDutyCycle(dc)
    p2.ChangeDutyCycle(dc2)


def backward():
    print("Moving backward")
    GPIO.output(20, GPIO.LOW)
    GPIO.output(5, GPIO.HIGH)
    GPIO.output(12, GPIO.HIGH)
    GPIO.output(21, GPIO.LOW)

    p.ChangeDutyCycle(dc)
    p2.ChangeDutyCycle(dc2)


def stop():
    print("stop")
    GPIO.output(20, GPIO.LOW)
    GPIO.output(5, GPIO.LOW)
    GPIO.output(12, GPIO.LOW)
    GPIO.output(21, GPIO.LOW)

if __name__ == "__main__":
    x = threading.Thread(target=run, daemon=True)
    x.start()
    with picamera.PiCamera(resolution='320x240', framerate=24) as camera:
        output = StreamingOutput()
        camera.start_recording(output, format='mjpeg')
        try:
            address = ('', 8000)
            server = StreamingServer(address, StreamingHandler)
            server.serve_forever()
        finally:
            camera.stop_recording()
    x.join()
