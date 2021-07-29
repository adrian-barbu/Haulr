package com.haulr.control.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.haulr.R;

/**
 * @description	Customized Text View
 *
 * @author 		Adrian
 */
public class HaulrTextView extends TextView {

	public HaulrTextView(Context context) {
		super(context);
		if (isInEditMode()) return;
		parseAttributes(null);
	}

	public HaulrTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		parseAttributes(attrs);
	}

	public HaulrTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		parseAttributes(attrs);
	}
	
	private void parseAttributes(AttributeSet attrs) {
		int typeface;
		if (attrs == null) { //Not created from xml
			typeface = Haulr.HAULR_REGULAR;
		} else {
		    TypedArray values = getContext().obtainStyledAttributes(attrs, R.styleable.HaulrTextView);
		    typeface = values.getInt(R.styleable.HaulrTextView_typeface, Haulr.HAULR_REGULAR);
		    values.recycle();
		}
	    setTypeface(getRoboto(typeface));
	}

	public void setRobotoTypeface(int typeface) {
	    setTypeface(getRoboto(typeface));
	}

	private Typeface getRoboto(int typeface) {
		return getRoboto(getContext(), typeface);
	}

	public static Typeface getRoboto(Context context, int typeface) {
		switch (typeface) {
			case Haulr.HAULR_LIGHT:
				if (Haulr.sHaulrLight == null) {
					Haulr.sHaulrLight = Typeface.createFromAsset(context.getAssets(), "fonts/Haulr-Light.otf");
				}
				return Haulr.sHaulrLight;

			case Haulr.HAULR_MEDIUM:
				if (Haulr.sHaulrMedium == null) {
					Haulr.sHaulrMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Haulr-Medium.otf");
				}
				return Haulr.sHaulrMedium;

			case Haulr.HAULR_BOLD:
				if (Haulr.sHaulrBold == null) {
					Haulr.sHaulrBold = Typeface.createFromAsset(context.getAssets(), "fonts/Haulr-Bold.otf");
				}
				return Haulr.sHaulrBold;

			case Haulr.HAULR_DEMI:
				if (Haulr.sHaulrDemi == null) {
					Haulr.sHaulrDemi = Typeface.createFromAsset(context.getAssets(), "fonts/Haulr-Demi.otf");
				}
				return Haulr.sHaulrDemi;

			case Haulr.HAULR_REGULAR:
			default:
				if (Haulr.sHaulrRegular == null) {
					Haulr.sHaulrRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Haulr-Regular.otf");
				}
				return Haulr.sHaulrRegular;
		}
	}

	public static class Haulr {
		/* From attrs.xml file:
        	<enum name="haulrLight" value="20" />
            <enum name="haulrRegular" value="21" />
            <enum name="haulrMedium" value="22" />
            <enum name="haulrBold" value="23" />
            <enum name="haulrDemi" value="24" />
        */

		public static final int HAULR_LIGHT = 20;
		public static final int HAULR_REGULAR = 21;
		public static final int HAULR_MEDIUM = 22;
		public static final int HAULR_BOLD = 23;
		public static final int HAULR_DEMI = 24;

		private static Typeface sHaulrLight;
		private static Typeface sHaulrRegular;
		private static Typeface sHaulrMedium;
		private static Typeface sHaulrBold;
		private static Typeface sHaulrDemi;
	}
}
