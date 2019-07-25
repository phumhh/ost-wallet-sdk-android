package ost.com.ostsdkui.uicomponents.uiutils.content;

import android.content.Context;

import org.json.JSONObject;

import ost.com.ostsdkui.util.CommonUtils;

public class ContentConfig implements Content {

    private JSONObject mContentObject;
    private static Content contentConfig;

    private ContentConfig(Context context, JSONObject themeObject) {
        mContentObject = new CommonUtils().deepMergeJSONObject(themeObject, ContentDefault.getDefaultContent(context));
    }

    public static void init(Context context, JSONObject contentObject) {
        contentConfig = new ContentConfig(context, contentObject);
    }

    public static Content getInstance() {
        if (null == contentConfig) {
            throw new RuntimeException("ContentConfig is not initialized");
        }
        return contentConfig;
    }
    @Override
    public DrawableConfig getDrawableConfig(String name) {
        return new DrawableConfig(mContentObject.optJSONObject(name));

    }

    @Override
    public StringConfig getStringConfig(String name) {
        return new StringConfig(mContentObject.optJSONObject(name));
    }
}
