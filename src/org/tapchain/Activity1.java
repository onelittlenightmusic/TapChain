package org.tapchain;

import org.tapchain.R;

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

public class Activity1 extends Activity {
	class LocalButton extends Button {
		Paint paint = null;
		LocalButton(Context context, final int j) {
			super(context);
			super.setBackgroundDrawable(getResources().getDrawable(R.drawable.withface1));
			super.setText(String.format("%d",j));
			super.setTextColor(0xff999999);
//			super.setT

			super.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("TEST",j);
					setResult(RESULT_OK, i);
					finish();
				}
			});
		}
	}
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);	
        ScrollView view = new ScrollView(this);
        view.setBackgroundColor(0xff444444);
        LinearLayout view3 = new LinearLayout(this);
        view3.setOrientation(LinearLayout.VERTICAL);
        for(int j=0; j<6; j++)
        	view3.addView(new LocalButton(this, j), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
/*		Button button_finish = new Button(this);
		button_finish.setText("OK");
		button_finish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("TEST",result);
				setResult(RESULT_OK, i);
				finish();
			}
		});
		view3.addView(button_finish, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
*/		view3.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		view.addView(view3, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setContentView(view);
    }
}
