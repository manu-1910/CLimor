package com.limor.app.scenes.utils.popupMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

/**
 * Created by felix on 16/11/21.
 */
@SuppressLint("AppCompatCustomView")
public class CustomOptionItemView extends CheckedTextView {

    public CustomOptionItemView(Context context) {
        this(context, null, 0);
    }

    public CustomOptionItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomOptionItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    public CustomOptionItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {

    }
}