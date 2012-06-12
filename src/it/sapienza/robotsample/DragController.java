package it.sapienza.robotsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.View;
import android.view.MotionEvent;
import java.util.ArrayList;

/**
 * This class is used to initiate a drag within a view or across multiple views.
 * When a drag starts it creates a special view (a DragView) that moves around the screen
 * until the user ends the drag. As feedback to the user, this object causes the device to
 * vibrate as the drag begins.
 *
 */

public class DragController extends MyAbsoluteLayout {

	/** Indicates the drag is a move.  */
	public static int DRAG_ACTION_MOVE = 0;

	/** Indicates the drag is a copy.  */
	public static int DRAG_ACTION_COPY = 1;

	//private static final int VIBRATE_DURATION = 35;

	private Context mContext;
	//private Vibrator mVibrator;

	private final int[] mCoordinatesTemp = new int[2];

	/** Whether or not we're dragging. */
	private boolean mDragging;

	/** Original view that is being dragged.  */
	private View mOriginator;

	/** The view that moves around while you drag.  */
	private DragView mDragView;

	/** Who can receive drop events */
	private ArrayList<DragLayer> mDragLayer = new ArrayList<DragLayer>();

	/** The window token used as the parent for the DragView. */
	private IBinder mWindowToken;

	private int view_center_x, view_center_y;

	public DragController(Context context) {
		super(context);
		mContext = context;
	}

	public void startDrag(View v, int dragAction, int center_x, int center_y) {

		//salvo le coordinate del centro della ImageView
		view_center_x = center_x;
		view_center_y = center_y;

		mOriginator = v;

		Bitmap b = getViewBitmap(v);

		if (b == null) return;

		int[] loc = mCoordinatesTemp;
		v.getLocationOnScreen(loc);
		int screenX = loc[0];
		int screenY = loc[1];

		startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(), v, dragAction);

		b.recycle();

		if (dragAction == DRAG_ACTION_MOVE) {
			v.setVisibility(View.GONE);
		}
	}


	//Starts a drag.
	public void startDrag(Bitmap b, int screenX, int screenY,
			int textureLeft, int textureTop, int textureWidth, int textureHeight, Object dragInfo, int dragAction) {

		int registrationX = ((int)view_center_x) - screenX;
		int registrationY = ((int)view_center_y) - screenY;

		mDragging = true;

		DragView dragView = mDragView = new DragView(mContext, b, registrationX, registrationY,
				textureLeft, textureTop, textureWidth, textureHeight);
		dragView.show(mWindowToken, (int)view_center_x, (int)view_center_y);
	}

	
	//Draw the view into a bitmap.
	private Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);

		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();

		if (cacheBitmap == null)
			return null;

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}

	
	//Stop dragging without dropping.
	private void endDrag() {
		if (mDragging) {
			mDragging = false;
			if (mOriginator != null) {
				mOriginator.setVisibility(View.VISIBLE);
			}

			if (mDragView != null) {
				mDragView.remove();
				mDragView = null;
			}
		}
	}

	
	//Call this from a drag source view.
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			break;

		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			endDrag();
			break;
		}

		return mDragging;
	}

	
	//Call this from a drag source view.
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mDragging) {
			return false;
		}

		final int action = ev.getAction();

		//calcolo range movimento e max coordinate possibili
		float origin_x = view_center_x;
		float origin_y = view_center_y;
		float touch_x = ev.getRawX();
		float touch_y = ev.getRawY();
		float rel_x = touch_x - origin_x;
		float rel_y = touch_y - origin_y;
		float sin, cos;
		int screenX, screenY;
		final float max_dist = 75;

		float dist = (float) Math.sqrt((rel_x*rel_x) + (rel_y*rel_y));

		if(dist <= max_dist) {

			screenX = (int)ev.getRawX();
			screenY = (int)ev.getRawY();
		}

		else {

			sin = rel_y / dist;

			if(rel_x > 0)
				cos = (float) Math.sqrt(1-(sin*sin));
			else			
				cos = (float) - (Math.sqrt(1-(sin*sin)));

			System.out.println("cos: " + cos + "sin: " + sin);

			screenX = (int)(cos*max_dist + origin_x);
			screenY = (int)(sin*max_dist + origin_y);
		}

		//Valori da inviare al robot
		float x_to_send = ((screenX - view_center_x)/max_dist)*128;
		float y_to_send = ((screenY - view_center_y)/max_dist)*128;

		System.out.println("X: " + x_to_send + "Y: " + y_to_send);

		switch (action) {

		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_MOVE:
			mDragView.move(screenX, screenY);
			break;

		case MotionEvent.ACTION_UP:
			if (mDragging) {
				//drop(mOriginator.getScrollX(), mOriginator.getScrollY());
			}
			endDrag();
			break;

		case MotionEvent.ACTION_CANCEL:
			endDrag();
		}

		return true;
	}

	
	//Add a DropTarget to the list of potential places to receive drop events
	public void addDropTarget(DragLayer target) {
		mDragLayer.add(target);
	}

	public void removeDropTarget(DragLayer target) {
		mDragLayer.remove(target);
	}

}
