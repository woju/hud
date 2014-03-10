package eu.woju.android.packages.hud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class CarHudActivity extends Activity
{
    private LocationListener ll;
    private LocationManager lm;
    private Timer timer;
    private Timer watchdog;
    private BatteryHandler handler_batt;
    private WatchdogHandler handler_watchdog;
    private long lastmod;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location.hasSpeed()) {
                    lastmod = SystemClock.elapsedRealtime();
                    disp_value(location.getSpeed());
                } else {
                    disp_value(-1f);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                lastmod = 0;
                disp_off();
            }

            @Override
            public void onProviderEnabled(String provider) {
                disp_on();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status == LocationProvider.AVAILABLE) {
                    disp_ava();
                } else {
                    lastmod = 0;
                    disp_nav();
                }

                disp_sate(extras.getInt("satellites", -1));
            }
        };

        handler_batt = new BatteryHandler();
        handler_watchdog = new WatchdogHandler();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        hide_nav(this.findViewById(R.id.layout));

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            disp_on();
        } else {
            disp_off();
        }

        disp_flag("NFX");
        disp_sate(-1);
        TextView stat = (TextView) findViewById(R.id.stat);
        stat.setText("---");
        stat.setTextColor(getResources().getColor(R.color.inactive));

        lastmod = 0;

        timer = new Timer();
        timer.scheduleAtFixedRate(new BatteryTask(), 0, 10000);
        timer.scheduleAtFixedRate(new WatchdogTask(), 0, 1000);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        lm.removeUpdates(ll);
        timer.cancel();
        timer.purge();
    }

    protected class BatteryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            disp_batt((Intent) msg.obj);
        }
    }

    protected class WatchdogHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            disp_flag("LAG");
        }
    }

    protected class BatteryTask extends TimerTask {
        private final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        @Override
        public void run() {
            final Intent battery = getApplicationContext().registerReceiver(null, filter);
            Message msg = new Message();
            msg.obj = battery;
            handler_batt.sendMessage(msg);
        }
    }

    protected class WatchdogTask extends TimerTask {
        @Override
        public void run() {
            /*
             * OBLICZANIE LAGA
             *
             * GPS ~1s (raczej nie wliczamy)
             * watchdog 1000 ms
             * 2500 ms
             *
             * razem [2.5 .. 3.5) s
             */
            if (lastmod > 0 && lastmod + 2500 < SystemClock.elapsedRealtime()) {
                handler_watchdog.sendMessage(new Message());
            }
        }
    }

    public void hide_nav(View v) {
        v.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    protected void disp_flag(String f) {
        TextView flag = (TextView) findViewById(R.id.flag);
        TextView value = (TextView) findViewById(R.id.v);

        if (f == null) {
            flag.setText("");
        } else {
            flag.setText(f);
            value.setText("---");
            value.setTextColor(getResources().getColor(R.color.inactive));
        }
    }
            

    protected void disp_on() {
        TextView enab = (TextView) findViewById(R.id.enab);
        enab.setText("ON");
        enab.setTextColor(getResources().getColor(R.color.enabled));
    }
    protected void disp_off() {
        TextView enab = (TextView) findViewById(R.id.enab);
        enab.setText("OFF");
        enab.setTextColor(getResources().getColor(R.color.disabled));
        disp_flag("");
    }
    protected void disp_ava() {
        TextView stat = (TextView) findViewById(R.id.stat);
        stat.setText("AVA");
        stat.setTextColor(getResources().getColor(R.color.enabled));
    }
    protected void disp_nav() {
        TextView stat = (TextView) findViewById(R.id.stat);
        stat.setText("NAV");
        stat.setTextColor(getResources().getColor(R.color.disabled));
        disp_flag("");
    }
    protected void disp_sate(int n) {
        TextView sate = (TextView) findViewById(R.id.sate);
        if (n >= 0) {
            sate.setText(String.format("%d", n));
            sate.setTextColor(getResources().getColor(R.color.active));
        } else {
            sate.setText("--");
            sate.setTextColor(getResources().getColor(R.color.inactive));
        }
    }
    protected void disp_batt(Intent battery) {
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        TextView batt = (TextView) findViewById(R.id.batt);
        batt.setText(String.format("%d", level));
                
        if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) {
            batt.setTextColor(getResources().getColor(R.color.charging));
        } else if (level < 15) {
            batt.setTextColor(getResources().getColor(R.color.warning));
        } else {
            batt.setTextColor(getResources().getColor(R.color.active));
        }
    }
    protected void disp_value(float v) {
        TextView value = (TextView) findViewById(R.id.v);
        if (v >= 0) {
            disp_flag(null);
            value.setText(String.format("%3.0f", v * 3.6f));
            value.setTextColor(getResources().getColor(R.color.active));
        } else {
            disp_flag("");
        }
    }
}

// vim: ts=4 sw=4 et
