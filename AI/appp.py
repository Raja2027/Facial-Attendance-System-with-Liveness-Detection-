import os
import tempfile
import numpy as np
import cv2 as cv
import onnxruntime as ort
from flask import Flask, request, jsonify
from flask_cors import CORS

from app.real_time_loading.loading_via_video import FacenetVideo
from app.real_time_loading.Facenet import FaceLoading
from insightface.app import FaceAnalysis

# =========================
# LOAD INSIGHTFACE
# =========================
app_face = FaceAnalysis(
    name="buffalo_l",
    providers=['CPUExecutionProvider']
)
app_face.prepare(ctx_id=0)

# =========================
# LOAD ANTI-SPOOF MODEL
# =========================
spoof_session = ort.InferenceSession("models/best_model_quantized.onnx")
spoof_input_name = spoof_session.get_inputs()[0].name
spoof_input_shape = spoof_session.get_inputs()[0].shape

print("Anti-Spoof Model Input Shape:", spoof_input_shape)

def check_spoof(face_img):

    face = cv.resize(face_img, (128, 128))

    face = face.astype(np.float32)
    face = (face - 127.5) / 128.0  # 🔥 CORRECT normalization

    face = np.transpose(face, (2, 0, 1))
    face = np.expand_dims(face, axis=0)

    outputs = spoof_session.run(None, {spoof_input_name: face})
    logits = outputs[0][0]

    # Softmax
    exp_scores = np.exp(logits - np.max(logits))
    probs = exp_scores / exp_scores.sum()

    real_prob = float(probs[0])
    fake_prob = float(probs[1])

    print("Real Prob:", real_prob, "Fake Prob:", fake_prob)

    return real_prob, fake_prob
# =========================
# FLASK INIT
# =========================
app = Flask(__name__)
CORS(app)

video_loader = FacenetVideo()
image_loader = FaceLoading()

# ==============================
# IMAGE → EMBEDDING (Attendance)
# ==============================
@app.route('/generate_embedding', methods=['POST'])
def generate_embedding():
    try:
        if 'file' not in request.files:
            return jsonify({"error": "No file provided"}), 400

        file = request.files['file']
        filestr = file.read()
        npimg = np.frombuffer(filestr, np.uint8)
        img = cv.imdecode(npimg, cv.IMREAD_COLOR)

        if img is None:
            return jsonify({"error": "Failed to decode image"}), 400

        # 🔍 Detect face using InsightFace
        faces = app_face.get(img)

        if len(faces) == 0:
            return jsonify({"error": "No face detected"}), 200

        # Crop first detected face
        bbox = faces[0].bbox.astype(int)
        x1, y1, x2, y2 = bbox
        face_crop = img[y1:y2, x1:x2]

        if face_crop.size == 0:
            return jsonify({"error": "Face crop failed"}), 200

        # 🔒 Anti-spoof check
        real_score, fake_score = check_spoof(face_crop)

        if fake_score > 0.6:
            return jsonify({"error": "Spoof detected"}), 200

        # 🔥 Generate embedding using your Facenet pipeline
        face_processed = image_loader.extract_face(img)

        if face_processed is None:
            return jsonify({"error": "Face extraction failed"}), 200

        embedding = image_loader.get_embedding(face_processed)

        if embedding is None:
            return jsonify({"error": "Embedding generation failed"}), 500

        return jsonify({
            "status": "success",
            "embedding": np.asarray(embedding).tolist()
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# =====================================
# VIDEO → EMBEDDING (Registration)
# =====================================
@app.route('/train_face', methods=['POST'])
def train_face():
    temp_path = None

    try:
        if 'file' not in request.files:
            return jsonify({"error": "Missing video file"}), 400

        video_file = request.files['file']

        if video_file.filename == "":
            return jsonify({"error": "Empty filename"}), 400

        temp_fd, temp_path = tempfile.mkstemp(suffix=".webm")
        os.close(temp_fd)
        video_file.save(temp_path)

        cap = cv.VideoCapture(temp_path)

        if not cap.isOpened():
            return jsonify({"error": "Cannot open video"}), 500

        real_face_detected = False

        while True:
            ret, frame = cap.read()
            if not ret:
                break

            faces = app_face.get(frame)

            if len(faces) > 0:
                bbox = faces[0].bbox.astype(int)
                x1, y1, x2, y2 = bbox
                face_crop = frame[y1:y2, x1:x2]

                if face_crop.size == 0:
                    continue

                real_score, fake_score = check_spoof(face_crop)

                if fake_score > real_score:
                    cap.release()
                    return jsonify({"error": "Spoof detected in video"}), 200

                real_face_detected = True
                break

        cap.release()

        if not real_face_detected:
            return jsonify({"error": "No real face detected in video"}), 200

        # 🔥 Existing average embedding logic
        embedding = video_loader.processing(temp_path)

        if embedding is None:
            return jsonify({"error": "Embedding extraction failed"}), 500

        return jsonify({
            "status": "success",
            "embedding": np.asarray(embedding).tolist()
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

    finally:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=False)