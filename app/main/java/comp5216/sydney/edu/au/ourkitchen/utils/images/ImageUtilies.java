package comp5216.sydney.edu.au.ourkitchen.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Image Utility class
 */
public class ImageUtilies {

    /**
     * Converts an exif orientation to degrees
     *
     * @param exifOrientation an int of rotation from exif data
     * @return the amount of rotation in degrees
     */
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    /**
     * Gets the rotation amount from an image file in degrees.
     *
     * @param imageFile an image file
     * @return rotation amount in degrees
     * @throws IOException uses image file
     */
    public static int getRotationInDegrees(File imageFile) throws IOException {
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        return exifToDegrees(rotation);
    }

    /**
     * Gets the rotation in degrees using an image uri and a context
     *
     * @param imageUri an image uri
     * @param ctx      context to find the image
     * @return int degrees of rotation
     * @throws IOException Thrown if input stream throws error
     */
    public static int getRotationInDegrees(Uri imageUri, Context ctx) throws IOException {

        try (InputStream inputStream = ctx.getContentResolver().openInputStream(imageUri)) {
            ExifInterface exif = new ExifInterface(inputStream);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            return exifToDegrees(rotation);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Performs a bitmap transform with a certain rotation angle
     *
     * @param toTransform         the bitmap to transform
     * @param rotateRotationAngle the rotation angle in degrees
     * @return the rotated bitmap
     */
    public static Bitmap transform(Bitmap toTransform, int rotateRotationAngle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(rotateRotationAngle);

        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(),
                toTransform.getHeight(), matrix, true);
    }
}
