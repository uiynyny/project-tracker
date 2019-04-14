package com.csi5175.projecttracker.recyclerViewHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.csi5175.projecttracker.R;

/*
* https://www.journaldev.com/23164/android-recyclerview-swipe-to-delete-undo
* */
abstract public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final ColorDrawable mBackground;
    private final int backgroundColor;
    private final Paint mClearPaint;
    private final Drawable deleteDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public SwipeToDeleteCallback(Context context) {
        mBackground = new ColorDrawable();
        backgroundColor = Color.parseColor("#b80f0a");
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();

    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;
        if (isCancelled) {
            clearCanvas(c, (float)itemView.getRight() + dX, (float)itemView.getTop(), (float)itemView.getRight(), (float)itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        mBackground.setColor(backgroundColor);
        mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        mBackground.draw(c);

        int deletIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconLeft = itemView.getRight() - deletIconMargin - intrinsicWidth;
        int deleteIconRight = itemView.getRight() - deletIconMargin;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, float left, float top, float right, float bottom) {
        c.drawRect(left,top,right,bottom,mClearPaint);
    }

    @Override
    public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }
}
