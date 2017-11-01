package ru.mhistory.screen.main.ui;

import android.os.Environment;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

import java.io.File;

public class StoryFilePickerActivity extends AbstractFilePickerActivity<File> {

    public StoryFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            final String startPath,
            final int mode,
            final boolean allowMultiple,
            final boolean allowCreateDir) {
        AbstractFilePickerFragment<File> fragment = new StoryFilePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}