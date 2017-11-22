package ru.mhistory.bus.event;

import android.net.Uri;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class LoadJsonFromServerEvent {
    Uri storyFileUri;

    public LoadJsonFromServerEvent(Uri storyFileUri) {
        this.storyFileUri = storyFileUri;
    }

    public Uri getStoryFileUri() {
        return storyFileUri;
    }
}
