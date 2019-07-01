package com.example.liammc.yarn.networking;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public final class Communicator {
    /*This  class is used when you want to set or removes data from the Real Time Database*/

    private static final String TAG  = "Communicator";

    public static void setData(DatabaseReference ref, final String dataType, final Object dataValue) {
        /*This method goes to the passed DataReference variable and then sets to the passed dataType
        with the passed dataValue
         */

        DatabaseReference dataRef = ref.child(dataType);

        //Write to the User database firebaseUser name
        dataRef.setValue(dataValue)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(TAG,dataType +" write to database was a success :"
                                + dataValue.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(TAG,dataType +"write to database was a failure -" + e);
                    }
                });
    }

    public static void removeData(DatabaseReference ref) {
        /*This method goes to the passed DataReference variable and removes it from the database*/

        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"Data removal was successful");
                }
                else{
                    Log.e(TAG,"Data removal failed");
                }
            }
        });
    }

}
