package eu.woju.android.packages.hud;

/*
 *  HUD -- Heads-up display for your car
 *  Copyright (C) 2014  Wojciech Porczyk <wojciech@porczyk.eu>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class HudActivity extends Activity
{
    private static final String TAG = "HudActivity";

    private LocationListener ll;
    private LocationManager lm;
    private Timer timer;
    private Timer watchdog;
    private WatchdogHandler handlerWatchdog;
//  private TestHandler handlerTest;
    private BatteryReceiver br;
    private long lastmod;

    private int battColor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.main);
        this.getDisplay().setBattColor(R.color.error);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location.hasSpeed()) {
                    lastmod = SystemClock.elapsedRealtime();
                    getDisplay().displayValue(location.getSpeed() * 3.6f); // m/s -> km/h
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                lastmod = 0;
                getDisplay().logInactive(getResources().getString(R.string.state_off));
            }

            @Override
            public void onProviderEnabled(String provider) {
                getDisplay().logInactive(getResources().getString(R.string.state_no_fix));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status != LocationProvider.AVAILABLE) {
                    lastmod = 0;
                    getDisplay().logInactive(getResources().getString(R.string.state_not_available));
                }

//              display_sate(extras.getInt("satellites", -1));
            }
        };

        handlerWatchdog = new WatchdogHandler();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            this.getDisplay().logInactive(getResources().getString(R.string.state_no_fix));
        } else {
            this.getDisplay().logInactive(getResources().getString(R.string.state_off));
        }

        lastmod = 0;

        timer = new Timer();
        timer.scheduleAtFixedRate(new WatchdogTask(), 0, 1000);

        this.br = new BatteryReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        getApplicationContext().registerReceiver(br, filter);

        br.onReceive(this, getApplicationContext().registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));

        this.getDisplay().callOnClick();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        lm.removeUpdates(ll);
        this.getApplicationContext().unregisterReceiver(br);
        timer.cancel();
        timer.purge();
    }

    public void onClick(View v) {
        v.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

/*
        testDisplay = 0f;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.obj = new Float(testDisplay);
                handlerTest.sendMessage(msg);

                testDisplay += 1f;

                if (testDisplay > 200f) {
                    timer.cancel();
                }
            }
        }, 0, 500);
*/
    }

    protected class WatchdogHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            getDisplay().logInactive(getResources().getString(R.string.state_lag));
        }
    }

    protected class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received intent " + intent.getAction());
            getDisplay().setBattColor(intent);
        }
    }

/*
    protected class TestHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            displayValue(((Float) msg.obj).floatValue());
        }
    }
*/

    protected class WatchdogTask extends TimerTask {
        @Override
        public void run() {
            /*
             * LAG ESTIMATION
             *
             * GPS ~1s (not included, device-dependent)
             * watchdog 1000 ms
             * 2500 ms
             *
             * total [2.5 .. 3.5) s
             */
            if (lastmod > 0 && lastmod + 2500 < SystemClock.elapsedRealtime()) {
                handlerWatchdog.sendMessage(new Message());
            }
        }
    }

    protected HudView getDisplay() {
        return (HudView) this.findViewById(R.id.display);
    }
}

// vim: ts=4 sw=4 et
