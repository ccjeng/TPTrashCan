package com.ccjeng.tptrashcan.presenter.base;

/**
 * Created by andycheng on 2016/9/9.
 */
public interface Presenter {

    void onCreate();
    void onStart();
    void onStop();
    void onResume();
    void onPause();
    void onDestroy();

}
