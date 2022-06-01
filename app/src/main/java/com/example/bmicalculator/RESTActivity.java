package com.example.bmicalculator;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class RESTActivity extends AppCompatActivity {
    protected String urlAddress="http://gp.gpashev.com:93/testTels/service.php";
    public interface FillListViewElems{
        public void FillListViewWithElements(ArrayList<String> elems);
    }

    public void AsyncDataGetBMI(String methodName, String userName, String fileJSON,FillListViewElems listViewEvent)
            throws Exception {
        HashMap<String, String> params =new HashMap<>();
        params.put("methodName", methodName);
        params.put("userName", userName);
        params.put("fileJSON", fileJSON);

        AsyncHttpClient client=new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder getRequest=
                client.prepareGet(urlAddress+"?"+getPostDataString(params));

        final ListenableFuture<Response> listenableFuture=
                client.executeRequest(getRequest.build());
        Executor e=new Executor() {
            @Override
            public void execute(Runnable runnable) {
                Thread thread=new Thread(runnable);
                thread.start();
            }
        };
        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                Thread t1=new Thread(new Runnable() {
                    public void run() {
                        Response response= null;
                        try {
                            response = listenableFuture.get();
                        } catch (ExecutionException executionException) {
                            executionException.printStackTrace();
                            runOnUiThread(()->
                                    Toast.makeText(getApplicationContext(),
                                            executionException.getLocalizedMessage(),
                                            Toast.LENGTH_LONG).show());
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                            runOnUiThread(()->
                                    Toast.makeText(getApplicationContext(),
                                            interruptedException.getLocalizedMessage(),
                                            Toast.LENGTH_LONG).show());
                        }
                        if(response.hasResponseBody()){
                            try {
                                String res= response.getResponseBody("UTF-8");
                                final ArrayList<String> arrayList=new ArrayList<>();

                                JSONArray ja=(JSONArray) new JSONTokener(res.toString())
                                        .nextValue();
                                Integer i;
                                for(i=0; i<ja.length(); i++){
                                    JSONObject jsonObject= (JSONObject) ja.get(i);
                                    String name=jsonObject.getString("name");
                                    arrayList.add(name);
                                }
                                runOnUiThread(()-> listViewEvent.FillListViewWithElements(arrayList));


                            } catch (IOException | JSONException ioException) {
                                ioException.printStackTrace();
                                runOnUiThread(()->
                                        Toast.makeText(getApplicationContext(),
                                                ioException.getLocalizedMessage(),
                                                Toast.LENGTH_LONG).show());

                            }

                        }
                    }
                });
                t1.start();


            }
        }, e);




    }
    public String getPostDataString(HashMap<String, String> params) throws Exception{
        StringBuffer feedback=new StringBuffer();
        boolean first=true;
        for(Map.Entry<String, String> entry: params.entrySet()){
            if(first)
                first=false;
            else
                feedback.append("&");
            feedback.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            feedback.append("=");
            feedback.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return feedback.toString();
    }
    public String postData(String methodName, String userName, String fileJSON)
            throws Exception
    {
        String result="";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("methodName", methodName);
        params.put("userName", userName);
        params.put("fileJSON", fileJSON);

        URL url=new URL(urlAddress);
        HttpURLConnection client=(HttpURLConnection) url.openConnection();

        client.setRequestMethod("POST");
        client.setRequestProperty("multipart/form-data", urlAddress+";charset=UTF-8");
        client.setDoInput(true);
        client.setDoOutput(true);

        OutputStream os=client.getOutputStream();
        BufferedWriter writer=new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8")
        );

        String a=getPostDataString(params);
        writer.write(a);
        writer.close();
        os.close();
        int ResponseCode=client.getResponseCode();
        if(ResponseCode==HttpURLConnection.HTTP_OK){
            BufferedReader br=new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );
            String line="";
            while((line=br.readLine())!=null){
                result+=line+"\n";
            }
            br.close();
        }else{
            throw new Exception("HTTP ERROR Response Code: "+ResponseCode);
        }
        return result;

    }
}
