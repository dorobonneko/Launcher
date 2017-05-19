package com.moe.utils;
import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageScale
{
	public static Bitmap Scale(Bitmap b,float width,float height){		// 设置想要的大小
		float scaleWidth = width / b.getWidth();
		float scaleHeight =height/ b.getHeight();
		// 取得想要缩放的matrix参数
		float scale=scaleWidth >= scaleHeight ?scaleWidth: scaleHeight;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		Bitmap newBitMap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		//b.recycle();
		return newBitMap;
	}
}
