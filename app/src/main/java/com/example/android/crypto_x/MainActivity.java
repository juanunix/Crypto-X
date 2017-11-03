package com.example.android.crypto_x;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ActivityFragmentCommunicator {


    //declare recycler view
    private RecyclerView cryptoRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    //define list
    List<CryptoX> cryptoXList;
    List<Double> listBtc;
    List<Double> listEth;

    //android views
    private FloatingActionButton floatingActionButton;
    private ProgressDialog pb;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//       set up android views
        mToolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(mToolbar);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.cryto_fab);
        cryptoRecyclerView = (RecyclerView) findViewById(R.id.crytoRecyclerView);

        cryptoXList = new ArrayList<>();
        listBtc = new ArrayList<>();
        listEth = new ArrayList<>();


        //set up recycler view
        layoutManager = new GridLayoutManager(this, 2);
        adapter = new RecyclerAdapter(this,cryptoXList, listBtc, listEth);
        cryptoRecyclerView.setLayoutManager(layoutManager);
        cryptoRecyclerView.setAdapter(adapter);


        //floating action btn for adding new cryto currency
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                FragmentManager fragmentManager = getSupportFragmentManager();
                CryptoDialogFragmentClass dialogFragment = CryptoDialogFragmentClass.newInstance("Crypto-X");

                dialogFragment.show(fragmentManager, "fragment_tag");


            }
        });


    }


    public void fetchdata(final String coinType) {

        //using OkHttp library for network request
        OkHttpClient cryptoClient = new OkHttpClient();


        Request request = new Request.Builder() // creat a request

                .url("https://min-api.cryptocompare.com/data/price?fsym=" + coinType + "&tsyms=USD,EUR,NGN,CNY,CAD," +
                        "ZAR,RUB,BRL,AUD,INR,SAR,AED,GHS,CLP,NOK,XOF,THB,JMD,CHE,KRW,TRY")
                .build();

        cryptoClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {


                // thread to update ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.dismiss();
                        Toast.makeText(MainActivity.this, "Error fetching data...", Toast.LENGTH_LONG).show();

                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                pb.dismiss();

                final String crytoResponse = response.body().string(); //API response


                //parsing JSON data from response

                try {


                    Resources resources = getResources();
                    String[] currency = resources.getStringArray(R.array.currency);
                    JSONObject jsonObject = new JSONObject(crytoResponse);


                    for (int i = 0; i < currency.length; i++) {

                        if (coinType == "BTC") {
                            listBtc.add(jsonObject.getDouble(currency[i]));

                        }

                        if (coinType == "ETH") {
                            listEth.add(jsonObject.getDouble(currency[i]));

                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                }

                //update ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        cryptoXList.add(new CryptoX(coinType)); // add new item to list
                        adapter.notifyItemInserted(cryptoXList.size()); // notify adapter of change

                    }
                });

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.action_clear_cards:
                //clear cards
                cryptoXList.clear();
                adapter.notifyDataSetChanged();
                listBtc.clear();
                listEth.clear();
                break;
            case R.id.action_help:
                //show alert help
                showHelpAlert();
                break;
            case R.id.action_about:
                //show alert about
                showAboutAlert();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showHelpAlert() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage(R.string.help_msg);
        alert.show();

    }

    public void showAboutAlert() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage(R.string.about_msg);
        alert.show();

    }


    @Override
    public void onDataQuery(String coinType) {
        pb = new ProgressDialog(MainActivity.this);
        pb.setMessage(getString(R.string.fetch_exchange_rate));
        pb.setCanceledOnTouchOutside(false);
        pb.show();


        fetchdata(coinType);


    }

    long previousTime;

    @Override
    public void onBackPressed() {
        //handle onB ack Pressed

        if (2000 + previousTime > (previousTime = System.currentTimeMillis())) {
            super.onBackPressed();

        } else {
            Toast.makeText(getBaseContext(), R.string.press_again, Toast.LENGTH_SHORT).show();
        }
    }

}