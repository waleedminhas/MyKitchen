package comp5216.sydney.edu.au.ourkitchen.utils.images;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * RotateTransformation class extends {@link BitmapTransformation} to enable
 * easy transformations with the Glide image library.
 */
public class RotateTransformation extends BitmapTransformation {
    private final float rotateRotationAngle;

    /**
     * Constructor
     *
     * @param rotateRotationAngle the rotation angle required of the transformations
     */
    public RotateTransformation(float rotateRotationAngle) {
        this.rotateRotationAngle = rotateRotationAngle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform,
                               int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateRotationAngle);
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(),
                toTransform.getHeight(), matrix, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(("rotate" + rotateRotationAngle).getBytes());
    }
}
