import os 
import sys 
import time
import logging

sys .path .append (os .path .abspath ("../antiSpoofing"))

import tempfile 
import numpy as np 
import cv2 as cv 

from src .anti_spoof_predict import AntiSpoofPredict 
from src .generate_patches import CropImage 
from src .utility import parse_model_name 

model_test =AntiSpoofPredict (0 )
image_cropper =CropImage ()
MODEL_DIR =os .path .abspath ("../antiSpoofing/resources/anti_spoof_models")

from flask import Flask ,request ,jsonify 
from flask_cors import CORS 

from app .real_time_loading .loading_via_video import FacenetVideo 
from app .real_time_loading .Facenet import FaceLoading 

logging.basicConfig(
level=os.getenv("LOG_LEVEL", "INFO"),
format="%(asctime)s %(levelname)s %(name)s %(message)s"
)
logger =logging .getLogger ("biopass-ai")

def check_real_face (image ):

    image_bbox =model_test .get_bbox (image )

    if image_bbox is None :
        logger .warning ("No face detected during liveness check")
        return False ,None 

    prediction =np .zeros ((1 ,3 ))

    for model_name in os .listdir (MODEL_DIR ):

        model_path =os .path .join (MODEL_DIR ,model_name )

        h_input ,w_input ,model_type ,scale =parse_model_name (model_name )

        param ={
        "org_img":image ,
        "bbox":image_bbox ,
        "scale":scale ,
        "out_w":w_input ,
        "out_h":h_input ,
        "crop":True ,
        }

        if scale is None :
            param ["crop"]=False 

        img =image_cropper .crop (**param )

        prediction +=model_test .predict (img ,model_path )

    label =np .argmax (prediction )

    is_real =(label ==1 )

    if is_real :
        logger .info ("Liveness check passed")
    else :
        logger .warning ("Liveness check failed: spoof suspected")

    return is_real ,image_bbox 

app =Flask (__name__ )
CORS (app )

video_loader =FacenetVideo ()
image_loader =FaceLoading ()

@app .before_request
def start_request_timer ():
    request .start_time =time .time ()

@app .after_request
def log_request (response ):
    duration_ms =round ((time .time ()-getattr (request ,"start_time",time .time ()))*1000 ,2 )
    logger .info (
    "request method=%s path=%s status=%s durationMs=%s",
    request .method ,
    request .path ,
    response .status_code ,
    duration_ms 
    )
    return response 

@app .route ('/health',methods =['GET'])
def health ():
    return jsonify ({
    "status":"UP",
    "service":"biopass-ai",
    "modelsLoaded":True 
    }),200 

@app .route ('/generate_embedding',methods =['POST'])
def generate_embedding ():
    try :
        if 'file'not in request .files :
            return jsonify ({"error":"No file provided"}),400 

        file =request .files ['file']
        filestr =file .read ()
        npimg =np .frombuffer (filestr ,np .uint8 )
        img =cv .imdecode (npimg ,cv .IMREAD_COLOR )

        if img is None :
            return jsonify ({"error":"Failed to decode image"}),400 

        is_real ,bbox =check_real_face (img )

        if not is_real :
            return jsonify ({"error":"Spoof detected"}),200 

        x ,y ,w ,h =bbox 
        face_crop =img [y :y +h ,x :x +w ]

        face_processed =image_loader .extract_face (face_crop )

        if face_processed is None :
            return jsonify ({"error":"Face extraction failed"}),200 

        embedding =image_loader .get_embedding (face_processed )

        if embedding is None :
            return jsonify ({"error":"Embedding generation failed"}),500 

        return jsonify ({
        "status":"success",
        "embedding":np .asarray (embedding ).tolist ()
        }),200 

    except Exception as e :
        logger .exception ("generate_embedding failed")
        return jsonify ({"error":str (e )}),500 

@app .route ('/train_face',methods =['POST'])
def train_face ():
    temp_path =None 

    try :
        if 'file'not in request .files :
            return jsonify ({"error":"Missing video file"}),400 

        video_file =request .files ['file']

        if video_file .filename =="":
            return jsonify ({"error":"Empty filename"}),400 

        temp_fd ,temp_path =tempfile .mkstemp (suffix =".webm")
        os .close (temp_fd )
        video_file .save (temp_path )

        cap =cv .VideoCapture (temp_path )

        if not cap .isOpened ():
            return jsonify ({"error":"Cannot open video"}),500 

        embeddings =[]
        frame_id =0 

        while True :
            ret ,frame =cap .read ()
            if not ret :
                break 

            if frame_id %4 ==0 :
                is_real ,bbox =check_real_face (frame )

                if is_real and bbox is not None :
                    x ,y ,w ,h =bbox 
                    face_crop =frame [y :y +h ,x :x +w ]

                    face_processed =image_loader .extract_face (face_crop )
                    if face_processed is not None :
                        embedding =image_loader .get_embedding (face_processed )
                        if embedding is not None :
                            embeddings .append (embedding )

            frame_id +=1 

        cap .release ()

        if len (embeddings )==0 :
            return jsonify ({"error":"No real face detected in video"}),200 

        mean_embedding =np .mean (embeddings ,axis =0 )
        mean_embedding =mean_embedding /np .linalg .norm (mean_embedding )

        return jsonify ({
        "status":"success",
        "embedding":np .asarray (mean_embedding ).tolist ()
        }),200 

    except Exception as e :
        logger .exception ("train_face failed")
        return jsonify ({"error":str (e )}),500 

    finally :
        if temp_path and os .path .exists (temp_path ):
            os .remove (temp_path )

if __name__ =='__main__':
    app .run (host ='0.0.0.0',port =5001 ,debug =False )
