package demo.riceball.xyz.demoedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import riceball.xyz.indicator.InfiniteCirclePageIndicator;

public class MainActivity extends AppCompatActivity {
    ViewPager pager;
    InfiniteCirclePageIndicator indicator;

    EditText input;
    Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = findViewById(R.id.pager);
        indicator = findViewById(R.id.indicator);
        input = findViewById(R.id.input);
        ok = findViewById(R.id.ok);

        ok.setOnClickListener(v -> pager.setCurrentItem(Integer.valueOf(input.getText().toString().trim())));
        indicator.setCount(20, 5, 0);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                TextView tv = new TextView(container.getContext());
                tv.setBackgroundColor(position * 110000 + 0xff00000);
                tv.setText(position + "");
                container.addView(tv);
                return tv;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });

        pager.addOnPageChangeListener(indicator);
    }
}
