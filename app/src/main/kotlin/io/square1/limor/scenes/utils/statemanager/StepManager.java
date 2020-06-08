package io.square1.limor.scenes.utils.statemanager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class StepManager{

    private final static int MAX_STEPS = 4;

    public StepManager() {}

    private ArrayList<Step> stepsToUndo = new ArrayList<>();
    private ArrayList<Step> stepsToRedo = new ArrayList<>();

    public void addNewUndoStep(Step step) {
        handleNewStep(step, stepsToUndo);
    }

    public void addNewRedoStep(Step step) {
        handleNewStep(step, stepsToRedo);
    }

    private void handleNewStep(Step step, ArrayList<Step> steps) {
        if (steps.size() < MAX_STEPS) {
            steps.add(step);
        } else {
            steps.remove(0);
            steps.add(step);
        }
    }

    public Step getLastUndoStep() {
        if (stepsToUndo.size() > 0) {
            return stepsToUndo.get(stepsToUndo.size() - 1);
        } else {
            return null;
        }
    }

    public Step getLastRedoStep() {
        if (stepsToRedo.size() > 0) {
            return stepsToRedo.get(stepsToRedo.size() - 1);
        } else {
            return null;
        }
    }

    public ArrayList<Step> getStepsToUndo() {
        return stepsToUndo;
    }

    public ArrayList<Step> getStepsToRedo() {
        return stepsToRedo;
    }

    public void handleLastUndoStep() {
        stepsToUndo.remove(stepsToUndo.get(stepsToUndo.size() - 1));
    }

    public void handleLastRedoStep() {
        stepsToRedo.remove(stepsToRedo.get(stepsToRedo.size() - 1));
    }

    public boolean canUndo() {
        return stepsToUndo.size() > 0;
    }

    public boolean canRedo() {
        return stepsToRedo.size() > 0;
    }

    public void resetRedoSteps() {
        stepsToRedo.clear();
    }

//   @Override
//   public int describeContents() {
//       return 0;
//   }

//   @Override
//   public void writeToParcel(Parcel dest, int flags) {
//       dest.writeTypedList(this.stepsToUndo);
//   }

//   protected StepManager(Parcel in) {
//       this.stepsToUndo = in.createTypedArrayList(Step.CREATOR);
//   }

//   public static final Parcelable.Creator<StepManager> CREATOR = new Parcelable.Creator<StepManager>() {
//       @Override
//       public StepManager createFromParcel(Parcel source) {
//           return new StepManager(source);
//       }
//       @Override
//       public StepManager[] newArray(int size) {
//           return new StepManager[size];
//       }
//   };
}