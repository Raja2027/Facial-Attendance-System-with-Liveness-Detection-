import cv2 as cv
import numpy as np
from flask import Flask, request, jsonify
from flask_cors import CORS

from app.real_time_loading.Facenet import FaceLoading 


app = Flask(__name__)
CORS(app)


loader = FaceLoading()


@app.route('/generate_embedding', methods=['POST'])
def generate_embedding():
    try:
        
        if 'file' not in request.files:
            return jsonify({"error": "No file part"}), 400

        file = request.files['file']
        
        filestr = file.read()
        npimg = np.frombuffer(filestr, np.uint8)
        img = cv.imdecode(npimg, cv.IMREAD_COLOR)

        if img is None:
             return jsonify({"error": "Failed to decode image"}), 400

        face = loader.extract_face(img)
        
        if face is None:
            return jsonify({"status": "no_face"}), 200

       
        embedding = loader.get_embedding(face)
        
        if embedding is None:
            return jsonify({"error": "Failed to generate embedding"}), 500

        return jsonify({
            "status": "success",
            "embedding": embedding.tolist()
        }), 200

    except Exception as e:
        print(f"Server Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

    
if __name__ == '__main__':
    # Run on port 5001
    app.run(host='0.0.0.0', port=5001, debug=True)