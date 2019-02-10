package com.example.liammc.yarn;


import java.util.Random;

public class Notification
{
    public String title;
    public String message;
    public boolean seen;
    public int id;

    public Notification(String _title,String _message)
    {
        this.title = _title;
        this.message = _message;
        this.seen = false;

        Random random = new Random();
        this.id = random.nextInt(100000);
    }

}