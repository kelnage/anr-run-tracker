package uk.org.nickmoore.runtrack.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import uk.org.nickmoore.runtrack.R;

/**
 * Created by Nick on 03/09/13.
 */
public class RotatableImageView extends ImageView {
    private int imageRotation;

    public RotatableImageView(Context context) {
        super(context);
    }

    public RotatableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RotatableImageView,
                0, 0);
        try {
            imageRotation = a.getInt(R.styleable.RotatableImageView_imageRotation, 0);
        }
        finally {
            a.recycle();
        }
    }

    public int getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(int rotation) {
        this.imageRotation = rotation;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        canvas.rotate(imageRotation, px, py);
        super.onDraw(canvas);
        canvas.restore();
    }
}
