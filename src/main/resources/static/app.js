const ENDPOINT = "/api/v1/transcribe";

const button = document.getElementById("recordButton");
const status = document.getElementById("status");
const transcript = document.getElementById("transcript");
const label = document.getElementById("recordLabel");

let state = "idle";
let recorder = null;
let stream = null;
let chunks = [];
let timer = null;
let startTime = 0;

// All UI updates
function setState(newState, message) {
  state = newState;

  if (state === "recording") {
    label.textContent = "Stop recording";
    button.setAttribute("aria-pressed", "true");
    button.disabled = false;
  } else if (state === "uploading") {
    label.textContent = "Transcribing";
    button.setAttribute("aria-pressed", "false");
    button.disabled = true;
  } else {
    label.textContent = "Start recording";
    button.setAttribute("aria-pressed", "false");
    button.disabled = false;
  }

  status.textContent = message;
}

// Recording timer
function startTimer() {
  startTime = Date.now();
  timer = setInterval(function () {
    const seconds = Math.floor((Date.now() - startTime) / 1000);
    status.textContent = "Recording. " + seconds + "s";
  }, 1000);
}

function stopTimer() {
  if (timer !== null) {
    clearInterval(timer);
    timer = null;
  }
}

function stopMicrophone() {
  if (stream !== null) {
    const tracks = stream.getTracks();
    for (let i = 0; i < tracks.length; i++) {
      tracks[i].stop();
    }
    stream = null;
  }
}
button.addEventListener("click", () => {
  if (state === "idle") {
    start();
  } else if (state === "recording") {
    recorder.stop();
  }
});

async function startRecording() {
  try {
    stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  } catch (e) {
    setState("idle", "Microphone error: " + e.name);
    return;
  }

  chunks = [];
  recorder = new MediaRecorder(stream);

  recorder.ondataavailable = function (e) {
    if (e.data.size > 0) {
      chunks.push(e.data);
    }
  };

  recorder.onstop = recordingStopped;
  recorder.start();

  startTimer();
  setState("recording", "Recording. Speak now.");
}

function stopRecording() {
  if (recorder !== null && recorder.state !== "inactive") {
    recorder.stop();
  }
  stopTimer();
}

async function recordingStopped() {
  stopMicrophone();

  const blob = new Blob(chunks, { type: recorder.mimeType });
  chunks = [];

  setState("uploading", "Transcribing your recording.");
  await upload(blob);
}

async function upload(blob) {
  const form = new FormData();
  form.append("audio", blob, "recording.webm");

  try {
    const res = await fetch(ENDPOINT, { method: "POST", body: form });
    const body = await res.json();

    if (res.ok) {
      transcript.textContent = body.text;
      setState("idle", "Complete. Ready for another recording.");
    } else {
      setState("idle", "Error: " + body.message);
    }
  } catch (e) {
    setState("idle", "Network error: " + e.message);
  }
}

button.addEventListener("click", function () {
  if (state === "idle") {
    startRecording();
  } else if (state === "recording") {
    stopRecording();
  }
});

setState("idle", "Ready.");
