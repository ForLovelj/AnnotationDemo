package com.clow.annotest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.clow.CokeAnnotation.anno.BindView;
import com.clow.cokeapi.Coke;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv_content)
    public TextView mTextView;
    @BindView(R.id.tv_button)
    public Button  mTvButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Coke.bind(this,this);
        mTextView.setText("textview注入成功");
        mTvButton.setText("button注入成功");
    }
}
