package com.waitou.wt_library.browser;

import android.content.Context;
import android.content.Intent;

import com.waitou.net_library.log.LogUtil;


/**
 * Created by waitou on 17/3/17.
 */

public class WebUtil {

    public static void turnWeb(Context context, String url, String title) {
        LogUtil.e("turn web url: " + url);
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(WebActivity.PLATFORM_WEB_URL, url);
        intent.putExtra(WebActivity.PLATFORM_WEB_TITLE, title);
        context.startActivity(intent);
    }
}
