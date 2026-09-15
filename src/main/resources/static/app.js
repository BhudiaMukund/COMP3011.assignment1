const ENDPOINT = "/api/v1/transcribe";
const UPLOAD_TIMEOUT_MS = 30000;

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
function setState(newState, message, isError) {
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

  if (isError === true) {
    status.className = "status error";
  } else {
    status.className = "status";
  }
}

// Check for mic related errors.
function checkSupported() {
  if (!window.isSecureContext) {
    return "The microphone needs a secure connection. Open this page over https or on localhost.";
  }
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    return "This browser does not support recording from the microphone.";
  }
  if (typeof MediaRecorder === "undefined") {
    return "This browser does not support audio recording.";
  }
  return null;
}

function microphoneError(e) {
  if (e.name === "NotAllowedError" || e.name === "SecurityError") {
    return "Microphone access blocked. Allow it in your browser's settings and try again.";
  }
  if (e.name === "NotFoundError") {
    return "No microphone found. Plug mic and try again.";
  }
  if (e.name === "NotReadableError") {
    return "The mic is being used by another app. Close it and try again.";
  }
  return "The microphone could not be started. Try again.";
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
  const problem = checkSupported();
  if (problem !== null) {
    setState("idle", problem, true);
    return;
  }
  try {
    stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  } catch (e) {
    setState("idle", microphoneError(e), true);
    return;
  }

  chunks = [];
  recorder = new MediaRecorder(stream);

  recorder.ondataavailable = function (e) {
    if (e.data.size > 0) {
      chunks.push(e.data);
    }
  };

  recorder.onerror = function () {
    stopMicrophone();
    stopTimer();
    setState("idle", "Recording stopped unexpectedly. Try again.", true);
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
  if (blob.size === 0) {
    setState("idle", "Nothing was recorded. Try again.", true);
    return;
  }
  chunks = [];

  setState("uploading", "Transcribing your recording.");
  await upload(blob);
}

async function upload(blob) {
  const form = new FormData();
  form.append("audio", blob, "recording.webm");

  // Avoid infinite transcribe.
  const controller = new AbortController();
  const timeout = setTimeout(function () {
    controller.abort();
  }, UPLOAD_TIMEOUT_MS);

  try {
    const res = await fetch(ENDPOINT, {
      method: "POST",
      body: form,
      signal: controller.signal,
    });

    if (!res.ok) {
      let message = "The server could not transcribe that recording.";
      try {
        const body = await res.json();
        if (body.message) {
          message = body.message;
        }
      } catch (e) {
      }
      setState("idle", message, true);
      return;
    }

    const body = await res.json();
    if (body.text && body.text.trim() !== "") {
      transcript.textContent = body.text;
    } else {
      transcript.textContent = "No speech was detected in that recording.";
    }
    setState("idle", "Done. Ready for another recording.");
  } catch (e) {
    if (e.name === "AbortError") {
      setState(
        "idle",
        "That took too long and was cancelled. Try a shorter recording.",
        true,
      );
    } else {
      setState(
        "idle",
        "Could not reach the server. Check your connection and try again.",
        true,
      );
    }
  }

  clearTimeout(timeout);
}

button.addEventListener("click", function () {
  if (state === "idle") {
    startRecording();
  } else if (state === "recording") {
    stopRecording();
  }
});

const startupProblem = checkSupported();
if (startupProblem === null) {
  setState("idle", "Ready.");
} else {
  setState("idle", startupProblem, true);
  button.disabled = true;
}
