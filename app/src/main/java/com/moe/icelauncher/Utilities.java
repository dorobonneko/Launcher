package com.moe.icelauncher;
import android.graphics.Bitmap;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.os.Build;

public class Utilities
{
	private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
	
	public final static boolean ATLEAST_KITKAT=Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;
	public final static boolean ATLEAST_LOLLIPOP=Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP;
	public static final boolean ATLEAST_LOLLIPOP_MR1 =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
	public static final boolean ATLEAST_JB_MR1 =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	public static final boolean ATLEAST_OREO_MR1 =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1;

    public static final boolean ATLEAST_OREO =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    public static final boolean ATLEAST_NOUGAT_MR1 =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;

    public static final boolean ATLEAST_NOUGAT =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    public static final boolean ATLEAST_MARSHMALLOW =
	Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

  
	public static Bitmap createIconBitmap(Cursor c, int iconIndex, Context context) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return createIconBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }
	public static byte[] flattenBitmap(Bitmap bitmap) {
        if(bitmap==null)return null;
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
             return null;
        }
    }
	public static byte[] flattenDrawable(Drawable drawable,Context context) {
		Bitmap bitmap=createIconBitmap(drawable,context);
		if(bitmap==null)return null;
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
			return null;
        }
    }
	public static Bitmap drawable2Bitmap(Drawable drawable){
		if(drawable==null)return null;
		synchronized(sCanvas){
			Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
			sCanvas.setBitmap(bitmap);
			sOldBounds.set(drawable.getBounds());
			drawable.setBounds(0,0,bitmap.getWidth(),bitmap.getHeight());
			drawable.draw(sCanvas);
			sCanvas.setBitmap(null);
			drawable.setBounds(sOldBounds);
			return bitmap;
		}
	}
    public static Bitmap createIconBitmap(Bitmap icon, Context context) {
        final int iconBitmapSize = getIconBitmapSize(context);
        if (iconBitmapSize == icon.getWidth() && iconBitmapSize == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), icon), context);
    }
    /**
     * Returns a bitmap suitable for the all apps view. If the package or the resource do not
     * exist, it returns null.
     */
    public static Bitmap createIconBitmap(String packageName, String resourceName,
										  Context context) {
        PackageManager packageManager = context.getPackageManager();
        // the resource
        try {
            Resources resources = packageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);
                return createIconBitmap(
					resources.getDrawableForDensity(id,context.getResources().getDisplayMetrics().densityDpi), context);
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }
	private static int getIconBitmapSize(Context context) {
		return context.getResources().getDisplayMetrics().densityDpi;
    }
	public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) {
            final int iconBitmapSize = getIconBitmapSize(context);

            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = iconBitmapSize;
            int textureHeight = iconBitmapSize;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
													  Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;
            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
	public static final class SharedPreferences{
		public final static String ALLOW_ROTATION_PREFERENCE_KEY="pref_allowRotation";
		public final static String COLNUMS="colnums";
		public final static String UNINSTALL="uninstall";
		public final static String PREVIEWTABLE="preview_table";
		
	}
}
