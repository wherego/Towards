package com.waitou.towards.model.activity;

import android.os.Bundle;

import com.waitou.towards.R;
import com.waitou.towards.databinding.ActivityRecommendedBinding;
import com.waitou.wt_library.base.XActivity;


/**
 * Created by waitou on 17/1/5.
 */

public class RecommendedActivity extends XActivity<RecommendedPresenter,ActivityRecommendedBinding> implements RecommendedContract.RecommendedView{


    @Override
    public void initData(Bundle savedInstanceState) {
        getP().start();

        getP().getData();
    }

    @Override
    public void reloadData() {

    }

    @Override
    public RecommendedPresenter createPresenter() {
        return new RecommendedPresenter();
    }

    @Override
    public boolean defaultXView() {
        return true;
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_recommended;
    }


    @Override
    public void showLoading() {
        runOnUiThread(() -> getXBinding().xContentLayout.showLoading());
    }

    @Override
    public void showContent() {
        runOnUiThread(() -> getXBinding().xContentLayout.showContent());
    }

    @Override
    public void setText(String text) {
        runOnUiThread(() -> getBinding().tex.setText(text));
    }
}
