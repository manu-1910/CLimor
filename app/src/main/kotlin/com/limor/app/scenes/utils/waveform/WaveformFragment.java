package com.limor.app.scenes.utils.waveform;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.limor.app.R;
import com.limor.app.common.BaseFragment;
import com.limor.app.scenes.utils.Commons;
import com.limor.app.scenes.utils.popupMenu.CustomPopupMenuView;
import com.limor.app.scenes.utils.statemanager.StepManager;
import com.limor.app.scenes.utils.waveform.soundfile.SoundFile;
import com.limor.app.scenes.utils.waveform.view.MarkerView;
import com.limor.app.scenes.utils.waveform.view.WaveformView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import me.kareluo.ui.OptionMenu;
import me.kareluo.ui.PopupView;
import timber.log.Timber;

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
    protected WaveformView waveformView;
    protected ImageButton playButton, rewindButton, forwardButton, btnRewindPreview, btnForwardPreview;
    protected ImageButton closeButton, infoButton;
    protected ImageButton btnClosePreview;
    protected AppCompatButton nextButton;
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
    private int playStartOffset;
    private SeekBar seekBar;
    private boolean isSeekBarTouched;
    private ImageView ivPlayPreview;
    private LinearLayout rlPreviewSection;
    private SeekBar seekBarPreview;
    protected boolean shouldReloadPreview;
    protected MarkerSet selectedMarker, editMarker;
    //    protected ArrayList<String> audioFilePaths = new ArrayList<>();
    protected StepManager stepManager;
    public static boolean isEditMode;
    protected boolean isInitialised;
    private View rootView;


    private Runnable updaterPreview;

    //    private final int ALLOWED_PIXEL_OFFSET = 18; // unused, I don't know what was it for
    public static final int NEW_WIDTH = 20;

    private enum MenuOption {
        Copy, Paste, Delete, Dismiss, Preview, Cancel
    }

    // endregion


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false);
            loadGui(rootView);
        }

        if (shouldWaitForAudio()) {
            return rootView;
        } else {
            loadFromFile();
        }

        return rootView;
    }

    protected void loadFromFile() {
        // if the soundFile object is not loaded yet, then let's load it
        if (soundFile == null) {
            loadFromFile(fileName); // this method also calls reloadVisualizer

            // if the soundObject is already loaded, then let's initialize the visualizer
        } else {
            handler.post(this::reloadVisualizer);
        }
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
        updaterPreview = new Runnable() {
            @Override
            public void run() {
                if (playerPreview != null && playerPreview.isPlaying()) {
                    int posMarkerStart = selectedMarker.getStartPos();
                    int currentStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);
                    seekBarPreview.setProgress(playerPreview.getCurrentPosition() - currentStartMillis);
                    tvTimePassPreview.setText(Commons.getLengthFromEpochForPlayer(seekBarPreview.getProgress()));
                }
                seekBarHandler.postDelayed(this, 10);
            }
        };
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
        Timber.d("waveformTouchStart");
        touchDragging = true;
        touchStart = x;
        touchInitialOffset = offset;
        waveformTouchStartMsec = System.currentTimeMillis();
    }

    @Override
    public void waveformTouchMove(float x) {
        Timber.d("waveformTouchMove");
        offset = trap((int) (touchInitialOffset + (touchStart - x)));
        updateDisplay();
    }

    @Override
    public void waveformTouchEnd(float x, float y) {
        Timber.d("waveformTouchEnd");
        touchDragging = false;
        offsetGoal = offset;
        long elapsedMsec = System.currentTimeMillis() - waveformTouchStartMsec;
        //We check could we create marker or not
        if (elapsedMsec < 200 &&
                //y < Commons.dpToPx(getActivity(), 48) &&
                x < waveformView.maxPos() && isAvailableArea(x) &&
                !isEditMode) {
//            int seekMsec = waveformView.pixelsToMillisecs((int) (touchStart + offset));

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
    public void markerDraw() {
    }


    @Override
    public void markerTouchStart(MarkerView marker, float x) {
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            return;
        }
        markerTouchStartMsec = System.currentTimeMillis();
        touchStart = x;
        touchInitialStartPos = marker.getMarkerSet().getStartPos();
        touchInitialMiddlePos = marker.getMarkerSet().getMiddlePos();
        touchInitialEndPos = marker.getMarkerSet().getEndPos();


//        if (marker.getType() == MarkerView.MIDDLE_MARKER) { //TODO JJ NEW LINES
//            showPopUpMenu(marker);
//        }


    }


    @Override
    public void markerTouchMove(MarkerView marker, float x) {
        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
            return;
        }

        // unused, I don't know what was it for
//        if ((x > touchStart && x - touchStart > ALLOWED_PIXEL_OFFSET) || (x < touchStart && touchStart - x > ALLOWED_PIXEL_OFFSET)) {
//        }
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
//        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
//            return;
//        }

//        long elapsedMsec = System.currentTimeMillis() - markerTouchStartMsec;
//        if (elapsedMsec > 350 && marker.getType() == MarkerView.MIDDLE_MARKER && !isMovingTooMuch) { //TODO JJ ORIGINAL LINES
//            showPopUpMenu(marker);
//        }

        if (marker.getType() == MarkerView.MIDDLE_MARKER) { //TODO JJ NEW LINES
            showPopUpMenu(marker);
        }

        touchDragging = false;

        if (playerPreview != null) {
            try {
                preparePlayerPreview(false);
            } catch (IOException e) {
                e.printStackTrace();
                Timber.e("Error trying to load preview");
                new AlertDialog.Builder(getContext())
                        .setMessage("Error trying to load preview")
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
            seekBarPreview.setProgress(0);
            updateButtonsPreview();
            updateDurationsPreview();
        }
    }


    @Override
    public void markerFocus(MarkerView marker) {
        //TODO JJ New the app explote copying and pasting multiple markers, I add try/catch to avoid ANR and investigate later
//        if (isEditMode && !marker.getMarkerSet().isEditMarker()) {
//            marker.clearFocus();
//        }
    }


    protected void addMarker(int startPos, int endPos, boolean isEditMarker, Integer color) {

        //Only 1 marker is available at same time, except when copy and paste marker is selected
        if (markerSets.size() >= 1 && !isEditMarker) {
            removeMarker(markerSets.get(0));
            //return;
        }

        MarkerSet newMarkerSet = new MarkerSet();

        MarkerView startMarker = new MarkerView(getActivity());
        startMarker.setType(MarkerView.START_MARKER);
        if (isEditMarker) {
            startMarker.setImageDrawable(getResources().getDrawable(R.drawable.marker_circle_green));
        } else {
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
            MarkerView markerView = (MarkerView) view;
            if (isSelected && !isEditMarker) {
                selectedMarker = markerView.getMarkerSet();
            } else if (isEditMarker) {
                editMarker = markerView.getMarkerSet();
            }
        });
        enableMarker(middleMarker, true);
        enableMarker(startMarker, true); //This line is new added sq1

        newMarkerSet.setMiddleMarker(middleMarker);
        newMarkerSet.setMiddlePos(startPos + ((endPos - startPos) / 2));
        newMarkerSet.setMiddleVisible(true);

        newMarkerSet.setStartMarker(startMarker);
        newMarkerSet.setStartPos(startPos);
        newMarkerSet.setStartVisible(true);

        newMarkerSet.setEndMarker(endMarker);
        newMarkerSet.setEndPos(endPos);
        newMarkerSet.setEndVisible(true);

        newMarkerSet.setBackgroundColor(isEditMarker ? R.color.white : color != null ? color : R.color.colorBackgroundMarker);
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
    }


    private void enableMarker(MarkerView markerView, boolean shouldEnable) {
        if (shouldEnable) {
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


    /*
     * This function will show a contextual menú at the top of the marker
     * */
    private void showPopUpMenu(MarkerView marker) {

        // Be careful with this SupressLint, this is intended to avoid a false error that android studio reports
        // with the following line but it's not real. But I insist, be careful, if this fails in the future, take a look at it.
        @SuppressLint("RestrictedApi")
        CustomPopupMenuView menuView = new CustomPopupMenuView(requireContext(), R.menu.menu_popup_edit, new MenuBuilder(requireActivity()));
        if (marker.getMarkerSet().isMiddleVisible() && marker.getMarkerSet().isEditMarker()) {
            menuView.setMenuItems(Arrays.asList(
                    new OptionMenu(getString(R.string.menu_paste)),
                    new OptionMenu(getString(R.string.menu_cancel))
            ));
        } else {
            menuView.setMenuItems(Arrays.asList(
                    new OptionMenu(getString(R.string.menu_preview)),
                    new OptionMenu(getString(R.string.menu_copy)),
                    new OptionMenu(getString(R.string.menu_delete)),
                    new OptionMenu(getString(R.string.menu_dismiss))
            ));
        }

        //menuView.setSites(PopupView.SITE_BOTTOM, PopupView.SITE_LEFT, PopupView.SITE_TOP, PopupView.SITE_RIGHT);
        menuView.setSites(PopupView.SITE_BOTTOM);

        menuView.setOnMenuClickListener((position, menu) -> {
            MenuOption menuOption = MenuOption.valueOf(menu.getTitle().toString());
            switch (menuOption) {
                case Copy:
                    handlePause();
                    tvCopy.performClick();
                    break;
                case Paste:
                    handlePause();
                    showPreviewLayout(false);
                    tvPaste.performClick();
                    break;
                case Delete:
                    handlePause();
                    showPreviewLayout(false);
                    tvDelete.performClick();
                    break;
                case Dismiss:
                case Cancel:
                    removeMarker(marker.getMarkerSet());
                    break;
                case Preview:
                    onPreviewClicked();
                    break;
            }
            return true;
        });

        menuView.show(marker);
    }


    private void onPreviewClicked() {
        showPreviewLayout(true);
        try {
            preparePlayerPreview(false);
            handlePause();
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("Error trying to load preview");
            new AlertDialog.Builder(getContext())
                    .setMessage("Error trying to load preview")
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
        updateButtonsPreview();
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

        showPreviewLayout(false);
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
                Commons.showAlertCustomButtons(getActivity(), getString(R.string.merge), getString(R.string.merge_sections_prompt),
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
                Commons.showAlertCustomButtons(getActivity(), getString(R.string.merge), getString(R.string.merge_sections_prompt),
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

        markerSize = (int) (24 * density);
        markerSizeDown = (int) (19 * density);
        markerInset = (int) (18 * density);
        markerTopOffset = (int) (10 * density);

        absoluteLayout = view.findViewById(R.id.layWaveform);
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
//        tvDelete = view.findViewById(R.id.tvDelete);
//        tvPaste = view.findViewById(R.id.tvPaste);
//        tvCopy = view.findViewById(R.id.tvCopy);
        tvDelete = new TextView(getContext());
        tvDelete.setText(getString(R.string.menu_delete));
        tvPaste = new TextView(getContext());
        tvPaste.setText(getString(R.string.menu_paste));
        tvCopy = new TextView(getContext());
        tvCopy.setText(getString(R.string.menu_copy));
        tvTimePassPreview = view.findViewById(R.id.tvTimePassPreview);
        tvDurationPreview = view.findViewById(R.id.tvDurationPreview);
        ivPlayPreview = view.findViewById(R.id.ivPlayPreview);
        ivPlayPreview.setOnClickListener(onPlayPreviewListener);
        btnClosePreview = view.findViewById(R.id.btnClosePreview);
        btnClosePreview.setOnClickListener(onClosePreviewListener);
        btnRewindPreview = view.findViewById(R.id.rewPreview);
        btnRewindPreview.setOnClickListener(onRewindPreviewListener);
        btnForwardPreview = view.findViewById(R.id.ffwdPreview);
        btnForwardPreview.setOnClickListener(onForwardReviewListener);

        rlPreviewSection = view.findViewById(R.id.rlPreviewSection);


        closeButton = view.findViewById(R.id.btnClose);
        //infoButton = view.findViewById(R.id.btnInfo);
        nextButton = view.findViewById(R.id.nextButtonEdit);

        maxPos = 0;
        if (soundFile != null && !waveformView.hasSoundFile()) {
            waveformView.setSoundFile(soundFile);
            waveformView.recomputeHeights(density);
            maxPos = waveformView.maxPos();
        }
        updateDisplay();
    }


    // this method loads the received filename into the mediaplayer, to be heard, and
    // prepares the soundFile object too, necessary to be used with the visualizer
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
                    enableDisableSeekButtons();
                } catch (final IOException e) {
                    getActivity().runOnUiThread(() -> new AlertDialog.Builder(getContext())
                            .setTitle(getContext().getString(R.string.title_error))
                            .setMessage(getContext().getString(R.string.error_loading_audio_file))
                            .setPositiveButton(getContext().getString(R.string.yes), null)
                            .show());
                    Timber.e("There was an error trying to load your audio file -> %s", file.getAbsolutePath());
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
                    Commons.showAlertYesNo(getContext(), getContext().getString(R.string.title_error), getContext().getString(R.string.error_memory_full), null);
                    return;
                } catch (final Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Commons.showAlertOkButton(getContext(), R.string.title_error, R.string.error_memory_full, null);
                    return;
                }
                if (loadingKeepGoing) {
                    handler.post(() -> reloadVisualizer());
                }
            }
        }.start();
    }


    // this method has to be called after the soundFile object is already initialized.
    // what it does is to actually initialize the visualizer itself
    protected void reloadVisualizer() {
        waveformView.setSoundFile(soundFile);
        waveformView.recomputeHeights(density);
        maxPos = waveformView.maxPos();
        touchDragging = false;
        offset = 0;
        offsetGoal = 0;
        progressDialog.dismiss();
        updateDisplay();
        setupSeekBar();
        populateMarkers();
        shouldReloadPreview = true;
    }


    protected synchronized void updateDisplay() {
        if (isPlaying) {
            int now = player.getCurrentPosition() + playStartOffset;
            int playbackPosition = waveformView.millisecsToPixels(now * NEW_WIDTH);

            if (waveformView != null) {
                waveformView.setPlayback(playbackPosition);
            }

            setOffsetGoalNoUpdate(playbackPosition - width / 2); //The offset is the responsible of scrolling velocity and keep the yellow play line inside the width of the screen
            int offsetDelta = offsetGoal - offset;

            int CORRECTION = 10; //TODO JJ había un 10
            if (offsetDelta > CORRECTION) {
                offsetDelta = offsetDelta / CORRECTION;
            } else if (offsetDelta > 0) {
                offsetDelta = 1;
            } else if (offsetDelta < -1 * CORRECTION) {
                offsetDelta = offsetDelta / CORRECTION;
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


    /**
     * this method updates the positions of the markers
     */
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


                markerSet.getMiddleMarker().setColorFilter(markerSet.isEditMarker() ? ContextCompat.getColor(getContext(), R.color.marker_paste_green) : ContextCompat.getColor(getContext(), R.color.marker_blue));

                //markerSet.getMiddleMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                markerSet.getMiddleMarker().setAlpha(markerSet.isEditMarker() ? 1.0f : 0.5f);
                //markerSet.getStartMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                markerSet.getStartMarker().setAlpha(0.5f);
                //markerSet.getEndMarker().setColorFilter(ContextCompat.getColor(getContext(), markerSet.getBackgroundColor()));
                markerSet.getEndMarker().setAlpha(0.5f);

                if (isEditMode && !markerSet.isEditMarker()) {
                    markerSet.getMiddleMarker().setAlpha(0.15f);
                    markerSet.getStartMarker().setAlpha(0.15f);
                    markerSet.getEndMarker().setAlpha(0.15f);
                }


                int markerTopMargin = (int) (25 * density);

                RelativeLayout.LayoutParams startMarkerParams = new RelativeLayout.LayoutParams(markerSizeDown, markerSizeDown);
                startMarkerParams.leftMargin = markerSet.getStartX() + Commons.dpToPx(getActivity(), 9);
                startMarkerParams.topMargin = waveformView.getMeasuredHeight() - markerTopMargin;
                markerSet.getStartMarker().setLayoutParams(startMarkerParams);

                RelativeLayout.LayoutParams endMarkerParams = new RelativeLayout.LayoutParams(markerSizeDown, markerSizeDown);
                endMarkerParams.leftMargin = markerSet.getEndX() - Commons.dpToPx(getActivity(), 9);
                endMarkerParams.topMargin = waveformView.getMeasuredHeight() - markerTopMargin;
                markerSet.getEndMarker().setLayoutParams(endMarkerParams);

                RelativeLayout.LayoutParams middleMarkerParams = new RelativeLayout.LayoutParams(markerSize, markerSize);
                middleMarkerParams.leftMargin = markerSet.getMiddleX();
                middleMarkerParams.topMargin = Commons.dpToPx(getActivity(), 28);
                markerSet.getMiddleMarker().setLayoutParams(middleMarkerParams);
            }
        }
    }

    /**
     * As the preview layoutThis method is used to hide the preview layout when the screen loads. It hides it
     * fast so the user cannot see the first hidding.
     */
    protected void hidePreviewLayoutQuickly() {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,
                0,// toXDelta
                rlPreviewSection.getHeight()
        );
        animate.setDuration(100);
        animate.setFillAfter(true);
        rlPreviewSection.startAnimation(animate);
    }

    protected void showPreviewLayout(boolean visible) {
        // we check if the layout is clickable to avoid showing or hiding it twice
        if (visible && !rlPreviewSection.isClickable()) {
            rlPreviewSection.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    rlPreviewSection.getHeight(),  // fromYDelta
                    0);                // toYDelta
            animate.setDuration(500);
            animate.setFillAfter(true);
            rlPreviewSection.startAnimation(animate);
            rlPreviewSection.setClickable(true);
            btnClosePreview.setClickable(true);
            btnForwardPreview.setClickable(true);
            btnRewindPreview.setClickable(true);
            seekBarPreview.setClickable(true);

            // we check if the layout is clickable to avoid showing or hiding it twice
        } else if (!visible && rlPreviewSection.isClickable()) {
            rlPreviewSection.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,
                    0,// toXDelta
                    rlPreviewSection.getHeight()
            );
            animate.setDuration(500);
            animate.setFillAfter(true);
            rlPreviewSection.startAnimation(animate);
            rlPreviewSection.setClickable(false);
            btnClosePreview.setClickable(false);
            btnForwardPreview.setClickable(false);
            btnRewindPreview.setClickable(false);
            seekBarPreview.setClickable(false);
        }
    }


    /**
     * This function is the responsible to set the offsetGoal in the center of the screen
     */
    private void setOffsetGoalNoUpdate(int offset) {
        if (touchDragging) {
            return;
        }

        int middle = width / 2;

        offsetGoal = offset;

        if (offsetGoal + middle > maxPos) {
            offsetGoal = maxPos - middle;
        }
        if (offsetGoal < 0) {
            offsetGoal = 0;
        }
    }

    protected int trap(int pos) {
        if (pos < 0) {
            return 0;
        }
        return Math.min(pos, maxPos);
    }

    protected synchronized void handlePause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }

        // this piece of code hid the yellow play line when paused, but we don't want that to happen anymore
//        if (waveformView != null) {
//            waveformView.setPlayback(-1);
//        }

        isPlaying = false;
        enableDisableButtons();
        enableDisableSeekButtons();
    }

    protected synchronized void handlePausePreview() {
        if (playerPreview != null && playerPreview.isPlaying()) {
            playerPreview.pause();
        }
        updateButtonsPreview();
    }

    private void updateButtonsPreview() {
        if (playerPreview != null && playerPreview.isPlaying()) {
            ivPlayPreview.setImageResource(R.drawable.ic_pause_big);
        } else if (playerPreview != null) {
            ivPlayPreview.setImageResource(R.drawable.play_button);
        }
    }

    protected synchronized void onPlay() {
        if (isPlaying) {
            handlePause();
            return;
        }
        if (player == null) {
            return;
        }
        int playStartMsec = waveformView.pixelsToMillisecs(0);
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

    protected View.OnClickListener playListener = sender -> {
        if (sender.getId() == R.id.play) {
            onPlay();
        } /*else if (sender.getId() == R.id.rlPreview){
            saveNewFileFromMarkers(true);
        }*/
    };

    protected View.OnClickListener onClosePreviewListener = sender -> {
        if (sender.getId() == R.id.btnClosePreview) {
            if (playerPreview != null && playerPreview.isPlaying())
                playerPreview.stop();
            showPreviewLayout(false);
        }
    };

    protected View.OnClickListener onRewindPreviewListener = sender -> {
        // we calculate the start previewPosition
        int posMarkerStart = selectedMarker.getStartPos();
        int currentPreviewStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);

        // we get the progress of the seekBar and substract 30seconds
        int newProgress = seekBarPreview.getProgress() - 5000;
        // let's do this to not to get negative progress
        if (newProgress < 0) {
            newProgress = 0;
        }

        // let's set the new progress to the seekbar
        seekBarPreview.setProgress(newProgress);

        // we have to add the currentPreviewStartMillis because the player has loaded all the audio, not just
        // the small piece of preview selected, so, for example:
        // - if you have a total audio of 10 secs
        // - and you have selected from second 2 to 6, 4 seconds selected in total
        // - the seekbarWill have 4000ms of progress
        // - but the playerPreview will have 10000ms of progress
        // - so if the user puts the seekbar to 0, it's actually not 0 in the player, it's 0 + 2 seconds
        //      of the start preview selected
        playerPreview.seekTo(newProgress + currentPreviewStartMillis);
    };

    protected View.OnClickListener onForwardReviewListener = sender -> {
        // we calculate the start previewPosition
        int posMarkerStart = selectedMarker.getStartPos();
        int currentPreviewStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);

        // let's get the progress of the seekbar and add 30 seconds
        int newProgress = seekBarPreview.getProgress() + 5000;

        // let's do this to not overflow the seekbar
        if (newProgress > seekBarPreview.getMax()) {
            newProgress = seekBarPreview.getMax();
        }
        seekBarPreview.setProgress(newProgress);


        // this specific case is to control that if the user clicks forward and gets to the end of
        // the audio, it will go back to the beginning
        if (newProgress >= seekBarPreview.getMax()) {
            playerPreview.pause();
            playerPreview.seekTo(currentPreviewStartMillis);
            updateButtonsPreview();
        } else {


            // we have to add the currentPreviewStartMillis because the player has loaded all the audio, not just
            // the small piece of preview selected, so, for example:
            // - if you have a total audio of 10 secs
            // - and you have selected from second 2 to 6, 4 seconds selected in total
            // - the seekbarWill have 4000ms of progress
            // - but the playerPreview will have 10000ms of progress
            // - so if the user puts the seekbar to 3, it's actually not 3 in the player, it's 3 + 2 seconds
            //      of the start preview selected
            playerPreview.seekTo(newProgress + currentPreviewStartMillis);
        }
    };

    protected View.OnClickListener onPlayPreviewListener = sender -> {
        if (sender.getId() == R.id.ivPlayPreview) {
            if (playerPreview != null && !playerPreview.isPlaying()) {
                seekPreviewPlayerToSeekbarPosition();
                playerPreview.start();
            } else if (playerPreview != null) {
                playerPreview.pause();
            }

            updateButtonsPreview();
        }
    };

    protected View.OnClickListener rewindListener = new View.OnClickListener() {
        public void onClick(View sender) {
//            if (isPlaying) {
            int newPos = player.getCurrentPosition() - 5000;
            player.seekTo(newPos);
            seekBar.setProgress(newPos);
//            }
        }
    };

    protected View.OnClickListener ffwdListener = new View.OnClickListener() {
        public void onClick(View sender) {
//            if (isPlaying) {
            int newPos = player.getCurrentPosition() + 5000;
            player.seekTo(newPos);
            seekBar.setProgress(newPos);
//            }
        }
    };

    protected void enableDisableButtons() {
        if (isPlaying) {
            playButton.setImageResource(R.drawable.ic_pause_big);
            tvDelete.setAlpha(0.6f);
            tvDelete.setEnabled(false);
            tvCopy.setAlpha(0.6f);
            tvCopy.setEnabled(false);
            tvPaste.setAlpha(0.6f);
            tvPaste.setEnabled(false);
        } else {
            playButton.setImageResource(R.drawable.play_button);
            tvDelete.setAlpha(1f);
            tvDelete.setEnabled(true);
            tvCopy.setAlpha(1f);
            tvCopy.setEnabled(true);
            tvPaste.setAlpha(1f);
            tvPaste.setEnabled(true);
        }
    }

    protected void enableDisableSeekButtons() {
        forwardButton.setAlpha(1f);
        forwardButton.setEnabled(true);
        rewindButton.setAlpha(1f);
        rewindButton.setEnabled(true);
    }

    private boolean isAvailableArea(float x) {
        for (MarkerSet markerSet : markerSets) {
            if (x >= markerSet.getStartPos() && x <= markerSet.getEndPos()) {
                return false;
            }
        }
        return true;
    }

    public void seekSeekBarToStartPosition() {
        seekBar.setProgress(0);
        seekPlayerToSeekbarPosition();
    }

    private void seekPlayerToSeekbarPosition() {
        if (player != null) {
            player.seekTo(seekBar.getProgress());
            tvDuration.setText(Commons.getLengthFromEpochForPlayer(player.getDuration()));
        }
    }

    private void setupSeekBar() {
        seekBar.setMax(player.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (isSeekBarTouched) {
                    tvTimePass.setText(Commons.getLengthFromEpochForPlayer(progress));
                    if (waveformView != null) {
                        player.seekTo(progress);
                        int xPlayPos = waveformView.millisecsToPixels(progress * NEW_WIDTH);
                        waveformView.setPlayback(xPlayPos);
                        updateDisplay();
                    }
                } else {
                    if (player != null) { //TODO JJ THIS IS NEW, TEST IT with other marker options (copy, paste, delete, ...)
                        seekBar.setProgress(progress);
                        int xPlayPos = waveformView.millisecsToPixels(progress * NEW_WIDTH);
                        waveformView.setPlayback(xPlayPos);
                        tvTimePass.setText(Commons.getLengthFromEpochForPlayer(progress));
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
        tvDuration.setText(Commons.getLengthFromEpochForPlayer(player.getDuration()));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isSeekBarTouched) {
                    seekBar.setProgress(player.getCurrentPosition());
                    tvTimePass.setText(Commons.getLengthFromEpochForPlayer(player.getCurrentPosition()));
                }
                seekBarHandler.postDelayed(this, 10);
            }
        });
    }

    private void seekPreviewPlayerToStartPosition() {
        seekBarPreview.setProgress(0);
        seekPreviewPlayerToSeekbarPosition();
    }

    private void seekPreviewPlayerToSeekbarPosition() {
        int posMarkerStart = selectedMarker.getStartPos();
        int currentStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);
        playerPreview.seekTo(currentStartMillis + seekBarPreview.getProgress());
    }

    private void setupSeekBarPreview() {
        int posMarkerStart = selectedMarker.getStartPos();
        int currentStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);

        int posMarkerEnd = selectedMarker.getEndPos();
        int currentEndMillis = (int) (waveformView.pixelsToSeconds(posMarkerEnd / NEW_WIDTH) * 1000);

        int currentDuration = currentEndMillis - currentStartMillis;

        seekBarPreview.setMax(currentDuration);
        seekBarPreview.setProgress(0);
        seekBarPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentStartMillis + progress >= currentEndMillis) {
                    playerPreview.pause();
                    seekPreviewPlayerToStartPosition();
                    updateButtonsPreview();
                } else {
                    if (fromUser) {
                        seekPreviewPlayerToSeekbarPosition();
                    }
                }
                tvTimePassPreview.setText(Commons.getLengthFromEpochForPlayer(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateDurationsPreview();
        getActivity().runOnUiThread(updaterPreview);
    }

    private void updateDurationsPreview() {
        if (selectedMarker == null)
            return;


        int posMarkerStart = selectedMarker.getStartPos();
        int currentStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);

        int posMarkerEnd = selectedMarker.getEndPos();
        int currentEndMillis = (int) (waveformView.pixelsToSeconds(posMarkerEnd / NEW_WIDTH) * 1000);

        int currentDuration = currentEndMillis - currentStartMillis;
        tvTimePassPreview.setText(Commons.getLengthFromEpochForPlayer(seekBarPreview.getProgress()));
        tvDurationPreview.setText(Commons.getLengthFromEpochForPlayer(currentDuration));
    }

    protected void preparePlayerPreview(boolean shouldPlay) throws IOException {
        if (markerSets == null || markerSets.size() == 0 || selectedMarker == null) {
            return;
        }
        if (playerPreview != null && playerPreview.isPlaying()) {
            handlePausePreview();
            return;
        }

        if (playerPreview != null) {
            playerPreview.release();
            seekBarHandler.removeCallbacks(updaterPreview);
        }

        int posMarkerStart = selectedMarker.getStartPos();
        int currentStartMillis = (int) (waveformView.pixelsToSeconds(posMarkerStart / NEW_WIDTH) * 1000);

        playerPreview = null;
        playerPreview = new MediaPlayer();
        playerPreview.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playerPreview.setDataSource(file.getAbsolutePath());
        playerPreview.setOnCompletionListener((MediaPlayer mediaPlayer) -> handlePausePreview());
        playerPreview.prepare();
        playerPreview.seekTo(currentStartMillis);
        if (shouldPlay)
            playerPreview.start();
        setupSeekBarPreview();
        shouldReloadPreview = false;
    }

//    @Deprecated
//    protected void saveNewFileFromMarkers(boolean shouldPlay) {
//        if (markerSets == null || markerSets.size() == 0) {
//            return;
//        }
//        if (isPlayingPreview) {
//            handlePausePreview();
//            return;
//        }
//        if (!shouldReloadPreview) {
//            onPlayPreview();
//            return;
//        }
//
//        List<String> audioFilePaths = new ArrayList<>();
//        for (MarkerSet markerSet : markerSets) {
//            final String outPath = getActivity().getExternalCacheDir().getAbsolutePath() + "/limor_record" + markerSet.getId() + ".m4a";
//            double startTime = waveformView.pixelsToSeconds(markerSet.getStartPos() / NEW_WIDTH); //Seems to be ok with the time of the marker
//            double endTime = waveformView.pixelsToSeconds(markerSet.getEndPos() / NEW_WIDTH);    //Seems to be ok with the time of the marker
//            final int startFrame = waveformView.secondsToFrames(startTime);
//            final int endFrame = waveformView.secondsToFrames(endTime);
//            final File outFile = new File(outPath);
//            try {
//                soundFile.WriteFile(outFile, startFrame, endFrame - startFrame);
//                audioFilePaths.add(outFile.getAbsolutePath());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        editedWithMarkersFileName = getActivity().getExternalCacheDir().getAbsolutePath();
//        editedWithMarkersFileName += "/limor_record" + System.currentTimeMillis() + "_marked.m4a";
//
//        try {
//            String mediaKey = "soun";
//            List<Movie> listMovies = new ArrayList<>();
//            for (String filename : audioFilePaths) {
//                listMovies.add(MovieCreator.build(filename));
//            }
//            List<Track> listTracks = new LinkedList<>();
//            for (Movie movie : listMovies) {
//                for (Track track : movie.getTracks()) {
//                    if (track.getHandler().equals(mediaKey)) {
//                        listTracks.add(track);
//                    }
//                }
//            }
//            Movie outputMovie = new Movie();
//            if (!listTracks.isEmpty()) {
//                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
//            }
//            Container container = new DefaultMp4Builder().build(outputMovie);
//            FileChannel fileChannel = new RandomAccessFile(editedWithMarkersFileName, "rws").getChannel();
//            container.writeContainer(fileChannel);
//            fileChannel.close();
//
//            if (shouldPlay) {
//                if (playerPreview != null) {
//                    playerPreview.release();
//                }
//                playerPreview = null;
//                playerPreview = new MediaPlayer();
//                playerPreview.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                playerPreview.setDataSource(editedWithMarkersFileName);
//                playerPreview.setOnCompletionListener((MediaPlayer mediaPlayer) -> handlePausePreview());
//                playerPreview.prepare();
//                onPlayPreview();
//                setupSeekBarPreview();
//                audioFilePaths = new ArrayList<>();
//                shouldReloadPreview = false;
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    protected void updateUndoRedoButtons() {
        if (stepManager.canUndo()) {
            tvUndo.setEnabled(true);
            //tvUndo.setAlpha(1f);
            tvUndo.setTextColor(ContextCompat.getColor(requireContext(), R.color.textAccent));
        } else {
            tvUndo.setEnabled(false);
            //tvUndo.setAlpha(0.5f);
            tvUndo.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary));
        }
        if (stepManager.canRedo()) {
            tvRedo.setEnabled(true);
            //tvRedo.setAlpha(1f);
            tvRedo.setTextColor(ContextCompat.getColor(requireContext(), R.color.textAccent));
        } else {
            tvRedo.setEnabled(false);
            //tvRedo.setAlpha(0.5f);
            tvRedo.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary));
        }
    }

    // endregion

    protected abstract String getFileName();

    protected abstract void populateMarkers();

    public boolean shouldWaitForAudio() {
        return false;
    }

    public int getLayoutId() {
        return R.layout.fragment_waveform;
    }

}