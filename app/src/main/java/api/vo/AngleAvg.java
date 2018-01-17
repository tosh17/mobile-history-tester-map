package api.vo;

import java.util.Arrays;

/**
 * Created by shcherbakov on 10.01.2018.
 */

public class AngleAvg {
    private int posinion=0;
    public int count;
    private float avg[];
    boolean init=true;

    public AngleAvg(int count) {
        this.count = count;
        avg=new float[count];
    }
    public float addAngle(float x){
        float result=0;
        if(init) Arrays.fill(avg,x);
        avg[posinion]=x;
        posinion++;
        posinion=posinion%count;
      for(float f:avg) result+=f;
      return result/count;
    }
}
