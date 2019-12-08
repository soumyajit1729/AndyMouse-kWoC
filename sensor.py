from bottle import route, run, template, get, request, post
import pyautogui
import threading

x=-5
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
	new_node = {'y_tilt' : request.json.get('y_tilt'),
		'x_tilt' : request.json.get('x_tilt'),
		'arrow' : request.json.get('arrow')
	}
	sensorData.insert(0, new_node)
	x = len(sensorData)
	try:
		if x>15:
			sensorData.pop(15)
		print(sensorData[0]['arrow'])
		if sensorData[0]['arrow'] == 10000 and sensorData[1]['arrow'] != 10000:
			pyautogui.press('left')
		if sensorData[0]['arrow'] == -1 and sensorData[1]['arrow'] != -1:
			pyautogui.press('right')
		if abs(float(sensorData[0]['y_tilt'])) > 0.2 or abs(float(sensorData[0]['x_tilt'])) > 0.2:
			pyautogui.moveRel(-50*pow(float(sensorData[0]['x_tilt']),3),
				-50*pow(float(sensorData[0]['y_tilt']),3),duration = 0.01)
	except:
		pass
	
	print("post request")
	return {'sensorData' : sensorData[0]}

run(host='0.0.0.0', port=8080, debug=True, reloader=True)
