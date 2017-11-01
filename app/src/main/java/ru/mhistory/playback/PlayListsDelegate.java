package ru.mhistory.playback;

import android.support.annotation.NonNull;

public abstract class PlayListsDelegate {
    protected final PlayLists playLists;

    public PlayListsDelegate(@NonNull PlayLists playLists) {
        this.playLists = playLists;
    }

    public abstract int getTrackSequence();

    public abstract void next();

    public abstract boolean hasNextTrack();

    public abstract void reset();

    @NonNull
    public abstract PlayList getCurrentPlayList();

    public abstract void setCurrentPlayList(@NonNull PlayList currentPlayListPlayList);


    public static class ByIndexDelegate extends PlayListsDelegate {
        private int currentPlayListIndex = -1;
        private int currentTrackSequence = -1;

        public ByIndexDelegate(@NonNull PlayLists playLists) {
            super(playLists);
        }

        @Override
        public int getTrackSequence() {
            return currentTrackSequence;
        }

        @Override
        public void next() {
            currentPlayListIndex++;
            if (currentPlayListIndex >= playLists.getSize()) {
                currentPlayListIndex = 0;
            }
            PlayList playList = playLists.getByIndex(currentPlayListIndex);
            if (playList.hasNextTrack()) {
                playList.next();
                currentTrackSequence++;
            } else {
                next();
            }
        }

        @Override
        public boolean hasNextTrack() {
            return currentTrackSequence < playLists.getLength() - 1;
        }

        @Override
        public void reset() {
            currentTrackSequence = -1;
            currentPlayListIndex = -1;
        }

        @NonNull
        @Override
        public PlayList getCurrentPlayList() {
            return playLists.getByIndex(currentPlayListIndex);
        }

        @Override
        public void setCurrentPlayList(@NonNull PlayList currentPlayList) {
            // empty
        }
    }


    public static class ByNameDelegate extends PlayListsDelegate {
        private int currentTrackSequence = -1;
        private PlayList currentPlayList;

        public ByNameDelegate(@NonNull PlayLists playLists) {
            super(playLists);
        }

        @Override
        public int getTrackSequence() {
            return currentTrackSequence;
        }

        @Override
        public void next() {
            if (currentPlayList == null) {
                return;
            }
            currentPlayList.next();
            currentTrackSequence++;
        }

        @Override
        public boolean hasNextTrack() {
            return currentPlayList != null && currentPlayList.hasNextTrack();
        }

        @Override
        public void reset() {
            currentTrackSequence = -1;
            currentPlayList = null;
        }

        @NonNull
        @Override
        public PlayList getCurrentPlayList() {
            return currentPlayList;
        }

        @Override
        public void setCurrentPlayList(@NonNull PlayList currentPlayList) {
            this.currentPlayList = currentPlayList;
        }
    }
}


