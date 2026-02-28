import os
import tempfile
import numpy as np
from flask import Flask, request, jsonify
from flask_cors import CORS

from app.real_time_loading.loading_via_video import FacenetVideo

app = Flask(__name__)
CORS(app)

loader = FacenetVideo()

@app.route('/train_face', methods=['POST'])
def train_face():
    temp_path = None
    try:
        
        
        if 'video' not in request.files:
            print("Error: No 'video' file in request")
            return jsonify({"error": "Missing video file"}), 400

        video_file = request.files['video']
        print(f"Received file: {video_file.filename}")

        if video_file.filename == "":
            return jsonify({"error": "Empty filename"}), 400

       
        temp_fd, temp_path = tempfile.mkstemp(suffix=".webm")
        os.close(temp_fd)
        video_file.save(temp_path)

       
        embedding = loader.processing(temp_path) 
        if embedding is None:
            return jsonify({"error": "Could not extract face embedding from video"}), 500

       

        return jsonify({
            "status": "success",
            "embedding": np.asarray(embedding).tolist()
        }), 200

    except Exception as e:
        
        import traceback
        traceback.print_exc()
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500
        
    finally:
        
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)
            print("🧹 Temp file cleaned up")

if __name__ == '__main__':
    
    app.run(host='0.0.0.0', port=5000, debug=True)