import cv2
from app.real_time_loading.anti_spoofing import AntiSpoof

# Load model
anti_spoof = AntiSpoof("models/2.7_80x80_MiniFASNetV2.pth")

# Open webcam
cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()
    if not ret:
        break

    # 🔥 For testing: use center crop (no InsightFace)
    h, w, _ = frame.shape

    x1 = w // 4
    y1 = h // 4
    x2 = 3 * w // 4
    y2 = 3 * h // 4

    face_crop = frame[y1:y2, x1:x2]

    if face_crop.size == 0:
        continue

    real, fake = anti_spoof.predict(face_crop)

    # Show result
    text = f"Real: {real:.2f} Fake: {fake:.2f}"
    cv2.putText(frame, text, (30, 30),
                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

    cv2.imshow("Test Anti-Spoof", frame)

    if cv2.waitKey(1) & 0xFF == 27:
        break

cap.release()
cv2.destroyAllWindows()