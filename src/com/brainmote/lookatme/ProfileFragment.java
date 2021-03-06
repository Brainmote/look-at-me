package com.brainmote.lookatme;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainmote.lookatme.bean.BasicProfile;
import com.brainmote.lookatme.bean.Contact;
import com.brainmote.lookatme.bean.FullProfile;
import com.brainmote.lookatme.bean.ProfileImage;
import com.brainmote.lookatme.chord.Node;
import com.brainmote.lookatme.constants.AppSettings;
import com.brainmote.lookatme.db.DBOpenHelper;
import com.brainmote.lookatme.db.DBOpenHelperImpl;
import com.brainmote.lookatme.enumattribute.Country;
import com.brainmote.lookatme.enumattribute.Gender;
import com.brainmote.lookatme.service.Event;
import com.brainmote.lookatme.service.Services;
import com.brainmote.lookatme.util.ImageUtil;
import com.brainmote.lookatme.util.Log;
import com.brainmote.lookatme.util.Nav;
import com.squareup.otto.Subscribe;

public class ProfileFragment extends Fragment {

	private ViewPager profilePhoto;
	private ImageButton buttonLike;
	private ImageButton buttonChat;
	private List<Bitmap> gallery_images;

	private ImageView countryImage;
	private ImageView genderImage;
	private TextView textName;
	private TextView textSurname;
	private TextView textStatus;

	private ViewGroup contactView;
	private ViewGroup phoneGroup;
	private ViewGroup mailGroup;
	private TextView textTelephone;
	private TextView textMail;
	private ImageView facebookLink;
	private ImageView linkedinLink;

	private ViewGroup profileActionContainer;
	private ViewGroup profileHiddenContainer;
	private boolean showHiddenContainer;

	private ProgressDialog loadingDialog;
	private boolean profileReady;

	private ImageButton showInterestsButton;
	private GridView interestGrid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d();

		// TODO Ricordarsi di posticipare l'aggancio dei listener perché non
		// viene più utilizzato l'enabled/disabled

		profileReady = false;
		View view = inflater.inflate(R.layout.fragment_profile, null);

		interestGrid = (GridView) view.findViewById(R.id.gridInterestInDetail);
		showInterestsButton = (ImageButton) view.findViewById(R.id.showInterestsButton);
		showInterestsButton.setVisibility(View.GONE);
		profileActionContainer = (ViewGroup) view.findViewById(R.id.profileActionContainer);
		profileHiddenContainer = (ViewGroup) view.findViewById(R.id.profileHiddenContainer);
		profileHiddenContainer.setVisibility(View.GONE);
		showHiddenContainer = false;

		countryImage = (ImageView) view.findViewById(R.id.imageCountry);
		genderImage = (ImageView) view.findViewById(R.id.imageGender);
		textName = (TextView) view.findViewById(R.id.textName);
		textSurname = (TextView) view.findViewById(R.id.textSurname);
		textStatus = (TextView) view.findViewById(R.id.textStatus);
		profilePhoto = (HackyViewPager) view.findViewById(R.id.hackyViewPager);
		gallery_images = new ArrayList<Bitmap>();
		profilePhoto.setAdapter(new SamplePagerAdapter());
		// recupero il node id
		Bundle parameters = getActivity().getIntent().getExtras();
		final String nodeId = parameters.getString(Nav.NODE_KEY_ID);
		// preparo i pulsanti
		buttonLike = (LikeButton) view.findViewById(R.id.buttonLike);
		buttonChat = (ImageButton) view.findViewById(R.id.buttonChat);
		// contatti
		contactView = (ViewGroup) view.findViewById(R.id.profileContactView);
		contactView.setVisibility(View.GONE);
		phoneGroup = (ViewGroup) view.findViewById(R.id.profilePhoneView);
		phoneGroup.setVisibility(View.GONE);
		mailGroup = (ViewGroup) view.findViewById(R.id.profileMailView);
		mailGroup.setVisibility(View.GONE);
		textTelephone = (TextView) view.findViewById(R.id.textTelephone);
		textMail = (TextView) view.findViewById(R.id.textEmail);
		facebookLink = (ImageView) view.findViewById(R.id.imageFacebook);
		facebookLink.setVisibility(View.GONE);
		linkedinLink = (ImageView) view.findViewById(R.id.imageLinkedin);
		linkedinLink.setVisibility(View.GONE);
		// Verifico se il profilo è di un utente fake
		if (Services.businessLogic.isFakeUserNode(nodeId)) {
			Services.currentState.setProfileViewed(Services.businessLogic.getFakeUser(nodeId).getNode());
			prepareProfileAttributes();
		} else {
			// Controllo se il nodo è ancora presente
			final CommonActivity activity = (CommonActivity) getActivity();
			if (Services.businessLogic.isNodeAlive(nodeId)) {
				// invio la richiesta di full profile
				Services.businessLogic.requestFullProfile(nodeId);
				// apro un dialog di attesa se trascorre troppo tempo dalla
				// richiesta
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (!profileReady) {
							loadingDialog = new ProgressDialog(getActivity());
							loadingDialog.setMessage(getActivity().getResources().getString(R.string.loading_profile_message));
							loadingDialog.setCancelable(false);
							loadingDialog.show();
							// Dopo AppSettings.LOADING_PROFILE_TIMEOUT
							// millisecondi, se il popup di caricamento non è
							// stato chiuso, viene chiuso in automatico e viene
							// mostrato il messaggio di errore all'utente.
							new Handler().postDelayed(new Runnable() {
								public void run() {
									if (loadingDialog.isShowing()) {
										loadingDialog.dismiss();
										activity.showDialog(activity.getString(R.string.no_profile_title), activity.getString(R.string.no_profile_message),
												activity.getString(R.string.no_profile_button_label), NearbyActivity.class, true, true);
									}
								}
							}, AppSettings.LOADING_PROFILE_TIMEOUT);
						}
					}
				}, AppSettings.WAIT_BEFORE_SHOWING_LOADING_PROFILE_DIALOG);
			} else {
				activity.showDialog(activity.getString(R.string.no_profile_title), activity.getString(R.string.no_profile_message),
						activity.getString(R.string.no_profile_button_label), NearbyActivity.class, true, true);
			}
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Services.event.register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Services.event.unregister(this);
	}

	@Subscribe
	public void onEventReceived(Event event) {
		switch (event.getEventType()) {
		case FULL_PROFILE_RECEIVED:
			prepareProfileAttributes();
			if (loadingDialog != null)
				loadingDialog.dismiss();
			break;
		default:
			break;
		}
	}

	public void prepareProfileAttributes() {
		final Node profileNode = Services.currentState.getProfileViewed();
		FullProfile profile = (FullProfile) profileNode.getProfile();
		((ProfileActivity) this.getActivity()).setFavourite(checkFavourite());
		gallery_images = new ArrayList<Bitmap>();
		if (profile != null) {
			if (profile.getStatus() != null) {
				textStatus.setText(profile.getStatus());
			}
			if (profile.getName() != null) {
				textName.setText(profile.getName());
			}
			if (profile.getSurname() != null) {
				textSurname.setText(profile.getSurname());
			}
			if (profile.getLivingCountry() != null) {
				Country country = Country.parse(profile.getLivingCountry());
				switch (country) {
				case CA:
					countryImage.setImageResource(R.drawable.canada);
					break;
				case CN:
					countryImage.setImageResource(R.drawable.china);
					break;
				case DE:
					countryImage.setImageResource(R.drawable.germany);
					break;
				case FR:
					countryImage.setImageResource(R.drawable.france);
					break;
				case IT:
					countryImage.setImageResource(R.drawable.italy);
					break;
				case JA:
					countryImage.setImageResource(R.drawable.japan);
					break;
				case KR:
					countryImage.setImageResource(R.drawable.korea);
					break;
				case TW:
					countryImage.setImageResource(R.drawable.taiwan);
					break;
				case UK:
					countryImage.setImageResource(R.drawable.uk);
					break;
				case US:
					countryImage.setImageResource(R.drawable.us);
					break;
				}
			}
			Gender gender = Gender.parse(profile.getGender());
			switch (gender) {
			case F:
				genderImage.setImageResource(R.drawable.venus_symbol);
				break;
			case M:
				genderImage.setImageResource(R.drawable.mars_symbol);
				break;
			case TG:
				genderImage.setImageResource(R.drawable.transgender_symbol);
				break;
			}
			buttonChat.setEnabled(true);
			// Imposto il title con il nickname e l'età dell'utente selezionato
			String age = profile.getAge() > 0 ? ", " + String.valueOf(profile.getAge()) : "";
			((CommonActivity) getActivity()).setTitle(profile.getNickname() + age);
			// Imposto le immagini del profilo utente
			if (profile.getProfileImages() != null) {
				for (ProfileImage image : profile.getProfileImages()) {
					gallery_images.add(BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length));
				}
			} else {
				gallery_images.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_image));
			}
			buttonLike.setEnabled(likeButtonIsEnabledFor(Services.currentState.getProfileViewed().getId()));

			// Preparo gli interessi e i contatti
			if ((profile.getInterestSet() != null && profile.getInterestSet().size() > 0) || (profile.getContactList() != null && profile.getContactList().size() > 0)) {
				showInterestsButton.setVisibility(View.VISIBLE);
			}

			if (profile.getInterestSet() != null && profile.getInterestSet().size() > 0) {
				interestGrid.setAdapter(new EditProfileInterestGridAdapter(getActivity(), profile.getInterestSet(), false));
			}

			if (profile.getContactList() != null && profile.getContactList().size() > 0) {
				contactView.setVisibility(View.VISIBLE);
				for (Contact contact : profile.getContactList()) {
					final String reference = contact.getReference();
					switch (contact.getContactType()) {
					case EMAIL:
						mailGroup.setVisibility(View.VISIBLE);
						textMail.setText(contact.getReference());
						textMail.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Intent email = new Intent(Intent.ACTION_SEND);
								email.putExtra(Intent.EXTRA_EMAIL, new String[] { reference });
								email.putExtra(Intent.EXTRA_SUBJECT, "subject");
								email.putExtra(Intent.EXTRA_TEXT, "message");
								email.setType("message/rfc822");
								startActivity(Intent.createChooser(email, "Choose an Email client:"));
							}
						});
						break;
					case FACEBOOK:
						facebookLink.setVisibility(View.VISIBLE);
						facebookLink.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String uri = AppSettings.URL_PREFIX_FACEBOOK + reference;
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
								startActivity(intent);
							}
						});
						break;
					case LINKEDIN:
						linkedinLink.setVisibility(View.VISIBLE);
						linkedinLink.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Intent intent = new Intent();
								intent.setAction(Intent.ACTION_VIEW);
								intent.addCategory(Intent.CATEGORY_BROWSABLE);
								intent.setData(Uri.parse(AppSettings.URL_PREFIX_LINKEDIN + reference));
								startActivity(intent);
							}
						});
						break;
					case PHONE:
						phoneGroup.setVisibility(View.VISIBLE);
						textTelephone.setText(contact.getReference());
						// Se è presente il modulo telefonico inserisco
						// l'opzione di
						// chiamare direttamente il numero indicato
						if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
							textTelephone.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_CALL);
									intent.setData(Uri.parse("tel:" + reference));
									startActivity(intent);
								}
							});
						}
						break;
					}
				}
			}

		}
		buttonLike.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Verificare che il nodo sia ancora attivo, altrimenti
				// dare un messaggio all'utente
				Services.businessLogic.sendLike(profileNode.getId());
				buttonLike.setEnabled(false);
				Toast.makeText(getActivity(), "You like " + Services.currentState.getSocialNodeMap().findNodeByNodeId(profileNode.getId()).getProfile().getNickname(),
						Toast.LENGTH_LONG).show();
			}
		});
		buttonChat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Verificare che il nodo sia ancora attivo, altrimenti
				// dare un messaggio all'utente
				Services.businessLogic.startChat(profileNode.getId());
				Bundle parameters = new Bundle();
				parameters.putString(
						Nav.CONVERSATION_KEY_ID,
						Services.currentState.getConversationsStore().calculateConversationId(Services.currentState.getMyBasicProfile().getId(),
								Services.currentState.getSocialNodeMap().getProfileIdByNodeId(profileNode.getId())));
				Nav.startActivityWithParameters(getActivity(), ChatMessagesActivity.class, parameters);
			}
		});
		profilePhoto.getAdapter().notifyDataSetChanged();
		profileReady = true;
	}

	private boolean likeButtonIsEnabledFor(String nodeId) {
		String profileId = Services.currentState.getSocialNodeMap().getProfileIdByNodeId(nodeId);
		return !Services.currentState.getILikeSet().contains(profileId);
	}

	protected void toggleInterests() {
		Log.d("toggling");
		// ViewGroup interests = (ViewGroup)
		// LayoutInflater.from(getActivity()).inflate(R.layout.interests_view,
		// profileBottomContainer, true);
		if (!showHiddenContainer) {
			// Aggiungo il layout degli interessi
			// LayoutInflater.from(getActivity()).inflate(R.layout.interests_view,
			// profileBottomContainer, true);
			// Nascondo i pulsanti like a chat
			profileHiddenContainer.setVisibility(View.VISIBLE);
			profileActionContainer.setVisibility(View.GONE);
			showInterestsButton.setRotation(180);
			// Inverto i valori di padding perché altrimenti vengono riportati
			// in maniera errata (il padding top diventa effettivamente padding
			// bottom)
			showInterestsButton.setPadding(showInterestsButton.getPaddingLeft(), showInterestsButton.getPaddingBottom(), showInterestsButton.getPaddingRight(),
					showInterestsButton.getPaddingTop());
			showHiddenContainer = true;
		} else {
			profileHiddenContainer.setVisibility(View.GONE);
			profileActionContainer.setVisibility(View.VISIBLE);
			showInterestsButton.setRotation(0);
			// Inverto nuovamente i valori di padding per riportarli ai valori
			// corretti
			showInterestsButton.setPadding(showInterestsButton.getPaddingLeft(), showInterestsButton.getPaddingBottom(), showInterestsButton.getPaddingRight(),
					showInterestsButton.getPaddingTop());
			showHiddenContainer = false;
		}
	}

	public void saveContact() {
		try {
			DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(getActivity());
			BasicProfile profile = (BasicProfile) Services.currentState.getProfileViewed().getProfile();
			dbOpenHelper.saveOrUpdateProfile(profile);
		} catch (Exception e) {
			Log.e("Error while saving favourite contact");
		}
	}

	public void removeContact() {
		try {
			DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(getActivity());
			BasicProfile profile = (BasicProfile) Services.currentState.getProfileViewed().getProfile();
			dbOpenHelper.deleteProfile(profile.getId());
		} catch (Exception e) {
			Log.e("Error while deleting favourite contact");
		}
	}

	private boolean checkFavourite() {
		try {
			DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(getActivity());
			BasicProfile profile = (BasicProfile) Services.currentState.getProfileViewed().getProfile();
			if (dbOpenHelper.getBasicProfile(profile.getId()) != null) {
				return true;
			}
		} catch (Exception e) {
			Log.e("Error while deleting favourite contact");
		}
		return false;
	}

	class SamplePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return gallery_images.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			Bitmap photoImage = gallery_images.get(position);
			// Crop to get same ratio
			photoView.setImageBitmap(ImageUtil.bitmapForGallery(photoImage));
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

}
