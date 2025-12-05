package com.happym.mathsquare.Animation;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.FrameLayout;

public class VignetteEffect {

    public static void apply(Context context, FrameLayout backgroundFrame) {
        int width = backgroundFrame.getWidth();
        int height = backgroundFrame.getHeight();

        if (width == 0 || height == 0) return;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        RadialGradient gradient = new RadialGradient(
                width / 2f, height / 2f,
                Math.max(width, height) * 0.8f,
                new int[]{Color.parseColor("#FFEF47"), Color.parseColor("#898021"), Color.parseColor("#504A31")},
                new float[]{0.2f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        );

        Paint paint = new Paint();
        paint.setShader(gradient);
        paint.setAlpha(180);

        canvas.drawRect(0, 0, width, height, paint);

        backgroundFrame.setBackground(new BitmapDrawable(context.getResources(), bitmap));
    }
}

