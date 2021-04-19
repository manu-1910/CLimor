package com.limor.app.scenes.utils.popupMenu;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import me.kareluo.ui.OptionMenu;
import me.kareluo.ui.OptionMenuView;
import me.kareluo.ui.PopHorizontalScrollView;
import me.kareluo.ui.PopLayout;
import me.kareluo.ui.PopVerticalScrollView;
import me.kareluo.ui.PopupView;

/**
 * Created by felix on 16/11/18.
 */
public class CustomPopupMenuView extends PopupView implements CustomOptionMenuView.OnOptionMenuClickListener {

    private final PopLayout mPopLayout;

    private final CustomOptionMenuView mOptionMenuView;

    private PopVerticalScrollView mVerticalScrollView;

    private PopHorizontalScrollView mHorizontalScrollView;

    private OptionMenuView.OnOptionMenuClickListener mOnOptionMenuClickListener;

    public CustomPopupMenuView(Context context) {
        this(context, 0);
    }

    public CustomPopupMenuView(Context context, int menuRes) {
        super(context);
        mOptionMenuView = new CustomOptionMenuView(context, menuRes);
        mOptionMenuView.setOnOptionMenuClickListener(this);
        mPopLayout = new PopLayout(context);
        ViewGroup scrollView = getScrollView(mOptionMenuView.getOrientation());
        scrollView.addView(mOptionMenuView);
        mPopLayout.addView(scrollView);
        setContentView(mPopLayout);
    }

    public CustomPopupMenuView(Context context, int menuRes, Menu menu) {
        this(context);
        inflate(menuRes, menu);
    }

    public void inflate(int menuRes, Menu menu) {
        mOptionMenuView.inflate(menuRes, menu);
        measureContentView();
    }

    public void setMenuItems(List<OptionMenu> optionMenus) {
        mOptionMenuView.setOptionMenus(optionMenus);
        measureContentView();
    }

    public List<OptionMenu> getMenuItems() {
        return mOptionMenuView.getOptionMenus();
    }

    public void setOrientation(int orientation) {
        mOptionMenuView.setOrientation(orientation);
        measureContentView();
    }

    public int getOrientation() {
        return mOptionMenuView.getOrientation();
    }

    // 暂时暴露出
    @Deprecated
    public PopLayout getPopLayout() {
        return mPopLayout;
    }

    // 暂时暴露出
    @Deprecated
    public CustomOptionMenuView getMenuView() {
        return mOptionMenuView;
    }

    public void setOnMenuClickListener(OptionMenuView.OnOptionMenuClickListener listener) {
        mOnOptionMenuClickListener = listener;
    }

    @Override
    public void show(View anchor, Rect frame, Point origin) {
        mOptionMenuView.notifyMenusChange();
        super.show(anchor, frame, origin);
    }

    @Override
    public void showAtTop(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_BOTTOM);
        mPopLayout.setOffset(origin.x - xOff);
        super.showAtTop(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtLeft(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_RIGHT);
        mPopLayout.setOffset(-origin.y - yOff);
        super.showAtLeft(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtRight(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_LEFT);
        mPopLayout.setOffset(-origin.y - yOff);
        super.showAtRight(anchor, origin, xOff, yOff);
    }

    @Override
    public void showAtBottom(View anchor, Point origin, int xOff, int yOff) {
        mPopLayout.setSiteMode(PopLayout.SITE_TOP);
        mPopLayout.setOffset(origin.x - xOff);
        super.showAtBottom(anchor, origin, xOff, yOff);
    }

    @Override
    public boolean onOptionMenuClick(int position, OptionMenu menu) {
        if (mOnOptionMenuClickListener != null) {
            if (mOnOptionMenuClickListener.onOptionMenuClick(position, menu)) {
                dismiss();
                return true;
            }
        }
        return false;
    }

    private ViewGroup getScrollView(int orientation) {
        if (orientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalScrollView == null) {
                mHorizontalScrollView = new PopHorizontalScrollView(getContext());
                mHorizontalScrollView.setHorizontalScrollBarEnabled(false);
                mHorizontalScrollView.setVerticalScrollBarEnabled(false);
            }
            return mHorizontalScrollView;
        } else {
            if (mVerticalScrollView == null) {
                mVerticalScrollView = new PopVerticalScrollView(getContext());
                mVerticalScrollView.setHorizontalScrollBarEnabled(false);
                mVerticalScrollView.setVerticalScrollBarEnabled(false);
            }
            return mVerticalScrollView;
        }
    }
}