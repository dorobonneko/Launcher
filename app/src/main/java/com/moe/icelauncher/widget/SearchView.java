package com.moe.icelauncher.widget;
import android.widget.EditText;
import android.util.AttributeSet;
import android.content.Context;
import android.view.Gravity;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import com.moe.icelauncher.R;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.text.InputType;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewConfiguration;
import android.text.method.MovementMethod;
import android.widget.TextView;
import android.text.method.ArrowKeyMovementMethod;
import android.text.InputFilter;

public class SearchView extends TextView
{
	private float oldY;
	private int touchSlop;
	private boolean scroll,first;
	private SpannableStringBuilder tips;
	public SearchView(Context context,AttributeSet attrs){
		super(context,attrs,0);
		setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
		setSelectAllOnFocus(false);
		touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
		setSingleLine(true);
		setGravity(Gravity.CENTER);
		tips=new SpannableStringBuilder(getResources().getText(R.string.search_apps));
		//Drawable searchIcon=getResources().getDrawable(R.drawable.magnify,getContext().getTheme());
		//searchIcon.setBounds(0,0,searchIcon.getIntrinsicWidth(),searchIcon.getIntrinsicHeight());
		//tips.setSpan(searchIcon,0,0,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		setText(tips);
		setFocusable(false);
		setFocusableInTouchMode(false);
		setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(isFocusable())return;
					setFocusable(true);
					setFocusableInTouchMode(true);
					setText(null);
					requestFocus();
					InputMethodManager ipm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					ipm.showSoftInput(p1,InputMethodManager.SHOW_FORCED);
				}
			});
		setOnKeyListener(new View.OnKeyListener(){

				@Override
				public boolean onKey(View p1, int p2, KeyEvent p3)
				{
					if(p2==p3.KEYCODE_BACK){
						setText(tips);
						setFocusable(false);
						setFocusableInTouchMode(false);
						invalidate();
					}
					return false;
				}
			});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if(!isEnabled())return false;
		if(!hasFocusable()){
			//无焦点，拦截事件
			switch(event.getAction()){
				case event.ACTION_DOWN:
					scroll=false;
					break;
				case event.ACTION_MOVE:
					scroll=true;
					break;
				case event.ACTION_UP:
					if(!scroll)
						performClick();
					break;
			}
			if(scroll)return false;
			return true;
		}else{
		switch(event.getAction()){
			case event.ACTION_DOWN:
				scroll=false;
				oldY=event.getRawY();
				super.onTouchEvent(event);
				first=true;
				break;
			case event.ACTION_MOVE:
				if(scroll)return false;
				if(first&&Math.abs(event.getRawY()-oldY)>touchSlop)
				scroll=true;
				else
					super.onTouchEvent(event);
					first=false;
				break;
			case event.ACTION_CANCEL:
			case event.ACTION_UP:
				if(!scroll)
				super.onTouchEvent(event);
				else
					return false;
				break;
		}
		return true;
		}
	}

	@Override
	protected MovementMethod getDefaultMovementMethod()
	{
		return new ArrowKeyMovementMethod();
	}

	@Override
	protected boolean getDefaultEditable()
	{
		// TODO: Implement this method
		return true;
	}

	
}
