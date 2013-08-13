package com.dreamteam.lookme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamteam.lookme.bean.FullProfile;
import com.dreamteam.lookme.bean.Interest;
import com.dreamteam.lookme.bean.ProfileImage;
import com.dreamteam.lookme.db.DBOpenHelper;
import com.dreamteam.lookme.db.DBOpenHelperImpl;
import com.dreamteam.lookme.service.Services;
import com.dreamteam.util.ImageUtil;
import com.dreamteam.util.Log;
import com.dreamteam.util.Nav;

public class EditProfileActivity extends CommonActivity {
	private static int RESULT_LOAD_IMAGE = 1;

	private static final int PICK_IMAGE = 1;

	String imageFilePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_edit_profile);
			FullProfile oldProfile = Services.currentState.getMyFullProfile();
			if (oldProfile != null) {
				switchToUpdateAccount(oldProfile);
			}
			initDrawerMenu(savedInstanceState, this.getClass(), true);
		} catch (Exception e) {
			Log.e("errore during create of registration activity! error: " + e.getMessage());
		}
	}

	public void onRegister(View view) {
		Log.d();
		try {

			TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

			TextView nameScreen = (TextView) findViewById(R.id.reg_name);
			TextView surnameScreen = (TextView) findViewById(R.id.reg_surname);
			TextView usernameScreen = (TextView) findViewById(R.id.reg_nickname);
			ImageView imageView = (ImageView) findViewById(R.id.imgView);

			Log.d(imageView.getDrawable().getConstantState().toString());
			Log.d(getResources().getDrawable(R.drawable.ic_profile_image).getConstantState().toString());
			if (usernameScreen.getText() == null || usernameScreen.getText().toString().equals("")
					|| imageView.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_profile_image).getConstantState())) {
				Toast.makeText(this, "To create a new profile you need to insert at least an image and a nickname.", Toast.LENGTH_SHORT).show();
				return;
			}

			FullProfile profile = Services.currentState.getMyFullProfile();

			if (profile == null)
				profile = new FullProfile();
			profile.setName(nameScreen.getText().toString());

			profile.setSurname(surnameScreen.getText().toString());

			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = manager.getConnectionInfo();
			String deviceId = info.getMacAddress();

			if (deviceId == null)
				deviceId = tm.getDeviceId();

			profile.setNickname(usernameScreen.getText().toString());

			profile.setId(deviceId);

			if (imageView.getDrawable() != null) {
				Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

				ProfileImage profileImage = null;
				if (profile.getProfileImages() != null & !profile.getProfileImages().isEmpty()) {

					profileImage = profile.getProfileImages().get(0);
					profile.getProfileImages().clear();
				} else
					profileImage = new ProfileImage();
				profileImage.setProfileId(profile.getId());
				profileImage.setImage(ImageUtil.bitmapToByteArray(bitmap));
				profileImage.setMainImage(true);
				profile.getProfileImages().add(profileImage);

			}

			DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(this);
			FullProfile savedProfile = dbOpenHelper.saveOrUpdateProfile(profile);
			Services.businessLogic.notifyMyProfileIsUpdated();
			switchToUpdateAccount(savedProfile);
			Toast toast = Toast.makeText(getApplicationContext(), "Welcome on Look@me!", 10);
			toast.show();

			Nav.startActivity(this, NearbyActivity.class);
			// Intent mainIntent = new Intent(this, SocialActivity.class);
			// this.startActivity(mainIntent);
			// this.finish();

		} catch (Exception e) {
			Log.e("errore during registration! error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void onChooseImage(View view) {
		try {

			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			// code for crop image
			intent.putExtra("crop", "true");
			// proporzione quadrata
			intent.putExtra("aspectX", 3);
			intent.putExtra("aspectY", 4);
			// dimensione di salvataggio
			// per ora messa la larghezza del galaxy S4 e proporzione 4:3
			intent.putExtra("outputX", 1080);
			intent.putExtra("outputY", 1440);
			intent.putExtra("return-data", true);
			// end code for crop image

			startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

		} catch (Exception e) {
			Log.e("errore during registration! error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Context context = getApplicationContext();
		CharSequence text = "";
		// byte[] image = null;
		try {
			super.onActivityResult(requestCode, resultCode, data);

			if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

				// recupera l'immagine cropped
				Bundle extras = data.getExtras();
				Bitmap photo = extras.getParcelable("data");
				ImageView imageView = (ImageView) findViewById(R.id.imgView);
				imageView.setImageBitmap(photo);

				// Uri selectedImage = data.getData();
				//
				// String[] filePathColumn = { MediaStore.Images.Media.DATA };
				//
				// Cursor cursor = getContentResolver().query(selectedImage,
				// filePathColumn, null, null, null);
				// cursor.moveToFirst();
				//
				// int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				// String picturePath = cursor.getString(columnIndex);
				//
				// image = ImageUtil.getImageFromPicturePath(picturePath);
				// ImageView imageView = (ImageView) findViewById(R.id.imgView);
				// imageView.setImageBitmap(BitmapFactory.decodeByteArray(image,
				// 0, image.length));
				//
				// cursor.close();
				text = "COOL PICTURE!";
			}

		} catch (Exception e) {
			Log.e("error changing image, error: " + e.toString());
			text = "ops! Unable to load image ";

		}
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();

	}

	private void switchToUpdateAccount(FullProfile profile) {
		TextView nameScreen = (TextView) findViewById(R.id.reg_name);
		nameScreen.setText(profile.getName());
		TextView surnameScreen = (TextView) findViewById(R.id.reg_surname);
		surnameScreen.setText(profile.getSurname());
		TextView usernameScreen = (TextView) findViewById(R.id.reg_nickname);
		usernameScreen.setText(profile.getNickname());

		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageBitmap(BitmapFactory.decodeByteArray(profile.getProfileImages().get(0).getImage(), 0, profile.getProfileImages().get(0).getImage().length));

		TextView interest = (TextView) findViewById(R.id.reg_interest);
		StringBuilder sb = new StringBuilder();

		for (Interest inter : Services.currentState.getMyFullProfile().getInterestList()) {
			sb.append(inter.getDesc() + " ");
		}

		interest.setText(sb.toString());

		interest.setOnFocusChangeListener(new InterestOnFocusListner(this));

		Button button = (Button) findViewById(R.id.btnRegister);
		button.setText("Save profile");
	}

	private class InterestOnFocusListner implements OnFocusChangeListener {
		private Activity activity;

		public InterestOnFocusListner(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				Nav.startActivity(activity, ManageInterestActivity.class);
			}
		}
	}

}
