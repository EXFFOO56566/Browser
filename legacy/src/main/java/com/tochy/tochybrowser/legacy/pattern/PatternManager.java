package com.tochy.tochybrowser.legacy.pattern;

import android.content.Context;

import java.io.File;

import com.tochy.tochybrowser.legacy.utils.matcher.AbstractPatternManager;

public abstract class PatternManager<T extends PatternChecker> extends AbstractPatternManager<T> {
    public static final int TYPE_URL = 1;

    private static final String FOLDER_NAME = "pattern1";

    public PatternManager(Context context, String filename) {
        super(context, new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), filename));
    }
}
