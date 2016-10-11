package io.github.dantetam.lessonsix;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.HashMap;

public class LessonSixActivity extends Activity 
{
	/** Hold a reference to our GLSurfaceView */
	private LessonSixGLSurfaceView mGLSurfaceView;
	private LessonSixRenderer mRenderer;
	
	private static final int MIN_DIALOG = 1;
	private static final int MAG_DIALOG = 2;
	
	private int mMinSetting = -1;
	private int mMagSetting = -1;
	
	private static final String MIN_SETTING = "min_setting";
	private static final String MAG_SETTING = "mag_setting";

    private String[] imageNames = {"usb_android", "camp", "info", "plains", "star", "wonder"};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.lesson_six);

		mGLSurfaceView = (LessonSixGLSurfaceView)findViewById(R.id.gl_surface_view);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);
			
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			// Set the renderer to our demo renderer, defined below.
			mRenderer = new LessonSixRenderer(this);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);					
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}
		
		findViewById(R.id.button_set_min_filter).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(MIN_DIALOG);
            }
        });

        /*SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("My string. ").append(" ");
        builder.setSpan(new ImageSpan(this, R.drawable.usb_android),
                builder.length() - 1, builder.length(), 0);
        builder.append(" <- Image");*/

/*
        TextView textView  = (TextView) findViewById(R.id.image_textview_test);

        textView.setText("abcdef <{usb_android}> -> <{usb_android}> result.");

        testImageSpan(textView);*/

        /*SpannableString ss = new SpannableString("abcdef <usb_android> -> <usb_android> result.");
        Drawable usbAndroid = getResources().getDrawable(R.drawable.usb_android);

        textView.setText(ss);

        Rect bounds = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);

        usbAndroid.setBounds(0, 0, bounds.width(), bounds.height());
        ImageSpan span = new ImageSpan(usbAndroid, ImageSpan.ALIGN_BASELINE);
        ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(ss);*/

		findViewById(R.id.button_set_mag_filter).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showDialog(MAG_DIALOG);
			}
		});
		
		// Restore previous settings
		if (savedInstanceState != null)
		{
			mMinSetting = savedInstanceState.getInt(MIN_SETTING, -1);
			mMagSetting = savedInstanceState.getInt(MAG_SETTING, -1);
			
			if (mMinSetting != -1) { setMinSetting(mMinSetting); }
			if (mMagSetting != -1) { setMagSetting(mMagSetting); }
		}
	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

    public String[] storedSpan = new String[5];
    public void update(TextView textView, int frame) {
        if (storedSpan[frame % storedSpan.length] == null) {
            int index = frame % imageNames.length;
            String stringy = "input -> ";
            for (int i = 0; i < imageNames.length; i++) {
                int newIndex = (i + index) % imageNames.length;
                stringy += "<{" + imageNames[newIndex] + "}> -> ";
            }
            stringy += "output";
            //TextView textView  = (TextView) findViewById(R.id.image_textview_test);
            storedSpan[frame % storedSpan.length] = stringy;
        }
        textView.setText(storedSpan[frame % storedSpan.length]);
        testImageSpan(textView);
    }

	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		outState.putInt(MIN_SETTING, mMinSetting);
		outState.putInt(MAG_SETTING, mMagSetting);
    }

    private HashMap<String, ImageSpan> imageSpansById = new HashMap<>();
    public void testImageSpan(TextView textView) {
        String text = textView.getText().toString();
        SpannableString ss = new SpannableString(text);
        for (int i = 0; i < textView.getText().length() - 1; i++) {
            if (textView.getText().charAt(i) == '<' && text.charAt(i + 1) == '{') {
                boolean foundTag = false;
                int j = i;
                for (; j < textView.getText().length() - 1; j++) {
                    if (textView.getText().charAt(j) == '}' && text.charAt(j + 1) == '>') {
                        foundTag = true;
                        break;
                    }
                }
                if (foundTag) {
                    String drawableName = text.substring(i + 2, j);

                    ImageSpan span = imageSpansById.get(drawableName);
                    if (span == null) {
                        int resId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                        //Drawable drawable = getResources().getDrawable(resId);

                        Bitmap bitmap = decodeSampledBitmapFromResource(this.getResources(), resId, 64, 64);
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                        Rect bounds = new Rect();
                        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);

                        drawable.setBounds(0, 0, bounds.height(), bounds.height());
                        span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

                        imageSpansById.put(drawableName, span);
                    }

                    ss.setSpan(span, i, j + 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    /*int resId = getResources().getIdentifier(drawableName, "drawable", this.getPackageName());
                    Drawable drawable = getResources().getDrawable(resId);*/

                    //textView.setText(ss);
                } else {
                    System.err.println("Could not find end tag ( }> ) to image declared");
                }
            }
        }
        textView.setText(ss);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /*public void testImageSpan(TextView textView, int appendDrawableId) {
        SpannableString ss = new SpannableString(textView.getText());
        Drawable d = getResources().getDrawable(appendDrawableId);

        int width = 32, height = 32; //Default values
        if (textView.getText().length() != 0) {
            Rect bounds = new Rect();
            textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);
            width = bounds.width();
            height = bounds.height();
        }

        d.setBounds(0, 0, width, height);

        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        ss.setSpan(span, textView.getText().length() - 1, textView.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(ss);

        textView.setText(textView.getText() + "<<<");
        System.out.println(textView.getText());
    }*/

	private void setMinSetting(final int item)
	{
		mMinSetting = item;
		
		mGLSurfaceView.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				final int filter;
				
				if (item == 0)
				{
					filter = GLES20.GL_NEAREST;
				}
				else if (item == 1)
				{
					filter = GLES20.GL_LINEAR;
				}
				else if (item == 2)
				{
					filter = GLES20.GL_NEAREST_MIPMAP_NEAREST;
				}
				else if (item == 3)
				{
					filter = GLES20.GL_NEAREST_MIPMAP_LINEAR;
				}
				else if (item == 4)
				{
					filter = GLES20.GL_LINEAR_MIPMAP_NEAREST;
				}
				else // if (item == 5)
				{
					filter = GLES20.GL_LINEAR_MIPMAP_LINEAR;
				}
				
				mRenderer.setMinFilter(filter);
			}
		});
	}
	
	private void setMagSetting(final int item)
	{
		mMagSetting = item;
		
		mGLSurfaceView.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				final int filter;
				
				if (item == 0)
				{
					filter = GLES20.GL_NEAREST;
				}
				else // if (item == 1)
				{
					filter = GLES20.GL_LINEAR;
				}	    						
				
				mRenderer.setMagFilter(filter);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
	    Dialog dialog = null;
	    
	    switch(id) 
	    {
	    	case MIN_DIALOG:
	    	{
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle(getText(R.string.lesson_six_set_min_filter_message));
	    		builder.setItems(getResources().getStringArray(R.array.lesson_six_min_filter_types), new DialogInterface.OnClickListener() 	    		
	    		{
	    			@Override
	    		    public void onClick(final DialogInterface dialog, final int item) 
	    			{
	    				setMinSetting(item);
	    		    }
	    		});
	    		
	    		dialog = builder.create();
	    	}
	        break;
	    	case MAG_DIALOG:
	    	{
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle(getText(R.string.lesson_six_set_mag_filter_message));
	    		builder.setItems(getResources().getStringArray(R.array.lesson_six_mag_filter_types), new DialogInterface.OnClickListener() 	    		
	    		{
	    			@Override
	    		    public void onClick(final DialogInterface dialog, final int item) 
	    			{
	    				setMagSetting(item);
	    		    }
	    		});
	    		
	    		dialog = builder.create();
	    	}
	        break;
	    default:
	        dialog = null;
	    }
	    
	    return dialog;
	}
}