package com.tochy.tochybrowser.legacy.pattern;

import com.tochy.tochybrowser.legacy.utils.matcher.AbstractPatternChecker;

public abstract class PatternChecker extends AbstractPatternChecker<PatternAction> {
    protected PatternChecker(PatternAction pattern_action) {
        super(pattern_action);
    }
}
