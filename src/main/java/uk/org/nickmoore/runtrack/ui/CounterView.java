package uk.org.nickmoore.runtrack.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.org.nickmoore.runtrack.R;

/**
 * A view for editing potentially infinite numbers easily.
 */
public class CounterView extends LinearLayout implements ImageView.OnClickListener {
    public static interface OnChangeListener {
        public void onChange(CounterView counter, int newValue, int oldValue);
    }

    private OnChangeListener changeListener;
    private InvertibleEditText counter;
    private ImageView increment;
    private ImageView decrement;
    private boolean allowedNegative;
    private boolean inverted;
    private int value;

    public CounterView(Context context, AttributeSet attributes) {
        super(context, attributes);
        LayoutInflater inflater = LayoutInflater.from(context);
        addView(inflater.inflate(R.layout.counter_view, null));
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.CounterView,
                0, 0);
        try {
            allowedNegative = a.getBoolean(R.styleable.CounterView_allowNegative, true);
            inverted = a.getBoolean(R.styleable.CounterView_inverted, false);
            value = a.getInt(R.styleable.CounterView_value, 0);
        }
        finally {
            a.recycle();
        }
        initialise();
    }

    private void initialise() {
        counter = (InvertibleEditText) findViewById(R.id.counter);
        counter.setInverted(inverted);
        counter.setText(Integer.toString(value));
        if(!allowedNegative) {
            counter.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else {
            counter.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        if(inverted) {
            increment = (ImageView) findViewById(R.id.bottom);
            decrement = (ImageView) findViewById(R.id.top);
        }
        else {
            increment = (ImageView) findViewById(R.id.top);
            decrement = (ImageView) findViewById(R.id.bottom);
        }
        increment.setOnClickListener(this);
        decrement.setOnClickListener(this);
    }

    public TextView getCounter() {
        return counter;
    }

    public boolean isAllowedNegative() {
        return allowedNegative;
    }

    public void setAllowedNegative(boolean allowedNegative) {
        this.allowedNegative = allowedNegative;
        initialise();
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        initialise();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        initialise();
    }

    @Override
    public void onClick(View view) {
        if(view.equals(increment)) {
            value++;
            changeListener.onChange(this, value, value - 1);
        }
        else if(view.equals(decrement)) {
            if(value == 0 && !allowedNegative) {
                return;
            }
            value--;
            changeListener.onChange(this, value, value + 1);
        }
        counter.setText(Integer.toString(value));
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.changeListener = listener;
    }
}
