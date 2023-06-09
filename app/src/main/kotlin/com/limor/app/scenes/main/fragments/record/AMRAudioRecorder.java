package com.limor.app.scenes.main.fragments.record;

import android.media.MediaRecorder;

import com.limor.app.scenes.utils.Commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * Android does not support pause and resume when recording amr audio,
 * so we implement it to provide pause and resume funciton.
 *
 * Created by Water Zhang on 11/25/15.
 */
public class AMRAudioRecorder  {

    private boolean singleFile = true;

    private MediaRecorder recorder;

    private ArrayList<String> files = new ArrayList<String>();

    private String fileDirectory;

    private String finalAudioPath;

    private boolean isRecording;

    public boolean isRecording() {
        return isRecording;
    }

    public String getAudioFilePath() {
        return finalAudioPath;
    }

    public AMRAudioRecorder (String audioFileDirectory) {
        this.fileDirectory = audioFileDirectory;

        if (!this.fileDirectory.endsWith("/")) {
            this.fileDirectory += "/";
        }

        newRecorder();
    }

    public boolean start() {
        prepareRecorder();

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (recorder != null)
        {
            recorder.start();
            isRecording = true;

            return true;
        }

        return false;
    }

    public boolean pause() {
        if (recorder == null || !isRecording) {
            throw new IllegalStateException("[AMRAudioRecorder] recorder is not recording!");
        }

        recorder.stop();
        recorder.release();
        recorder = null;

        isRecording = false;

        return true;
    }

    public boolean resume() {
        if (isRecording) {
            throw new IllegalStateException("[AMRAudioRecorder] recorder is recording!");
        }

        singleFile = false;
        newRecorder();
        return start();
    }

    public boolean stop() {
        if (!isRecording) {
            return merge();
        }

        if (recorder == null) {
            return false;
        }

        recorder.stop();
        recorder.release();
        recorder = null;
        isRecording = false;

        return merge();
    }

    public void clear()
    {
        if (recorder != null || isRecording) {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
        }
        for (int i = 0,len = files.size();i<len;i++) {
            File file = new File(this.files.get(i));
            file.delete();
        }
    }

    private boolean merge() {

        // If never paused, just return the file
        if (singleFile) {
            this.finalAudioPath = this.files.get(0);
            return true;
        }

        // Merge files
        String mergedFilePath = this.fileDirectory + new Date().getTime() + ".amr";
        try {
            FileOutputStream fos = new FileOutputStream(mergedFilePath);

            for (int i = 0,len = files.size();i<len;i++) {
                File file = new File(this.files.get(i));
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

            this.finalAudioPath = mergedFilePath;
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void newRecorder() {
        recorder = new MediaRecorder();
    }

    private void prepareRecorder() {

        if(!this.fileDirectory.endsWith("/")){
            this.fileDirectory = this.fileDirectory + "/";
        }

        File directory = new File(this.fileDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }

        if (!directory.exists()) {
            throw new IllegalArgumentException("[AMRAudioRecorder] audioFileDirectory not exists!");
        }
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("[AMRAudioRecorder] audioFileDirectory is a not valid directory!");
        }

        String filePath = directory.getAbsolutePath() + "/" + new Date().getTime() + Commons.audioFileFormat;
        this.files.add(filePath);

        recorder.setOutputFile(filePath);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    public int getMaxAmplitude(){
        if (recorder != null){
            return recorder.getMaxAmplitude();
        }else{
            return 0;
        }
    }

    public File getFileRecording(){
        return new File(this.files.get(files.size() -1));
    }

    public String getFinalAudioPath(){
        return this.finalAudioPath;
    }


}
/*
*  public int getMaxAmplitude(){
        if (recorder != null){
            return recorder.getMaxAmplitude();
        }else{
            return 0;
        }
    }
* */

//
//import android.media.MediaRecorder;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//
///**
// *
// * Android does not support pause and resume when recording amr audio,
// * so we implement it to provide pause and resume function.
// *
// */
//public class AMRAudioRecorder  {
//
//    private boolean singleFile = true;
//
//    private MediaRecorder recorder;
//
//    private ArrayList<String> files = new ArrayList<String>();
//
//    private String fileDirectory;
//
//    private String finalAudioPath;
//
//    private boolean isRecording;
//
//    public boolean isRecording() {
//        return isRecording;
//    }
//
//    public String getAudioFilePath() {
//        return finalAudioPath;
//    }
//
//    public AMRAudioRecorder (String audioFileDirectory) {
//        this.fileDirectory = audioFileDirectory;
//
//        if (!this.fileDirectory.endsWith("/")) {
//            this.fileDirectory += "/";
//        }
//
//        newRecorder();
//    }
//
//    public boolean start() {
//        prepareRecorder();
//
//        try {
//            recorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        if (recorder != null)
//        {
//            recorder.start();
//            isRecording = true;
//
//            return true;
//        }
//
//        return false;
//    }
//
//    public boolean pause() {
//        if (recorder == null || !isRecording) {
//            throw new IllegalStateException("[AMRAudioRecorder] recorder is not recording!");
//        }
//
//        recorder.stop();
//        recorder.release();
//        recorder = null;
//
//        isRecording = false;
//
//        return true;
//    }
//
//    public boolean resume() {
//        if (isRecording) {
//            throw new IllegalStateException("[AMRAudioRecorder] recorder is recording!");
//        }
//
//        singleFile = false;
//        newRecorder();
//        return start();
//    }
//
//    public boolean stop() {
//        if (!isRecording) {
//            return merge();
//        }
//
//        if (recorder == null) {
//            return false;
//        }
//
//        recorder.stop();
//        recorder.release();
//        recorder = null;
//        isRecording = false;
//
//        return merge();
//    }
//
//    public void clear()
//    {
//        if (recorder != null || isRecording) {
//            recorder.stop();
//            recorder.release();
//            recorder = null;
//            isRecording = false;
//        }
//        for (int i = 0,len = files.size();i<len;i++) {
//            File file = new File(this.files.get(i));
//            file.delete();
//        }
//    }
//
//    private boolean merge() {
//
//        // If never paused, just return the file
//        if (singleFile) {
//            this.finalAudioPath = this.files.get(0);
//            return true;
//        }
//
//        // Merge files
//        String mergedFilePath = this.fileDirectory + new Date().getTime() + ".amr";
//        try {
//            FileOutputStream fos = new FileOutputStream(mergedFilePath);
//
//            for (int i = 0,len = files.size();i<len;i++) {
//                File file = new File(this.files.get(i));
//                FileInputStream fis = new FileInputStream(file);
//
//                // Skip file header bytes,
//                // amr file header's length is 6 bytes
//                if (i > 0) {
//                    for (int j=0; j<6; j++) {
//                        fis.read();
//                    }
//                }
//
//                byte[] buffer = new byte[512];
//                int count = 0;
//                while ( (count = fis.read(buffer)) != -1 ) {
//                    fos.write(buffer,0, count);
//                }
//
//                fis.close();
//                fos.flush();
//                file.delete();
//            }
//
//            fos.flush();
//            fos.close();
//
//            this.finalAudioPath = mergedFilePath;
//            return true;
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//
//
//
//
//
//
//
//    private void newRecorder() {
//        recorder = new MediaRecorder();
//    }
//
//
//    public int getMaxAmplitude(){
//        if (recorder != null){
//            return recorder.getMaxAmplitude();
//        }else{
//            return 0;
//        }
//    }
//
//    private void prepareRecorder() {
//        File directory = new File(this.fileDirectory);
//        if (!directory.exists() || !directory.isDirectory()) {
//            throw new IllegalArgumentException("[AMRAudioRecorder] audioFileDirectory is a not valid directory!");
//        }
//
//        String filePath = directory.getAbsolutePath() + "/" + new Date().getTime() + ".amr";
//        this.files.add(filePath);
//
//        recorder.setOutputFile(filePath);
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
//
//
//    }
//
////    public String getFileRecording(){
////
////        File filetoReturn = new File(this.files.get(files.size() -1));
////        return filetoReturn.getAbsolutePath();
////    }
//}
