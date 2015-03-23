package com.tarkus.merchantmonitor;

import android.os.AsyncTask;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LumiOnlineRequest extends AsyncTask<String, String, String>
{
    public AboutActivity delegate;

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString("Cp1251");
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            responseString = "404";
            //TODO Handle problems..
        } catch (IOException e) {
            responseString = "404";
            //TODO Handle problems..
        }
        return responseString;
    }



    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        TextView onlineText = (TextView) delegate.findViewById(R.id.onlineText);
        onlineText.setText("Online: "+result);

    }
}
