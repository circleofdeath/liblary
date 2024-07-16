package com.liblary.app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requests
{
    public static final String BACKEND_URL = "http://localhost:8080/";

    public static Request method(String url_half, String method, String json)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) new URL(BACKEND_URL + url_half).openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                byte[] data = json.getBytes();
                wr.write(data);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            int status = con.getResponseCode();
            con.disconnect();

            return new Request(status, content.toString());
        }
        catch(Exception e)
        {
            return new Request(407, e.toString());
        }
    }

    public static Request method(String url_half, String method)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) new URL(BACKEND_URL + url_half).openConnection();
            con.setRequestMethod(method);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            int status = con.getResponseCode();
            con.disconnect();

            return new Request(status, content.toString());
        }
        catch(Exception e)
        {
            return new Request(407, e.toString());
        }
    }

    public record Request(int status, String content)
    {}
}