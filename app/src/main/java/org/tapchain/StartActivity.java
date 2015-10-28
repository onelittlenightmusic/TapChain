package org.tapchain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.tapchain.realworld.TapChainView;

public class StartActivity extends Activity {
	class LocalButton extends Button {
		Paint paint = null;

		LocalButton(Context context, String text) {
			super(context);
			// super.setBackgroundDrawable(getResources().getDrawable(R.drawable.withface1));
			super.setText(text);
			super.setTextColor(0xff999999);
			// super.setT

			super.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), TapChainView.class);
					startActivity(i);
				}
			});
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ScrollView view = new ScrollView(this);
		view.setBackgroundColor(0xff444444);
		LinearLayout linear = new LinearLayout(this);
		linear.addView(new LocalButton(this, "Car Engineering Start!!!"));
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		view.addView(linear, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setContentView(view);
	}
}
