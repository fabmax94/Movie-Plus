package com.plus.filme.fabiosilva.filme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OrderActivity extends Activity {

    public static final String ORDER_TYPE = "ORDER_TYPE";
    private ListView listView;
    private String mOrderMode;
    private TextView mOrderLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            mOrderMode = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            mOrderLabel = findViewById(R.id.tv_order);
            mOrderLabel.setText(mOrderLabel.getText().toString() + " " + mOrderMode);
        }
        listView = findViewById(R.id.lv_order);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.order_item_layout, android.R.id.text1, getResources().getStringArray(R.array.order_type));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view;
                Intent intent = getIntent();
                intent.putExtra(OrderActivity.ORDER_TYPE, textView.getText().toString());
                setResult(MainActivity.ORDER_BY, intent);
                finish();
            }
        });
    }
}
