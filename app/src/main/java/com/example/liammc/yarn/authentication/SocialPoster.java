package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class SocialPoster {

    private final Activity activity;
    private final String yarnURL = "www.yarn.com.au";
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    public SocialPoster(Activity _activity){
        this.activity =  _activity;
    }


    //region Public Methods

    public void postFB(FacebookCallback<Sharer.Result> callback){
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(activity);
        // this part is optional
        if(callback != null) shareDialog.registerCallback(callbackManager, callback);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(yarnURL))
                    .build();
            shareDialog.show(linkContent);
        }
    }

    public void postTW(int requestCode){
        String tweetUrl = "https://twitter.com/intent/tweet?text= \n" + yarnURL + "&url=";
        Uri uri = Uri.parse(tweetUrl);
        activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, uri),requestCode);
    }

    //endregion

}
