package com.example.liammc.yarn.yarnPlace;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatActivity;
import com.example.liammc.yarn.core.Recorder;

public class InfoElement {
    //The info element class is used to describe a created chat at the yarn place

    private final InfoWindow infoWindow;
    private final Chat chat;

    //UI
    public View elementView;
    private Button joinButton;
    private TextView dateTime;
    private ImageView hostImage;
    private ImageView guestImage;

    public InfoElement(InfoWindow _infoWindow,Chat _chat){

        this.chat = _chat;
        this.infoWindow = _infoWindow;
        this.initUI();
    }

    //region Init

    private void initUI(){
        //This method initializes the UI element objects, sets the buttons callbacks

        elementView = inflate(R.layout.info_window_scroll_view_element);

        joinButton = elementView.findViewById(R.id.joinChatButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnJoinChatPressed();
            }
        });

        dateTime = elementView.findViewById(R.id.dateTime);
        dateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPressedChat(v);
            }
        });

        hostImage =  elementView.findViewById(R.id.hostImage);
        guestImage = elementView.findViewById(R.id.guestImage);

        update();
    }

    //endregion

    //region Public Methods

    public void update(){
        //This method updates the Info element's visuals with the current data

        String displayText = chat.chatDate + " " + chat.chatTime;

        elementView.setContentDescription(chat.yarnPlace.placeMap.get("id"));
        dateTime.setText(displayText);

        if(chat.hostUser.userID.equals(LocalUser.getInstance().userID))
            joinButton.setVisibility(View.INVISIBLE);

        if(chat.hostUser == null) hostImage.setVisibility(View.INVISIBLE);
        else hostImage.setVisibility(View.VISIBLE);

        if(chat.guestUser == null) guestImage.setVisibility(View.INVISIBLE);
        else guestImage.setVisibility(View.VISIBLE);
    }

    //endregion

    //region Button Methods
    private void OnJoinChatPressed() {
        /*This method is run when the firebaseUser clicks on one of the chat's join buttons.
         */
        chat.acceptChat(infoWindow.mapsActivity, LocalUser.getInstance());
        Recorder.getInstance().recordChat(chat);
        infoWindow.dismiss();
    }

    private void onPressedChat(View view){
        /*This method runs when the user pressed on the chat details button. It takes then to the
        chat Activity*/

        Intent intent = new Intent(infoWindow.mapsActivity, ChatActivity.class);
        intent.putExtra("chatID",chat.chatID);
        infoWindow.mapsActivity.startActivity(intent);
    }
    //endregion

    //region private Methods

    private View inflate(int layoutID ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = infoWindow.mapsActivity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    //endregion
}
