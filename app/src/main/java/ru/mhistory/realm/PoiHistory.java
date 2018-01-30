package ru.mhistory.realm;
import io.realm.RealmObject;

/**
 * Created by shcherbakov on 28.01.2018.
 */

public class PoiHistory extends RealmObject {
    public long idPoi;
    public long idContent;

    public PoiHistory config(long idPoi, long idContent) {
        this.idPoi = idPoi;
        this.idContent = idContent;
        return this;
    }
}
