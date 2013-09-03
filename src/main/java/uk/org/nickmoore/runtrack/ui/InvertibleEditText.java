package uk.org.nickmoore.runtrack.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import uk.org.nickmoore.runtrack.R;

public class InvertibleEditText extends EditText {
    private boolean inverted = false;

    public InvertibleEditText(Context context) {
        super(context);
    }

    public InvertibleEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.InvertibleEditText,
                0, 0);
        try {
            inverted = a.getBoolean(R.styleable.InvertibleEditText_inverted, false);
        }
        finally {
            a.recycle();
        }
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        if(inverted) {
            float py = this.getHeight()/2.0f;
            float px = this.getWidth()/2.0f;
            canvas.rotate(180, px, py);
        }
        super.onDraw(canvas);
        canvas.restore(); 
    }
}