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

import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
//import android.view.View;

import eu.woju.android.packages.hud.FontFitTextView;

public class HudView extends FontFitTextView
{
    public HudView(Context context) {
        super(context);
    }

    public HudView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void log(String e) {
        Log.d(TAG, String.format("log(%s)", e));
        this.setText(e);
    }

    public void log(String e, int color) {
        this.setText(e);
        this.setTextColor(getResources().getColor(color));
    }

    public void logError(String e) {
        Log.d(TAG, String.format("logError(%s)", e));
        this.log(e, R.color.error);
    }

    public void logWarning(String e) {
        Log.d(TAG, String.format("logWarning(%s)", e));
        this.log(e, R.color.warning);
    }

    public void logInactive(String e) {
        Log.d(TAG, String.format("logInactive(%s)", e));
        this.log(e, R.color.inactive);
    }

    public void displayValue(float v) {
        Log.d(TAG, String.format("displayValue(%3.0f)", v));
        this.setText(String.format("%3.0f", v));
        this.setTextColor(getResources().getColor(battColor));
    }

    public void setBattColor(int color) {
        Log.d(TAG, String.format("battColor=%#x", color));
        this.battColor = color;
    }

    public void setBattColor(Intent intent) {
        Log.d(TAG, "parsing intent " + intent.getAction());
        String action = intent.getAction();
        if (action == Intent.ACTION_BATTERY_CHANGED) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            Resources sys = Resources.getSystem();
            int threshold = sys.getInteger(sys.getIdentifier("config_lowBatteryWarningLevel", "integer", "android"));

            if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL) {
                setBattColor(R.color.charging);
            } else if (level < threshold) {
                setBattColor(R.color.warning);
            } else {
                setBattColor(R.color.active);
            }

        } else if (action == Intent.ACTION_POWER_CONNECTED) {
            setBattColor(R.color.charging);

        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            // if power is low, next intent will be sent shortly
            setBattColor(R.color.active);

        } else if (action == Intent.ACTION_BATTERY_LOW) {
            setBattColor(R.color.warning);

        } else if (action == Intent.ACTION_BATTERY_OKAY) {
            setBattColor(R.color.active);
        }
    }

    private static final String TAG = "HudView";
    private int battColor;

/*
    protected class HideUIListener extends View.OnClickListener {
        public void onClick(View v) {
            v.setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
*/
}

// vim: ts=4 sw=4 et
