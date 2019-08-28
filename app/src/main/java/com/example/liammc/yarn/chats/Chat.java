package com.example.liammc.yarn.chats;

import android.content.Context;

import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.networking.Communicator;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.UUID;

public class Chat
{
    /*This class holds all the data about a particular chat. It is a formatted representation of a
    chat path in the real time database. There for when ever that path is created, updated
    or destroyed so does its respective object. This connection is made through the chat ID
     */

    //region Value Change Listener
    /*This listener is used to alert other objects when particular values of the chat are changed*/
    private ValueChangeListener valueChangelistener;

    public interface ValueChangeListener {
        void onAccepted(Chat chat, DatabaseReference chatRef);
        void onActiveChange(Chat chat, DatabaseReference chatRef);
        void onDeletion(Chat chat, DatabaseReference chatRef);
    }

    public ValueChangeListener getValueChangelistener() {
        return valueChangelistener;
    }

    public void setValueChangelistener(ValueChangeListener valueChangelistener) {
        this.valueChangelistener = valueChangelistener;
    }
    //endregion

    //region Ready Listener
    /*This Listener is used to alert other objects to when the chat is ready for firebaseUser interaction*/

    private ChatReadyListener readyListener;

    public interface ChatReadyListener {
        void onReady(Chat chat);
    }

    public ChatReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ChatReadyListener readyListener) {
        this.readyListener = readyListener;
    }

    //endregion

    public static final String PLACE_INFO_REF = "Yarn_Place_Info";
    private  String TAG = "Chat" ;

    public DatabaseReference chatRef;
    public ChatUpdator updator;
    public String localUserID;


    //public String chatID;
    public YarnUser hostUser;
    public YarnUser guestUser;
    private boolean guestReady  = false;

    public YarnPlace yarnPlace;
    public String chatID;
    public String chatDate;
    public String chatTime;
    public String chatLength;
    public Boolean chatActive = null;

    //TODO Chat Activity not behaving correctly (See email from Jarred)
    //TODO properly remove chat from realtime database backend

    //region Constructors

    public Chat(YarnPlace _place,HashMap<String, String> chatMap, ChatReadyListener _readyListener) {
        //This is the constructor for creating an instance of a new chat

        //Initialize chat variables
        this.hostUser = new YarnUser(chatMap.get("host"));
        this.localUserID = LocalUser.getInstance().userID;
        this.updator = new ChatUpdator(this);

        this.yarnPlace = _place;
        this.chatID = this.generateChatID();
        this.TAG = TAG + " " + chatID;
        this.chatDate = chatMap.get("date");
        this.chatTime = chatMap.get("time");
        this.chatLength = chatMap.get("length");

        this.chatRef = initReference();

        this.initializeChatState();

        //Initialize database
        this.yarnPlace.initYarnPlaceDatabase();
        this.initializeChatDatabase();

        //Set ready listener;
        this.readyListener = _readyListener;
    }

    public Chat(YarnPlace _place, String _chatID) {
        //This is the constructor for creating an instance of an existing chat
        this.localUserID = LocalUser.getInstance().userID;
        this.updator = new ChatUpdator(this);

        this.TAG = TAG + " " + _chatID;
        this.yarnPlace = _place;
        this.chatID = _chatID;

        this.chatRef = initReference();

        //Get Chat info
        updator.initDataListener("host");
        updator.initDataListener("guest");
        updator.initDataListener("date");
        updator.initDataListener("time");
        updator.initDataListener("length");
        updator.initDataListener("active");
    }

    //endregion Constructors

    //region Getters and Setter

    public boolean getGuestReady(){return guestReady;}

    public void setGuestReady(boolean ready){ guestReady = ready;}

    public YarnUser getOtherUser(){
        if(guestUser.userID.equals(localUserID)) return guestUser;
        else if(hostUser.userID.equals(localUserID)) return hostUser;

        return null;
    }
    //endregion

    //region Initialization
    /*This region is used for initialization of a chat. When initializing a chat we need to set the
    data in the database and add listeners with initChatDatabase, initialise the state with
    initChatSate
     */

    private void initializeChatState(){
        /*This method initializes the state of the chat*/
        chatActive = false;
    }

    private void initializeChatDatabase() {
        /*This method initializes the chat in the database*/

        Communicator.setData(chatRef,"host",hostUser.userID);
        Communicator.setData(chatRef,"guest","");
        Communicator.setData(chatRef,"date",chatDate);
        Communicator.setData(chatRef,"time",chatTime);
        Communicator.setData(chatRef,"length",chatLength);
        Communicator.setData(chatRef,"active", chatActive);

        updator.initDataListener("host");
        updator.initDataListener("guest");
        updator.initDataListener("date");
        updator.initDataListener("time");
        updator.initDataListener("length");
        updator.initDataListener("active");
    }

    private DatabaseReference initReference(){
        /*This method gets the references for the chat*/

        return AddressTools.getChatDatabaseReference(
                yarnPlace.address.getCountryName(),yarnPlace.address.getAdminArea(),
                yarnPlace.placeMap.get("id"),chatID);

    }
    //endregion

    //region Public Methods

    public void acceptChat(Context context, YarnUser guestUser) {
        /*Accepts the chat by setting the data in the database and recording the chat*/
        Communicator.setData(chatRef,"guest",guestUser.userID);
        Recorder.getInstance().recordChat(this);
    }

    public void activateChat(){
        Communicator.setData(chatRef,"active", true);
    }

    public void removeChat() {
        /*Cancels the chat by setting the data in the database */
        Communicator.removeData(chatRef);
    }

    public boolean checkForUserInChat(String userID){
        /*Checks if the passed User ID is present in the chat*/

        if(guestUser == null){
            return hostUser.userID.equals(userID);
        }

        else return guestUser.userID.equals(userID) || hostUser.userID.equals(userID);
    }

    public void checkReady(){
        /*Checks is the chat is ready. It's ready if the users, id, date, time, length and active
        variables have value
         */

        boolean ready = (hostUser != null &&
                guestReady &&
                chatID != null &&
                chatDate != null &&
                chatTime != null &&
                chatLength != null &&
                chatActive != null);

        if(readyListener != null && ready){
            readyListener.onReady(this);
        }
    }
    //endregion

    //region Public Static Methods

    public static HashMap<String, String> buildChatMap(String _hostUserID, String _chatDate, String _chatTime
            , String _chatLength){
        /*This static method returns a Chat HashMap*/

        HashMap<String,String> chatMap = new HashMap<>();

        chatMap.put("host",_hostUserID);
        chatMap.put("date",_chatDate);
        chatMap.put("time",_chatTime);
        chatMap.put("length",_chatLength);

        return chatMap;
    }

    //endregion

    //region Private Methods

    private String generateChatID() {
        //generates a random ID
        return UUID.randomUUID().toString();
    }

    //endregion



}
