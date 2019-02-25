# Lampe2

Project to control your raspberry-pi-zero-w unicornhathd lamp with your android phone.
This is the Android part.

Current minSdkVersion 14, so it should work for Android 4.0 upwards, though I only tested it on Android 7.1

Project compiles with Android Studio 3.3.1. Might work with other versions too, but that is the one I used.

# Base

I used [this](http://it-in-der-hosentasche.blogspot.com/2014/03/bluetooth-zwischen-raspberry-pi-und.html) tutorial for the bluetooth communication.

# Note:

Currently I assume that you are paired with the raspberry and the raspberry  is named (host name) "unit2".
If you named your pi differently, you have to change the name in the strings.xml
