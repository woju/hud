HUD
===

*Heads-up display for your car*

<img src="res/drawable-xxxhdpi/ic_launcher.png" align="right"
    alt="Heads-up display" title="Heads-up display" />

This app converts your Android phone to simple, clear HUD for your car
displaying momentary velocity as measured by your GPS. Lodge it between the
windshield and the dashboard. Usable mainly during the night.

There is absolutely no configuration or interaction. The app displays just one
thing: the speed (in km/h only at the moment), and nothing else, not to distract
the driver. The only thing you can do is touch anywhere and then app will try to
hide navigation bar if you have soft buttons. The app does not attempt to adjust
brightness (you probably have to set static brightness yourself taking into
account ambient light and your preference).

Occasionaly you may see these alerts:

* `OFF` - location access is turned off
* `NFX` - no fix (yet)
* `NAV` - not available (GPS engine status)
* `LAG` - GPS speed did not update since at least 2500 ms

When battery is charging, colour is green. When battery is low, colour is yellow
(<15%). Unfortunately there is no way to block battery warning popup.

If you are on CM, adjust Privacy Guard to allow location access.

Contact: Wojciech Porczyk &lt;wojciech porczyk eu&gt;
