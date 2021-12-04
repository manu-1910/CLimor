package com.limor.app;

import android.util.Log;

import com.bugfender.sdk.Bugfender;
import com.bugfender.sdk.LogLevel;

import timber.log.Timber;

public class BugfenderTree extends Timber.Tree {
    // note: some people prefer extending DebugTree to get the tag automatically filled in

    @Override protected void log(int priority, String tag, String message, Throwable t) {
        // determine log level
        LogLevel logLevel = LogLevel.Debug;
        switch (priority) {
            case Log.VERBOSE:
                logLevel = LogLevel.Trace;
                break;
            case Log.DEBUG:
                logLevel = LogLevel.Debug;
                break;
            case Log.INFO:
                logLevel = LogLevel.Info;
                break;
            case Log.WARN:
                logLevel = LogLevel.Warning;
                break;
            case Log.ERROR:
                logLevel = LogLevel.Error;
                break;
            case Log.ASSERT:
                logLevel = LogLevel.Fatal;
                break;
        }
        // fill in caller info, skipping Timber's calls
        int lineNumber = -1;
        String method = "";
        String fileName = "";
        StackTraceElement callerInfo = findCaller(new Exception());
        if(callerInfo != null) {
            lineNumber = callerInfo.getLineNumber();
            method = callerInfo.getClassName() + "." + callerInfo.getMethodName();
            fileName = callerInfo.getFileName();
        }
        Bugfender.log(lineNumber, method, fileName, logLevel, tag, message);
    }

    private static StackTraceElement findCaller(Throwable t) {
        StackTraceElement[] stack = t.getStackTrace();
        for (int i = 2; i < stack.length; i++) { // 0th will be the caller of this method, inside this class, 1st will be Timber, 2nd might be Timber or the real caller
            final StackTraceElement stackTraceElement = stack[i];
            if (!stackTraceElement.getClassName().startsWith("timber.log.")) {
                return stackTraceElement;
            }
        }
        return null;
    }
}