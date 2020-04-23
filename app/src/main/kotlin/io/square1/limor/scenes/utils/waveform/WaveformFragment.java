package io.square1.limor.scenes.utils.waveform;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import org.joda.time.DateTime;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import io.square1.limor.R;
import io.square1.limor.common.BaseFragment;
import io.square1.limor.scenes.main.fragments.record.EditFragment;
import io.square1.limor.scenes.utils.statemanager.StepManager;
import io.square1.limor.scenes.utils.waveform.soundfile.SoundFile;
import io.square1.limor.scenes.utils.waveform.view.MarkerView;
import io.square1.limor.scenes.utils.waveform.view.WaveformView;
import me.kareluo.ui.OptionMenu;
import me.kareluo.ui.OptionMenuView;
import me.kareluo.ui.PopupMenuView;
import me.kareluo.ui.PopupView;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

public abstract class WaveformFragment extends BaseFragment implements WaveformView.WaveformListener, MarkerView.MarkerListener {

    // region Variables

    private RelativeLayout absoluteLayout;
    private ProgressDialog progressDialog;
    protected SoundFile soundFile;
    protected File file;
    protected String fileName;
    protected String editedWithMarkersFileName;
    protected WaveformView waveformView;
    protected ImageButton playButton, rewindButton, forwardButton;
    protected ImageButton closeButton, infoButton;
    protected Button nextButton;
    protected TextView tvTimePass, tvDuration, tvTimePassPreview, tvDurationPreview, tvDelete, tvCopy, tvPaste, tvUndo, tvRedo;
    protected Handler handler;
    protected Handler seekBarHandler = new Handler();
    protected MediaPlayer player;
    protected MediaPlayer playerPreview;
    protected int width, maxPos, offset, offsetGoal;
    protected boolean touchDragging;
    protected long loadingLastUpdateTime;
    protected boolean loadingKeepGoing;
    protected float touchStart;
    protected int touchInitialOffset;
    protected int touchInitialStartPos;
    protected int touchInitialMiddlePos;
    protected int touchInitialEndPos;
    protected long waveformTouchStartMsec;
    protected long markerTouchStartMsec;
    protected float density;
    protected int markerSize;
    protected int markerSizeDown;
    protected int markerInset;
    protected int markerTopOffset;
    protected ArrayList<MarkerSet> markerSets;
    private boolean shouldDisableTouch;
    private boolean newMarkerAdded;
    protected boolean isPlaying;
    protected boolean isPlayingPreview;
    private int playStartOffset;
    private int playEndMsec;
    private int playStartMsec;
    private SeekBar seekBar;
    private boolean isSeekBarTouched;
    private ImageView ivPlayPreview;
    private RelativeLayout rlPreview, rlPreviewSection;
    private SeekBar seekBarPreview;
    private boolean isSeekBarTouchedPreview;
    protected boolean shouldReloadPreview;
    protected MarkerSet selectedMarker, editMarker;
    protected ArrayList<String> audioFilePaths = new ArrayList<>();
    protected StepManager stepManager;
    public static boolean isEditMode;
    protected boolean isInitialised;
    private boolean isMovingTooMuch;

    private final int ALLOWED_PIXEL_OFFSET = 18;

    private enum MenuOption {
        Copy, Paste, Delete, Dismiss, Preview;
    }

    // endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_waveform, container, false);
        loadGui(view);
        if (soundFile == null) {
            loadFromFile(fileName);
        } else {
            handler.post(() -> finishOpeningSoundFile());
        }
        return view;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        stepManager = new StepManager();
        markerSets = new ArrayList<>();
        player = null;
        fileName = getFileName();
        soundFile = null;
        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
        }
        soundFile = null;
        waveformView = null;
        super.onDestroy();
    }

    // region Waveform

    @Override
    public void waveformDraw() {
        if (waveformView == null) {
            return;
        }

        width = waveformView.getMeasuredWidth();
        if (newMarkerAdded || (offsetGoal != offset)) {
            updateMarkers();
            newMarkerAdded = false;
        }
        if (isPlaying) {
            updateDisplay();
        }
    }

    @Override
    public void waveformTouchStart(float x) {
        touchDragging = true;
        touchStart = x;
        touchInitialOffset = offset;
        waveformTouchStartMsec = System.currentTimeMillis();
    }

    @Override
    public void waveformTouchMove(float x) {
        offset = trap((int) (touchInitialOffset + (touchStart - x)));
        updateDisplay();
    }

    @Override
    public void waveformTouchEnd(float x, float y) {
        touchDragging = false;
        offsetGoal = offset;
        long elapsedMsec = System.currentTimeMillis() - waveformTouchStartMsec;
        if (elapsedMsec < 200 && y < dpToPx(getActivity(), 48) && x < waveformView.maxPos() && isAvailableArea(x) && !isEditMode) {
            int seekMsec = waveformView.pixelsToMillisecs((int) (touchStart + offset));
            int startPos = trap((int) (touchStart + offset));
            int endPos = startPos + (100);
            addMarker(startPos, endPos, false, null);
        }
    }

    @Override
    public void waveformFling(float vx) {
        touchDragging = false;
        offsetGoal = offset;
        updateDisplay();
    }

    @Override
    public void waveformZoomIn() {
        waveformView.zoomIn();
        maxPos = waveformView.maxPos();
        offset = waveformView.getOffset();
        offsetGoal = offset;
        updateDisplay();
    }

    @Override
    public void waveformZoomOut() {
        waveformView.zoomOut();
        maxPos = waveformView.maxPos();
        offset = waveformView.getOffset();
        offsetGoal = offset;
        updateDisplay();
    }

    // endregion

    // region Markers

    @Override
    public void markerDraw() {}

    @Override
    public void markerTouchStart(MarkerView marker, float x) {
        isMovingTooMuch = false;
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            return;
        }
        markerTouchStartMsec = System.currentTimeMillis();
        touchStart = x;
        touchInitialStartPos = marker.getMarkerSet().getStartPos();
        touchInitialMiddlePos = marker.getMarkerSet().getMiddlePos();
        touchInitialEndPos = marker.getMarkerSet().getEndPos();
    }

    @Override
    public void markerTouchMove(MarkerView marker, float x) {
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            return;
        }
        if ((x > touchStart && x - touchStart > ALLOWED_PIXEL_OFFSET) || (x < touchStart && touchStart - x > ALLOWED_PIXEL_OFFSET)) {
            isMovingTooMuch = true;
        }
        touchDragging = true;
        if (shouldDisableTouch) {
            return;
        }
        float delta = x - touchStart;
        if (marker.getType() == MarkerView.START_MARKER) {
            int currentStartPos = trap((int) (touchInitialStartPos + delta));
            marker.getMarkerSet().setMiddlePos(currentStartPos + ((marker.getMarkerSet().getEndPos() - currentStartPos) / 2));
            marker.getMarkerSet().setStartPos(currentStartPos);
            if (marker.getMarkerSet().getStartPos() > marker.getMarkerSet().getEndPos()) {
                marker.getMarkerSet().setStartPos(marker.getMarkerSet().getEndPos());
                marker.getMarkerSet().setMiddlePos(marker.getMarkerSet().getEndPos());
            }
            checkIfTriggerMerge(marker);
        } else if (marker.getType() == MarkerView.MIDDLE_MARKER) {
            int currentMiddlePos = trap((int) (touchInitialMiddlePos + delta));
            marker.getMarkerSet().setMiddlePos(currentMiddlePos);
            marker.getMarkerSet().setStartPos(currentMiddlePos - marker.getMarkerSet().getDistanceFromTheMiddle());
            marker.getMarkerSet().setEndPos(currentMiddlePos + marker.getMarkerSet().getDistanceFromTheMiddle());
            checkIfTriggerMerge(marker);
        } else {
            int currentEndPos = trap((int) (touchInitialEndPos + delta));
            marker.getMarkerSet().setMiddlePos(marker.getMarkerSet().getStartPos() + ((currentEndPos - marker.getMarkerSet().getStartPos()) / 2));
            marker.getMarkerSet().setEndPos(currentEndPos);
            if (marker.getMarkerSet().getEndPos() < marker.getMarkerSet().getStartPos()) {
                marker.getMarkerSet().setEndPos(marker.getMarkerSet().getStartPos());
                marker.getMarkerSet().setMiddlePos(marker.getMarkerSet().getStartPos());
            }
            checkIfTriggerMerge(marker);
        }
        updateDisplay();
        shouldReloadPreview = true;
    }

    @Override
    public void markerTouchEnd(MarkerView marker) {
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            return;
        }
        long elapsedMsec = System.currentTimeMillis() - markerTouchStartMsec;

        if (elapsedMsec > 350 && marker.getType() == MarkerView.MIDDLE_MARKER && !isMovingTooMuch) {

            showPopUpMenu(marker); //TODO JJ new

            /*showAlertYesNo(getActivity(), getString(R.string.remove), getString(R.string.remove_marker_prompt), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    removeMarker(marker.getMarkerSet());
                }
            });*/
        }
        isMovingTooMuch = false;
        touchDragging = false;

    }

    @Override
    public void markerFocus(MarkerView marker) {
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            //TODO JJ New the app explote copying and pasting multiple markers, I add try/catch to avoid ANR and investigate later
                marker.clearFocus();
        }
    }


    protected void addMarker(int startPos, int endPos, boolean isEditMarker, Integer color) {

        MarkerSet newMarkerSet = new MarkerSet();

        MarkerView startMarker = new MarkerView(getActivity());
        startMarker.setType(MarkerView.START_MARKER);
        if (isEditMarker){
            startMarker.setImageDrawable(getResources().getDrawable(R.drawable.marker_circle_green));
        }else{
            startMarker.setImageDrawable(getResources().getDrawable(R.drawable.marker_circle_blue));
        }
        startMarker.setListener(this);
        enableMarker(startMarker, !isEditMarker);


        MarkerView endMarker = new MarkerView(getActivity());
        endMarker.setType(MarkerView.END_MARKER);
        endMarker.setImageDrawable(getResources().getDrawable(R.drawable.marker_circle_blue));
        endMarker.setListener(this);
        enableMarker(endMarker, !isEditMarker);

        MarkerView middleMarker = new MarkerView(getActivity());
        middleMarker.setType(MarkerView.MIDDLE_MARKER);
        middleMarker.setImageDrawable(getResources().getDrawable(isEditMarker ? R.drawable.more_tab : R.drawable.marker_top));

        middleMarker.setListener(this);
        middleMarker.setOnFocusChangeListener((view, isSelected) -> {
            MarkerView markerView = (MarkerView)view;
            if (isSelected && !isEditMarker) {
                selectedMarker = markerView.getMarkerSet();
            } else if (isEditMarker){
                editMarker = markerView.getMarkerSet();
            }
        });
        enableMarker(middleMarker, true);
        enableMarker(startMarker, true); //TODO JJ new

        newMarkerSet.setMiddleMarker(middleMarker);
        newMarkerSet.setMiddlePos(startPos + ((endPos - startPos) / 2));
        newMarkerSet.setMiddleVisible(true);

        newMarkerSet.setStartMarker(startMarker);
        newMarkerSet.setStartPos(startPos);
        newMarkerSet.setStartVisible(true);

        newMarkerSet.setEndMarker(endMarker);
        newMarkerSet.setEndPos(endPos);
        newMarkerSet.setEndVisible(true);

        newMarkerSet.setBackgroundColor(isEditMarker ?  R.color.white : color != null ? color : R.color.colorBackgroundMarker);
        newMarkerSet.setId(System.currentTimeMillis());
        newMarkerSet.setEditMarker(isEditMarker);

        markerSets.add(newMarkerSet);
        absoluteLayout.addView(startMarker);
        absoluteLayout.addView(endMarker);
        absoluteLayout.addView(middleMarker);

        newMarkerAdded = true;
        shouldReloadPreview = true;
        isEditMode = isEditMarker;
        updateDisplay();
        if (markerSets.size() > 0) {
            //rlPreviewSection.setAlpha(1.0f);
            rlPreviewSection.setAlpha(0.4f); //TODO JJ set alpha to 60% of visibility
        }
    }

    private void enableMarker(MarkerView markerView, boolean shouldEnable) {
        if (shouldEnable) {
            //markerView.setImageAlpha(255);
            markerView.setImageAlpha(255);
            markerView.setFocusable(true);
            markerView.setFocusableInTouchMode(true);
        } else {
            markerView.setImageAlpha(0);
            markerView.setFocusable(false);
            markerView.setFocusableInTouchMode(false);
        }
    }

    private void proceedMerge(MarkerSet firstMarkerSet, MarkerSet secondMarkerSet) {
        addMarker(firstMarkerSet.getStartPos(), secondMarkerSet.getEndPos(), false, null);
        removeMarker(firstMarkerSet);
        removeMarker(secondMarkerSet);
        shouldReloadPreview = true;
    }


    public void showPopUpMenu(MarkerView marker){ //TODO JJ new

        PopupMenuView menuView = new PopupMenuView(getContext(), R.menu.menu_popup_edit, new MenuBuilder(getActivity()));

        if (marker.getMarkerSet().isMiddleVisible() && marker.getMarkerSet().isEditMarker()){
            //If is the Paste marker I only will show the "paste" option menu
            menuView.setMenuItems(Arrays.asList(
                    new OptionMenu(getString(R.string.menu_paste))//,
                    //new OptionMenu(getString(R.string.menu_dismiss))
            ));
        }else{
            menuView.setMenuItems(Arrays.asList(
                    //new OptionMenu("Copy"), new OptionMenu("copy1"),
                    new OptionMenu(getString(R.string.menu_preview)),
                    new OptionMenu(getString(R.string.menu_copy)),
                    new OptionMenu(getString(R.string.menu_delete)),
                    new OptionMenu(getString(R.string.menu_dismiss))
            ));
        }

        //menuView.setSites(PopupView.SITE_BOTTOM, PopupView.SITE_LEFT, PopupView.SITE_TOP, PopupView.SITE_RIGHT);
        menuView.setSites( PopupView.SITE_TOP );

        menuView.setOnMenuClickListener(new OptionMenuView.OnOptionMenuClickListener() {
            @Override
            public boolean onOptionMenuClick(int position, OptionMenu menu) {
                MenuOption menuOption = MenuOption.valueOf(menu.getTitle().toString());
                switch (menuOption){
                    case Copy:
                        tvCopy.performClick();
                        break;
                    case Paste:
                        tvPaste.performClick();
                        break;
                    case Delete:
                        tvDelete.performClick();
                        break;
                    case Dismiss:
                        removeMarker(marker.getMarkerSet());
                        break;
                    case Preview:
                        ivPlayPreview.performClick();
                        break;
                }
                return true;
            }
        });

        menuView.show(marker);
    }

    protected void removeMarker(MarkerSet markerSet) {
        if (markerSet.getStartMarker() != null) {
            markerSet.getStartMarker().setVisibility(View.GONE);
        }
        markerSet.setStartMarker(null);
        if (markerSet.getMiddleMarker() != null) {
            markerSet.getMiddleMarker().setVisibility(View.GONE);
        }
        markerSet.setMiddleMarker(null);
        if (markerSet.getEndMarker() != null) {
            markerSet.getEndMarker().setVisibility(View.GONE);
        }
        markerSet.setEndMarker(null);

        if (markerSet.isEditMarker()) {
            isEditMode = false;
        }
        markerSets.remove(markerSet);
        selectedMarker = null;
        updateDisplay();
        shouldReloadPreview = true;
        if (markerSets.size() <= 0) {
            rlPreviewSection.setAlpha(0.4f);
        }
    }

    private void checkIfTriggerMerge(MarkerView marker) {
        for (MarkerSet markerSet : markerSets) {

            if (marker.getMarkerSet().isEditMarker() || markerSet.isEditMarker()) {
                return;
            }

            if (marker.getMarkerSet().getStartPos() < markerSet.getEndPos() && markerSet.getStartPos() < marker.getMarkerSet().getStartPos() && marker.getMarkerSet().getId() != markerSet.getId()) {
                shouldDisableTouch = true;
                marker.getMarkerSet().setStartPos(markerSet.getEndPos());
                updateDisplay();
                showAlertCustomButtons(getActivity(), getString(R.string.merge), getString(R.string.merge_sections_prompt),
                        (dialogInterface, i) -> {
                            shouldDisableTouch = false;
                            proceedMerge(markerSet, marker.getMarkerSet());
                        }, getString(R.string.merge),
                        (dialogInterface, i) -> {
                            shouldDisableTouch = false;
                            marker.getMarkerSet().setStartPos(touchInitialStartPos);
                            marker.getMarkerSet().setMiddlePos(touchInitialMiddlePos);
                            marker.getMarkerSet().setEndPos(touchInitialEndPos);
                            updateDisplay();
                        }, getString(R.string.btn_cancel));
                return;
            } else if (marker.getMarkerSet().getEndPos() > markerSet.getStartPos() && markerSet.getEndPos() > marker.getMarkerSet().getStartPos() && marker.getMarkerSet().getId() != markerSet.getId()) {
                shouldDisableTouch = true;
                marker.getMarkerSet().setEndPos(markerSet.getStartPos());
                updateDisplay();
                showAlertCustomButtons(getActivity(), getString(R.string.merge), getString(R.string.merge_sections_prompt),
                        (dialogInterface, i) -> {
                            shouldDisableTouch = false;
                            proceedMerge(marker.getMarkerSet(), markerSet);
                        }, getString(R.string.merge),
                        (dialogInterface, i) -> {
                            shouldDisableTouch = false;
                            marker.getMarkerSet().setStartPos(touchInitialStartPos);
                            marker.getMarkerSet().setMiddlePos(touchInitialMiddlePos);
                            marker.getMarkerSet().setEndPos(touchInitialEndPos);
                            updateDisplay();
                        }, getString(R.string.btn_cancel));
                return;
            }
        }
    }

    // endregion

    // region Internal methods

    protected void loadGui(View view) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;

        markerSize = (int) (24 * density); //TODO JJ Había un 24
        markerSizeDown = (int) (19 * density); //TODO JJ Había un 14
        markerInset = (int) (18 * density); //18
        markerTopOffset = (int) (10 * density); //10

        absoluteLayout = view.findViewById(R.id.absoluteLayout);
        playButton = view.findViewById(R.id.play);
        playButton.setOnClickListener(playListener);
        rewindButton = view.findViewById(R.id.rew);
        rewindButton.setOnClickListener(rewindListener);
        forwardButton = view.findViewById(R.id.ffwd);
        forwardButton.setOnClickListener(ffwdListener);
        waveformView = view.findViewById(R.id.waveform);
        waveformView.setListener(this);
        seekBar = view.findViewById(R.id.seekBar);
        seekBarPreview = view.findViewById(R.id.seekBarPreview);
        tvTimePass = view.findViewById(R.id.tvTimePass);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvUndo = view.findViewById(R.id.tvUndo);
        tvRedo = view.findViewById(R.id.tvRedo);
        tvDelete = view.findViewById(R.id.tvDelete);
        tvPaste = view.findViewById(R.id.tvPaste);
        tvCopy = view.findViewById(R.id.tvCopy);
        tvTimePassPreview = view.findViewById(R.id.tvTimePassPreview);
        tvDurationPreview = view.findViewById(R.id.tvDurationPreview);
        ivPlayPreview = view.findViewById(R.id.ivPlayPreview);
        rlPreview = view.findViewById(R.id.rlPreview);
        rlPreviewSection = view.findViewById(R.id.rlPreviewSection);
        rlPreview.setOnClickListener(playListener);

        closeButton = view.findViewById(R.id.btnClose);
        infoButton = view.findViewById(R.id.btnInfo);
        nextButton = view.findViewById(R.id.nextButtonEdit);

        maxPos = 0;
        if (soundFile != null && !waveformView.hasSoundFile()) {
            waveformView.setSoundFile(soundFile);
            waveformView.recomputeHeights(density);
            maxPos = waveformView.maxPos();
        }
        updateDisplay();
    }

    protected void loadFromFile(String fileName) {
        file = new File(fileName);
        loadingLastUpdateTime = System.currentTimeMillis();
        loadingKeepGoing = true;
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.processing_audio));
        progressDialog.setCancelable(false);
        progressDialog.setOnCancelListener((DialogInterface dialog) -> loadingKeepGoing = false);
        progressDialog.show();

        final SoundFile.ProgressListener listener = (double fractionComplete) -> {
            long now = System.currentTimeMillis();
            if (now - loadingLastUpdateTime > 100) {
                progressDialog.setProgress((int) (progressDialog.getMax() * fractionComplete));
                loadingLastUpdateTime = now;
            }
            return loadingKeepGoing;
        };

        // Create the MediaPlayer in a background thread
        new Thread() {
            public void run() {
                try {
                    player = new MediaPlayer();
                    player.setDataSource(file.getAbsolutePath());
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                } catch (final java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    soundFile = SoundFile.create(file.getAbsolutePath(), listener);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    showAlertYesNo(getContext(), "ERROR", "You don't have enough free memory", null);
                    return;
                } catch (final Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    return;
                }
                if (loadingKeepGoing) {
                    handler.post(() -> finishOpeningSoundFile());
                }
            }
        }.start();
    }

    protected void finishOpeningSoundFile() {
        waveformView.setSoundFile(soundFile);
        waveformView.recomputeHeights(density);
        maxPos = waveformView.maxPos();
        touchDragging = false;
        offset = 0;
        offsetGoal = 0;
        progressDialog.dismiss();
        updateDisplay();
        setupSeekBar();
        rewindButton.setAlpha(0.6f);
        rewindButton.setEnabled(false);
        populateMarkers();
        shouldReloadPreview = true;
    }

    protected synchronized void updateDisplay() {
        if (isPlaying) {
            int now = player.getCurrentPosition() + playStartOffset;
            int frames = waveformView.millisecsToPixels(now);
            if (waveformView != null) {
                waveformView.setPlayback(frames);
            }
            setOffsetGoalNoUpdate(frames - width / 2);
            int offsetDelta = offsetGoal - offset;
            if (offsetDelta > 10) {
                offsetDelta = offsetDelta / 10;
            } else if (offsetDelta > 0) {
                offsetDelta = 1;
            } else if (offsetDelta < -10) {
                offsetDelta = offsetDelta / 10;
            } else if (offsetDelta < 0) {
                offsetDelta = -1;
            } else {
                offsetDelta = 0;
            }
            offset += offsetDelta;
            enableDisableSeekButtons();
        } else {
            if (offset + width > maxPos) {
                offset = maxPos - width;
            }
            if (offset < 0) {
                offset = 0;
            }
        }
        offsetGoal = offset;
        updateMarkers();
        waveformView.setParameters(markerSets, offset);
        waveformView.invalidate();
    }

    private synchronized void updateMarkers() {

        if (markerSets != null && markerSets.size() > 0) {

            for (MarkerSet markerSet : markerSets) {
                markerSet.setStartX(markerSet.getStartPos() - offset - markerInset);
                if (markerSet.getStartX() + markerSet.getStartMarker().getWidth() >= 0) {
                    if (!markerSet.isStartVisible()) {
                        handler.postDelayed(() -> {
                            if (!markerSet.isEditMarker()) {
                                markerSet.setStartVisible(true);
                                enableMarker(markerSet.getStartMarker(), true);
                            }
                        }, 0);
                    }
                } else {
                    if (markerSet.isStartVisible()) {
                        markerSet.setStartVisible(false);
                        enableMarker(markerSet.getStartMarker(), false);
                    }
                    markerSet.setStartX(0);
                }
                markerSet.setMiddleX(markerSet.getMiddlePos() - offset - markerSize + (markerSize / 2));
                if (markerSet.getMiddleX() + markerSet.getMiddleMarker().getWidth() >= 0) {
                    if (!markerSet.isMiddleVisible()) {
                        handler.postDelayed(() -> {
                            markerSet.setMiddleVisible(true);
                            enableMarker(markerSet.getMiddleMarker(), true);
                        }, 0);
                    }
                } else {
                    if (markerSet.isMiddleVisible()) {
                        markerSet.setMiddleVisible(false);
                        enableMarker(markerSet.getMiddleMarker(), false);
                    }
                    markerSet.setMiddleX(0);
                }
                markerSet.setEndX(markerSet.getEndPos() - offset - markerSet.getEndMarker().getWidth() + markerInset);
                if (markerSet.getEndX() + markerSet.getEndMarker().getWidth() >= 0) {
                    if (!markerSet.isEndVisible()) {
                        handler.postDelayed(() -> {
                            if (!markerSet.isEditMarker()) {
                                markerSet.setEndVisible(true);
                                enableMarker(markerSet.getEndMarker(), true);
                            }
                        }, 0);
                    }
                } else {
                    if (markerSet.isEndVisible()) {
                        markerSet.setEndVisible(false);
                        enableMarker(markerSet.getEndMarker(), false);
                    }
                    markerSet.setEndX(0);
                }



                markerSet.getMiddleMarker().setColorFilter( markerSet.isEditMarker() ? ContextCompat.getColor(getContext(), R.color.marker_paste_green) : ContextCompat.getColor(getContext(), R.color.marker_blue));

                //markerSet.getMiddleMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                //markerSet.getMiddleMarker().setAlpha(markerSet.isEditMarker() ? 1.0f : 0.5f);
                //markerSet.getStartMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                //markerSet.getStartMarker().setAlpha(0.5f);
                //markerSet.getEndMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                //markerSet.getEndMarker().setAlpha(0.5f);

                if (isEditMode && !markerSet.isEditMarker()) {
                    /*markerSet.getMiddleMarker().setAlpha(0.15f);
                    markerSet.getStartMarker().setAlpha(0.15f);
                    markerSet.getEndMarker().setAlpha(0.15f);*/
                    //TODO JJ I've commented lines above
                    //markerSet.getStartMarker().setColorFilter(ContextCompat.getColor(getContext(), R.color.marker_paste_green));
                }


                int markerTopMargin = (int) (25 * density);

                RelativeLayout.LayoutParams startMarkerParams = new RelativeLayout.LayoutParams(markerSizeDown, markerSizeDown);
                startMarkerParams.leftMargin = markerSet.getStartX() + dpToPx(getActivity(), 9);
                startMarkerParams.topMargin = waveformView.getMeasuredHeight() - markerTopMargin;
                markerSet.getStartMarker().setLayoutParams(startMarkerParams);

                RelativeLayout.LayoutParams endMarkerParams = new RelativeLayout.LayoutParams(markerSizeDown, markerSizeDown);
                endMarkerParams.leftMargin = markerSet.getEndX() - dpToPx(getActivity(), 9);
                endMarkerParams.topMargin = waveformView.getMeasuredHeight() - markerTopMargin;
                markerSet.getEndMarker().setLayoutParams(endMarkerParams);

                RelativeLayout.LayoutParams middleMarkerParams = new RelativeLayout.LayoutParams(markerSize, markerSize);
                middleMarkerParams.leftMargin =  markerSet.getMiddleX();
                middleMarkerParams.topMargin = dpToPx(getActivity(), 28);
                markerSet.getMiddleMarker().setLayoutParams(middleMarkerParams);
            }
        }
    }

    protected void setOffsetGoalNoUpdate(int offset) {
        if (touchDragging) {
            return;
        }
        offsetGoal = offset;
        if (offsetGoal + width / 2 > maxPos)
            offsetGoal = maxPos - width / 2;
        if (offsetGoal < 0)
            offsetGoal = 0;
    }

    protected int trap(int pos) {
        if (pos < 0) {
            return 0;
        }
        if (pos > maxPos) {
            return maxPos;
        }
        return pos;
    }

    protected synchronized void handlePause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
        if (waveformView != null) {
            //waveformView.setPlayback(-1); //TODO JJ Aquí no escondo la barra vertical amarilla del play
        }
        isPlaying = false;
        enableDisableButtons();
    }

    protected synchronized void handlePausePreview() {
        if (playerPreview != null && playerPreview.isPlaying()) {
            playerPreview.pause();
        }
        isPlayingPreview = false;
        enableDisableButtonsPreview();
    }

    protected synchronized void onPlay() {
        if (isPlaying) {
            handlePause();
            return;
        }
        if (player == null) {
            return;
        }
        playStartMsec = waveformView.pixelsToMillisecs(0);
        playStartOffset = 0;
        try {
            isPlaying = true;
            player.start();
            playStartOffset = playStartMsec;
            player.setOnCompletionListener((MediaPlayer mediaPlayer) -> handlePause());
            enableDisableButtons();
            updateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onPlayPreview() {
        if (playerPreview == null) {
            return;
        }
        try {
            isPlayingPreview = true;
            playerPreview.start();
            enableDisableButtonsPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected View.OnClickListener playListener = sender -> {
        if (sender.getId() == R.id.play) {
            onPlay();
        } /*else if (sender.getId() == R.id.rlPreview){
            saveNewFileFromMarkers(true);
        }*/
    };

    protected View.OnClickListener rewindListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (isPlaying) {
                int newPos = player.getCurrentPosition() - 30000;
                player.seekTo(newPos);
            }
        }
    };

    protected View.OnClickListener ffwdListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (isPlaying) {
                int newPos = player.getCurrentPosition() + 30000;
                player.seekTo(newPos);
            }
        }
    };

    protected void enableDisableButtons() {
        if (isPlaying) {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            tvDelete.setAlpha(0.6f);
            tvDelete.setEnabled(false);
            tvCopy.setAlpha(0.6f);
            tvCopy.setEnabled(false);
            tvPaste.setAlpha(0.6f);
            tvPaste.setEnabled(false);
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
            tvDelete.setAlpha(1f);
            tvDelete.setEnabled(true);
            tvCopy.setAlpha(1f);
            tvCopy.setEnabled(true);
            tvPaste.setAlpha(1f);
            tvPaste.setEnabled(true);
        }
    }

    protected void enableDisableButtonsPreview() {
        if (isPlayingPreview) {
            ivPlayPreview.setImageDrawable(getResources().getDrawable(R.drawable.record));
        } else {
            ivPlayPreview.setImageDrawable(getResources().getDrawable(R.drawable.play));
        }
    }

    protected void enableDisableSeekButtons() {
        if (player.getCurrentPosition() + 30000 > player.getDuration()) {
            forwardButton.setAlpha(0.6f);
            forwardButton.setEnabled(false);
        } else {
            forwardButton.setAlpha(1f);
            forwardButton.setEnabled(true);
        }
        if (player.getCurrentPosition() - 30000 < 0){
            rewindButton.setAlpha(0.6f);
            rewindButton.setEnabled(false);
        } else {
            rewindButton.setAlpha(1f);
            rewindButton.setEnabled(true);
        }
    }

    private boolean isAvailableArea(float x) {
        for (MarkerSet markerSet : markerSets) {
            if (x >= markerSet.getStartPos() && x <= markerSet.getEndPos()) {
                return false;
            }
        }
        return true;
    }

    private void setupSeekBar() {
        seekBar.setMax(player.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (isSeekBarTouched) {
                    tvTimePass.setText(getLengthFromEpochForPlayer(progress));
                    //TODO JJ new
                    if (waveformView != null) {
                        //System.out.println("seekbar progress       : " + progress);
                        //player.seekTo(progress);

                        int xPlayPos = waveformView.millisecsToPixels(progress);
                        waveformView.setPlayback(xPlayPos);

                        //updateDisplay();


                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouched = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouched = false;
                player.seekTo(seekBar.getProgress());
            }
        });
        tvDuration.setText(getLengthFromEpochForPlayer(player.getDuration()));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isSeekBarTouched) {
                    seekBar.setProgress(player.getCurrentPosition());
                    tvTimePass.setText(getLengthFromEpochForPlayer(player.getCurrentPosition()));
                }
                seekBarHandler.postDelayed(this, 10);
            }
        });
    }

    private void setupSeekBarPreview() {
        seekBarPreview.setMax(playerPreview.getDuration());
        //seekBarPreview.setMinimumHeight(3); //TODO JJ
        seekBarPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (isSeekBarTouchedPreview) {
                    tvTimePassPreview.setText(getLengthFromEpochForPlayer(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouchedPreview = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouchedPreview = false;
                playerPreview.seekTo(seekBarPreview.getProgress());
            }
        });
        tvDurationPreview.setText(getLengthFromEpochForPlayer(playerPreview.getDuration()));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerPreview != null && playerPreview.isPlaying() && !isSeekBarTouchedPreview) {
                    seekBarPreview.setProgress(playerPreview.getCurrentPosition());
                    tvTimePassPreview.setText(getLengthFromEpochForPlayer(playerPreview.getCurrentPosition()));
                }
                seekBarHandler.postDelayed(this, 10);
            }
        });
    }

    protected void saveNewFileFromMarkers(boolean shouldPlay) {
        if (markerSets == null || markerSets.size() == 0) {
            return;
        }
        if (isPlayingPreview) {
            handlePausePreview();
            return;
        }
        if (!shouldReloadPreview) {
            onPlayPreview();
            return;
        }

        for (MarkerSet markerSet : markerSets) {
            final String outPath = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record" + markerSet.getId() + ".m4a";
            double startTime = waveformView.pixelsToSeconds(markerSet.getStartPos());
            double endTime = waveformView.pixelsToSeconds(markerSet.getEndPos());
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

        editedWithMarkersFileName = getActivity().getExternalCacheDir().getAbsolutePath();
        editedWithMarkersFileName += "/limor_record" + System.currentTimeMillis() + "_marked.m4a";

        /*try {
            String mediaKey = "soun";
            List<Movie> listMovies = new ArrayList<>();
            for (String filename : audioFilePaths) {
                listMovies.add(MovieCreator.build(filename));
            }
            List<Track> listTracks = new LinkedList<>();
            for (Movie movie : listMovies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals(mediaKey)) {
                        listTracks.add(track);
                    }
                }
            }
            Movie outputMovie = new Movie();
            if (!listTracks.isEmpty()) {
                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
            }
            Container container = new DefaultMp4Builder().build(outputMovie);
            FileChannel fileChannel = new RandomAccessFile(String.format(editedWithMarkersFileName), "rws").getChannel();
            container.writeContainer(fileChannel);
            fileChannel.close();

            if (shouldPlay) {
                if (playerPreview != null) {
                    playerPreview.release();
                }
                playerPreview = null;
                playerPreview = new MediaPlayer();
                playerPreview.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playerPreview.setDataSource(editedWithMarkersFileName);
                playerPreview.setOnCompletionListener((MediaPlayer mediaPlayer) -> handlePausePreview());
                playerPreview.prepare();
                onPlayPreview();
                setupSeekBarPreview();
                audioFilePaths = new ArrayList<>();
                shouldReloadPreview = false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }

    protected void updateUndoRedoButtons() {
        if (stepManager.canUndo()) {
            tvUndo.setEnabled(true);
            tvUndo.setAlpha(1f);
        } else {
            tvUndo.setEnabled(false);
            tvUndo.setAlpha(0.5f);
        }
        if (stepManager.canRedo()) {
            tvRedo.setEnabled(true);
            tvRedo.setAlpha(1f);
        } else {
            tvRedo.setEnabled(false);
            tvRedo.setAlpha(0.5f);
        }
    }

    // endregion

    protected abstract String getFileName();

    protected abstract void populateMarkers();

    public static int dpToPx(Context context, int dps) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics()));
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

    public static String getLengthFromEpochForPlayer(double milliSeconds) {
        return getLengthFromEpochForPlayer((long)milliSeconds);
    }

    public static String getLengthFromEpochForPlayer(long milliSeconds) {

        final long ONE_SECOND = 1000;
        final long ONE_MINUTE = ONE_SECOND * 60;
        final long ONE_HOUR = ONE_MINUTE * 60;

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
}