package riceball.xyz.indicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import java.io.Serializable;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class InfiniteCirclePageIndicator extends View implements ViewPager.OnPageChangeListener {

    //
    PoContainer poContainer;

    public InfiniteCirclePageIndicator(Context context) {
        this(context, null);
    }

    public InfiniteCirclePageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfiniteCirclePageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int defRadius = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        int defGap = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InfiniteCirclePageIndicator, defStyleAttr, 0);
        int radius = a.getDimensionPixelSize(R.styleable.InfiniteCirclePageIndicator_ind_radius, defRadius);
        int gap = a.getDimensionPixelSize(R.styleable.InfiniteCirclePageIndicator_ind_gap, defGap);
        int selectedColor = a.getColor(R.styleable.InfiniteCirclePageIndicator_ind_selected_color, Color.BLUE);
        int unSelectedColor = a.getColor(R.styleable.InfiniteCirclePageIndicator_ind_unselected_color, Color.WHITE);
        a.recycle();
        poContainer = new PoContainer(selectedColor, unSelectedColor);
        poContainer.radius = radius;
        poContainer.pointGap = gap;
        if (isInEditMode()) {
            setCount(9, 5, 0);
        }
    }

    public void setCount(int dataCount, int pointCount, int selectedIndex) {
        poContainer.init(dataCount, pointCount, selectedIndex);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getShowWidth(widthMeasureSpec), getShowHeight(heightMeasureSpec));
    }

    private int getShowWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY)) {
            result = specSize;
        } else {
            result = getPaddingLeft() + getPaddingRight() + poContainer.controlLine.length * poContainer.getRadius() * 2 + (poContainer.controlLine.length - 1) * poContainer.pointGap;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int getShowHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY)) {
            result = specSize;
        } else {
            result = getPaddingTop() + getPaddingBottom() + poContainer.getRadius() * 2;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        poContainer.draw(canvas);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentSelected(position);
    }

    private void setCurrentSelected(int position) {
        poContainer.setCurrentSelected(position);
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    class PoContainer implements Serializable {
        public static final float SCALE_RATIO = 0.5f;
        SparseArray<Po> pos;
        Paint soldPaint;
        Paint selectedPaint;
        int offset;
        //
        int selectedIndex;
        int animLastSelectedIndex;
        ControlLine controlLine;
        private int radius;
        private int pointGap;
        private boolean inAnim;

        public PoContainer(int selectedColor, int unSelectedColor) {
            pos = new SparseArray<>();
            soldPaint = new Paint(ANTI_ALIAS_FLAG);
            soldPaint.setColor(unSelectedColor);
            selectedPaint = new Paint(ANTI_ALIAS_FLAG);
            selectedPaint.setColor(selectedColor);
        }

        public void init(int dataCount, int pointCount, int selectedIndex) {
            if (pointCount > dataCount) {
                pointCount = dataCount;
            }
            controlLine = new ControlLine(0, dataCount);
            controlLine.setLine(selectedIndex, pointCount);
            this.selectedIndex = selectedIndex;

            for (int i = 0; i < dataCount; i++) {
                Po po = new Po().setIndex(i).setDrawX(getDrawX(i)).setDrawY(getDrawY(i));
                if (i == 0 || i == dataCount - 1 || controlLine.include(i)) {
                    po.setRadius(getRadius());
                } else {
                    po.setRadius(getRadius() * SCALE_RATIO);
                }
                if (controlLine.isFirst(i) && controlLine.canMove(i - 1)) {
                    po.setRadius(getRadius() * SCALE_RATIO);
                }
                if (controlLine.isLast(i) && controlLine.canMove(i + 1)) {
                    po.setRadius(getRadius() * SCALE_RATIO);
                }
                pos.put(i, po);
            }
            setCurrentSelected(selectedIndex);
        }


        private int getRadius() {
            return radius;
        }

        private int getDrawY(int i) {
            return radius;
        }

        private int getDrawX(int i) {
            return getRadius() + getRadius() * 2 * i + pointGap * i;
        }

        public void draw(Canvas canvas) {
            if (pos.size() <= 0) return;
            for (int i = controlLine.index; i < controlLine.index + controlLine.length; i++) {
                Po po = pos.get(i);
//                if (controlLine.include(i)) {
//                    soldPaint.setColor(unSelectedColor);
                canvas.drawCircle(po.drawX + offset, po.drawY, po.getRadius(), soldPaint);
//                } else {
//                    soldPaint.setColor(0x55555555);
//                    canvas.drawCircle(po.drawX + offset, po.drawY, po.getRadius(), soldPaint);
//                }
//                canvas.drawText(i + "", po.drawX + offset, po.drawY, selectedPaint);
                if (inAnim && animLastSelectedIndex == i) {
                    canvas.drawCircle(po.drawX + offset, po.drawY, po.getRadius(), selectedPaint);
                } else if (!inAnim && selectedIndex == i) {
                    canvas.drawCircle(po.drawX + offset, po.drawY, po.getRadius(), selectedPaint);
                }
            }
        }

        public void setCurrentSelected(int position) {
            position = controlLine.fixIndex(position);
            int lastSelectedIndex = selectedIndex;
            selectedIndex = position;
            if (controlLine.include(position)) {
                if (controlLine.isFirst(position) && controlLine.canMove(controlLine.index - 1)) {
                    controlLine.moveTo(controlLine.index - 1);
                    doAnim(lastSelectedIndex);
                } else if (controlLine.isLast(position) && controlLine.canMove(controlLine.index + 1)) {
                    controlLine.moveTo(controlLine.index + 1);
                    doAnim(lastSelectedIndex);
                } else {
                    //...
                }
            } else {
                controlLine.moveTo(selectedIndex);
                offset = getOffset(selectedIndex);
                setCurrentSelected(selectedIndex);
            }
            invalidate();
        }

        private void doAnim(int lastSelectedIndex) {
            animLastSelectedIndex = lastSelectedIndex;
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator animator = ValueAnimator.ofInt(offset, getOffset(controlLine.index));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    PoContainer.this.updateOffset((int) animation.getAnimatedValue());
                }
            });
            animator.setDuration(200);

            ValueAnimator scaleDownAnimator = ValueAnimator.ofFloat(1f, SCALE_RATIO);
            scaleDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Po left = pos.get(controlLine.getFirst());
                    if (left != null && controlLine.canMove(controlLine.getFirst() - 1)) {
                        left.radius = Math.min(left.radius, PoContainer.this.getRadius() * (float) animation.getAnimatedValue());
                    }
                    //
                    Po right = pos.get(controlLine.getLast());
                    if (right != null && controlLine.canMove(controlLine.getLast() + 1)) {
                        right.radius = Math.min(right.radius, PoContainer.this.getRadius() * (float) animation.getAnimatedValue());
                    }
                }
            });
            scaleDownAnimator.setDuration(200);
            //
            ValueAnimator scaleUpAnimator = ValueAnimator.ofFloat(SCALE_RATIO, 1f);
            scaleUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (int i = controlLine.index; i < controlLine.index + controlLine.length; i++) {
                        Po po = pos.get(i);
                        if (po != null && po.radius != PoContainer.this.getRadius() && !controlLine.isFirst(i) && !controlLine.isLast(i)) {
                            po.radius = PoContainer.this.getRadius() * (float) animation.getAnimatedValue();
                        }
                    }
                }
            });
            scaleUpAnimator.setDuration(200);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    inAnim = true;

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    inAnim = false;
                }
            });
            animatorSet.playTogether(animator, scaleDownAnimator, scaleUpAnimator);
            animatorSet.start();
        }

        private int getOffset(int index) {
            return -(getRadius() * 2 * index + pointGap * index);
        }

        private void updateOffset(int animatedValue) {
            offset = animatedValue;
            invalidate();
        }
    }

    private static class Po implements Serializable {
        int index;
        float drawX;
        float drawY;
        float radius;

        public int getIndex() {
            return index;
        }

        public Po setIndex(int index) {
            this.index = index;
            return this;
        }

        public float getDrawX() {
            return drawX;
        }

        public Po setDrawX(float drawX) {
            this.drawX = drawX;
            return this;
        }

        public float getDrawY() {
            return drawY;
        }

        public Po setDrawY(float drawY) {
            this.drawY = drawY;
            return this;
        }

        public float getRadius() {
            return radius;
        }

        public Po setRadius(float radius) {
            this.radius = radius;
            return this;
        }
    }


    private static class ControlLine implements Serializable {
        int index;
        int length;
        int limitStart;
        //        int limitEnd;
        int limitLength;

        public ControlLine(int limitStart, int limitLength) {
            if (limitLength - limitStart < 0) {
                throw new RuntimeException("ex: end > start");
            }
            this.limitStart = limitStart;
            this.limitLength = limitLength;
        }

        public void setLine(int index, int length) {
            length = Math.min(length, limitLength);
            index = fixIndex(index);
            this.index = index;
            this.length = length;
        }

        private int fixIndex(int index) {
            if (index > limitStart + limitLength) index = limitStart + limitLength - 1;
            if (index < limitStart) index = limitStart;
            return index;
        }

        public boolean canMove(int index) {
            return index >= limitStart && index < limitStart + limitLength - 1;
        }

        public int moveTo(int index) {
            index = fixIndex(index);
            if (index < limitStart) {
                index = limitStart;
            }
            if (index + length > limitStart + limitLength) {
                this.index = limitStart + limitLength - length;
            } else {
                this.index = index;
            }
            return this.index;
        }

        public boolean include(int index) {
            return this.index <= index && this.index + this.length > index;
        }

        public boolean isFirst(int index) {
            return index == this.index;
        }

        public boolean isLast(int index) {
            return index == this.index + length - 1;
        }

        public int getFirst() {
            return index;
        }

        public int getLast() {
            return index + length - 1;
        }
    }
}
