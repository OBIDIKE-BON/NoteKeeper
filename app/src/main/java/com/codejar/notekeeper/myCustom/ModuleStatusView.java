package com.codejar.notekeeper.myCustom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import com.codejar.notekeeper.R;

import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {


    private static final int EDIT_MODE_MODULE_COUNT = 7;
    private static final int INVALID_MODULE_INDEX = -1;
    private static final int SHAPE_SQUARE = 1;
    private static final int DEFAULT_OUTLINE_WIDTH = 3;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private Rect[] mModuleRectAngles;
    private int mOutlineColor;
    private Paint mPaintOutline;
    private int mFillColor;
    private Paint mPaintFill;
    private int mRadius;
    private int mMaxNumberOfModulesThatCanFit;
    private int mSelectedModuleIndex = INVALID_MODULE_INDEX;
    private int mShapePosition;
    private ModuleStatusAccessibilityHelper mAccessibilityHelper;

    public boolean[] getModuleStatus() {
        return mModuleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    private boolean[] mModuleStatus;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setFocusable(true);
        mAccessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityHelper);

        int defaultOutlineColor = Color.BLACK;
        int defaultFillColor = getContext().getResources().getColor(R.color.colorAccent);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float displayDensity = displayMetrics.density;
        float defaultOutlineWidth = displayDensity * DEFAULT_OUTLINE_WIDTH;
        int defaultShape = 0;
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);

        mOutlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, defaultOutlineColor);
        mOutlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidth);
        mFillColor = a.getColor(R.styleable.ModuleStatusView_fillColor, defaultFillColor);
        mShapePosition = a.getInt(R.styleable.ModuleStatusView_shape, defaultShape);

        a.recycle();

        if (isInEditMode())
            setupEditModeSamples();

        mShapeSize = 144f;
        mSpacing = 30f;
        mRadius = (int) ((mShapeSize - mOutlineWidth) / 2);

        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setStrokeWidth(mOutlineWidth);
        mPaintOutline.setColor(mOutlineColor);

        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(mFillColor);
    }

    private void setupEditModeSamples() {
        boolean[] editModeSamples = new boolean[EDIT_MODE_MODULE_COUNT];
        int numberOfCompletedModules = EDIT_MODE_MODULE_COUNT / 2;
        for (int i = 0; i < numberOfCompletedModules; i++)
            editModeSamples[i] = true;

        setModuleStatus(editModeSamples);
    }

    private void setupModuleRectangles(int width) {
//        mModuleRecangles = new Rect[mModuleStatus.length];
//        int column = -1;
//        int row = 0;
//        for (int moduleIndex = 0; moduleIndex < mModuleStatus.length; moduleIndex++) {
//            column++;
//            if (column == mMaxNumberOfModulesThatCanFit) {
//                column = 0;
//                row++;
//            }
//            int x = getPaddingLeft() + (int) (column * (mShapeSize + mSpacing));
//            int y = row == 0 ? getPaddingTop() : (getPaddingTop() + (int) (row * (mShapeSize + mSpacing)));
//            mModuleRecangles[moduleIndex] = new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
        mModuleRectAngles = new Rect[mModuleStatus.length];

        int availableWidth = getAvailableWidth(width);
        int numberOfModulesThatCanFit = getNumberOfModulesThatCanFit(availableWidth);
        int maxNumberOfModulesThatCanFit = getMin(numberOfModulesThatCanFit);

        for (int moduleIndex = 0; moduleIndex < mModuleStatus.length; moduleIndex++) {

            int column = moduleIndex % maxNumberOfModulesThatCanFit;
            int row = moduleIndex / maxNumberOfModulesThatCanFit;

            int x = getPaddingLeft() + getDisplayPosition(column);
            int y = (getPaddingTop() + getDisplayPosition(row));

            mModuleRectAngles[moduleIndex] = getRect(x, y);
        }
    }

    private Rect getRect(int x, int y) {
        return new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
    }

    private int getDisplayPosition(int value) {
        return (int) (value * (mShapeSize + mSpacing));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = getAvailableWidth(specWidth);
        int numberOfModulesThatCanFit = getNumberOfModulesThatCanFit(availableWidth);
        mMaxNumberOfModulesThatCanFit = getMin(numberOfModulesThatCanFit);

        int desiredWidth = (int) (mMaxNumberOfModulesThatCanFit * (mShapeSize + mSpacing) - mSpacing);
        desiredWidth += getPaddingLeft() + getPaddingRight();


        int rows = ((mModuleStatus.length - 1) / mMaxNumberOfModulesThatCanFit) + 1;

        int desiredHeight = (int) ((rows * (mShapeSize + mSpacing)) - mSpacing);
        desiredHeight += getPaddingBottom() + getPaddingTop();

        width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);
    }

    private int getMin(int numberOfModulesThatCanFit) {
        return Math.min(numberOfModulesThatCanFit, mModuleStatus.length);
    }

    private int getNumberOfModulesThatCanFit(float availableWidth2) {
        return (int) (availableWidth2 / (mShapeSize + mSpacing));
    }

    private int getAvailableWidth(int width) {
        return width - getPaddingLeft() - getPaddingRight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setupModuleRectangles(w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int moduleIndex = 0; moduleIndex < mModuleRectAngles.length; moduleIndex++) {
            float x = mModuleRectAngles[moduleIndex].centerX();
            float y = mModuleRectAngles[moduleIndex].centerY();

            switch (mShapePosition) {
                case SHAPE_SQUARE:
                    drawRect(canvas, moduleIndex);
                    break;
                default:
                    drawCircle(canvas, moduleIndex, x, y);
            }
        }
    }

    private void drawCircle(Canvas canvas, int moduleIndex, float x, float y) {
        if (mModuleStatus[moduleIndex])
            canvas.drawCircle(x, y, mRadius, mPaintFill);
        canvas.drawCircle(x, y, mRadius, mPaintOutline);
    }

    private void drawRect(Canvas canvas, int moduleIndex) {
        if (mModuleStatus[moduleIndex])
            canvas.drawRect(mModuleRectAngles[moduleIndex].left + mSpacing,
                    mModuleRectAngles[moduleIndex].top + getPaddingTop(),
                    mModuleRectAngles[moduleIndex].right,
                    mModuleRectAngles[moduleIndex].bottom - getPaddingBottom(),
                    mPaintFill);

        canvas.drawRect(mModuleRectAngles[moduleIndex].left + mSpacing,
                mModuleRectAngles[moduleIndex].top + getPaddingTop(),
                mModuleRectAngles[moduleIndex].right,
                mModuleRectAngles[moduleIndex].bottom - getPaddingBottom(),
                mPaintOutline);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                mSelectedModuleIndex = findItemAtPoint(event.getX(), event.getY());
                performClick();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        if (mSelectedModuleIndex != INVALID_MODULE_INDEX) {
            setupModuleStatus(mSelectedModuleIndex);
            return true;
        } else {
            return super.performClick();
        }
    }

    private void setupModuleStatus(int selectedModuleIndex) {
        mModuleStatus[selectedModuleIndex] = !mModuleStatus[selectedModuleIndex];
        invalidate();

        mAccessibilityHelper.invalidateVirtualView(selectedModuleIndex);
        mAccessibilityHelper.sendEventForVirtualView(selectedModuleIndex, AccessibilityNodeInfoCompat.ACTION_CLICK);
    }

    private int findItemAtPoint(float x, float y) {
        int moduleIndex = INVALID_MODULE_INDEX;
        for (int i = 0; i < mModuleRectAngles.length; i++) {
            if (mModuleRectAngles[i].contains((int) x, (int) y)) {
                moduleIndex = i;
                break;
            }
        }
        return moduleIndex;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return mAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    private class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper {
        /**
         * Constructs a new helper that can expose a virtual view hierarchy for the
         * specified host view.
         *
         * @param host view whose virtual view hierarchy is exposed by this helper
         */
        public ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findItemAtPoint(x, y);
            return moduleIndex == INVALID_MODULE_INDEX ? ExploreByTouchHelper.INVALID_ID : moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            int length = mModuleStatus.length;
            if (length == 0)
                return;
            for (int moduleIndex = 0; moduleIndex < length; moduleIndex++) {
                virtualViewIds.add(moduleIndex);
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            node.setFocusable(true);
            node.setContentDescription("Module" + virtualViewId);
            node.setBoundsInParent(mModuleRectAngles[virtualViewId]);
            node.setCheckable(true);
            node.setChecked(mModuleStatus[virtualViewId]);
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
                setupModuleStatus(virtualViewId);
                return true;
            }
            return false;
        }
    }
}
