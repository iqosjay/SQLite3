package com.roy.sqlite3.widget;

import static com.roy.sqlite3.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Roy on 2021/9/28.
 * ListView没有数据的空视图
 */
public class EmptyView extends View {

  private static final String NO_DATA_TEXT = "暂无数据";

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public EmptyView(Context context) {
    super(context);
    paint.setTextSize(sp(24));
    paint.setColor(Color.DKGRAY);
    paint.setFakeBoldText(true);
    paint.setTextAlign(Paint.Align.CENTER);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    final int height = getHeight();
    canvas.drawText(NO_DATA_TEXT, getWidth() >> 1, height >> 1, paint);
  }

}
