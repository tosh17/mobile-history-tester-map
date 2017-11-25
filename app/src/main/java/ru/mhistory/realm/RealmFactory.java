package ru.mhistory.realm;

import android.content.Context;

import java.util.List;
import java.util.Set;

import api.vo.Poi;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class RealmFactory {
    //
    private Context context;
    Realm realmPoi,realContent,realmHistory;
    RealmConfiguration configPoi,configContent,configHistory;
    private static RealmFactory factory=null;
    private RealmFactory(Context context){
      Realm.init(context);
      this.context=context;
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


    private void clearHistory(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<PoiRealm> results = realm.where(PoiRealm.class).findAll();
                results.deleteAllFromRealm();
            }
        }); }

    public static RealmFactory getInstance(Context context)  {
        if(factory==null) factory= new RealmFactory(context);
        return factory;
    }
    public static RealmFactory getInstance()  {
       return factory;
    }
    public static <T> String listToStr(T[] list){
        if(list.length==0) return "";
        StringBuffer str=new StringBuffer("");
        for(T t:list) str.append(t.toString()+";");
        return str.toString().substring(0,str.length()-1);
    }

    public void savePoi(RealmList<PoiRealm> poi) {
        Realm realm=Realm.getInstance(configPoi);
        realm.beginTransaction();
        for (PoiRealm p:poi){
            realm.copyToRealm(p);}
        realm.commitTransaction();
        realm.close();
    }

    public void SaveContent(List<PoiContentRealm> content) {
        Realm realm=Realm.getInstance(configContent);
        realm.beginTransaction();
        for(PoiContentRealm c:content)
        realm.copyToRealm(c);
        realm.commitTransaction();
        realm.close();
    }

    public Set<Poi> findSquare() {
        return null;
    }
}
