import { spawn, spawnSync } from "node:child_process";
import { existsSync } from "node:fs";
import os from "node:os";
import path from "node:path";

const projectRoot = path.resolve(import.meta.dirname, "..");
const container = "irctc-admin-mysql";

const docker = spawnCaptured("docker", ["info"]);
const dockerInfo = await docker;
if (dockerInfo.error || dockerInfo.code !== 0) {
    fail("Docker Desktop is not running. Open Docker Desktop, wait until it says it is running, then run npm run dev again.");
}

console.log("Starting MySQL...");
const compose = await spawnCaptured("docker", ["compose", "up", "-d"], { cwd: projectRoot, quiet: false });
if (compose.code !== 0) {
    fail("MySQL did not start. Fix the Docker error above, then run npm run dev again.");
}

console.log("Waiting for MySQL...");
const ready = await waitForMysql();
if (!ready) {
    fail("MySQL did not become ready. In this folder, run: docker compose logs db");
}
console.log("MySQL is ready.");

const mvn = findMaven();
console.log("Compiling and starting the server...");
const server = spawn(`"${mvn}" -B -q compile exec:java`, {
    cwd: projectRoot,
    stdio: ["inherit", "pipe", "pipe"],
    shell: true
});
pipeServer(server.stdout);
pipeServer(server.stderr);

let stopped = false;
const stopServer = () => {
    if (stopped) {
        return;
    }
    stopped = true;
    if (server.exitCode === null && server.pid) {
        spawn(process.platform === "win32" ? "taskkill" : "kill",
            process.platform === "win32" ? ["/PID", String(server.pid), "/T", "/F"] : ["-TERM", String(server.pid)],
            { stdio: "ignore", shell: false });
    }
};
process.on("SIGINT", () => {
    stopServer();
    process.exit(0);
});
process.on("SIGTERM", () => {
    stopServer();
    process.exit(0);
});

server.on("error", () => {
    fail("Maven could not be started. Install Maven 3.9+ or keep the Maven 3.9.9 folder in %TEMP%.");
});
server.on("exit", (code) => {
    if (!stopped && code !== 0) {
        process.exit(code ?? 1);
    }
    process.exit(0);
});

function pipeServer(stream) {
    let pending = "";
    stream.on("data", (chunk) => {
        pending += chunk.toString();
        const lines = pending.split(/\r?\n/);
        pending = lines.pop() ?? "";
        for (const line of lines) {
            if (!line.trim() || isStartupNoise(line)) {
                continue;
            }
            console.log(line);
        }
    });
}

function isStartupNoise(line) {
    if (line.includes("IRCTC Admin Dashboard")) {
        return false;
    }
    if (/ERROR|SEVERE|Exception|Caused by|FAILURE|error:/.test(line)) {
        return false;
    }
    return line.startsWith("INFO:")
        || line.startsWith("WARNING:")
        || line.includes("org.apache.")
        || line.includes("TldScanner");
}

function findMaven() {
    const command = process.platform === "win32" ? "where.exe" : "which";
    try {
        const found = spawnSync(command, ["mvn"], { encoding: "utf8" });
        const first = found.stdout.split(/\r?\n/).map((line) => line.trim()).find(Boolean);
        if (found.status === 0 && first) {
            return first;
        }
    } catch {
        // Fall through to the local Maven copy.
    }
    const fallback = path.join(os.tmpdir(), "apache-maven-3.9.9", "bin", process.platform === "win32" ? "mvn.cmd" : "mvn");
    if (existsSync(fallback)) {
        return fallback;
    }
    fail("Maven was not found. Install Maven 3.9+ and add it to PATH, or keep the Maven 3.9.9 folder in %TEMP%.");
}

async function waitForMysql() {
    const deadline = Date.now() + 120_000;
    while (Date.now() < deadline) {
        const status = await spawnCaptured("docker", ["inspect", "-f", "{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}", container]);
        if (status.out.trim() === "healthy") {
            return true;
        }
        await delay(2000);
    }
    return false;
}

function spawnCaptured(command, args, options = {}) {
    return new Promise((resolve) => {
        const child = spawn(command, args, {
            cwd: options.cwd,
            shell: false,
            windowsHide: true,
            stdio: ["ignore", "pipe", "pipe"]
        });
        let out = "";
        child.stdout.on("data", (chunk) => {
            out += chunk;
            if (options.quiet === false) {
                process.stdout.write(chunk);
            }
        });
        child.stderr.on("data", (chunk) => {
            out += chunk;
            if (options.quiet === false) {
                process.stderr.write(chunk);
            }
        });
        child.on("error", (error) => resolve({ code: 1, out, error }));
        child.on("close", (code) => resolve({ code: code ?? 1, out }));
    });
}

function fail(message) {
    console.error(message);
    process.exit(1);
}

function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}
