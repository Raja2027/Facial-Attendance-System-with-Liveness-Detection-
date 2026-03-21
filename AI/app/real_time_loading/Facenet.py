import sys
import numpy as np
import cv2 as cv
from keras_facenet import FaceNet



class FaceLoading:
    def __init__(self):
        self.target_size = (160, 160)
        self.embedder = FaceNet()

    def extract_face(self, img):
        try:
            if img is None or img.size == 0:
                return None

            img_rgb = cv.cvtColor(img, cv.COLOR_BGR2RGB)
            face = cv.resize(img_rgb, self.target_size)

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