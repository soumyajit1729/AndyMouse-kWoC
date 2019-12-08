## AndyMouse-kWoC
This is the project of AndyMouse, basically to control mouse movements by android app.
### please go through [THIS](https://docs.google.com/presentation/d/1v2q9_JhMh9z1giz6h9mKU9XZ4hZ3JlYG2z34-czweeQ/edit?usp=sharing) slides(mainly endsem one) to know about the project.

### skills needed
Android studio(java, xml design), Bottle library of python.
### location directory
sensor.py contains the server side code, it is a bottle(a python library) based restful server.

in the AndyMouse folder all the android app code is placed.

### working principle
The android appis a bottle client here. Also the app collects the accelorometer data and calculates the the tilting along X and Y axis. And sends the tan of that angle to the server as a json format.
and our server (sensor.py) is a bottle server and it takes the 2 ratio value and a very simple math to output the mouse pointer movement.
we used pyautogui python library to move the mouse pointer and the slide changes.

### For the client side(app) follow [this](https://hmkcode.com/android-send-json-data-to-server/) blog
the client side is based on this blog. no need to go deeper into this blog. just try find out the major code parts.

### setup
to setup the project first clone the sensor.py as a simple python code. Install **pyautogui** and **bottle** libraries. then run the sensor.py file.

for the server side clone the andymouse folder and then open it in android studio, run the app in your phone. **now both the phone and the server side laptop should be connect with same wifi network** so that the app can access the server. now see the ip address of your server side laptop and write it on your app( in the format ```10.15.22.129```) the default port is given 8080. now press the start button. the server will get data from app and by tilting the mobile you can control the mouse pointer. 2 buttons are there for left and right button.


