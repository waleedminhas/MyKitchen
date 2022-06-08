package comp5216.sydney.edu.au.ourkitchen.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.ui.createpost.CreatePost;
import comp5216.sydney.edu.au.ourkitchen.utils.images.ImageUtilies;

/**
 * Worker class to be added to the work manager queue of tasks to complete asynchronously
 */
public class UploadWorker extends Worker {
    Context context;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    /**
     * Specifies the work for each upload worker to execute when the constraints are met
     *
     * @return the result of the work
     */
    @NonNull
    @Override
    public Result doWork() {
        if (!isStopped()) {
            String photoFilePath = getInputData().getString(CreatePost.PHOTO_URI);
            String userID = getInputData().getString(CreatePost.POST_USER_ID);
            float rating = getInputData().getFloat(CreatePost.POST_RATING, 0);
            String link = getInputData().getString(CreatePost.POST_LINK);
            String modification = getInputData().getString(CreatePost.POST_MODIFICATION);
            String comment = getInputData().getString(CreatePost.POST_COMMENT);

            List<String> hashtags = new ArrayList<>();
            String[] hashtagsArray = getInputData().getStringArray(CreatePost.POST_TAGS);
            if (hashtagsArray != null) {
                hashtags = Arrays.asList(hashtagsArray);
            }

            Post post = new Post(userID, null, rating, link, modification, comment, null,
                    new Timestamp(new Date()), hashtags);

            uploadToFirestore(photoFilePath, post);
            return Result.success();
        }
        return Result.retry();
    }

    /**
     * Upload the post to Firestore.
     *
     * @param photoFilePath local path to photo file
     * @param post          post to upload
     */
    private void uploadToFirestore(String photoFilePath, Post post) {
        try {
            DocumentReference document =
                    FirebaseFirestore.getInstance().collection(Constants.POSTS_COLLECTION).document();

            post.setId(document.getId());

            if (photoFilePath != null) {
                Bitmap bitmapImage = CreatePost.decodeBitmap(getApplicationContext(),
                        Uri.parse(photoFilePath));

                Uri imgUri = Uri.parse(photoFilePath);
                int rotationInDegrees = ImageUtilies.getRotationInDegrees(imgUri,
                        getApplicationContext());

                Bitmap correctedBitmap = ImageUtilies.transform(bitmapImage, rotationInDegrees);

                StorageReference mediaRef = FirebaseStorage.getInstance().getReference().child(
                        "images/" + post.getUserID() + "-" + photoFilePath);

                document.get().addOnSuccessListener(documentSnapshot -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    correctedBitmap.compress(Bitmap.CompressFormat.WEBP, 75, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = mediaRef.putBytes(data);

                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(exception -> Log.i("UPLOAD", "upload " +
                            "failed")).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            Log.i("FirebaseStorage", task.getException().toString());
                        }
                        return mediaRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Upload to Firestore database
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                post.setLinkToPhoto(downloadUri.toString());
                                Log.i("FirestoreDB", "uploaded: " + downloadUri.toString());
                            }
                            document.set(post).addOnCompleteListener(task1 -> Toast.makeText(context, context.getResources().getString(R.string.upload_post_success_toast), Toast.LENGTH_SHORT).show());
                        }
                    }));
                });
            } else {
                document.set(post).addOnCompleteListener(task -> Toast.makeText(context,
                        context.getResources().getString(R.string.post_uploaded), Toast.LENGTH_SHORT).show());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
