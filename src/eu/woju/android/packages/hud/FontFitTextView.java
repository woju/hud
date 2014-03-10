package eu.woju.android.packages.hud;

/*
 * This class is lifted from http://stackoverflow.com/questions/2617266
 * and Hacked.
 */

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class FontFitTextView extends TextView {
    private static final String TAG = "FontFitTextView";

    public FontFitTextView(Context context) {
        super(context);
        initialise();
    }

    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //max size defaults to the initially specified text size unless it is too small
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(int textWidth) 
    { 
        final String[] charset = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "F", "G", "L", "N", "O", "V", "X"};

        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = getHeight() * .9f;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        float maxwidth = 0;
        String widest = "0";
        // choose widest char in current font
        for (int i = 0; i < charset.length; i++) {
            float measure = mTestPaint.measureText(charset[i]);
            if (measure > maxwidth) {
                widest = charset[i];
                maxwidth = measure;
            }
        }

        String testString = widest + widest + widest;

        Log.d(TAG, String.format("maxwidth=%f widest=%s", maxwidth, widest));

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
//          if(mTestPaint.measureText(text) >= targetWidth) 
            if(mTestPaint.measureText(testString) >= targetWidth) 
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

/*
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(this.getWidth());
    }
*/

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        refitText(w);
    }

    //Attributes
    private Paint mTestPaint;
}

// vim: ts=4 sw=4 et
