package io.square1.limor.scenes.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;

import com.coremedia.iso.boxes.Container;
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
import java.util.TimeZone;

import io.square1.limor.R;
import timber.log.Timber;

public class Commons {


    private static final long ONE_SECOND = 1000;
    private static final long ONE_MINUTE = ONE_SECOND * 60;
    private static final long ONE_HOUR = ONE_MINUTE * 60;



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

}
