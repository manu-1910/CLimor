package io.square1.limor.scenes.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.TypedValue;

import androidx.annotation.Nullable;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.coremedia.iso.boxes.Container;
import com.google.gson.Gson;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import io.square1.limor.R;
import io.square1.limor.common.Constants;
import io.square1.limor.uimodels.UIUser;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

public class Commons {

    private static Commons instance;
    private static final long ONE_SECOND = 1000;
    private static final long ONE_MINUTE = ONE_SECOND * 60;
    private static final long ONE_HOUR = ONE_MINUTE * 60;

    public static final int IMAGE_TYPE_PROFILE = 0;
    public static final int IMAGE_TYPE_PODCAST = 1;
    public static final int AUDIO_TYPE_PODCAST = 2;
    public static final int AUDIO_TYPE_COMMENT = 3;
    public static final int IMAGE_TYPE_ATTACHMENT = 4;
    public static final int IMAGE_TYPE_ATTACHMENT_VIDEO = 5;
    public static final int AUDIO_TYPE_ATTACHMENT = 6;

    public interface AudioUploadCallback {
        void onSuccess(String audioUrl);
        void onError(String error);
    }

    public interface ImageUploadCallback {
        void onStateChanged(int id, TransferState state);
        void onProgressChanged(int id, long bytesCurrent, long bytesTotal);
        void onError(String error);
        void onSuccess(String completeUrl);
    }

    private File imageFile;
    private String imageUrl;
    public boolean isImageReadyForUpload;

    public static Commons getInstance() {
        if (instance == null) {
            instance = new Commons();
        }
        return instance;
    }
    private Commons() {
    }

    public static int dpToPx(Context context, int dps) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics()));
    }


    public static String getLengthFromEpochForPlayer(long milliSeconds) {
        DateTime time = new DateTime(milliSeconds);
        final DateTime hour = time.secondOfMinute().roundHalfCeilingCopy();
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, hour.getSecondOfMinute());
        if (milliSeconds <= ONE_HOUR) {
            return String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", calendar.get(Calendar.HOUR) - 1, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        }
    }


    public static void showAlertYesNo(Context context, int title, int message, DialogInterface.OnClickListener listener) {
        showAlertCustomButtons(context, context.getString(title), context.getString(message), listener, context.getString(R.string.yes), null, context.getString(R.string.no));
    }

    public static void showAlertYesNo(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        showAlertCustomButtons(context, title, message, listener, context.getString(R.string.yes), null, context.getString(R.string.no));
    }

    public static void showAlertCustomButtons(Context context, String title, String message, DialogInterface.OnClickListener listenerPositive, String stringButtonPositive,
                                              DialogInterface.OnClickListener listenerNegative, String stringButtonNegative) {
        if (context != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            if (title != null && !title.equals("")) {
                alert.setTitle(title);
            }
            alert.setMessage(message);
            alert.setPositiveButton(stringButtonPositive, listenerPositive);
            if (listenerNegative != null || stringButtonNegative != null) {
                alert.setNegativeButton(stringButtonNegative, listenerNegative);
            }
            alert.setCancelable(false);
            try {
                alert.show();
            } catch (Exception ex) {
                // let it fail when activity is closing
            }
        }
    }

    //public static String getLengthFromEpochForPlayer(double milliSeconds) {
    //    return getLengthFromEpochForPlayer((long)milliSeconds);
    //}


    public static boolean mergeMediaFiles(boolean isAudio, ArrayList<File> sourceFiles, String targetFile) {
        try {
            String mediaKey = isAudio ? "soun" : "vide";
            List<com.googlecode.mp4parser.authoring.Movie> listMovies = new ArrayList<com.googlecode.mp4parser.authoring.Movie>();

            for (File filename : sourceFiles) {
                listMovies.add(MovieCreator.build(filename.getAbsolutePath()));
            }
            List<Track> listTracks = new LinkedList<>();
            for (com.googlecode.mp4parser.authoring.Movie movie : listMovies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals(mediaKey)) {
                        listTracks.add(track);
                    }
                }
            }
            com.googlecode.mp4parser.authoring.Movie outputMovie = new com.googlecode.mp4parser.authoring.Movie();
            if (!listTracks.isEmpty()) {
                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
            }
            Container container = new DefaultMp4Builder().build(outputMovie);
            FileChannel fileChannel = new RandomAccessFile(String.format(targetFile), "rws").getChannel();
            container.writeContainer(fileChannel);
            fileChannel.close();
            return true;
        }
        catch (Exception e) {
            Timber.e("Error merging media files. exception: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteFilesInArray(ArrayList<File> filesToDelete) {
        try {
            for (File singleFile : filesToDelete){
                singleFile.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean mergeAmrAudioFiles(ArrayList<File> files, String mergedFilePath){

        // Merge files
        try {
            FileOutputStream fos = new FileOutputStream(mergedFilePath);

            for (int i = 0,len = files.size();i<len;i++) {
                File file = new File(files.get(i).getAbsolutePath());
                FileInputStream fis = new FileInputStream(file);

                // Skip file header bytes,
                // amr file header's length is 6 bytes
                if (i > 0) {
                    for (int j=0; j<6; j++) {
                        fis.read();
                    }
                }

                byte[] buffer = new byte[512];
                int count = 0;
                while ( (count = fis.read(buffer)) != -1 ) {
                    fos.write(buffer,0,count);
                }

                fis.close();
                fos.flush();
                file.delete();
            }

            fos.flush();
            fos.close();

            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void uploadAudio(Context context, File audioFile, int audioType, final AudioUploadCallback callback) {
        if (audioFile == null) {
            callback.onError(context.getString(R.string.error_something_went_wrong));
            return;
        }

        String path = "";
        switch (audioType) {
            case Constants.AUDIO_TYPE_PODCAST:
                path = Constants.AWS_FOLDER_AUDIO_PODCAST + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + ".amr";
                break;
            case Constants.AUDIO_TYPE_COMMENT:
                path = Constants.AWS_FOLDER_AUDIO_COMMENT + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + ".amr";
                break;
            case Constants.AUDIO_TYPE_ATTACHMENT:
                path = Constants.AWS_FOLDER_MESSAGE_ATTACHMENTS + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + ".amr";
                break;
            default:
                break;
        }

        final String completeUrl = Constants.AWS_IMAGE_BASE_URL + path;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/amr");
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                Constants.AWS_IDENTITY_POOL,
                Regions.EU_WEST_1
        );
        final AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, context);
        TransferObserver observer = transferUtility.upload(
                Constants.AWS_BUCKET,
                path,
                audioFile,
                metadata);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    //isImageReadyForUpload = false;
                    callback.onSuccess(completeUrl);
                }
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {}
            @Override
            public void onError(int id, Exception ex) {
                callback.onError(ex.getLocalizedMessage());
            }
        });
    }


    public void uploadImage(Context context, final ImageUploadCallback imageUploadCallback, final int imageType) {

        String imageUrlToUpload = imageUrl;
        File imageFileToUpload = imageFile;

        if (imageFileToUpload == null) {
            imageUploadCallback.onError(context.getString(R.string.error_something_went_wrong));
            return;
        }
        final String completeUrl = Constants.AWS_IMAGE_BASE_URL + imageUrlToUpload;
        ObjectMetadata metadata = new ObjectMetadata();
        if (imageType == IMAGE_TYPE_ATTACHMENT_VIDEO) {
            metadata.setContentType("video/mp4");
        } else {
            metadata.setContentType("image/png");
        }
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                Constants.AWS_IDENTITY_POOL,
                Regions.EU_WEST_1
        );
        final AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, context);
        TransferObserver observer = transferUtility.upload(
                Constants.AWS_BUCKET,
                imageUrlToUpload,
                imageFileToUpload,
                metadata);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    isImageReadyForUpload = false;
                    imageUploadCallback.onSuccess(completeUrl);
                }
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {}
            @Override
            public void onError(int id, Exception ex) {
                imageUploadCallback.onError(ex.getLocalizedMessage());
            }
        });
    }





    public void handleImage(Context context, int imageType, File imageFile, @Nullable String email) {
        long userId = 0;

        SharedPreferences mPrefs = context.getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user_key", "");
        UIUser userObj = gson.fromJson(json, UIUser.class);

        try {
            userId = userObj.getId();
        } catch(Exception ex) {
            // ignore
        }
        String timestamp = String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1);
        String fileName;
        switch (imageType) {
            case IMAGE_TYPE_PROFILE:
                fileName = Constants.AWS_FILE_PROFILE_IMAGE_IDENTIFIER + "_" + email + "_" + timestamp;
                imageUrl = Constants.AWS_FOLDER_PROFILE_IMAGE + ShoSha265.hash(fileName) + ".png";
                this.imageFile = imageFile;
                break;
            case IMAGE_TYPE_PODCAST:
                fileName = Constants.AWS_FILE_PODCAST_IMAGE_IDENTIFIER + timestamp;
                imageUrl = Constants.AWS_FOLDER_PODCAST_IMAGE + ShoSha265.hash(fileName) + ".png";
                this.imageFile = imageFile;
                break;
            case IMAGE_TYPE_ATTACHMENT:
                fileName = Constants.AWS_FILE_MESSAGE_ATTACHMENT + "_" + timestamp + userId;
                this.imageUrl = Constants.AWS_FOLDER_MESSAGE_ATTACHMENTS + Constants.AWS_FOLDER_IMAGE + ShoSha265.hash(fileName) + ".png";
                this.imageFile = imageFile;
                break;
            case IMAGE_TYPE_ATTACHMENT_VIDEO:
                fileName = Constants.AWS_FILE_MESSAGE_ATTACHMENT + "_" + timestamp + userId;
                this.imageUrl = Constants.AWS_FOLDER_MESSAGE_ATTACHMENTS + Constants.AWS_FOLDER_VIDEO + ShoSha265.hash(fileName) + ".mp4";
                this.imageFile = imageFile;
                break;
            case AUDIO_TYPE_ATTACHMENT:
                fileName = Constants.AWS_FILE_MESSAGE_ATTACHMENT + "_" + timestamp + userId;
                this.imageUrl = Constants.AWS_FOLDER_MESSAGE_ATTACHMENTS + Constants.AWS_FOLDER_AUDIO + ShoSha265.hash(fileName) + ".mp4";
                this.imageFile = imageFile;
                break;
            default:
        }
        isImageReadyForUpload = true;
    }




}
