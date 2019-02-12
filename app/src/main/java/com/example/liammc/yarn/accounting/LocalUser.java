package com.example.liammc.yarn.accounting;

import com.google.firebase.auth.FirebaseAuth;

public class LocalUser {

    //region singleton pattern
    private static final LocalUser instance = new LocalUser();

    //private constructor to avoid client applications to use constructor
    private LocalUser(){
        user = new YarnUser("LocalUser",
                FirebaseAuth.getInstance().getCurrentUser().getUid()
                ,YarnUser.UserType.LOCAL);
    }

    public static LocalUser getInstance(){
        return instance;
    }
    //endregion

    public YarnUser user;
}
