package com.example.liammc.yarn.networking;

import android.app.Activity;
import android.location.Geocoder;
import androidx.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JoinedDownloader {
    /*This class downloads all the chats that this firebaseUser has joined from the Real Time Database*/

    //region Ready Listener
    /*This Ready Listener is used when ever the system needs to know when the JoinedDownloader is
    ready.It is ready once it gets all of its required data */

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    private final String TAG = "JoinedDownloader";
    private FirebaseFunctions firebaseFunctions;
    private Recorder recorder;
    private LocalUser localUser;

    public JoinedDownloader(){
        this.firebaseFunctions = FirebaseFunctions.getInstance();
        this.recorder = Recorder.getInstance();
        this.localUser = LocalUser.getInstance();
    }

    //region Public Methods
    public void getJoinedYarnPlaces(final Activity activity, final Geocoder geocoder, ReadyListener listener){
        readyListener = listener;

        requestJoinedChats().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                //The task is a failure
                if (!task.isSuccessful()) {

                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();

                        Log.e(TAG, "Error trying to receive joined Yarn Places - " + code + " "
                                + details);
                    }
                    Log.w(TAG, "Get Joined Yarn Places failed - ", e);

                    return;
                }
                //The task isn't a failure
                else{
                    try{
                        JSONObject JSONResult = new JSONObject(task.getResult());
                        String status = JSONResult.getString("status");

                        if(!status.equals("success")) {
                            Log.w(TAG, "Getting joined Yarn Places  failed - "
                                    + JSONResult.toString());
                            readyListener.onReady();
                        }
                        else{
                            //Build chat objects and record them
                            parse(activity,geocoder,JSONResult);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
            }
        });
    }

    //endregion
    //region Private Methods
    private Task<String> requestJoinedChats() {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", localUser.lastAddress.getCountryName());
        parameters.put("state", localUser.lastAddress.getAdminArea());
        parameters.put("userID", localUser.userID);

        Log.d(TAG,"Getting joined Yarn Places");
        return firebaseFunctions
                .getHttpsCallable("getJoinedPlaces")
                .call(parameters)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        HashMap resultMap =  (HashMap) task.getResult().getData();
                        JSONObject JSONResult = new JSONObject(resultMap);

                        return JSONResult.toString();
                    }
                });
    }

    private void parse(Activity activity, Geocoder geocoder, final JSONObject resultJSON){

        try {
            final JSONArray JSONArray = resultJSON.getJSONArray("result");
            final ArrayList<YarnPlace> foundPlaces = new ArrayList<>();

            for(int i = 0 ; i < JSONArray.length(); i++)
            {
                JSONObject JSONObject = JSONArray.getJSONObject(i);

                String id = JSONObject.getString("id");
                String name = JSONObject.getString("name");
                String type = JSONObject.getString("type");
                String lat = JSONObject.getString("lat");
                String lng = JSONObject.getString("lng");

                final HashMap<String,String> placeMap = YarnPlace.buildPlaceMap(id,name,type,lat,lng);

                final YarnPlace yarnPlace = new YarnPlace(placeMap);
                yarnPlace.init(activity,geocoder);

                yarnPlace.setReadyListener(new ReadyListener() {
                    @Override
                    public void onReady() {

                        foundPlaces.add(yarnPlace);

                        if(checkReady(foundPlaces, JSONArray.length())){
                            recorder.recordYarnPlaces(foundPlaces);
                            Log.d(TAG,"Recorder is ready!");
                            if(readyListener != null) readyListener.onReady();
                        }
                    }
                });
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Private Methods

    private boolean checkReady(ArrayList<YarnPlace> list,int amount){

        return amount == list.size();
    }

    //endregion

}
