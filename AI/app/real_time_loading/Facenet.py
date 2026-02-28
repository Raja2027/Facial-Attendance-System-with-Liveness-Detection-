import sys
import numpy as np
import cv2 as cv
from mtcnn.mtcnn import MTCNN
from keras_facenet import FaceNet



class FaceLoading:
    def __init__(self):
        self.target_size = (160, 160)
        self.detector = MTCNN()
        self.embedder = FaceNet()

    def extract_face(self, img):
        try:
            
            if img is None or img.size == 0:
                return None
            
            height, width, _ = img.shape
            if height < 20 or width < 20: 
                return None

            img_rgb = cv.cvtColor(img, cv.COLOR_BGR2RGB)
            
           
            try:
                results = self.detector.detect_faces(img_rgb)
            except ValueError as ve:
                return None

            if not results:
                return None   

          
            max_area = 0
            max_face = None
            
            for res in results:
                x, y, w, h = res['box']
                if w * h > max_area:
                    max_area = w * h
                    max_face = res['box']

            if max_face is None:
                return None

            x, y, w, h = max_face
            x, y = max(0, x), max(0, y)

            face = img_rgb[y:y+h, x:x+w]
            
            if face.size == 0:
                return None

            face = cv.resize(face, self.target_size)
            return face

        except Exception as e:
            print(f"Error in extract_face: {e}")
            return None

    def get_embedding(self, face_img):
        try:
            face_img = face_img.astype('float32')
            face_img = np.expand_dims(face_img, axis=0)
           
            embedding = self.embedder.embeddings(face_img)
            embedding = embedding / np.linalg.norm(embedding)
            return embedding[0]

        except Exception as e:
            print(f"Error generating embedding: {e}")
            return None