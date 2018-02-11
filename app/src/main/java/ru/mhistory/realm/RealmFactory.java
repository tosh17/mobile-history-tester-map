package ru.mhistory.realm;

import android.content.Context;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import api.vo.Poi;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.PoiCacheAvailableEvent;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.geo.LatLng;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class RealmFactory {
    //
    private Context context;
    Realm realmPoi, realContent, realmHistory;
    RealmConfiguration configPoi, configContent, configHistory;
    private static RealmFactory factory = null;

    private RealmFactory(Context context) {
        Realm.init(context);
        this.context = context;
        configPoi = new RealmConfiguration.Builder()
                .name("poi.realm")
                .build();
        configContent = new RealmConfiguration.Builder()
                .name("content.realm")
                .build();
        configHistory = new RealmConfiguration.Builder()
                .name("history.realm")
                .build();
    }


    private void clearHistory(Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<PoiHistory> results = realm.where(PoiHistory.class).findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    public static RealmFactory getInstance(Context context) {
        if (factory == null) factory = new RealmFactory(context);
        return factory;
    }

    public static RealmFactory getInstance() {
        return factory;
    }

    public static <T> String listToStr(T[] list) {
        if (list.length == 0) return "";
        StringBuffer str = new StringBuffer("");
        for (T t : list) str.append(t.toString() + ";");
        return str.toString().substring(0, str.length() - 1);
    }

    public void savePoi(RealmList<PoiRealm> poi) {
        Realm realm = Realm.getInstance(configPoi);
        realm.beginTransaction();
        for (PoiRealm p : poi) {
            realm.copyToRealm(p);
        }
        realm.commitTransaction();
        Logger.d(LogType.Realm,"Poi save");
        realm.close();
    }

    public void SaveContent(List<PoiContentRealm> content) {
        Realm realm = Realm.getInstance(configContent);
        realm.beginTransaction();
        for (PoiContentRealm c : content)
            realm.copyToRealm(c);
        realm.commitTransaction();
        Logger.d(LogType.Realm,"Content save");
        realm.close();
    }

    public Set<Poi> findSquare(Pair<LatLng, LatLng> square) {
        Realm realm = Realm.getInstance(configPoi);
        Set<Poi> pois = new HashSet<>();
        RealmResults<PoiRealm> points = realm.where(PoiRealm.class)
              .between("longitude", square.first.longitude, square.second.longitude)
              .between("latitude", square.first.latitude, square.second.latitude)
              .findAll();

        Realm realm1 = Realm.getInstance(configContent);
        RealmResults<PoiContentRealm> contents; //todo  при привышении памяти или тормазах, можноискать забивать меньший квадрат
        Realm realm2 = Realm.getInstance(configHistory);
        RealmResults<PoiHistory> contentsHistory;
        for (PoiRealm p : points) {
            Poi poi = p.toPoi();
            pois.add(poi);
            contents = realm1.where(PoiContentRealm.class).equalTo("idPoi", poi.objId)
                    .findAll();
            for (PoiContentRealm c : contents) poi.addContent(c.toPoiContent());
            contentsHistory=realm2.where(PoiHistory.class).equalTo("idPoi", poi.objId).findAll();
            for (PoiHistory c : contentsHistory) poi.putContentToHistory(c.idContent);
            }

        realm.close();
        realm1.close();

        return pois;
    }

    public void saveHistory(long idPoi, long idContent){
        Realm realm = Realm.getInstance(configHistory);
        realm.beginTransaction();
        realm.copyToRealm(new PoiHistory().config(idPoi,idContent));
        realm.commitTransaction();
        Logger.d(LogType.Realm,"History save");
        realm.close();
    }

    public boolean isCreate() {
        Realm realm = Realm.getInstance(configPoi);
        PoiRealm point = realm.where(PoiRealm.class)
                .findFirst();
        return point!=null;
    }
}
