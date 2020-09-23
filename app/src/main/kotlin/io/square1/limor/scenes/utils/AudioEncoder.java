package io.square1.limor.scenes.utils;

import android.media.MediaExtractor;
import android.media.MediaFormat;


import java.io.File;
import java.io.IOException;

/**
 * Created by michael_xu on 2017/8/11.
 */

public abstract class AudioEncoder {
    protected int SAMPLE_RATE = 8000;
    protected int BIT_RATE = 64;
    protected int CHANNEL_COUNT = 1;
    //protected String destinationFile;
    protected File pcmFile;
    protected File armFile;

    public void init(int SAMPLE_RATE, int BIT_RATE, int CHANNEL_COUNT, File pcmFile, File armFile) {
        this.SAMPLE_RATE = SAMPLE_RATE;
        this.BIT_RATE = BIT_RATE;
        this.CHANNEL_COUNT = CHANNEL_COUNT;
        this.pcmFile = pcmFile;
        this.armFile = armFile;
    }

    public String getAmrFile() {
        return this.armFile.getAbsolutePath();
    }

    public String getPcmFile(){
        return this.pcmFile.getAbsolutePath();
    }

    protected MediaFormat getMediaFormat(String sourceFile) throws IOException {
        MediaExtractor mex = new MediaExtractor();
        mex.setDataSource(sourceFile);
        return mex.getTrackFormat(0);
    }

    public abstract void encode();


}