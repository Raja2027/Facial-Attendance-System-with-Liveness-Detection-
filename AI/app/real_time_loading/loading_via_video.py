import cv2 as cv 
import numpy as np 
from mtcnn .mtcnn import MTCNN 
from keras_facenet import FaceNet 
import os 
import sys 
from app .exception .execption import NetworkSecurityException 

class FacenetVideo :
    def __init__ (self ):
        self .target_size =(160 ,160 )
        self .detector =MTCNN ()
        self .embedder =FaceNet ()
        self .embeddings =[]

    def extract_face (self ,img ):
        img_rgb =cv .cvtColor (img ,cv .COLOR_BGR2RGB )
        face =cv .resize (img_rgb ,self .target_size )
        return face 

    def get_embedding (self ,face ):
        try :
            face =face .astype ('float32')
            face =np .expand_dims (face ,axis =0 )

            emb =self .embedder .embeddings (face )

            emb =emb [0 ]/np .linalg .norm (emb [0 ])

            return emb 

        except Exception as e :
            raise NetworkSecurityException (e ,sys )

    def processing (self ,video ):
        cap =cv .VideoCapture (video )

        if not cap .isOpened ():
            print (f"Error: Could not open video file {self .INPUT_FILE }")
            return None 

        self .embeddings =[]
        frame_id =0 
        while True :
            ret ,frame =cap .read ()
            if not ret :
                break 
            try :
                if frame_id %4 ==0 :
                    face =self .extract_face (frame )
                    embedding =self .get_embedding (face )
                    self .embeddings .append (embedding )
            except ValueError :
                pass 
            except Exception as e :
                print (f"Error on frame {frame_id }: {e }")

            frame_id +=1 

        cap .release ()

        if not self .embeddings :
            print ("No Face Detected in Video")
            return None 

        mean_embedding =np .mean (self .embeddings ,axis =0 )

        mean_embedding =mean_embedding /np .linalg .norm (mean_embedding )

        return mean_embedding 

