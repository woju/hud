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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HudView extends View
{
    public HudView(Context context) {
        super(context);
        init();
    }

    public HudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HudView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        this.setBackgroundColor(getResources().getColor(R.color.background));
        this.setPadding(0, 0, 0, 0);
        this.setScaleY(-1); /* XXX config me */

        this.paint = new Paint();
        this.paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        this.paint.setHinting(Paint.HINTING_OFF);
        this.paint.setTypeface(Typeface.MONOSPACE);
        this.paint.setTextAlign(Paint.Align.RIGHT);
        this.paint.setTextSize(100);
        this.paint.setStyle(Paint.Style.FILL);
    }

    /*
     * public API
     */

    public void log(String e) {
        this.setText(e);
        this.invalidate();
    }

    public void log(String e, int color) {
        this.setText(e);
        this.setTextColor(getResources().getColor(color));
        this.invalidate();
    }

    public void logError(String e) {
        this.log(e, R.color.error);
    }

    public void logWarning(String e) {
        this.log(e, R.color.warning);
    }

    public void logInactive(String e) {
        this.log(e, R.color.inactive);
    }

    public void displayValue(float v) {
        this.setText(String.format("%.0f", v));
        this.setTextColor(this.battColor);
        this.invalidate();
    }

    public void setBattColor(int color) {
        this.battColor = getResources().getColor(color);
        this.invalidate();
    }

    public void setBattColor(Intent intent) {
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
    private String text;
    private Paint paint;
    private int battColor;
    private int baseline;


    /*
     * not-so-public API
     */

    /*
     * setText and setTextColor are protected, because they do not call
     * invalidate(); use log*() or displayValue().
     */
    protected void setText(String text) {
        this.text = text;
    }

    protected void setTextColor(int color) {
        this.paint.setColor(color);
    }

    /*
     * We do not override onMeasure, as the super's fits view to the parent.
     */

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.paint.setTextSize(h);
        this.baseline = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*
         * Calculate text size. We try not to have text jumping around on each
         * update, so we store result and try to reuse it.
         */

        final float measure = this.paint.measureText(this.text);

        if (measure > canvas.getWidth()) {
            this.paint.setTextSize((float)Math.floor(this.paint.getTextSize() * canvas.getWidth() / measure));
        }

        /*
         * Bounds are different for each text, specifically round glyphs have
         * non-zero bottom); hope this won't bite.
         */
        if (this.baseline == 0) {
            Rect bounds = new Rect();
            paint.getTextBounds(this.text, 0, this.text.length(), bounds);
            this.baseline = (canvas.getHeight() - bounds.bottom - bounds.top) / 2;
        }

//      final int y = (this.paint.getTextAlign() == Paint.Align.LEFT) ? 0 : canvas.getWidth();

        /*
         * Drawing directly (Canvas.drawText) seems to be unreliable when
         * attempting to draw very high text (comparable to canvas height).
         * No problem getting text as path and drawing path.
         */
        Path path = new Path();
        this.paint.getTextPath(this.text, 0, this.text.length(), canvas.getWidth(), baseline, path);
        path.close();
        canvas.drawPath(path, this.paint);
    }
}

// vim: ts=4 sw=4 et
