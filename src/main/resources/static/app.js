const ENDPOINT = "/api/v1/transcribe";

const button = document.getElementById("recordButton");
const status = document.getElementById("status");
const transcript = document.getElementById("transcript");

let state = "idle";
let recorder = null;
let stream = null;
let chunks = [];

button.addEventListener("click", () => {
    if (state === "idle") start();
    else if (state === "recording") recorder.stop();
});

async function start() {
    try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch (e) {
        status.textContent = "Microphone error: " + e.name;
        return;
    }

    chunks = [];
    recorder = new MediaRecorder(stream);
    recorder.ondataavailable = (e) => { if (e.data.size > 0) chunks.push(e.data); };
    recorder.onstop = upload;
    recorder.start();

    state = "recording";
    button.textContent = "Stop recording";
    status.textContent = "Recording. Speak now.";
}

async function upload() {
    stream.getTracks().forEach((t) => t.stop());

    state = "uploading";
    button.textContent = "Transcribing";
    button.disabled = true;
    status.textContent = "Transcribing your recording.";

    const blob = new Blob(chunks, { type: recorder.mimeType });
    const form = new FormData();
    form.append("audio", blob, "recording.webm");

    try {
        const res = await fetch(ENDPOINT, { method: "POST", body: form });
        const body = await res.json();
        if (res.ok) {
            transcript.textContent = body.text || "(empty)";
            status.textContent = "Done. Ready for another recording.";
        } else {
            status.textContent = "Error: " + (body.message || res.status);
        }
    } catch (e) {
        status.textContent = "Network error: " + e.message;
    }

    state = "idle";
    button.textContent = "Start recording";
    button.disabled = false;
}