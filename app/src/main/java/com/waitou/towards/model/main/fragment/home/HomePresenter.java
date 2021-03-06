package com.waitou.towards.model.main.fragment.home;

import android.databinding.ObservableField;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;
import com.waitou.net_library.helper.RxTransformerHelper;
import com.waitou.net_library.log.LogUtil;
import com.waitou.towards.R;
import com.waitou.towards.bean.GankResultsInfo;
import com.waitou.towards.bean.GankResultsTypeInfo;
import com.waitou.towards.common.ExtraValue;
import com.waitou.towards.net.DataLoader;
import com.waitou.towards.net.cache.Repository;
import com.waitou.wt_library.base.XPresent;
import com.waitou.wt_library.browser.WebUtil;
import com.waitou.wt_library.kit.USharedPref;
import com.waitou.wt_library.kit.AlertToast;
import com.waitou.wt_library.kit.UDate;
import com.waitou.wt_library.kit.UString;
import com.waitou.wt_library.kit.Util;
import com.waitou.wt_library.recycler.adapter.BaseViewAdapter;
import com.waitou.wt_library.view.viewpager.SingleViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by waitou on 17/3/5.
 * 首页presenter
 */

public class HomePresenter extends XPresent<HomeFragment> implements SingleViewPagerAdapter.Presenter, BaseViewAdapter.Presenter {

    private HomeCommendFragment mHomeCommendFragment;
    private HomeCargoFragment   mCargoFragment;
    private HomeAndroidFragment mHomeAndroidFragment;

    public ObservableField<String> txName = new ObservableField<>("all");

    /**
     * banner item 点击
     */
    public void onLinkClick(View view, int type, String url, String title) {
        if (UString.isEmpty(url)) {
            Log.e("aa", " url缺失了..");
            AlertToast.show("url缺失了..");
            return;
        }
        WebUtil.turnWeb(getV().getActivity(), url, title);
    }

    /**
     * 加载HomeCommendFragment首页数据
     */
    void loadHomeData() {
        getV().pend(Observable.zip(DataLoader.getGithubApi().getBannerPage(), DataLoader.getGithubApi().getHomeData(), Pair::create)
                .compose(RxTransformerHelper.applySchedulers())
                .map(pair -> {
                    if (pair.first != null && pair.first.result != null && pair.first.result.size() > 0) {
                        int dayOfWeek = UDate.getWeekIndex(UDate.getNowDate()) - 1;
                        getHomeCommendFragment().onBannerSuccess(pair.first.result.get(dayOfWeek));
                    }
                    if (pair.second != null && pair.second.result != null && pair.second.result.function.size() > 0) {
                        getHomeCommendFragment().onFunctionSuccess(pair.second.result.function);
                    }
                    return UDate.getNowString(UDate.FORMAT_DATE);
                })
                .observeOn(Schedulers.io())
                .flatMap(currentDate -> {
                    String everyday = USharedPref.get().getString(ExtraValue.EVERYDAY_DATA, "2017-03-04");
                    boolean isReload = false;
                    if (!everyday.equals(currentDate)) { //第二天
                        if (!UDate.isRightTime(12, 30)) { //如果是早上 取缓存 如果缓存没有 请求前一天数据
                            currentDate = UDate.getYesterday(currentDate, UDate.FORMAT_DATE); //请求前一天数据
                        } else {
                            isReload = true; //第二天 大于十二点三十 更新数据
                        }
                    }
                    //如果请求的数据是null 请求前一天数据
                    final String[] finalCurrentDate = {currentDate};
                    return getGankIoDay(currentDate, isReload)
                            .flatMap(info -> {
                                LogUtil.e("aa", "loadHomeData is null = " + info.isNull); //请求数据如果为null 尝试再次请求前一天数据
                                if (info.isNull) {
                                    finalCurrentDate[0] = UDate.getYesterday(finalCurrentDate[0], UDate.FORMAT_DATE);
                                    return getGankIoDay(finalCurrentDate[0], false);//请求前一天数据
                                }
                                return Observable.just(info);
                            }).doOnNext(info ->
                                    USharedPref.get().put(ExtraValue.EVERYDAY_DATA, finalCurrentDate[0])
                            );
                })
                .flatMap(gankResultsInfo -> {
                    List<List<GankResultsTypeInfo>> lists = new ArrayList<>();
                    if (gankResultsInfo != null) {
                        if (Util.isNotEmptyList(gankResultsInfo.福利)) {
                            lists.add(gankResultsInfo.福利);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.休息视频)) {
                            lists.add(gankResultsInfo.休息视频);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.Android)) {
                            lists.add(gankResultsInfo.Android);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.瞎推荐)) {
                            lists.add(gankResultsInfo.瞎推荐);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.App)) {
                            lists.add(gankResultsInfo.App);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.iOS)) {
                            lists.add(gankResultsInfo.iOS);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.拓展资源)) {
                            lists.add(gankResultsInfo.拓展资源);
                        }
                        if (Util.isNotEmptyList(gankResultsInfo.前端)) {
                            lists.add(gankResultsInfo.前端);
                        }
                        if (lists.size() > 0) {
                            lists.get(0).get(0).isShowTitle = true;
                        }
                    }
                    return Observable.just(lists);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(lists -> {
                    if (lists.size() > 0) {
                        for (List<GankResultsTypeInfo> list : lists) {
                            addWelfareImg(list);
                        }
                    }
                })
                .subscribe(lists -> getHomeCommendFragment().onSuccess(lists)
                        , throwable -> getHomeCommendFragment().onError(throwable)));
    }

    private void addWelfareImg(List<GankResultsTypeInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).type.equals("福利")) {
                list.get(i).desc = "今日美图";
                if (list.get(i).images == null) {
                    list.get(i).images = new ArrayList<>();
                    list.get(i).images.add(list.get(i).url);
                }
            }
        }
    }

    private Observable<GankResultsInfo> getGankIoDay(String date, boolean isReload) {
        String[] current = date.split("-");
        return Repository.getRepository().getGankIoDay(current[0], current[1], current[2], isReload)
                .map(reply -> {
                    LogUtil.e("aa", " day = " + date + " loadHomeData " + reply.toString());
                    return reply.getData();
                });
    }

    /**
     * 加载干货数据
     */
    void loadCargoData(String type, int page) {
        getV().pend(Repository.getRepository().getGankIoData(type, page)
                .map(reply -> {
                    LogUtil.e("aa", " type = " + type + " loadCargoData " + reply.toString());
                    return reply.getData();
                })
                .compose(RxTransformerHelper.applySchedulers())
                .filter(RxTransformerHelper.verifyNotEmpty())
                .doOnNext(this::addWelfareImg)
                .subscribe(info -> {
                            if (type.equals(HomeAndroidFragment.TYPE_ANDROID)) {
                                mHomeAndroidFragment.onSuccess(info, page == 1);
                            } else {
                                mCargoFragment.onSuccess(info, page == 1);
                            }
                        },
                        throwable -> {
                            if (type.equals(HomeAndroidFragment.TYPE_ANDROID)) {
                                mHomeAndroidFragment.showError(page == 1);
                            } else {
                                mCargoFragment.showError(page == 1);
                            }
                            AlertToast.show(throwable.toString());
                        }));
    }

    public void showBottomSheet() {
        new BottomSheet.Builder(getV().getActivity())
                .title("选择分类").sheet(R.menu.menu_gank_bottom_sheet)
                .listener(item -> {
                    if (txName.get().equals(item.getTitle())) {
                        AlertToast.show("当前已经是 " + txName.get() + " 分类");
                        return true;
                    }
                    txName.set(item.getTitle().toString());
                    mCargoFragment.showLoading();
                    loadCargoData(txName.get(), 1);
                    return true;
                }).grid().show();
    }

    public String setGankPageTime(String publishedAt) {
        String date = publishedAt.replace('T', ' ').replace('Z', ' ');
        return UDate.getFriendlyTimeSpanByNow(date);
    }

    /*--------------- fragment ---------------*/
    HomeCommendFragment getHomeCommendFragment() {
        if (mHomeCommendFragment == null) {
            mHomeCommendFragment = new HomeCommendFragment();
            mHomeCommendFragment.setPresenter(this);
        }
        return mHomeCommendFragment;
    }

    HomeCargoFragment getCargoFragment() {
        if (mCargoFragment == null) {
            mCargoFragment = new HomeCargoFragment();
            mCargoFragment.setPresenter(this);
        }
        return mCargoFragment;
    }

    HomeAndroidFragment getHomeAndroidFragment() {
        if (mHomeAndroidFragment == null) {
            mHomeAndroidFragment = new HomeAndroidFragment();
            mHomeAndroidFragment.setPresenter(this);
        }
        return mHomeAndroidFragment;
    }
}
