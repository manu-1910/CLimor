package io.square1.limor.scenes.main.fragments.record;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.square1.limor.R;
import io.square1.limor.scenes.utils.statemanager.Step;
import io.square1.limor.scenes.utils.waveform.MarkerSet;
import io.square1.limor.scenes.utils.waveform.WaveformFragment;
import io.square1.limor.uimodels.UIRecordingItem;
import io.square1.limor.uimodels.UITimeStamp;


public class EditFragment extends WaveformFragment {

    protected UIRecordingItem recordingItem;

    private String initialFilePath;
    private long initialLength;
    private List<UITimeStamp> initialTimeStamps;
    public boolean hasAnythingChanged;
    protected Queue<ProgressDialog> progressDialogQueue = new LinkedList<>();

    public static String MEDIA_KEY = "soun";
    public static String BROADCAST_UPDATE_DRAFTS = "update_drafts";


    private TextView tvRedo, tvUndo, tvPaste, tvCopy, tvDelete, tvToolbarTitle;
    private ImageButton closeButton, infoButton;
    private Button nextButton;

    private BroadcastReceiver receiver;


    public static EditFragment newInstance(UIRecordingItem recordingItem) {
        //AnalyticsManager.getInstance().recordEditEvent(AnalyticsManager.RecordingEditEventType.RECORDING_EDIT_DRAFT);
        //return EditFragment.builder().recordingItem(recordingItem).build();


        EditFragment myFragment = new EditFragment();
        return myFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        recordingItem = new UIRecordingItem();
        recordingItem = (UIRecordingItem) getArguments().get("recordingItem");
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        listeners();
        registerReceivers();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isEditMode = false;
//        if (!DataManager.getInstance().isSkipRecordScreen()) {
//            SHOFragmentUtils.addFragmentWithVerticalAnimationCustomBackStackTag(
//                    getActivity(),
//                    R.id.recordContainer,
//                    RecordFragment.newInstance(recordingItem),
//                    Constants.BACK_STACK_TAG_RECORD);
//        }

        if (receiver != null) {
            getContext().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected String getFileName() {
        return recordingItem.getFilePath();
    }

    @Override
    protected void populateMarkers() {
        if (recordingItem != null && recordingItem.getTimeStamps().size() > 0) {
            for (UITimeStamp timeStamp : recordingItem.getTimeStamps()) {
                if (timeStamp.getStartSample() > 0 && timeStamp.getEndSample() < player.getDuration()) {
                    addMarker(waveformView.millisecsToPixels(timeStamp.getStartSample()), waveformView.millisecsToPixels(timeStamp.getEndSample()), false, timeStamp.getColor());
                }
            }
        }
        if (!isInitialised) {
            saveInitialState();
        }
        isInitialised = true;
        updateUndoRedoButtons();
    }

    private void saveInitialState() {
        initialFilePath = fileName;
        initialLength = recordingItem.getLength();
        initialTimeStamps = new ArrayList<>();
        for (UITimeStamp timeStamp : recordingItem.getTimeStamps()) {
            initialTimeStamps.add(new UITimeStamp(timeStamp.getDuration(), timeStamp.getEndSample(), timeStamp.getStartSample()));
        }
    }

    protected void listeners() {

        tvRedo = (TextView) getView().findViewById(R.id.tvRedo);
        tvUndo = (TextView) getView().findViewById(R.id.tvUndo);
        tvPaste = (TextView) getView().findViewById(R.id.tvPaste);
        tvCopy = (TextView) getView().findViewById(R.id.tvCopy);
        tvDelete = (TextView) getView().findViewById(R.id.tvDelete);

        tvToolbarTitle = (TextView) getView().findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText(getString(R.string.edit));

        closeButton = (ImageButton) getView().findViewById(R.id.btnClose);
        infoButton = (ImageButton) getView().findViewById(R.id.btnInfo);
        nextButton = (Button) getView().findViewById(R.id.nextButtonEdit);


        tvRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stepManager.getLastRedoStep() != null && stepManager.getStepsToRedo().size() > 0) {
                    stepManager.addNewUndoStep(new Step(System.currentTimeMillis(), fileName, recordingItem.getTimeStamps()));
                    recordingItem.setTimeStamps(stepManager.getLastRedoStep().getTimeStamps());
                    fileName = stepManager.getLastRedoStep().getFilePath();
                    loadFromFile(fileName);
                    stepManager.handleLastRedoStep();
                }
                updateUndoRedoButtons();
            }
        });

        tvUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stepManager.getLastUndoStep() != null && stepManager.getStepsToUndo().size() > 0) {
                    stepManager.addNewRedoStep(new Step(System.currentTimeMillis(), fileName, recordingItem.getTimeStamps()));
                    fileName = stepManager.getLastUndoStep().getFilePath();
                    loadFromFile(fileName);
                    stepManager.handleLastUndoStep();
                }
                updateUndoRedoButtons();
            }
        });

        tvPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    return;
                }
                if (editMarker == null || (editMarker.getStartPos() >= selectedMarker.getStartPos() && editMarker.getStartPos() <= selectedMarker.getEndPos())) {
                    showAlertOK(getActivity(), getString(R.string.alert_title_oops), getString(R.string.paste_overlap_alert), null);
                    return;
                }
                showAlertYesNo(getActivity(), getString(R.string.paste), getString(R.string.paste_prompt), (dialog, which) -> {
                    pasteMarkedChunk();
                    //AnalyticsManager.getInstance().recordEditEvent(AnalyticsManager.RecordingEditEventType.RECORDING_EDIT_COPY_PASTE);
                });
            }
        });


        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMarker == null) {
                    showAlertOK(getActivity(), getString(R.string.alert_title_oops), getString(R.string.select_marker_firs_prompt), null);
                    return;
                }
                for (MarkerSet markerSet : markerSets){
                    if (markerSet.isEditMarker()) {
                        showAlertOK(getActivity(), getString(R.string.alert_title_oops), getString(R.string.cant_create_more_than_one_marker_prompt), null);
                        return;
                    }
                }
                //AnalyticsManager.getInstance().recordEditEvent(AnalyticsManager.RecordingEditEventType.RECORDING_EDIT_COPY_PASTE);
                //addMarker(selectedMarker.getStartPos(), selectedMarker.getStartPos() + 2, true, null);    //TODO JJ esta es la original
                addMarker(selectedMarker.getStartPos(), selectedMarker.getStartPos(), true, null); //TODO JJ
            }
        });


        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMarker == null) {
                    showAlertOK(getActivity(), getString(R.string.alert_title_oops), getString(R.string.select_marker_firs_prompt), null);
                    return;
                }
                showAlertOkCancel(getActivity(), getString(R.string.remove), getString(R.string.remove_piece_of_audio_prompt), (dialogInterface, i) -> deleteMarkedChunk());
            }
        });


        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreToInitialState();
            }
        });


        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHowToEdit();
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPublishFragment();
            }
        });


    }

    private void pasteMarkedChunk() {
        if (markerSets == null || markerSets.size() == 0 || isPlayingPreview || isPlaying || selectedMarker == null) {
            return;
        }
        stepManager.addNewUndoStep(new Step(System.currentTimeMillis(), fileName, recordingItem.getTimeStamps()));
        showProgress(getString(R.string.progress_please_wait));
        new Thread(() -> {
            audioFilePaths = new ArrayList<>();
            String copiedChunkPath = null;
            final String outPathCopied = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record_chunk_copied.m4a";
            final int startFrameCopied = waveformView.secondsToFrames(waveformView.pixelsToSeconds(selectedMarker.getStartPos()));
            final int endFrameCopied = waveformView.secondsToFrames(waveformView.pixelsToSeconds(selectedMarker.getEndPos()));
            final File outFileCopied = new File(outPathCopied);
            try {
                soundFile.WriteFile(outFileCopied, startFrameCopied, endFrameCopied - startFrameCopied);
                copiedChunkPath = outFileCopied.getAbsolutePath();
            } catch (Exception e) {
                dismissProgress();
                e.printStackTrace();
            }

            for (int i = 0; i < 2; i++) {
                final String outPath = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record_chunk_" + i + ".m4a";
                double startTime = waveformView.pixelsToSeconds(i == 0 ? 0 : editMarker.getStartPos() - 10);
                double endTime = waveformView.pixelsToSeconds(i == 0 ? editMarker.getStartPos() : waveformView.millisecsToPixels(player.getDuration() - 10));
                final int startFrame = waveformView.secondsToFrames(startTime);
                final int endFrame = waveformView.secondsToFrames(endTime);
                final File outFile = new File(outPath);
                try {
                    soundFile.WriteFile(outFile, startFrame, endFrame - startFrame);
                    if (i == 1) {
                        audioFilePaths.add(copiedChunkPath);
                    }
                    audioFilePaths.add(outFile.getAbsolutePath());
                } catch (Exception e) {
                    dismissProgress();
                    e.printStackTrace();
                }
            }

            fileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a";

            try {
                List<Movie> listMovies = new ArrayList<>();
                for (String filename : audioFilePaths) {
                    listMovies.add(MovieCreator.build(filename));
                }
                List<Track> listTracks = new LinkedList<>();
                for (Movie movie : listMovies) {
                    for (Track track : movie.getTracks()) {
                        if (track.getHandler().equals(MEDIA_KEY)) {
                            listTracks.add(track);
                        }
                    }
                }
                Movie outputMovie = new Movie();
                if (!listTracks.isEmpty()) {
                    outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
                }
                Container container = new DefaultMp4Builder().build(outputMovie);
                FileChannel fileChannel = new RandomAccessFile(String.format(fileName), "rws").getChannel();
                container.writeContainer(fileChannel);
                fileChannel.close();

                //SHOUtils.deleteFiles(audioFilePaths);

                audioFilePaths = new ArrayList<>();

                int copiedLength;
                int startPosMilliseconds = waveformView.pixelsToMillisecs(selectedMarker.getStartPos());
                int endPosMilliseconds = waveformView.pixelsToMillisecs(selectedMarker.getEndPos());
                copiedLength = endPosMilliseconds - startPosMilliseconds;

                getActivity().runOnUiThread(() -> {
                    ArrayList<UITimeStamp> timeStamps = new ArrayList<>();
                    if (markerSets != null && markerSets.size() > 0) {
                        Iterator<MarkerSet> iterator = markerSets.iterator();
                        while (iterator.hasNext()) {
                            MarkerSet markerSet = iterator.next();
                            if (!markerSet.isEditMarker()) {
                                int startPosMillisecondsAdjusted;
                                int endPosMillisecondsAdjusted;
                                if (waveformView.pixelsToMillisecs(markerSet.getStartPos()) < waveformView.pixelsToMillisecs(editMarker.getStartPos())) {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getStartPos());
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getEndPos());
                                } else {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getStartPos()) + copiedLength;
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getEndPos()) + copiedLength;
                                }
                                handleTimeStamps(markerSet, timeStamps, startPosMillisecondsAdjusted, endPosMillisecondsAdjusted);
                                iterator.remove();
                            } else if (markerSet.isEditMarker()){
                                markerSet.getMiddleMarker().setVisibility(View.GONE);
                                markerSet.setMiddleMarker(null);
                                iterator.remove();
                            }
                        }
                    }
                    shouldReloadPreview = true;
                    recordingItem.setTimeStamps(timeStamps);
                    recordingItem.setLength(recordingItem.getLength() + copiedLength);
                    recordingItem.setFilePath(fileName);
                    //updateRecordingItem();
                    dismissProgress();
                    loadFromFile(fileName);
                    getActivity().sendBroadcast(new Intent(BROADCAST_UPDATE_DRAFTS));
                    stepManager.resetRedoSteps();
                    isEditMode = false;
                    editMarker = null;
                });
                hasAnythingChanged = true;
            } catch (IOException ex) {
                dismissProgress();
                ex.printStackTrace();
            }
        }).start();
    }


    protected void deleteMarkedChunk() {
        if (markerSets == null || markerSets.size() == 0 || isPlayingPreview || isPlaying || selectedMarker == null) {
            return;
        }
        stepManager.addNewUndoStep(new Step(System.currentTimeMillis(), fileName, recordingItem.getTimeStamps()));
        showProgress(getString(R.string.progress_please_wait));
        new Thread(() -> {
            audioFilePaths = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                final String outPath = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record_chunk_" + i + ".m4a";
                double startTime = waveformView.pixelsToSeconds(i == 0 ? 0 : selectedMarker.getEndPos());
                double endTime = waveformView.pixelsToSeconds(i == 0 ? selectedMarker.getStartPos() : waveformView.millisecsToPixels(player.getDuration() - 10));
                final int startFrame = waveformView.secondsToFrames(startTime);
                final int endFrame = waveformView.secondsToFrames(endTime);
                final File outFile = new File(outPath);
                try {
                    soundFile.WriteFile(outFile, startFrame, endFrame - startFrame);
                    audioFilePaths.add(outFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            fileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a";

            try {
                List<Movie> listMovies = new ArrayList<>();
                for (String filename : audioFilePaths) {
                    listMovies.add(MovieCreator.build(filename));
                }
                List<Track> listTracks = new LinkedList<>();
                for (Movie movie : listMovies) {
                    for (Track track : movie.getTracks()) {
                        if (track.getHandler().equals(MEDIA_KEY)) {
                            listTracks.add(track);
                        }
                    }
                }
                Movie outputMovie = new Movie();
                if (!listTracks.isEmpty()) {
                    outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
                }
                Container container = new DefaultMp4Builder().build(outputMovie);
                FileChannel fileChannel = new RandomAccessFile(String.format(fileName), "rws").getChannel();
                container.writeContainer(fileChannel);
                fileChannel.close();

                //SHOUtils.deleteFiles(audioFilePaths);

                audioFilePaths = new ArrayList<>();

                int deletedLength;
                int startPosMilliseconds = waveformView.pixelsToMillisecs(selectedMarker.getStartPos());
                int endPosMilliseconds = waveformView.pixelsToMillisecs(selectedMarker.getEndPos());
                deletedLength = endPosMilliseconds - startPosMilliseconds;

                getActivity().runOnUiThread(() -> {
                    removeMarker(selectedMarker);
                    ArrayList<UITimeStamp> timeStamps = new ArrayList<>();
                    if (markerSets != null && markerSets.size() > 0) {
                        Iterator<MarkerSet> iterator = markerSets.iterator();
                        while (iterator.hasNext()) {
                            MarkerSet markerSet = iterator.next();
                            if (!markerSet.isEditMarker()) {
                                int startPosMillisecondsAdjusted;
                                int endPosMillisecondsAdjusted;
                                if (waveformView.pixelsToMillisecs(markerSet.getStartPos()) < startPosMilliseconds) {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getStartPos());
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getEndPos());
                                } else {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getStartPos()) - deletedLength;
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(markerSet.getEndPos()) - deletedLength;
                                }
                                handleTimeStamps(markerSet, timeStamps, startPosMillisecondsAdjusted, endPosMillisecondsAdjusted);
                                iterator.remove();
                                shouldReloadPreview = true;
                            }
                        }
                    }

                    recordingItem.setTimeStamps(timeStamps);
                    recordingItem.setLength(recordingItem.getLength() - deletedLength);
                    recordingItem.setFilePath(fileName);
                    //updateRecordingItem();
                    dismissProgress();
                    loadFromFile(fileName);
                    getActivity().sendBroadcast(new Intent(BROADCAST_UPDATE_DRAFTS));
                    stepManager.resetRedoSteps();
                    isEditMode = false;
                    selectedMarker = null;
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                dismissProgress();
            }
            hasAnythingChanged = true;
        }).start();

    }


    private void handleTimeStamps(MarkerSet markerSet, ArrayList<UITimeStamp> timeStamps, int startPos, int endPos) {
        UITimeStamp timeStamp = new UITimeStamp();
        timeStamp.setStartSample(startPos);
        timeStamp.setEndSample(endPos);
        timeStamp.setDuration(endPos - startPos);
        timeStamps.add(timeStamp);
        markerSet.getStartMarker().setVisibility(View.GONE);
        markerSet.setStartMarker(null);
        markerSet.getMiddleMarker().setVisibility(View.GONE);
        markerSet.setMiddleMarker(null);
        markerSet.getEndMarker().setVisibility(View.GONE);
        markerSet.setEndMarker(null);
    }


    private void registerReceivers(){

        // your oncreate code should be
        IntentFilter filter = new IntentFilter();
        filter.addAction("BROADCAST_OPEN_HOW_TO_EDIT");
        filter.addAction("BROADCAST_OPEN_PUBLISH_SCREEN");
        filter.addAction("BROADCAST_RESTORE_INITIAL_RECORDING");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //do something based on the intent's action

                if (intent.getAction().contains("BROADCAST_OPEN_HOW_TO_EDIT")){
                    openHowToEdit();
                }else if(intent.getAction().contains("BROADCAST_OPEN_PUBLISH_SCREEN")){
                    openPublishFragment();
                }else if(intent.getAction().contains("BROADCAST_RESTORE_INITIAL_RECORDING")){
                    restoreToInitialState();
                }
            }
        };
        getContext().registerReceiver(receiver, filter);
    }


    private void openHowToEdit() {
        showAlertOK(getActivity(), getString(R.string.how_to_edit_title), getString(R.string.how_to_edit_description), null);
    }

    private void openPublishFragment() {
        handlePause();
        handlePausePreview();
        //DataManager.getInstance().setSkipRecordScreen(true);
        ArrayList<UITimeStamp> timeStamps = new ArrayList<>();
        if (markerSets != null && markerSets.size() > 0) {
            for (MarkerSet markerSet : markerSets) {
                if (!markerSet.isEditMarker()) {
                    int startPosMilliseconds = waveformView.pixelsToMillisecs(markerSet.getStartPos());
                    int endPosMilliseconds = waveformView.pixelsToMillisecs(markerSet.getEndPos());
                    UITimeStamp timeStamp = new UITimeStamp();
                    timeStamp.setStartSample(startPosMilliseconds);
                    timeStamp.setEndSample(endPosMilliseconds);
                    timeStamp.setDuration(endPosMilliseconds - startPosMilliseconds);
                    timeStamps.add(timeStamp);
                }
            }
            saveNewFileFromMarkers(false);
            recordingItem.setEditedFilePath(editedWithMarkersFileName);
        } else {
            recordingItem.setEditedFilePath("");
        }
        //AnalyticsManager.getInstance().createRecordingEditEventMarkers(String.valueOf(markerSets.size()));
        recordingItem.setTimeStamps(timeStamps);
        //updateRecordingItem();
        //SHOFragmentUtils.addFragmentWithHorizontalAnimation(
        //        getActivity(),
        //        R.id.recordContainer,
        //        PublishFragment.newInstance(recordingItem),
        //        true);
    }


    protected void restoreToInitialState() {
        recordingItem.setTimeStamps(initialTimeStamps);
        recordingItem.setLength(initialLength);
        recordingItem.setFilePath(initialFilePath);
        //updateRecordingItem();
        getActivity().sendBroadcast(new Intent(BROADCAST_UPDATE_DRAFTS));
        hasAnythingChanged = false;
        try {
            getFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showAlertOK(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        showAlertCustomButtons(context, title, message, listener, context.getString(R.string.ok), null, null);
    }

    public static void showAlertOkCancel(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        showAlertCustomButtons(context, title, message, listener, context.getString(R.string.ok), null, context.getString(R.string.cancel));
    }

    public void showProgress(String message) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        if (message != null)
            progressDialog.setMessage(message);
        progressDialog.show();
        progressDialogQueue.add(progressDialog);
    }

    public void dismissProgress(){
        ProgressDialog progressDialog = progressDialogQueue.poll();
        if(progressDialog !=null) {
            progressDialog.dismiss();
        }
    }


}