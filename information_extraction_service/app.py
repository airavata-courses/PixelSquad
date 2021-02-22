from extract_info import make_yolov3_model, get_objects, preprocess_input, WeightReader, decode_netout, correct_yolo_boxes
from flask import Flask, request, session, g, redirect, url_for, abort, \
     render_template, flash, jsonify
# from wtforms import Form, StringField, validators, TextAreaField
from PIL import Image
import base64
import numpy as np
import io
import json

# create the application
app = Flask(__name__)
app.config.from_object(__name__)

yolomodel = make_yolov3_model()
# load the weights trained on COCO into the model
weights_path = '/Users/vivekkarna/Documents/yolov3.weights'
weight_reader = WeightReader(weights_path)
weight_reader.load_weights(yolomodel)

app.config.update(dict(
    SECRET_KEY='development key',
    USERNAME='admin',
    PASSWORD='default'
))


net_h, net_w = 416, 416
obj_thresh, nms_thresh = 0.5, 0.45
anchors = [[116,90,  156,198,  373,326],  [30,61, 62,45,  59,119], [10,13,  16,30,  33,23]]
labels = ["person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", \
          "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", \
          "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", \
          "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", \
          "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", \
          "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", \
          "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", \
          "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", \
          "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", \
          "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"]


def extract_objects(img):
    image_h, image_w, _ = img.shape
    new_image = preprocess_input(img, net_h, net_w)
    yolos = yolomodel.predict(new_image)
    boxes = []

    for i in range(len(yolos)):
        # decode the output of the network
        boxes += decode_netout(yolos[i][0], anchors[i], obj_thresh, nms_thresh, net_h, net_w)
    correct_yolo_boxes(boxes, image_h, image_w, net_h, net_w)
    return get_objects(img, boxes, labels, obj_thresh)


@app.route('/annotate', methods=['POST'])
def annotate():
    input_ = request.get_json()
    print(type(input_))
    input_decoded = base64.b64decode(json.loads(input_))
    print(type(input_decoded))
    loaded = np.array(Image.open(io.BytesIO(input_decoded)))
    return jsonify(extract_objects(loaded))
    # return jsonify(extract_nutrients(loaded)['1234'])
