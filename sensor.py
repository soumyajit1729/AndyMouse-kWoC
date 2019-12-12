from bottle import route, run, template, get, request, post
import pyautogui
import threading
import numpy

#set the values of all these variables to 0 to prevent any delay
#in the mouse movement
pyautogui.MINIMUM_DURATION = 0  
pyautogui.MINIMUM_SLEEP = 0  
pyautogui.PAUSE = 0  

x=-5
stop=False;
i=0
sensorData = [{'y_tilt' : 0, 'x_tilt' : 0, 'arrow': 0},
	{'y_tilt' : 0, 'x_tilt' : 0, 'arrow':  0}
]


@get('/sensor')
def getAll():
    x=0
    print("get request")
    return {'sensorData' : sensorData[0]}

@post('/sensor')
def addone():
    global stop
    new_node = {'y_tilt' : request.json.get('y_tilt'),
                'x_tilt' : request.json.get('x_tilt'),
                'arrow' : request.json.get('arrow') }
    sensorData.insert(0, new_node)
    x = len(sensorData)
    try:
            if x>15:
                    sensorData.pop(15)
            print(sensorData[0]['arrow'])
            if sensorData[0]['arrow'] == 10 and sensorData[1]['arrow'] != 10:
                    stop=False
            if sensorData[0]['arrow'] == -10 and sensorData[1]['arrow'] != -10:
                    stop=True
            if sensorData[0]['arrow'] == 10000 and sensorData[1]['arrow'] != 10000:
                    pyautogui.press('left')
            if sensorData[0]['arrow'] == -1 and sensorData[1]['arrow'] != -1:
                    pyautogui.press('right')
            # changed the threshold to 0.1 
            if (abs(float(sensorData[0]['y_tilt'])) > 0.1 or abs(float(sensorData[0]['x_tilt'])) > 0.1) and (not stop) :
                #changed the parameters and  set the duration to 0 to get instant response without any delay
                    pyautogui.moveRel(-25*float(sensorData[0]['x_tilt']),
                                      25*float(sensorData[0]['y_tilt']),
                                      duration = 0)
    except:
            pyautogui.FAILSAFE=False

    print('post request')
    return {'sensorData' : sensorData[0]}

run(host='0.0.0.0', port=8080, debug=True, reloader=True)
