package ru.dakon.CConventerBot;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class Test {
    public static void main(String []args) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request request = new Request.Builder()
                .url("https://api.apilayer.com/fixer/convert?to=RUB&from=BYN&amount=100")
                .addHeader("apikey", "GxULJ7FSlKBNDYsNo7lWqsESM0ikI8j0")
                .get()
            .build();
    Response response = client.newCall(request).execute();
    System.out.println(response.body().string());
}
}
