package io.square1.limor.scenes.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;
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
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.coremedia.iso.boxes.Container;
import com.google.gson.Gson;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

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

    //public static final String audioFileFormat = ".amr";
    public static final String audioFileFormat = ".wav";

    public interface AudioUploadCallback {
        void onSuccess(String audioUrl);

        void onError(String error);
    }

    public interface AudioDownloadCallback {
        void onSuccess(File downloadedFile);

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
    // private FFmpeg ffmpeg;

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

    public static String getHourMinutesFromDateString(String dateString) {
        DateTime dateTime = new DateTime(dateString, DateTimeZone.getDefault());
        String hour = String.valueOf(dateTime.getHourOfDay());
        hour = (hour.length() == 1) ? ("0" + hour) : hour;
        String minuteOfHour = String.valueOf(dateTime.getMinuteOfHour());
        minuteOfHour = (minuteOfHour.length() == 1) ? ("0" + minuteOfHour) : minuteOfHour;
        return hour + ":" + minuteOfHour;
    }

    public static String getDatePlusHourMinutesFromDateInt(int dateInt){
        DateTime dateTime = new DateTime( dateInt, DateTimeZone.getDefault() );

        String dayOfMonth = dateTime.dayOfMonth().getAsString();
        String monthOfYear = dateTime.monthOfYear().getAsString();
        String year = dateTime.year().getAsString();
        String date = dayOfMonth + "/" + monthOfYear + "/" + year;

        String hour = String.valueOf(dateTime.getHourOfDay());
        hour = (hour.length() == 1) ? ("0" + hour) : hour;
        String minuteOfHour = String.valueOf(dateTime.getMinuteOfHour());
        minuteOfHour = (minuteOfHour.length() == 1) ? ("0" + minuteOfHour) : minuteOfHour;

        return date + " " + hour + ":" + minuteOfHour;
    }

    public static String getHumanReadableTimeFromMillis(int durationMillis){
        int minutes = durationMillis / 1000 / 60;
        int seconds = durationMillis / 1000 % 60;
        return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
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
        } catch (Exception e) {
            Timber.e("Error merging media files. exception: " + e.getMessage());
            return false;
        }
    }


    public static boolean deleteFilesInArray(ArrayList<File> filesToDelete) {
        try {
            for (File singleFile : filesToDelete) {
                singleFile.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean mergeAmrAudioFiles(ArrayList<File> files, String mergedFilePath) {

        // Merge files
        try {
            FileOutputStream fos = new FileOutputStream(mergedFilePath);

            for (int i = 0, len = files.size(); i < len; i++) {
                File file = new File(files.get(i).getAbsolutePath());
                FileInputStream fis = new FileInputStream(file);

                // Skip file header bytes,
                // amr file header's length is 6 bytes
                if (i > 0) {
                    for (int j = 0; j < 6; j++) {
                        fis.read();
                    }
                }

                byte[] buffer = new byte[512];
                int count = 0;
                while ((count = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }

                fis.close();
                fos.flush();
                file.delete();
            }

            fos.flush();
            fos.close();

            //this.finalAudioPath = mergedFilePath;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public static boolean mergeWavAudioFiles(ArrayList<File> files, String mergedFilePath) {

        /*
        *    mRecorder.waveConfig.sampleRate = 44100
            mRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
            mRecorder.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        * */

        boolean respuesta = true;

        int RECORDER_SAMPLERATE = 44100;
        try {
            DataOutputStream amplifyOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() + "/limorv2/" + System.currentTimeMillis() + audioFileFormat)));
            DataInputStream[] mergeFilesStream = new DataInputStream[files.size()];
            long[] sizes = new long[files.size()];
            for (int i = 0; i < files.size(); i++) {
                File file = new File(files.get(i).getAbsolutePath());
                sizes[i] = (file.length() - 44) / 2;
            }
            for (int i = 0; i < files.size(); i++) {
                mergeFilesStream[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(files.get(i).getAbsolutePath())));

                if (i == files.size() - 1) {
                    mergeFilesStream[i].skip(24);
                    byte[] sampleRt = new byte[4];
                    mergeFilesStream[i].read(sampleRt);
                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
                    RECORDER_SAMPLERATE = bbInt.getInt();
                    mergeFilesStream[i].skip(16);
                } else {
                    mergeFilesStream[i].skip(44);
                }

            }

            for (int b = 0; b < files.size(); b++) {
                for (int i = 0; i < (int) sizes[b]; i++) {
                    byte[] dataBytes = new byte[2];
                    try {
                        dataBytes[0] = mergeFilesStream[b].readByte();
                        dataBytes[1] = mergeFilesStream[b].readByte();
                    } catch (EOFException e) {
                        amplifyOutputStream.close();
                    }
                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    float dataInFloat = (float) dataInShort / 37268.0f;


                    short outputSample = (short) (dataInFloat * 37268.0f);
                    byte[] dataFin = new byte[2];
                    dataFin[0] = (byte) (outputSample & 0xff);
                    dataFin[1] = (byte) ((outputSample >> 8) & 0xff);
                    amplifyOutputStream.write(dataFin, 0, 2);

                }
            }
            amplifyOutputStream.close();
            for (int i = 0; i < files.size(); i++) {
                mergeFilesStream[i].close();
            }

        } catch (IOException e) {
            respuesta = false;
            e.printStackTrace();
        }
        long size = 0;
        try {
            //FileInputStream fileSize = new FileInputStream(Environment.getExternalStorageDirectory() + "/limorv2/" + System.currentTimeMillis() + audioFileFormat);
            FileInputStream fileSize = new FileInputStream(mergedFilePath);
            size = fileSize.getChannel().size();
            fileSize.close();
        } catch (IOException e1) {
            respuesta = false;
            e1.printStackTrace();
        }


        final int RECORDER_BPP = 16;

        long datasize = size + 36;
        long byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE) / 8;
        long longSampleRate = RECORDER_SAMPLERATE;
        byte[] header = new byte[44];


        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (datasize & 0xff);
        header[5] = (byte) ((datasize >> 8) & 0xff);
        header[6] = (byte) ((datasize >> 16) & 0xff);
        header[7] = (byte) ((datasize >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) 1;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) ((RECORDER_BPP) / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (size & 0xff);
        header[41] = (byte) ((size >> 8) & 0xff);
        header[42] = (byte) ((size >> 16) & 0xff);
        header[43] = (byte) ((size >> 24) & 0xff);
        // out.write(header, 0, 44);

        try {
            //RandomAccessFile rFile = new RandomAccessFile(Environment.getExternalStorageDirectory() + "/limorv2/" + System.currentTimeMillis() + audioFileFormat, "rw");
            RandomAccessFile rFile = new RandomAccessFile(mergedFilePath, "rw");
            rFile.seek(0);
            rFile.write(header);
            rFile.close();
        } catch (IOException e) {
            respuesta = false;
            e.printStackTrace();
        }


        return respuesta;














//
//
//        //**********************
//        //isProcessingOn=true;
//
//        int RECORDER_SAMPLERATE = 0;
//        try {
//            DataOutputStream amplifyOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mergedFilePath)));
//            DataInputStream[] mergeFilesStream = new DataInputStream[files.size()];
//            long[] sizes=new long[files.size()];
//            for(int i=0; i<files.size(); i++) {
//                File file = new File(files.get(i).getAbsolutePath());
//                sizes[i] = (file.length()-44)/2;
//            }
//            for(int i =0; i<files.size(); i++) {
//                mergeFilesStream[i] =new DataInputStream(new BufferedInputStream(new FileInputStream(files.get(i).getAbsolutePath())));
//
//                if(i == files.size()-1) {
//                    mergeFilesStream[i].skip(24);
//                    byte[] sampleRt = new byte[4];
//                    mergeFilesStream[i].read(sampleRt);
//                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
//                    RECORDER_SAMPLERATE = bbInt.getInt();
//                    mergeFilesStream[i].skip(16);
//                }
//                else {
//                    mergeFilesStream[i].skip(44);
//                }
//
//            }
//
//            for(int b=0; b<files.size(); b++) {
//                for(int i=0; i<(int)sizes[b]; i++) {
//                    byte[] dataBytes = new byte[2];
//                    try {
//                        dataBytes[0] = mergeFilesStream[b].readByte();
//                        dataBytes[1] = mergeFilesStream[b].readByte();
//                    }
//                    catch (EOFException e) {
//                        amplifyOutputStream.close();
//                    }
//                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
//                    float dataInFloat= (float) dataInShort/37268.0f;
//
//
//                    short outputSample = (short)(dataInFloat * 37268.0f);
//                    byte[] dataFin = new byte[2];
//                    dataFin[0] = (byte) (outputSample & 0xff);
//                    dataFin[1] = (byte)((outputSample >> 8) & 0xff);
//                    amplifyOutputStream.write(dataFin, 0 , 2);
//
//                }
//            }
//            amplifyOutputStream.close();
//            for(int i=0; i<files.size(); i++) {
//                mergeFilesStream[i].close();
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        long size =0;
//        try {
//            FileInputStream fileSize = new FileInputStream(mergedFilePath);
//            size = fileSize.getChannel().size();
//            fileSize.close();
//        } catch (FileNotFoundException e1) {
//            e1.printStackTrace();
//            return false;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//
//        final int RECORDER_BPP = 16;
//
//        long datasize=size+36;
//        long byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE)/8;
//        long longSampleRate = RECORDER_SAMPLERATE;
//        byte[] header = new byte[44];
//
//
//        header[0] = 'R';  // RIFF/WAVE header
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//        header[4] = (byte) (datasize & 0xff);
//        header[5] = (byte) ((datasize >> 8) & 0xff);
//        header[6] = (byte) ((datasize >> 16) & 0xff);
//        header[7] = (byte) ((datasize >> 24) & 0xff);
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//        header[12] = 'f';  // 'fmt ' chunk
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' ';
//        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        header[20] = 1;  // format = 1
//        header[21] = 0;
//        header[22] = (byte) 1;
//        header[23] = 0;
//        header[24] = (byte) (longSampleRate & 0xff);
//        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
//        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
//        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
//        header[28] = (byte) (byteRate & 0xff);
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//        header[32] = (byte) ((RECORDER_BPP) / 8);  // block align
//        header[33] = 0;
//        header[34] = RECORDER_BPP;  // bits per sample
//        header[35] = 0;
//        header[36] = 'd';
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a';
//        header[40] = (byte) (size & 0xff);
//        header[41] = (byte) ((size >> 8) & 0xff);
//        header[42] = (byte) ((size >> 16) & 0xff);
//        header[43] = (byte) ((size >> 24) & 0xff);
//        // out.write(header, 0, 44);
//
//        try {
//            RandomAccessFile rFile = new RandomAccessFile(mergedFilePath, "rw");
//            rFile.seek(0);
//            rFile.write(header);
//            rFile.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//
//        return true;
    }


    public void uploadAudio(Context context, File audioFile, int audioType, final AudioUploadCallback callback) {
        if (audioFile == null) {
            callback.onError(context.getString(R.string.error_something_went_wrong));
            return;
        }

        String path = "";
        switch (audioType) {
            case Constants.AUDIO_TYPE_PODCAST:
                path = Constants.AWS_FOLDER_AUDIO_PODCAST + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + audioFileFormat;
                break;
            case Constants.AUDIO_TYPE_COMMENT:
                path = Constants.AWS_FOLDER_AUDIO_COMMENT + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + audioFileFormat;
                break;
            case Constants.AUDIO_TYPE_ATTACHMENT:
                path = Constants.AWS_FOLDER_MESSAGE_ATTACHMENTS + "audioFile_" + String.valueOf(new Random().nextInt((9999 - 1) + 1) + 1) + "_" + System.currentTimeMillis() + audioFileFormat;
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


    // not working, it should be deleted in the future
//    public void downloadAudio(Context context, String url, String destinationPath, final AudioDownloadCallback callback) {
//        if (url == null || url.equals("")) {
//            callback.onError(context.getString(R.string.error_something_went_wrong));
//            return;
//        }
//
//
//        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        String finalPath = destinationPath + "/" + fileName;
//        File destinationFile = new File(finalPath);
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                context,
//                Constants.AWS_IDENTITY_POOL,
//                Regions.EU_WEST_1
//        );
//        final AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
//        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3).context(context).build();
//        transferUtility.download(
//                Constants.AWS_BUCKET,
//                url,
//                destinationFile,
//                new TransferListener() {
//                    @Override
//                    public void onStateChanged(int id, TransferState state) {
//                        if (state == TransferState.COMPLETED) {
//                            callback.onSuccess(destinationFile);
//                        }
//                    }
//                    @Override
//                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                        Timber.d("AWS Download: id["+id+"] bytesCurrent["+bytesCurrent+"] bytesTotal["+bytesTotal+"]");
//                    }
//                    @Override
//                    public void onError(int id, Exception ex) {
//                        callback.onError(ex.getLocalizedMessage());
//                    }
//                });
//    }


    public void uploadImage(Context context, final ImageUploadCallback imageUploadCallback, final int imageType) {

        String imageUrlToUpload = imageUrl;
        File imageFileToUpload = imageFile;

        if (imageFileToUpload == null) {
            imageUploadCallback.onError(context.getString(R.string.error_something_went_wrong));
            return;
        } else if(!imageFileToUpload.exists() || !imageFileToUpload.isFile()) {
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


    public static void deleteFiles(ArrayList<String> filePaths) {
        for (String cachedPath : filePaths) {
            deleteFile(cachedPath);
        }
    }


    public static void deleteFile(String filePath) {
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        file.delete();
    }


//    public static void convertWavToAmr(File wavFilename, File amrFilename){
//        InputStream wavInputStream = null;
//        FileOutputStream outputStream = null;
//        try {
//            wavInputStream = new FileInputStream(wavFilename);
//            //noinspection ResultOfMethodCallIgnored
//            wavInputStream.skip(44);
//
//            //AMR头文件"#!AMR-WB\n"
//            //byte[] header = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x2D, 0x57, 0x42, 0x0A};
//            //AMR header AMR-NB
//            byte[] header = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};
//
//
//            byte[] wavBuffer = new byte[512];
//            byte[] outBuffer = new byte[512];
//            if (!amrFilename.exists()) {
//                //noinspection ResultOfMethodCallIgnored
//                amrFilename.createNewFile();
//            }
//            outputStream = new FileOutputStream(amrFilename);
//            outputStream.write(header);
//
//            AmrWbEncoder.init();
//            int readSize;
//            while ((readSize = wavInputStream.read(wavBuffer)) != -1) {
//                if (readSize > 0) {
//                    short[] buffer = bytes2shorts(wavBuffer);
//
//                    int encodedSize = AmrWbEncoder.encode(AmrWbEncoder.Mode.MD1825.ordinal(), buffer, outBuffer, 0);
//                    if (encodedSize > 0) {
//                        try {
//                            outputStream.write(outBuffer, 0, encodedSize);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//
//            //Toast.makeText(, "Convert Success", Toast.LENGTH_SHORT).show();
//            System.out.println("Convert wav to amr succesfully");
//            //play();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            closeStream(wavInputStream);
//            closeStream(outputStream);
//            try {
//                AmrWbEncoder.exit();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static short[] bytes2shorts(byte[] input) {
        short[] buffer = new short[input.length / 2];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (short) (((input[2 * i] & 0xFF)) | ((input[2 * i + 1] & 0xFF) << 8));
        }
        return buffer;
    }


    public static void changePermissionsForJave(Context context){

        Process process = null;
        DataOutputStream dataOutputStream = null;

        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("chmod 0755 /data/user/0/io.square1.limor.app.dev/cache/jave-1/ffmpeg\n");
            //dataOutputStream.writeBytes("chmod 0755 /data/user/0/io.square1.limor.app.dev/cache/jave-1/ffmpeg\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void convertWavToAmr2(File wavFilename, File amrFilename){
        Encoder encoder = new Encoder();
        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setFormat("amr");
        AudioAttributes audio = new AudioAttributes();
        audio.setBitRate(Integer.valueOf(64000));
        audio.setChannels(Integer.valueOf(1));
        audio.setSamplingRate(Integer.valueOf(22050));
        audio.setCodec("libamr_nb");
        attributes.setAudioAttributes(audio);

        File source = wavFilename;
        File target = amrFilename;
        try {
            encoder.encode(source, target, attributes);
        } catch (IllegalArgumentException | EncoderException e1) {
            e1.printStackTrace();
        }

    }


    public static void convertWavToAmr3(File wavFilename, File amrFilename){

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new File(amrFilename.getAbsolutePath()), new File(wavFilename.getAbsolutePath()), attrs);
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static boolean CombineWavFilesWithFfmpeg(String file1, String file2, String outPutFile){

        String s1 = "-i "+file1+" -i "+file2+" -filter_complex [0:a][1:a]concat=n=2:v=0:a=1[out] -map [out] "+outPutFile;
        String s2 = "-i "+file1+" -i "+file2+" -filter_complex [0:a][1:a]concat=n=2:v=0:a=1 "+outPutFile;
        String s3 = "-i \"concat:"+file1+"|"+file2+"\" -acodec copy "+outPutFile;
        String s4 = "-i "+file1+" -i "+file2+" -filter_complex [0:a][1:a]concat=n=2:v=0:a=1 -c:a pcm_s16le -vn -dn -sn -strict -2 "+outPutFile;

        int rc = FFmpeg.execute(s4);

        if (rc == RETURN_CODE_SUCCESS) {
            Timber.i("Command execution completed successfully.");

            try {
                deleteFile(file1);
                deleteFile(file2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;

        } else if (rc == RETURN_CODE_CANCEL) {
            Timber.i("Command execution cancelled by user.");
            return false;
        } else {
            Timber.i("Command execution failed with rc=%d and the output below.", rc);
            //Config.printLastCommandOutput(Log.INFO)
            return false;
        }

    }


    public static boolean CombineWaveFile(String file1, String file2, String outPutFile, boolean skipFirst, boolean skipSecond) {
        int RECORDER_SAMPLERATE = 16000;
        int RECORDER_BPP = 16;
        int bufferSize = 1024;
        boolean retorno = true;

        FileInputStream in1 = null, in2 = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long sizein1 = 0;
        long sizein2 = 0;
        long byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE * channels) / 8;
        System.out.println("--------- byterate is: " + byteRate);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, channels, AudioFormat.ENCODING_PCM_16BIT) * 3;

        byte[] data = new byte[bufferSize];
        //byte[] data = new byte[4096];

        try {
            in1 = new FileInputStream(file1);
            in2 = new FileInputStream(file2);
            out = new FileOutputStream(outPutFile);

            sizein1 = in1.getChannel().size();
            sizein2 = in2.getChannel().size();
            totalAudioLen = sizein1 + sizein2;
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate, RECORDER_BPP);

            //Skip the blip noise at start of the first audio file
            if(skipFirst){
                in1.skip(20000);
                System.out.println("Skipping 20000 in first file");
            }
            //***********************************************

            while (in1.read(data) != -1) {
                out.write(data);
            }

            //Skip the blip noise at start of the second audio file
            if(skipSecond){
                in2.skip(20000);
                System.out.println("Skipping 20000 in second file");
            }
            //***********************************************

            while (in2.read(data) != -1) {
                out.write(data);
            }

            out.close();

            in1.close();
            in2.close();

            out.close();
            out.flush();

            System.out.println("Done");
        } catch (IOException e) {
            retorno = false;
            e.printStackTrace();
        }
        return retorno;
    }

    public static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate, int RECORDER_BPP) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte)(totalDataLen & 0xff);
        header[5] = (byte)((totalDataLen >> 8) & 0xff);
        header[6] = (byte)((totalDataLen >> 16) & 0xff);
        header[7] = (byte)((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte)(longSampleRate & 0xff);
        header[25] = (byte)((longSampleRate >> 8) & 0xff);
        header[26] = (byte)((longSampleRate >> 16) & 0xff);
        header[27] = (byte)((longSampleRate >> 24) & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)((byteRate >> 8) & 0xff);
        header[30] = (byte)((byteRate >> 16) & 0xff);
        header[31] = (byte)((byteRate >> 24) & 0xff);
        header[32] = (byte)( 2 * 16 / 8);
        header[33] = 0;
        header[34] = (byte) RECORDER_BPP;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(totalAudioLen & 0xff);
        header[41] = (byte)((totalAudioLen >> 8) & 0xff);
        header[42] = (byte)((totalAudioLen >> 16) & 0xff);
        header[43] = (byte)((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
