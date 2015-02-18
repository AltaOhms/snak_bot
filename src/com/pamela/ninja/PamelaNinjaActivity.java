package com.pamela.ninja;

import android.os.Bundle;
import ioio.lib.api.PwmOutput;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.api.exception.ConnectionLostException;

//Joystick library, which needed to be a little modified for multitouch. 
//I found this library when going through a lot of different tutorials for the IOIO. http://mitchtech.net/ used the same library in one of his tutorials, which was of help
// You can find the base of this great library here http://code.google.com/p/mobile-anarchy-widgets/
// The mobile library is under the The BSD 2-Clause License
import com.MobileAnarchy.Android.Widgets.Joystick.DualJoystickView;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;

//The Code that isn't part of the JoystickView Library is Open Source, for educational use, and written by Pamela Cortez from SparkFun. 
//Beerware license: Instead of buying me a beer, I prefer JonesSoda or being sent cute photos of Pulis or one of my favorite drummers, Animal (aka the best Muppet)

//Use IOIOActivity over the abstract counterpart
public class PamelaNinjaActivity extends IOIOActivity {
	/** Called when the activity is first created. */
	public SeekBar leftStar_;
	public SeekBar rightStar_;
	public ToggleButton coKick_;
	public ToggleButton coKickClose_;

	public final String TAG = PamelaNinjaActivity.class.getSimpleName();

	private final int leftTopWheel_PIN = 3;
	private final int RightTopWheel_PIN = 4;
	private final int leftBottomWheel_PIN = 5;
	private final int RightBottomWheel_PIN = 6;

	private final int PWM_FREQ = 100;
	DualJoystickView joystick;

	//Starting position
	int xDegrees = 500;
	int yDegrees = 500;
	
	// Michael Mitchell examples helped with Servo control here
	private JoystickMovedListener _leftlistener = new JoystickMovedListener() {
		@Override
		public void OnMoved(int x, int y) {
			Log.i(TAG, "x: " + x + " y: " + y);
			if (y >= 0)
				xDegrees = y * 50 + 500;
			else
				xDegrees = 500 - (Math.abs(y) * 50);
		}

		@Override
		public void OnReleased() {
		}

		public void OnReturnedToCenter() {
		};
	};
	
	private JoystickMovedListener _rightlistener = new JoystickMovedListener() {
		@Override
		public void OnMoved(int x, int y) {
			y *= -1;
			Log.i(TAG, "x: " + x + " y: " + y);
			if (y >= 0) 
				yDegrees = y * 50 + 500;
			 else 
				yDegrees = 500 - (Math.abs(y) * 50);
		}

		@Override
		public void OnReleased() {
		}

		public void OnReturnedToCenter() {
		};
	};

	// GUI
	@Override

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		leftStar_ = (SeekBar) findViewById(R.id.leftStar);
		rightStar_ = (SeekBar) findViewById(R.id.rightStar);
		coKick_ = (ToggleButton) findViewById(R.id.coKick);
		coKickClose_ = (ToggleButton) findViewById(R.id.coKickClose);
		joystick = (DualJoystickView) findViewById(R.id.dualJoystick);
		joystick.setOnJostickMovedListener(_leftlistener, _rightlistener);
	}

	// for the seekbar interface I found on android dev reference. 
	

	// aka "Looper"
	// setup IOIO pins
	public class Ninja extends BaseIOIOLooper {
		private PwmOutput leftStar;
		private PwmOutput rightStar;
		private PwmOutput coKick;
		private PwmOutput leftTopWheelPwmOutput;
		private PwmOutput RightTopWheelPwmOutput;
		private PwmOutput leftBottomWheelPwmOutput;
		private PwmOutput RightBottomWheelPwmOutput;

		@Override
		// "setup"
		protected void setup() throws ConnectionLostException {
			try {
				leftStar = ioio_.openPwmOutput(10, 100);
				rightStar = ioio_.openPwmOutput(11, 100);
				coKick = ioio_.openPwmOutput(7, 100);
				leftTopWheelPwmOutput = ioio_.openPwmOutput(leftTopWheel_PIN, PWM_FREQ);
				RightTopWheelPwmOutput = ioio_.openPwmOutput(RightTopWheel_PIN, PWM_FREQ);
				leftBottomWheelPwmOutput = ioio_.openPwmOutput(leftBottomWheel_PIN, PWM_FREQ);
				RightBottomWheelPwmOutput = ioio_.openPwmOutput(RightBottomWheel_PIN, PWM_FREQ);
			} catch (ConnectionLostException e) {

			}
		}
		//"loop"
		public void loop() throws ConnectionLostException {
			try {
				leftTopWheelPwmOutput.setPulseWidth(500 + xDegrees * 2);
				leftBottomWheelPwmOutput.setPulseWidth(500 + xDegrees * 2);
				RightTopWheelPwmOutput.setPulseWidth(500 + yDegrees * 2);
				RightBottomWheelPwmOutput.setPulseWidth(500 + yDegrees * 2);
				// Note: The progress range of the seekbars are currently 0-100.
				//   This was left in place for now as we would want a simple on/off for the competition.
				//   In order to change the speed or direction of the ninja stars, change the
				//   min/max/default level of the sliders.
				leftStar.setPulseWidth(leftStar_.getProgress());
				rightStar.setPulseWidth(rightStar_.getProgress());

				if (coKick_.isChecked())
					coKick.setPulseWidth(2500);
				else if (((ToggleButton) coKickClose_).isChecked())
					coKick.setPulseWidth(500);
				else
					coKick.setPulseWidth(0);

				Thread.sleep(10);

			} catch (ConnectionLostException e) {
			} catch (InterruptedException e) {
				ioio_.disconnect();
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		// aka "Looper"
		return new Ninja();
	}
}
