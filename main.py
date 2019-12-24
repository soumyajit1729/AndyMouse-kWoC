def startServer():
    run(host='0.0.0.0', port=8080, debug=True)

from bottle import route, run, template, get, request, post
import pyautogui
import threading

#set the values of all these variables to 0 to prevent any delay
#in the mouse movement
pyautogui.MINIMUM_DURATION = 0  
pyautogui.MINIMUM_SLEEP = 0  
pyautogui.PAUSE = 0  

x=-5
stop=False;
i=0
sensorData = [{'y_tilt' : 0, 'x_tilt' : 0, 'arrow': 0, 'sensitivity':25},
        {'y_tilt' : 0, 'x_tilt' : 0, 'arrow':  0, 'sensitivity':25}
]
sensorData_touchpad = [ {'check' : 0, 'count_left_clicks' : 0, 'count_right_clicks' : 0, 'dx' : 0, 'dy' : 0, 'scroll' : 0, 'drag' : 'false', 'left_click' : 0, 'right_click' : 0, 'action' : 'touchpad'},
               {'check' : 0, 'count_left_clicks' : 0, 'count_right_clicks' : 0, 'dx' : 0, 'dy' : 0, 'scroll' : 0, 'drag' : 'false', 'left_click' : 0, 'right_click' : 0, 'action' : 'touchpad'} ]

x0=0.0
y0=0.0

flag = 0
password = '12345678'

@get('/')
def getAll():
    global flag
    print("get request")
    return { 'flag' : flag }

@get('/sensor')
def getAll():
    x=0
    print("get request")
    return {'sensorData' : sensorData[0]}

@get('/touchpad')
def getAll():
    print("get request")
    return {'sensorData_touchpad' : sensorData_touchpad[0]}

@post('/')
def addone():
    global flag
    get_password = request.json.get('password');
    password = ''
    try:
        text_file = open('.password','r')
        password = text_file.readline().strip()
        text_file.close()
    except:
        print('Error reading the file')
    if password == get_password :
        flag = 1
    else: 
        flag = 0
    print('post request')
    return { 'flag' : flag }

@post('/sensor')
def addone():
    global stop
    new_node = {'y_tilt' : request.json.get('y_tilt'),
                'x_tilt' : request.json.get('x_tilt'),
                'arrow' : request.json.get('arrow'),
                'sensitivity' : request.json.get('sensitivity') }
    sensorData.insert(0, new_node)
    x = len(sensorData)
    try:
            if x>15:
                    sensorData.pop(15)
            print(sensorData[0]['arrow'])
            print(sensorData[0]['sensitivity'])
            if sensorData[0]['arrow'] == 10 and sensorData[1]['arrow'] != 10:
                    stop=False
            if sensorData[0]['arrow'] == -10 and sensorData[1]['arrow'] != -10:
                    stop=True
            if sensorData[0]['arrow'] == 10000 and sensorData[1]['arrow'] != 10000:
                    pyautogui.press('left')
            if sensorData[0]['arrow'] == -1 and sensorData[1]['arrow'] != -1:
                    pyautogui.press('right')
            if sensorData[0]['arrow'] == 3 and sensorData[1]['arrow'] != 3:
                    pyautogui.click(button='left')
            if sensorData[0]['arrow'] == 2 and sensorData[1]['arrow'] != 2:
                    pyautogui.click(button='right')
            # changed the threshold to 0.1 
            if (abs(float(sensorData[0]['y_tilt'])) > 0.1 or abs(float(sensorData[0]['x_tilt'])) > 0.1) and (not stop) :
                #changed the parameters and  set the duration to 0 to get instant response without any delay
                    pyautogui.moveRel(-1*sensorData[0]['sensitivity']*float(sensorData[0]['x_tilt']),
                                      -1*sensorData[0]['sensitivity']*float(sensorData[0]['y_tilt']),
                                      duration = 0)
    except:
            pyautogui.FAILSAFE=False

    print('post request')
    return {'sensorData' : sensorData[0]}

@post('/touchpad')
def addone():
    global x0
    global y0
    pyautogui.FAILSAFE = False
    new_node = {'check' : int(request.json.get('check')),
                'count_left_clicks' : int(request.json.get('count_left_clicks')),
                'count_right_clicks' : int(request.json.get('count_right_clicks')),
                'dx' : float(request.json.get('dx')),            
                'dy' : float(request.json.get('dy')),
                'scroll' : float(request.json.get('scroll')),
                'drag' : request.json.get('drag'),
                'left_click' : int(request.json.get('left_click')),
                'right_click' : int(request.json.get('right_click')),
                'action' : request.json.get('action') }
    sensorData_touchpad.insert(0, new_node)
    x = len(sensorData_touchpad)
    if x>30:
        sensorData_touchpad.pop(30)
    diff_check=sensorData_touchpad[0]['check']-sensorData_touchpad[1]['check']
    diff_left=sensorData_touchpad[0]['count_left_clicks']-sensorData_touchpad[1]['count_left_clicks']
    diff_right=sensorData_touchpad[0]['count_right_clicks']-sensorData_touchpad[1]['count_right_clicks']
    print('diff_check',diff_check)
    print('diff_left',diff_left)
    print('check',sensorData_touchpad[0]['check'])
    print('left_clicks',sensorData_touchpad[0]['count_left_clicks'])
    print('left_clicks',sensorData_touchpad[0]['left_click'])
    print('drag',sensorData_touchpad[0]['drag'])
    print('--------------------------------------------------------------------------------')
    if sensorData_touchpad[0]['action'] == 'button':
        if sensorData_touchpad[0]['left_click'] == 1:
            pyautogui.mouseDown(button='left')
        else:
            pyautogui.mouseUp(button='left')
        if sensorData_touchpad[0]['right_click'] == 1:
            pyautogui.mouseDown(button='right')
        else:
            pyautogui.mouseUp(button='right')
    elif sensorData_touchpad[0]['action'] == 'touchpad':
        if diff_left > 0:
            pyautogui.mouseDown(button='left')
        else:
            pyautogui.mouseUp(button='left')
        if diff_right > 0:
            pyautogui.mouseDown(button='right')
        else:
            pyautogui.mouseUp(button='right')
    else:
        if sensorData_touchpad[0]['drag'] == 'true':
            pyautogui.mouseDown(button='left')
        else:
            pyautogui.mouseUp(button='left')
    if diff_check > 0:
        x0=pyautogui.position()[0]
        y0=pyautogui.position()[1]
    pyautogui.moveTo((x0+sensorData_touchpad[0]['dx']),(y0+sensorData_touchpad[0]['dy']),duration=0)
    if sensorData_touchpad[0]['scroll'] != 0 :
        pyautogui.scroll(sensorData_touchpad[0]['scroll']/abs(sensorData_touchpad[0]['scroll']))
    print('post request')
    return {'sensorData_touchpad' : sensorData_touchpad[0]}
def menu():
    print('1.) Change password (default 12345678)')
    print('2.) Start server')
    print('3.) Exit')
    print('Enter your choice')
    try:
        choice = int(input())
    except:
        print('Invalid choice')
        print('Aborting')
        raise SystemExit
    if choice == 1:
        global password
        print('Enter new password')
        password = input()
        try:
            text_file = open('.password','w')
            text_file.write(password)
            text_file.close()
            print('Password saved successfully')
        except:
            print('Error')
            raise SystemExit
        menu()
    if choice == 2:
        startServer()
    if choice == 3:
        raise SystemExit
menu()
