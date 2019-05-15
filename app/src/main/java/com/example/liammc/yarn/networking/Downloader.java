package com.example.liammc.yarn.networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class Downloader
{
    /*This class is used when you want to download data from a URL*/

    public static String readUrl(String myUrl) throws IOException {
        /*This method reads the URL string and opens up a connection. Once the connection is made it
        downloads the data and returns a string containing its result*/

        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            //Open up a connection to the URL
            URL url = new URL(myUrl);
            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.connect();

            //Get the input stream from the URL connection
            inputStream = urlConnection.getInputStream();

            //Read each line of the URL connection stream and append it to a result
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            //Set the result variable and close the reader;
            data = sb.toString();
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            //Close the streams and connections
            if(inputStream != null) inputStream.close();
            if(urlConnection != null) urlConnection.disconnect();
        }

        Log.d("DownloadURL","Returning data= "+data);
        return data;
    }
}
