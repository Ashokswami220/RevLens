import http.server
import socketserver
import os
import mimetypes
import sys

PORT = 8088
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DIRECTORY = os.path.join(PROJECT_ROOT, "app", "build", "dist", "wasmJs", "productionExecutable")

if not os.path.exists(DIRECTORY):
    print(f"Error: Production build not found at {DIRECTORY}")
    print("Please run './gradlew wasmJsBrowserDistribution' first.")
    sys.exit(1)

mimetypes.add_type('application/wasm', '.wasm')
mimetypes.add_type('application/javascript', '.js')

class WasmHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def end_headers(self):
        self.send_header("Cross-Origin-Opener-Policy", "same-origin")
        self.send_header("Cross-Origin-Embedder-Policy", "require-corp")
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()

if __name__ == "__main__":
    os.chdir(DIRECTORY)
    # Allow port reuse immediately
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), WasmHandler) as httpd:
        print(f"🚀 RevLens WebAssembly running at: http://localhost:{PORT}")
        print("Press Ctrl+C to stop.")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nServer stopped.")
