/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
*/


/* <!-- +++
package com.almalence.opencam_plus.ui;
+++ --> */
// <!-- -+-
package com.almalence.opencam.ui;
//-+- -->


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.almalence.ui.Panel;
import com.almalence.ui.RotateImageView;
import com.almalence.ui.Panel.OnPanelListener;

import com.almalence.googsharing.Thumbnail;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


//<!-- -+-
import com.almalence.opencam.CameraParameters;
import com.almalence.opencam.ConfigParser;
import com.almalence.opencam.MainScreen;
import com.almalence.opencam.Mode;
import com.almalence.opencam.Plugin;
import com.almalence.opencam.PluginManager;
import com.almalence.opencam.PluginType;
import com.almalence.opencam.Preferences;
import com.almalence.opencam.R;
import com.almalence.opencam.cameracontroller.CameraController;

//-+- -->
/* <!-- +++
import com.almalence.opencam_plus.CameraController;
import com.almalence.opencam_plus.CameraParameters;
import com.almalence.opencam_plus.ConfigParser;
import com.almalence.opencam_plus.MainScreen;
import com.almalence.opencam_plus.Mode;
import com.almalence.opencam_plus.Plugin;
import com.almalence.opencam_plus.PluginManager;
import com.almalence.opencam_plus.PluginType;
import com.almalence.opencam_plus.Preferences;
import com.almalence.opencam_plus.R;
+++ --> */

import com.almalence.util.Util;

/***
 * AlmalenceGUI is an instance of GUI class, implements current GUI
 ***/

public class AlmalenceGUI extends GUI implements
		SeekBar.OnSeekBarChangeListener, View.OnLongClickListener,
		View.OnClickListener {
	private SharedPreferences preferences;

	private final int INFO_ALL = 0;
	private final int INFO_NO = 1;
	private final int INFO_GRID = 2;
	private final int INFO_PARAMS = 3;
	private int infoSet = INFO_PARAMS;

	public enum ShutterButton {
		DEFAULT, RECORDER_START, RECORDER_STOP, RECORDER_RECORDING, RECORDER_PAUSED
	};

	public enum SettingsType {
		SCENE, WB, FOCUS, FLASH, ISO, METERING, CAMERA, EV, MORE
	};

	private OrientationEventListener orientListener;	

	// certain quick control visible
	private boolean quickControlsVisible = false;

	// Quick control customization variables
	private ElementAdapter quickControlAdapter;
	private List<View> quickControlChangeres;
	private View currentQuickView = null; // Current quick control to replace
	private boolean quickControlsChangeVisible = false; // If qc customization
														// layout is showing now

	// Settings layout
	private ElementAdapter settingsAdapter;
	private List<View> settingsViews;
	private boolean settingsControlsVisible = false; // If quick settings layout
														// is showing now

	// Mode selector layout
	private ElementAdapter modeAdapter;
	private List<View> modeViews;
	private boolean modeSelectorVisible = false; // If quick settings layout is
													// showing now

	// Assoc list for storing association between mode button and mode ID
	private Hashtable<View, String> buttonModeViewAssoc;

	//store grid adapter
	private ElementAdapter storeAdapter;
	private List<View> storeViews;
	private Hashtable<View, Integer> buttonStoreViewAssoc;
	
	// private SharePopup mSharePopup;
	private Thumbnail mThumbnail;
	private RotateImageView thumbnailView;
	
	private RotateImageView shutterButton;

	private static final Integer icon_ev = R.drawable.gui_almalence_settings_exposure;
	private static final Integer icon_cam = R.drawable.gui_almalence_settings_changecamera;
	private static final Integer icon_settings = R.drawable.gui_almalence_settings_more_settings;

	private static final int focusAfLock = 10;

	// Lists of icons for camera parameters (scene mode, flash mode, focus mode,
	// white balance, iso)
	private static final Map<Integer, Integer> icons_scene = new Hashtable<Integer, Integer>() {
		{
			put(CameraParameters.SCENE_MODE_AUTO, R.drawable.gui_almalence_settings_scene_auto);
			put(CameraParameters.SCENE_MODE_ACTION, R.drawable.gui_almalence_settings_scene_action);
			put(CameraParameters.SCENE_MODE_PORTRAIT, R.drawable.gui_almalence_settings_scene_portrait);
			put(CameraParameters.SCENE_MODE_LANDSCAPE, R.drawable.gui_almalence_settings_scene_landscape);
			put(CameraParameters.SCENE_MODE_NIGHT, R.drawable.gui_almalence_settings_scene_night);
			put(CameraParameters.SCENE_MODE_NIGHT_PORTRAIT, R.drawable.gui_almalence_settings_scene_nightportrait);
			put(CameraParameters.SCENE_MODE_THEATRE, R.drawable.gui_almalence_settings_scene_theater);
			put(CameraParameters.SCENE_MODE_BEACH, R.drawable.gui_almalence_settings_scene_beach);
			put(CameraParameters.SCENE_MODE_SNOW, R.drawable.gui_almalence_settings_scene_snow);
			put(CameraParameters.SCENE_MODE_SUNSET, R.drawable.gui_almalence_settings_scene_sunset);
			put(CameraParameters.SCENE_MODE_STEADYPHOTO, R.drawable.gui_almalence_settings_scene_steadyphoto);
			put(CameraParameters.SCENE_MODE_FIREWORKS, R.drawable.gui_almalence_settings_scene_fireworks);
			put(CameraParameters.SCENE_MODE_SPORTS, R.drawable.gui_almalence_settings_scene_sports);
			put(CameraParameters.SCENE_MODE_PARTY, R.drawable.gui_almalence_settings_scene_party);
			put(CameraParameters.SCENE_MODE_CANDLELIGHT, R.drawable.gui_almalence_settings_scene_candlelight);
			put(CameraParameters.SCENE_MODE_BARCODE, R.drawable.gui_almalence_settings_scene_barcode);
		}
	};

	private static final Map<Integer, Integer> icons_wb = new Hashtable<Integer, Integer>() {
		{
			put(CameraParameters.WB_MODE_AUTO, R.drawable.gui_almalence_settings_wb_auto);
			put(CameraParameters.WB_MODE_INCANDESCENT, R.drawable.gui_almalence_settings_wb_incandescent);
			put(CameraParameters.WB_MODE_FLUORESCENT, R.drawable.gui_almalence_settings_wb_fluorescent);
			put(CameraParameters.WB_MODE_WARM_FLUORESCENT, R.drawable.gui_almalence_settings_wb_warmfluorescent);
			put(CameraParameters.WB_MODE_DAYLIGHT, R.drawable.gui_almalence_settings_wb_daylight);
			put(CameraParameters.WB_MODE_CLOUDY_DAYLIGHT, R.drawable.gui_almalence_settings_wb_cloudydaylight);
			put(CameraParameters.WB_MODE_TWILIGHT, R.drawable.gui_almalence_settings_wb_twilight);
			put(CameraParameters.WB_MODE_SHADE, R.drawable.gui_almalence_settings_wb_shade);
		}
	};

	private static final Map<Integer, Integer> icons_focus = new Hashtable<Integer, Integer>() {
		{
			put(CameraParameters.AF_MODE_AUTO, R.drawable.gui_almalence_settings_focus_auto);
			put(CameraParameters.AF_MODE_INFINITY, R.drawable.gui_almalence_settings_focus_infinity);
			put(CameraParameters.AF_MODE_NORMAL, R.drawable.gui_almalence_settings_focus_normal);
			put(CameraParameters.AF_MODE_MACRO, R.drawable.gui_almalence_settings_focus_macro);
			put(CameraParameters.AF_MODE_FIXED, R.drawable.gui_almalence_settings_focus_fixed);
			put(CameraParameters.AF_MODE_EDOF, R.drawable.gui_almalence_settings_focus_edof);
			put(CameraParameters.AF_MODE_CONTINUOUS_VIDEO, R.drawable.gui_almalence_settings_focus_continiuousvideo);
			put(CameraParameters.AF_MODE_CONTINUOUS_PICTURE, R.drawable.gui_almalence_settings_focus_continiuouspicture);
			put(focusAfLock, R.drawable.gui_almalence_settings_focus_aflock);
		}
	};

	private static final Map<Integer, Integer> icons_flash = new Hashtable<Integer, Integer>() {
		{
			put(CameraParameters.FLASH_MODE_OFF, R.drawable.gui_almalence_settings_flash_off);
			put(CameraParameters.FLASH_MODE_AUTO, R.drawable.gui_almalence_settings_flash_auto);
			put(CameraParameters.FLASH_MODE_SINGLE, R.drawable.gui_almalence_settings_flash_on);
			put(CameraParameters.FLASH_MODE_REDEYE, R.drawable.gui_almalence_settings_flash_redeye);
			put(CameraParameters.FLASH_MODE_TORCH, R.drawable.gui_almalence_settings_flash_torch);
		}
	};

	private static final Map<Integer, Integer> icons_iso = new Hashtable<Integer, Integer>() {
		{
			put(CameraParameters.ISO_AUTO, R.drawable.gui_almalence_settings_iso_auto);
			put(CameraParameters.ISO_50, R.drawable.gui_almalence_settings_iso_50);
			put(CameraParameters.ISO_100, R.drawable.gui_almalence_settings_iso_100);				
			put(CameraParameters.ISO_200, R.drawable.gui_almalence_settings_iso_200);
			put(CameraParameters.ISO_400, R.drawable.gui_almalence_settings_iso_400);
			put(CameraParameters.ISO_800, R.drawable.gui_almalence_settings_iso_800);
			put(CameraParameters.ISO_1600, R.drawable.gui_almalence_settings_iso_1600);
			put(CameraParameters.ISO_3200, R.drawable.gui_almalence_settings_iso_3200);			
		}
	};
	
	private static final Map<String, Integer> icons_default_iso = new Hashtable<String, Integer>() {
		{
			put(MainScreen.thiz.getResources().getString(R.string.isoAutoDefaultSystem), R.drawable.gui_almalence_settings_iso_auto);
			put(MainScreen.thiz.getResources().getString(R.string.iso50DefaultSystem), R.drawable.gui_almalence_settings_iso_50);
			put(MainScreen.thiz.getResources().getString(R.string.iso100DefaultSystem), R.drawable.gui_almalence_settings_iso_100);				
			put(MainScreen.thiz.getResources().getString(R.string.iso200DefaultSystem), R.drawable.gui_almalence_settings_iso_200);
			put(MainScreen.thiz.getResources().getString(R.string.iso400DefaultSystem), R.drawable.gui_almalence_settings_iso_400);
			put(MainScreen.thiz.getResources().getString(R.string.iso800DefaultSystem), R.drawable.gui_almalence_settings_iso_800);
			put(MainScreen.thiz.getResources().getString(R.string.iso1600DefaultSystem), R.drawable.gui_almalence_settings_iso_1600);
			put(MainScreen.thiz.getResources().getString(R.string.iso3200DefaultSystem), R.drawable.gui_almalence_settings_iso_3200);			
		}
	};
	
	private static final Map<Integer, Integer> icons_metering = new Hashtable<Integer, Integer>() {
		{
			put(0, R.drawable.gui_almalence_settings_metering_auto);
			put(1, R.drawable.gui_almalence_settings_metering_matrix);
			put(2, R.drawable.gui_almalence_settings_metering_center);				
			put(3, R.drawable.gui_almalence_settings_metering_spot);
		}
	};

	// List of localized names for camera parameters values	
	private static final Map<Integer, String> names_scene = new Hashtable<Integer, String>() {
		{
			put(CameraParameters.SCENE_MODE_AUTO, MainScreen.thiz.getResources().getString(R.string.sceneAuto));
			put(CameraParameters.SCENE_MODE_ACTION, MainScreen.thiz.getResources().getString(R.string.sceneAction));
			put(CameraParameters.SCENE_MODE_PORTRAIT, MainScreen.thiz.getResources().getString(R.string.scenePortrait));
			put(CameraParameters.SCENE_MODE_LANDSCAPE,	MainScreen.thiz.getResources().getString(R.string.sceneLandscape));
			put(CameraParameters.SCENE_MODE_NIGHT,	MainScreen.thiz.getResources().getString(R.string.sceneNight));
			put(CameraParameters.SCENE_MODE_NIGHT_PORTRAIT, MainScreen.thiz.getResources().getString(R.string.sceneNightPortrait));
			put(CameraParameters.SCENE_MODE_THEATRE, MainScreen.thiz.getResources().getString(R.string.sceneTheatre));
			put(CameraParameters.SCENE_MODE_BEACH,	MainScreen.thiz.getResources().getString(R.string.sceneBeach));
			put(CameraParameters.SCENE_MODE_SNOW, MainScreen.thiz.getResources().getString(R.string.sceneSnow));
			put(CameraParameters.SCENE_MODE_SUNSET, MainScreen.thiz.getResources().getString(R.string.sceneSunset));
			put(CameraParameters.SCENE_MODE_STEADYPHOTO, MainScreen.thiz.getResources().getString(R.string.sceneSteadyPhoto));
			put(CameraParameters.SCENE_MODE_FIREWORKS, MainScreen.thiz.getResources().getString(R.string.sceneFireworks));
			put(CameraParameters.SCENE_MODE_SPORTS, MainScreen.thiz.getResources().getString(R.string.sceneSports));
			put(CameraParameters.SCENE_MODE_PARTY, MainScreen.thiz.getResources().getString(R.string.sceneParty));
			put(CameraParameters.SCENE_MODE_CANDLELIGHT, MainScreen.thiz.getResources().getString(R.string.sceneCandlelight));
			put(CameraParameters.SCENE_MODE_BARCODE, MainScreen.thiz.getResources().getString(R.string.sceneBarcode));
		}
	};

	private static final Map<Integer, String> names_wb = new Hashtable<Integer, String>() {
		{
			put(CameraParameters.WB_MODE_AUTO, MainScreen.thiz.getResources().getString(R.string.wbAuto));
			put(CameraParameters.WB_MODE_INCANDESCENT, MainScreen.thiz.getResources().getString(R.string.wbIncandescent));
			put(CameraParameters.WB_MODE_FLUORESCENT, MainScreen.thiz.getResources().getString(R.string.wbFluorescent));
			put(CameraParameters.WB_MODE_WARM_FLUORESCENT, MainScreen.thiz.getResources().getString(R.string.wbWarmFluorescent));
			put(CameraParameters.WB_MODE_DAYLIGHT, MainScreen.thiz.getResources().getString(R.string.wbDaylight));
			put(CameraParameters.WB_MODE_CLOUDY_DAYLIGHT, MainScreen.thiz.getResources().getString(R.string.wbCloudyDaylight));
			put(CameraParameters.WB_MODE_TWILIGHT, MainScreen.thiz.getResources().getString(R.string.wbTwilight));
			put(CameraParameters.WB_MODE_SHADE, MainScreen.thiz.getResources().getString(R.string.wbShade));
		}
	};


	private static final Map<Integer, String> names_focus = new Hashtable<Integer, String>() {
		{
			put(CameraParameters.AF_MODE_AUTO, MainScreen.thiz.getResources().getString(R.string.focusAuto));
			put(CameraParameters.AF_MODE_INFINITY, MainScreen.thiz.getResources().getString(R.string.focusInfinity));
			put(CameraParameters.AF_MODE_NORMAL, MainScreen.thiz.getResources().getString(R.string.focusNormal));
			put(CameraParameters.AF_MODE_MACRO, MainScreen.thiz.getResources().getString(R.string.focusMacro));
			put(CameraParameters.AF_MODE_FIXED, MainScreen.thiz.getResources().getString(R.string.focusFixed));
			put(CameraParameters.AF_MODE_EDOF, MainScreen.thiz.getResources().getString(R.string.focusEdof));
			put(CameraParameters.AF_MODE_CONTINUOUS_VIDEO, MainScreen.thiz.getResources().getString(R.string.focusContinuousVideo));
			put(CameraParameters.AF_MODE_CONTINUOUS_PICTURE, MainScreen.thiz.getResources().getString(R.string.focusContinuousPicture));
		}
	};

	private static final Map<Integer, String> names_flash = new Hashtable<Integer, String>() {
		{
			put(CameraParameters.FLASH_MODE_OFF, MainScreen.thiz.getResources().getString(R.string.flashOff));
			put(CameraParameters.FLASH_MODE_AUTO, MainScreen.thiz.getResources().getString(R.string.flashAuto));
			put(CameraParameters.FLASH_MODE_SINGLE, MainScreen.thiz.getResources().getString(R.string.flashOn));
			put(CameraParameters.FLASH_MODE_REDEYE, MainScreen.thiz.getResources().getString(R.string.flashRedEye));
			put(CameraParameters.FLASH_MODE_TORCH, MainScreen.thiz.getResources().getString(R.string.flashTorch));
		}
	};

	private static final Map<Integer, String> names_iso = new Hashtable<Integer, String>() {
		{
			put(CameraParameters.ISO_AUTO, MainScreen.thiz.getResources().getString(R.string.isoAuto));
			put(CameraParameters.ISO_50, MainScreen.thiz.getResources().getString(R.string.iso50));
			put(CameraParameters.ISO_100, MainScreen.thiz.getResources().getString(R.string.iso100));
			put(CameraParameters.ISO_200, MainScreen.thiz.getResources().getString(R.string.iso200));
			put(CameraParameters.ISO_400, MainScreen.thiz.getResources().getString(R.string.iso400));
			put(CameraParameters.ISO_800, MainScreen.thiz.getResources().getString(R.string.iso800));
			put(CameraParameters.ISO_1600, MainScreen.thiz.getResources().getString(R.string.iso1600));
			put(CameraParameters.ISO_3200, MainScreen.thiz.getResources().getString(R.string.iso3200));
		}
	};
	
	private static final Map<Integer, String> names_metering = new Hashtable<Integer, String>() {
		{
			put(0, MainScreen.thiz.getResources().getString(R.string.meteringAutoSystem));
			put(1, MainScreen.thiz.getResources().getString(R.string.meteringMatrixSystem));
			put(2, MainScreen.thiz.getResources().getString(R.string.meteringCenterSystem));
			put(3, MainScreen.thiz.getResources().getString(R.string.meteringSpotSystem));
		}
	};
	
	
	private static final Map<String, String> unlockModePreference = new Hashtable<String, String>() {
		{
			put("hdrmode", "plugin_almalence_hdr");
			put("movingobjects", "plugin_almalence_moving_burst");
			put("sequence", "plugin_almalence_hdr");
			put("groupshot", "plugin_almalence_groupshot");
			put("panorama_augmented", "plugin_almalence_panorama");
		}
	};
	
	// Defining for top menu buttons (camera parameters settings)
	private final int MODE_EV = R.id.evButton;
	private final int MODE_SCENE = R.id.sceneButton;
	private final int MODE_WB = R.id.wbButton;
	private final int MODE_FOCUS = R.id.focusButton;
	private final int MODE_FLASH = R.id.flashButton;
	private final int MODE_ISO = R.id.isoButton;
	private final int MODE_CAM = R.id.camerachangeButton;
	private final int MODE_MET = R.id.meteringButton;

	private Map<Integer, View> topMenuButtons;
	private Map<String, View> topMenuPluginButtons; // Each plugin may have one
													// top menu (and appropriate
													// quick control) button

	// Current quick controls
	private View quickControl1 = null;
	private View quickControl2 = null;
	private View quickControl3 = null;
	private View quickControl4 = null;

	private ElementAdapter scenemodeAdapter;
	private ElementAdapter wbmodeAdapter;
	private ElementAdapter focusmodeAdapter;
	private ElementAdapter flashmodeAdapter;
	private ElementAdapter isoAdapter;
	private ElementAdapter meteringmodeAdapter;

	private Map<Integer, View> SceneModeButtons;
	private Map<Integer, View> WBModeButtons;
	private Map<Integer, View> FocusModeButtons;
	private Map<Integer, View> FlashModeButtons;
	private Map<Integer, View> ISOButtons;
	private Map<Integer, View> MeteringModeButtons;

	// Camera settings values which is exist at current device
	private List<View> activeScene;
	private List<View> activeWB;
	private List<View> activeFocus;
	private List<View> activeFlash;
	private List<View> activeISO;
	private List<View> activeMetering;

	private List<Integer> activeSceneNames;
	private List<Integer> activeWBNames;
	private List<Integer> activeFocusNames;
	private List<Integer> activeFlashNames;
	private List<Integer> activeISONames;
	private List<Integer> activeMeteringNames;

	private boolean isEVEnabled = true;
	private boolean isSceneEnabled = true;
	private boolean isWBEnabled = true;
	private boolean isFocusEnabled = true;
	private boolean isFlashEnabled = true;
	private boolean isIsoEnabled = true;
	private boolean isCameraChangeEnabled = true;
	private boolean isMeteringEnabled = true;

	// GUI Layout
	public View guiView;

	// Current camera parameters
	private int mEV = 0;
	private int mSceneMode = -1;
	private int mFlashMode = -1;
	private int mFocusMode = -1;
	private int mWB = -1;
	private int mISO = -1;
	private int mMeteringMode = -1;

	public boolean showAEAWLock = false;
	
	private Matrix mMeteringMatrix;

	// Prefer sizes for plugin's controls in pixels for screens with density =
	// 1;
	private float fScreenDensity;

	private int iInfoViewMaxHeight;
	private int iInfoViewMaxWidth;
	private int iInfoViewHeight;

	private int iCenterViewMaxHeight;
	private int iCenterViewMaxWidth;

	// indicates if it's first launch - to show hint layer.
	private boolean isFirstLaunch = true;
	
	private static int iScreenType = MainScreen.thiz.getResources().getInteger(R.integer.screen_type);

	public AlmalenceGUI() {
		
		mThumbnail = null;
		topMenuButtons = new Hashtable<Integer, View>();
		topMenuPluginButtons = new Hashtable<String, View>();

		scenemodeAdapter = new ElementAdapter();
		wbmodeAdapter = new ElementAdapter();
		focusmodeAdapter = new ElementAdapter();
		flashmodeAdapter = new ElementAdapter();
		isoAdapter = new ElementAdapter();
		meteringmodeAdapter = new ElementAdapter();

		SceneModeButtons = new Hashtable<Integer, View>();
		WBModeButtons = new Hashtable<Integer, View>();
		FocusModeButtons = new Hashtable<Integer, View>();
		FlashModeButtons = new Hashtable<Integer, View>();
		ISOButtons = new Hashtable<Integer, View>();
		MeteringModeButtons = new Hashtable<Integer, View>();

		activeScene = new ArrayList<View>();
		activeWB = new ArrayList<View>();
		activeFocus = new ArrayList<View>();
		activeFlash = new ArrayList<View>();
		activeISO = new ArrayList<View>();
		activeMetering = new ArrayList<View>();

		activeSceneNames = new ArrayList<Integer>();
		activeWBNames = new ArrayList<Integer>();
		activeFocusNames = new ArrayList<Integer>();
		activeFlashNames = new ArrayList<Integer>();
		activeISONames = new ArrayList<Integer>();
		activeMeteringNames = new ArrayList<Integer>();

		settingsAdapter = new ElementAdapter();
		settingsViews = new ArrayList<View>();

		quickControlAdapter = new ElementAdapter();
		quickControlChangeres = new ArrayList<View>();

		modeAdapter = new ElementAdapter();
		modeViews = new ArrayList<View>();
		buttonModeViewAssoc = new Hashtable<View, String>();
		
		storeAdapter = new ElementAdapter();
		storeViews = new ArrayList<View>();
		buttonStoreViewAssoc = new Hashtable<View, Integer>();
		
		mMeteringMatrix = new Matrix();
	}

	/*
	 * CAMERA PARAMETERS SECTION Supplementary methods for those plugins that
	 * need an icons of supported camera parameters (scene, iso, wb, flash,
	 * focus) Methods return id of drawable icon
	 */
	public int getSceneIcon(int sceneMode)
	{
		if(icons_scene.containsKey(sceneMode))
			return icons_scene.get(sceneMode);
		else
			return -1;
	}

	public int getWBIcon(int wb)
	{
		if(icons_wb.containsKey(wb))
			return icons_wb.get(wb);
		else
			return -1;
	}

	public int getFocusIcon(int focusMode)
	{
		if(icons_focus.containsKey(focusMode))
		{
			try {
				return icons_focus.get(focusMode);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("getFocusIcon", "icons_focus.get exception: " + e.getMessage());
				return -1;
			}
		}
		else
			return -1;
	}

	public int getFlashIcon(int flashMode)
	{
		if(icons_flash.containsKey(flashMode))
			return icons_flash.get(flashMode);
		else
			return -1;
	}

	public int getISOIcon(int isoMode)
	{
		if(icons_iso.containsKey(isoMode))
			return icons_iso.get(isoMode);
		else if(icons_default_iso.containsKey(isoMode))
			return icons_default_iso.get(isoMode);
		else
			return -1;
	}
	
	public int getMeteringIcon(String meteringMode)
	{
		if(icons_metering.containsKey(meteringMode))
			return icons_metering.get(meteringMode);
		else
			return -1;
	}

	@Override
	public float getScreenDensity() {
		return fScreenDensity;
	}

	@Override
	public void onStart() {

		// Calculate right sizes for plugin's controls
		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		fScreenDensity = metrics.density;

		iInfoViewMaxHeight = (int) (MainScreen.mainContext.getResources()
				.getInteger(R.integer.infoControlHeight) * fScreenDensity);
		iInfoViewMaxWidth = (int) (MainScreen.mainContext.getResources()
				.getInteger(R.integer.infoControlWidth) * fScreenDensity);

		iCenterViewMaxHeight = (int) (MainScreen.mainContext.getResources()
				.getInteger(R.integer.centerViewHeight) * fScreenDensity);
		iCenterViewMaxWidth = (int) (MainScreen.mainContext.getResources()
				.getInteger(R.integer.centerViewWidth) * fScreenDensity);

		// set orientation listener to rotate controls
		this.orientListener = new OrientationEventListener(
				MainScreen.mainContext) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (orientation == ORIENTATION_UNKNOWN)
					return;

				final Display display = ((WindowManager) MainScreen.thiz
						.getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay();
				final int orientationProc = (display.getWidth() <= display
						.getHeight()) ? Configuration.ORIENTATION_PORTRAIT
						: Configuration.ORIENTATION_LANDSCAPE;
				final int rotation = display.getRotation();

				boolean remapOrientation = (orientationProc == Configuration.ORIENTATION_LANDSCAPE && rotation == Surface.ROTATION_0)
						|| (orientationProc == Configuration.ORIENTATION_LANDSCAPE && rotation == Surface.ROTATION_180)
						|| (orientationProc == Configuration.ORIENTATION_PORTRAIT && rotation == Surface.ROTATION_90)
						|| (orientationProc == Configuration.ORIENTATION_PORTRAIT && rotation == Surface.ROTATION_270);

				if (remapOrientation)
					orientation = (orientation - 90 + 360) % 360;

				AlmalenceGUI.mDeviceOrientation = Util.roundOrientation(
						orientation, AlmalenceGUI.mDeviceOrientation);

				((RotateImageView) topMenuButtons.get(MODE_EV))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_SCENE))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_WB))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_FOCUS))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_FLASH))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_ISO))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_CAM))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) topMenuButtons.get(MODE_MET))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);

				Set<String> keys = topMenuPluginButtons.keySet();
				Iterator<String> it = keys.iterator();
				while (it.hasNext()) {
					String key = it.next();
					((RotateImageView) topMenuPluginButtons.get(key))
							.setOrientation(AlmalenceGUI.mDeviceOrientation);
				}

				((RotateImageView) guiView.findViewById(R.id.buttonGallery))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) guiView.findViewById(R.id.buttonShutter))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);
				((RotateImageView) guiView.findViewById(R.id.buttonSelectMode))
						.setOrientation(AlmalenceGUI.mDeviceOrientation);

				final int degree = AlmalenceGUI.mDeviceOrientation >= 0 ? AlmalenceGUI.mDeviceOrientation % 360
						: AlmalenceGUI.mDeviceOrientation % 360 + 360;
				if (AlmalenceGUI.mPreviousDeviceOrientation != AlmalenceGUI.mDeviceOrientation)
					rotateSquareViews(degree, 250);

				((TextView) guiView.findViewById(R.id.blockingText))
						.setRotation(-AlmalenceGUI.mDeviceOrientation);
				
				// <!-- -+-
				((RotateImageView) guiView.findViewById(R.id.Unlock))
				.setOrientation(AlmalenceGUI.mDeviceOrientation);
				//-+- -->
				
				AlmalenceGUI.mPreviousDeviceOrientation = AlmalenceGUI.mDeviceOrientation;
				
				PluginManager.getInstance().onOrientationChanged(getDisplayOrientation());
				
				if (timeLapseButton!=null)
				{
					timeLapseButton.setOrientation(AlmalenceGUI.mDeviceOrientation);
					timeLapseButton.invalidate();
					timeLapseButton.requestLayout();
				}
			}
		};

		// create merged image for select mode button
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		String defaultModeName = prefs.getString(MainScreen.sDefaultModeName, "");
		Mode mode = ConfigParser.getInstance().getMode(defaultModeName);
		try{
			Bitmap bm = null;
			Bitmap iconBase = BitmapFactory.decodeResource(
					MainScreen.mainContext.getResources(),
					R.drawable.gui_almalence_select_mode);
			Bitmap iconOverlay = BitmapFactory.decodeResource(
					MainScreen.mainContext.getResources(),
					MainScreen.thiz.getResources().getIdentifier(mode.icon,
							"drawable", MainScreen.thiz.getPackageName()));
			iconOverlay = Bitmap.createScaledBitmap(iconOverlay,
					(int) (iconBase.getWidth() / 1.8),
					(int) (iconBase.getWidth() / 1.8), false);
	
			bm = mergeImage(iconBase, iconOverlay);
			bm = Bitmap.createScaledBitmap(bm, (int) (MainScreen.mainContext
					.getResources().getDimension(R.dimen.paramsLayoutHeight)),
					(int) (MainScreen.mainContext.getResources()
							.getDimension(R.dimen.paramsLayoutHeight)), false);
			((RotateImageView) guiView.findViewById(R.id.buttonSelectMode))
					.setImageBitmap(bm);
		}catch(Exception e){}
		
		// <!-- -+-
		RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		unlock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) 
			{
				if (guiView.findViewById(R.id.postprocessingLayout).getVisibility() == View.VISIBLE)
					return;
				
				if (MainScreen.thiz.titleUnlockAll == null || MainScreen.thiz.titleUnlockAll.endsWith("check for sale"))
				{
					Toast.makeText(MainScreen.mainContext, "Error connecting to Google Play. Check internet connection.", Toast.LENGTH_LONG).show();
					return;
				}
				//start store
				showStore();
			}
		});
		//-+- -->
	}

	@Override
	public void onStop() {
		removePluginViews();
		mDeviceOrientation = 0;
		mPreviousDeviceOrientation = 0;
	}

	@Override
	public void onPause() {
		if (quickControlsChangeVisible)
			closeQuickControlsSettings();
		orientListener.disable();
		if (modeSelectorVisible)
			hideModeList();
		if (settingsControlsVisible)
			((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false,true);
		if (((RelativeLayout) guiView.findViewById(R.id.viewPagerLayout)).getVisibility() == View.VISIBLE)
			hideStore();
	}

	//<!-- -+-
	@Override
	public void showStore()
	{
		LayoutInflater inflater = LayoutInflater.from(MainScreen.thiz);
        List<RelativeLayout> pages = new ArrayList<RelativeLayout>();
        
        //page 1
        RelativeLayout page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
		initStoreList();
		RelativeLayout store = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_store, null);
		final ImageView imgStoreNext = (ImageView) store.findViewById(R.id.storeWhatsNew);
		GridView gridview = (GridView) store.findViewById(R.id.storeGrid);
		gridview.setAdapter(storeAdapter);
		
		gridview.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
			
		});
		page.addView(store);
        pages.add(page);
        
        //page 2
        page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
        RelativeLayout whatsnew = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_whatsnew, null);
        final ImageView imgWhatNewNext = (ImageView) whatsnew.findViewById(R.id.storeFeatures);
        final ImageView imgWhatNewPrev = (ImageView) whatsnew.findViewById(R.id.storeStore);
        imgWhatNewNext.setVisibility(View.INVISIBLE);
		imgWhatNewPrev.setVisibility(View.INVISIBLE);
        TextView text_whatsnew = (TextView) whatsnew.findViewById(R.id.text_whatsnew);
		text_whatsnew.setText("version 3.24"+
							  "\n- exif tags fixed"+
							  "\n- auto backup/sharing fixed"+
							  "\n- fixed work from 3rd party apps"+
							  "\n- UI corrections"+
							  "\n- video on some devices improved"+
							  "\n- stability fixes");

		page.addView(whatsnew);
        pages.add(page);
        
        //page 3
        page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
        RelativeLayout features = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_features, null);
        final ImageView imgFeaturesNext = (ImageView) features.findViewById(R.id.storeTips);
        final ImageView imgFeaturesPrev = (ImageView) features.findViewById(R.id.storeWhatsNew);
		TextView text_features= (TextView) features.findViewById(R.id.text_features);
		text_features.setText("BLA BLA BLA BLA");

		page.addView(features);
        pages.add(page);
        
        //page 4
        page = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_pager_fragment, null);
        RelativeLayout tips = (RelativeLayout) inflater.inflate(R.layout.gui_almalence_tips, null);
        final ImageView imgTipsPrev = (ImageView) tips.findViewById(R.id.storeTips);
		TextView text_tips = (TextView) tips.findViewById(R.id.text_tips);
		text_tips.setText("ABC tips and tricks"+
						  "\n\nIf you long press on any of the quick settings on the top bar you get a dropdown list that lets you select and change them to any setting you'd like."+
						  "\n\nSwipe left/write on main scree to see more/less info on the screen - histogram, grids, info controls, top quick settings menu."+
						  "\n\nPull down top menu to see all quick settings.");

		page.addView(tips);
        pages.add(page);
        
        SamplePagerAdapter pagerAdapter = new SamplePagerAdapter(pages);
        ViewPager viewPager = new ViewPager(MainScreen.thiz);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
        	@Override
            public void onPageSelected(int position) 
        	{
        		switch (position)
        		{
        		case 0:
        			//0
        			imgStoreNext.setVisibility(View.VISIBLE);
        			//1
        			imgWhatNewNext.setVisibility(View.INVISIBLE);
        			imgWhatNewPrev.setVisibility(View.INVISIBLE);
        			break;
        		case 1:
        			//0
        			imgStoreNext.setVisibility(View.INVISIBLE);
        			//1
        			imgWhatNewNext.setVisibility(View.VISIBLE);
        			imgWhatNewPrev.setVisibility(View.VISIBLE);
        			//2
        			imgFeaturesNext.setVisibility(View.INVISIBLE);
        			imgFeaturesPrev.setVisibility(View.INVISIBLE);
        			break;
        		case 2:
        			//1
        			imgWhatNewNext.setVisibility(View.INVISIBLE);
        			imgWhatNewPrev.setVisibility(View.INVISIBLE);
        			//2
        			imgFeaturesNext.setVisibility(View.VISIBLE);
        			imgFeaturesPrev.setVisibility(View.VISIBLE);
        			//3
        			imgTipsPrev.setVisibility(View.INVISIBLE);
        			break;
        		case 3:
        			//2
        			imgFeaturesNext.setVisibility(View.INVISIBLE);
        			imgFeaturesPrev.setVisibility(View.INVISIBLE);
        			//3
        			imgTipsPrev.setVisibility(View.VISIBLE);
        			break;
        		}
            }
        });
		
		guiView.findViewById(R.id.buttonGallery).setEnabled(false);
		guiView.findViewById(R.id.buttonShutter).setEnabled(false);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(false);
		
		Message msg2 = new Message();
		msg2.arg1 = PluginManager.MSG_CONTROL_LOCKED;
		msg2.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg2);
		
		MainScreen.guiManager.lockControls = true;
		
		if (MainScreen.thiz.showPromoRedeemed == true)
		{
			Toast.makeText(MainScreen.thiz, "The promo code has been successfully redeemed. All PRO-Features are unlocked", Toast.LENGTH_LONG).show();
			MainScreen.thiz.showPromoRedeemed = false;
		}
		
		final RelativeLayout pagerLayout = ((RelativeLayout) guiView.findViewById(R.id.viewPagerLayout));
		pagerLayout.addView(viewPager);
		pagerLayout.setVisibility(View.VISIBLE);
		pagerLayout.bringToFront();
	}
	
	@Override
	public void hideStore()
	{
		((RelativeLayout) guiView.findViewById(R.id.viewPagerLayout)).setVisibility(View.INVISIBLE);
		
		guiView.findViewById(R.id.buttonGallery).setEnabled(true);
		guiView.findViewById(R.id.buttonShutter).setEnabled(true);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(true);
		
		Message msg2 = new Message();
		msg2.arg1 = PluginManager.MSG_CONTROL_UNLOCKED;
		msg2.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg2);
		
		MainScreen.guiManager.lockControls = false;
	}
	
	private void initStoreList() {
		storeViews.clear();
		buttonStoreViewAssoc.clear();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		boolean bOnSale = prefs.getBoolean("bOnSale", false);
		
		for (int i =0; i<6; i++) {

			LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
			View item = inflator.inflate(
					R.layout.gui_almalence_store_grid_element, null,
					false);
			ImageView icon = (ImageView) item.findViewById(R.id.storeImage);
			TextView description = (TextView) item.findViewById(R.id.storeText);
			TextView price = (TextView) item.findViewById(R.id.storePriceText);
			switch (i)
			{
				case 0:
					// unlock all
					icon.setImageResource(R.drawable.store_all);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_All_Preference_Title));
					
					if(MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
					{
						if (MainScreen.thiz.isCouponSale())
						{
							price.setText(MainScreen.thiz.titleUnlockAllCoupon);
							((ImageView) item.findViewById(R.id.storeSaleImage)).setVisibility(View.VISIBLE);
						}
						else
						{
							price.setText(MainScreen.thiz.titleUnlockAll);
							if (bOnSale)
								((ImageView) item.findViewById(R.id.storeSaleImage)).setVisibility(View.VISIBLE);
						}
					}
					break;
				case 1:
					// HDR
					icon.setImageResource(R.drawable.store_hdr);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_HDR_Preference_Title));
					if(MainScreen.thiz.isPurchasedHDR() || MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
						price.setText(MainScreen.thiz.titleUnlockHDR);
					break;
				case 2:
					// Panorama
					icon.setImageResource(R.drawable.store_panorama);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_Panorama_Preference_Title));
					if(MainScreen.thiz.isPurchasedPanorama() || MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
						price.setText(MainScreen.thiz.titleUnlockPano);
					break;
				case 3:
					// Moving
					icon.setImageResource(R.drawable.store_moving);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_Moving_Preference_Title));
					if(MainScreen.thiz.isPurchasedMoving() || MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
						price.setText(MainScreen.thiz.titleUnlockMoving);
					break;
				case 4:
					// Groupshot
					icon.setImageResource(R.drawable.store_groupshot);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_Groupshot_Preference_Title));
					if(MainScreen.thiz.isPurchasedGroupshot() || MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
						price.setText(MainScreen.thiz.titleUnlockGroup);
					break;
				case 5:
					// Promo code
					icon.setImageResource(R.drawable.store_promo);
					description.setText(MainScreen.thiz.getResources().getString(R.string.Pref_Upgrde_PromoCode_Preference_Title));
					if(MainScreen.thiz.isPurchasedAll())
						price.setText(R.string.already_unlocked);
					else
						price.setText("");
					break;
			}

			item.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
						purchasePressed(v);
					return false;
				}
			});
			
			item.setOnClickListener(new OnClickListener() 
			{
				public void onClick(View v) {
					// get inapp associated with pressed button
					purchasePressed(v);
				}
			});
			
			buttonStoreViewAssoc.put(item, i);
			storeViews.add(item);
		}

		storeAdapter.Elements = storeViews;
	}
	
	private void purchasePressed(View v)
	{
		// get inapp associated with pressed button
		Integer id = buttonStoreViewAssoc.get(v);
		switch (id)
		{
		case 0:// unlock all
			MainScreen.thiz.purchaseAll();
			break;
		case 1:// HDR
			MainScreen.thiz.purchaseHDR();
			break;
		case 2:// Panorama
			MainScreen.thiz.purchasePanorama();
			break;
		case 3:// Moving
			MainScreen.thiz.purchaseMoving();
			break;
		case 4:// Groupshot
			MainScreen.thiz.purchaseGroupshot();
			break;
		case 5:// Promo
			MainScreen.thiz.enterPromo();
			break;
		}
	}
	
	private void showWhatsNew()
	{
		
	}
	
	public void ShowUnlockControl()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		boolean bOnSale = prefs.getBoolean("bOnSale", false);
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		unlock.setImageResource(bOnSale?R.drawable.unlock_sale:R.drawable.unlock);
		unlock.setAlpha(1.0f);
		unlock.setVisibility(View.VISIBLE);
		
		Animation invisible_alpha = new AlphaAnimation(1, 0.4f);
		invisible_alpha.setDuration(7000);
		invisible_alpha.setRepeatCount(0);

		invisible_alpha.setAnimationListener(new AnimationListener() 
		{
			@Override
			public void onAnimationEnd(Animation animation) {
				unlock.clearAnimation();
				unlock.setImageResource(R.drawable.unlock_gray);
				unlock.setAlpha(0.4f);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});

		unlock.startAnimation(invisible_alpha);
	}
	
	
	public void ShowGrayUnlockControl()
	{
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));
		if (unlock.getVisibility() == View.VISIBLE)
			return;
		unlock.setImageResource(R.drawable.unlock_gray);
		unlock.setAlpha(0.4f);
		unlock.setVisibility(View.VISIBLE);
	}
	
	public void HideUnlockControl()
	{
		final RotateImageView unlock = ((RotateImageView) guiView.findViewById(R.id.Unlock));		
		unlock.setVisibility(View.GONE);
	}
	
	//-+- -->
	
	@Override
	public void onResume() {
		MainScreen.thiz.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlmalenceGUI.this.updateThumbnailButton();
			}
		});
		setShutterIcon(ShutterButton.DEFAULT);

		lockControls = false;

		orientListener.enable();

		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_EV, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_SCENE, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_WB, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_FOCUS, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_FLASH, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_ISO, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_METERING, false, false);
		disableCameraParameter(CameraParameter.CAMERA_PARAMETER_CAMERACHANGE, false, true);

		// if first launch - show layout with hints
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		if (true == prefs.contains("isFirstLaunch")) {
			isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);
		} else {
			Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean("isFirstLaunch", false);
			prefsEditor.commit();
			isFirstLaunch = true;
		}

		// show/hide hints
		if (!isFirstLaunch)
			guiView.findViewById(R.id.hintLayout).setVisibility(View.GONE);
		else
			guiView.findViewById(R.id.hintLayout).setVisibility(View.VISIBLE);
		
		manageUnlockControl();
	}
	
	
	@Override
	public void onDestroy() {

	}

	@Override
	public void createInitialGUI() {
		guiView = LayoutInflater.from(MainScreen.mainContext).inflate(
				R.layout.gui_almalence_layout, null);
		// Add GUI Layout to main layout of OpenCamera
		((RelativeLayout) MainScreen.thiz.findViewById(R.id.mainLayout1))
				.addView(guiView);
	}

	// Create standard OpenCamera's buttons and theirs OnClickListener
	@Override
	public void onCreate() {
		// Get application preferences object
		preferences = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);

		guiView.findViewById(R.id.evButton).setOnTouchListener(MainScreen.thiz);
		guiView.findViewById(R.id.sceneButton).setOnTouchListener(
				MainScreen.thiz);
		guiView.findViewById(R.id.wbButton).setOnTouchListener(MainScreen.thiz);
		guiView.findViewById(R.id.focusButton).setOnTouchListener(
				MainScreen.thiz);
		guiView.findViewById(R.id.flashButton).setOnTouchListener(
				MainScreen.thiz);
		guiView.findViewById(R.id.isoButton)
				.setOnTouchListener(MainScreen.thiz);
		guiView.findViewById(R.id.meteringButton)
				.setOnTouchListener(MainScreen.thiz);
		guiView.findViewById(R.id.camerachangeButton).setOnTouchListener(
				MainScreen.thiz);

		// Long clicks are needed to open quick controls customization layout
		guiView.findViewById(R.id.evButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.sceneButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.wbButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.focusButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.flashButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.isoButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.meteringButton).setOnLongClickListener(this);
		guiView.findViewById(R.id.camerachangeButton).setOnLongClickListener(
				this);

		// Get all top menu buttons
		topMenuButtons.put(MODE_EV, guiView.findViewById(R.id.evButton));
		topMenuButtons.put(MODE_SCENE, guiView.findViewById(R.id.sceneButton));
		topMenuButtons.put(MODE_WB, guiView.findViewById(R.id.wbButton));
		topMenuButtons.put(MODE_FOCUS, guiView.findViewById(R.id.focusButton));
		topMenuButtons.put(MODE_FLASH, guiView.findViewById(R.id.flashButton));
		topMenuButtons.put(MODE_ISO, guiView.findViewById(R.id.isoButton));
		topMenuButtons.put(MODE_MET, guiView.findViewById(R.id.meteringButton));
		topMenuButtons.put(MODE_CAM, guiView.findViewById(R.id.camerachangeButton));

		SceneModeButtons = initCameraParameterModeButtons(icons_scene,
				names_scene, SceneModeButtons, MODE_SCENE);
		WBModeButtons = initCameraParameterModeButtons(icons_wb, names_wb,
				WBModeButtons, MODE_WB);
		FocusModeButtons = initCameraParameterModeButtons(icons_focus,
				names_focus, FocusModeButtons, MODE_FOCUS);
		FlashModeButtons = initCameraParameterModeButtons(icons_flash,
				names_flash, FlashModeButtons, MODE_FLASH);
		ISOButtons = initCameraParameterModeButtons(icons_iso, names_iso,
				ISOButtons, MODE_ISO);
		MeteringModeButtons = initCameraParameterModeButtons(icons_metering, names_metering,
				MeteringModeButtons, MODE_MET);

		// Create top menu buttons for plugins (each plugin may have only one
		// top menu button)
		createPluginTopMenuButtons();

		thumbnailView = (RotateImageView) guiView
				.findViewById(R.id.buttonGallery);

		((RelativeLayout) MainScreen.thiz.findViewById(R.id.mainLayout1))
				.setOnTouchListener(MainScreen.thiz);
		((LinearLayout) MainScreen.thiz.findViewById(R.id.evLayout))
				.setOnTouchListener(MainScreen.thiz);
		
		shutterButton = ((RotateImageView) guiView.findViewById(R.id.buttonShutter));
		shutterButton.setOnLongClickListener(this);
		
		manageUnlockControl();
	}

	
	private void manageUnlockControl()
	{
		// <!-- -+-
		//manage unlock control
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		if (true == prefs.getBoolean("unlock_all_forever", false))
			HideUnlockControl();
		else 
		{
			String modeID = PluginManager.getInstance().getActiveMode().modeID;
			visibilityUnlockControl(unlockModePreference.get(modeID));
		}
		//-+- -->
	}
	
	private void visibilityUnlockControl(String prefName)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		
		if (prefs.getBoolean(prefName, false))
			HideUnlockControl();
		else
			ShowUnlockControl();
	}
	
	private Map<Integer, View> initCameraParameterModeButtons(
			Map<Integer, Integer> icons_map, Map<Integer, String> names_map,
			Map<Integer, View> paramMap, final int mode) {
		paramMap.clear();
		Set<Integer> keys = icons_map.keySet();		
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			final int system_name = it.next();
			final String value_name = names_map.get(system_name);
			LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
			View paramMode = inflator.inflate(
					R.layout.gui_almalence_quick_control_grid_element, null,
					false);
			// set some mode icon
			((ImageView) paramMode.findViewById(R.id.imageView))
					.setImageResource(icons_map.get(system_name));
			((TextView) paramMode.findViewById(R.id.textView))
					.setText(value_name);
			
			//final boolean isFirstMode = mode_number == 0? true : false;
			if (system_name == CameraParameters.AF_MODE_AUTO)
			paramMode.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
					{
						settingsModeClicked(mode, system_name);
						return false;
					}
					return false;
				}					
			});

			paramMode.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					settingsModeClicked(mode, system_name);
					guiView.findViewById(R.id.topPanel).setVisibility(
							View.VISIBLE);
					quickControlsVisible = false;
				}
			});

			paramMap.put(system_name, paramMode);
		}

		return paramMap;
	}

	private void settingsModeClicked(int mode, int system_name)
	{
		switch (mode) {
		case MODE_SCENE:
			setSceneMode(system_name);
			break;
		case MODE_WB:
			setWhiteBalance(system_name);
			break;
		case MODE_FOCUS:
			setFocusMode(system_name);
			break;
		case MODE_FLASH:
			setFlashMode(system_name);
			break;
		case MODE_ISO:
			setISO(system_name);
			break;
		case MODE_MET:
			setMeteringMode(system_name);
			break;
		}
	}
	
	@Override
	public void onGUICreate() {
		if (MainScreen.thiz.findViewById(R.id.infoLayout).getVisibility() == View.VISIBLE)
			iInfoViewHeight = MainScreen.thiz.findViewById(R.id.infoLayout)
					.getHeight();
		// Recreate plugin views
		removePluginViews();
		createPluginViews();
		
		//add self-timer control
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		boolean showDelayedCapturePrefCommon = prefs.getBoolean(MainScreen.sShowDelayedCapturePref, false);
		AddSelfTimerControl(showDelayedCapturePrefCommon);

		LinearLayout infoLayout = (LinearLayout) guiView
				.findViewById(R.id.infoLayout);
		RelativeLayout.LayoutParams infoParams = (RelativeLayout.LayoutParams) infoLayout
				.getLayoutParams();
		if (infoParams != null) {
			int width = infoParams.width;
			if (infoLayout.getChildCount() == 0)
				infoParams.rightMargin = -width;
			else
				infoParams.rightMargin = 0;
			infoLayout.setLayoutParams(infoParams);
			infoLayout.requestLayout();
		}
		
		infoSet = prefs.getInt(MainScreen.sDefaultInfoSetPref, INFO_PARAMS);
		if (infoSet == INFO_PARAMS && !isAnyViewOnViewfinder()) {
			infoSet = INFO_ALL;
			prefs.edit().putInt(MainScreen.sDefaultInfoSetPref, infoSet).commit();
		}
		setInfo(false, 0, 0, false);

		MainScreen.thiz.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlmalenceGUI.this.updateThumbnailButton();
			}
		});

		final View postProcessingLayout = guiView
				.findViewById(R.id.postprocessingLayout);
		final View topPanel = guiView.findViewById(R.id.topPanel);
		final View mainButtons = guiView.findViewById(R.id.mainButtons);
		final View qcLayout = guiView.findViewById(R.id.qcLayout);
		final View buttonsLayout = guiView.findViewById(R.id.buttonsLayout);
		final View hintLayout = guiView.findViewById(R.id.hintLayout);

		mainButtons.bringToFront();
		qcLayout.bringToFront();
		buttonsLayout.bringToFront();
		topPanel.bringToFront();
		postProcessingLayout.bringToFront();
		hintLayout.bringToFront();

		View help = guiView.findViewById(R.id.mode_help);
		help.bringToFront();
	}
	
	@Override
	public void setupViewfinderPreviewSize(CameraController.Size previewSize)
	{
		float cameraAspect = (float) previewSize.getWidth() / previewSize.getHeight();

		RelativeLayout ll = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.mainLayout1);

		int previewSurfaceWidth = ll.getWidth();
		int previewSurfaceHeight = ll.getHeight();
		float surfaceAspect = (float) previewSurfaceHeight
				/ previewSurfaceWidth;

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int screen_height = metrics.heightPixels;

		lp.width = previewSurfaceWidth;
		lp.height = previewSurfaceHeight;
		if (Math.abs(surfaceAspect - cameraAspect) > 0.05d) {
			if (surfaceAspect > cameraAspect
					&& (Math.abs(1 - cameraAspect) > 0.05d)) {
				int paramsLayoutHeight = (int) MainScreen.thiz.getResources()
						.getDimension(R.dimen.paramsLayoutHeight);
				// if wide-screen - decrease width of surface
				lp.width = previewSurfaceWidth;

				lp.height = (int) (screen_height - 2 * paramsLayoutHeight);
				lp.topMargin = (int) (paramsLayoutHeight);
			} else if (surfaceAspect > cameraAspect) {
				int paramsLayoutHeight = (int) MainScreen.thiz.getResources()
						.getDimension(R.dimen.paramsLayoutHeight);
				// if wide-screen - decrease width of surface
				lp.width = previewSurfaceWidth;

				lp.height = previewSurfaceWidth;
				lp.topMargin = (int) (paramsLayoutHeight);
			}
		}

		MainScreen.thiz.preview.setLayoutParams(lp);
		guiView.findViewById(R.id.fullscreenLayout).setLayoutParams(lp);
		guiView.findViewById(R.id.specialPluginsLayout).setLayoutParams(lp);

	}

	/*
	 * Each plugin may have only one top menu button Icon id and Title (plugin's
	 * members) is use to make design of button
	 */
	public void createPluginTopMenuButtons() {
		topMenuPluginButtons.clear();

		createPluginTopMenuButtons(PluginManager.getInstance()
				.getActivePlugins(PluginType.ViewFinder));
		createPluginTopMenuButtons(PluginManager.getInstance()
				.getActivePlugins(PluginType.Capture));
		createPluginTopMenuButtons(PluginManager.getInstance()
				.getActivePlugins(PluginType.Processing));
		createPluginTopMenuButtons(PluginManager.getInstance()
				.getActivePlugins(PluginType.Filter));
		createPluginTopMenuButtons(PluginManager.getInstance()
				.getActivePlugins(PluginType.Export));
	}

	public void createPluginTopMenuButtons(List<Plugin> Plugins) {
		if (Plugins.size() > 0) {
			for (int i = 0; i < Plugins.size(); i++) {
				Plugin Plugin = Plugins.get(i);

				if (Plugin == null || Plugin.getQuickControlIconID() <= 0)
					continue;

				LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
				ImageView qcView = (ImageView) inflator.inflate(
						R.layout.gui_almalence_quick_control_button,
						(ViewGroup) guiView.findViewById(R.id.paramsLayout),
						false);

				qcView.setOnTouchListener(MainScreen.thiz);
				qcView.setOnClickListener(this);
				qcView.setOnLongClickListener(this);

				topMenuPluginButtons.put(Plugin.getID(), qcView);
			}
		}
	}

	// onGUICreate called when main layout is rendered and size's variables is
	// available
	// @Override
	public void createPluginViews() 
	{
		createPluginViews(PluginType.ViewFinder);
		createPluginViews(PluginType.Capture);
		createPluginViews(PluginType.Processing);
		createPluginViews(PluginType.Filter);
		createPluginViews(PluginType.Export);
	}

	private void createPluginViews(PluginType type)
	{
		int iInfoControlsRemainingHeight = iInfoViewHeight;

		List<View> info_views = null;
		Map<View, Plugin.ViewfinderZone> plugin_views = null;
		
		List<Plugin> plugins = PluginManager.getInstance().getActivePlugins(
				type);
		if (plugins.size() > 0) {
			for (int i = 0; i < plugins.size(); i++) {
				Plugin _plugin = plugins.get(i);
				if (_plugin != null) {
					plugin_views = _plugin.getPluginViews();
					addPluginViews(plugin_views);

					// Add info controls
					info_views = _plugin.getInfoViews();
					for (int j = 0; j < info_views.size(); j++) {
						View infoView = info_views.get(j);

						// Calculate appropriate size of added plugin's view
						android.widget.LinearLayout.LayoutParams viewLayoutParams = (android.widget.LinearLayout.LayoutParams) infoView
								.getLayoutParams();
						viewLayoutParams = this.getTunedLinearLayoutParams(
								infoView, viewLayoutParams, iInfoViewMaxWidth,
								iInfoViewMaxHeight);

						if (iInfoControlsRemainingHeight >= viewLayoutParams.height) {
							iInfoControlsRemainingHeight -= viewLayoutParams.height;
							this.addInfoView(infoView, viewLayoutParams);
						}
					}
				}
			}
		}
	}
	
	//
	private void initDefaultQuickControls() {
		initDefaultQuickControls(quickControl1);
		initDefaultQuickControls(quickControl2);
		initDefaultQuickControls(quickControl3);
		initDefaultQuickControls(quickControl4);
	}

	private void initDefaultQuickControls(View quickControl) 
	{
		LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
		quickControl = inflator.inflate(
				R.layout.gui_almalence_invisible_button,
				(ViewGroup) guiView.findViewById(R.id.paramsLayout), false);
		quickControl.setOnLongClickListener(this);
		quickControl.setOnClickListener(this);
	}
	
	// Called when camera object created in MainScreen.
	// After camera creation it is possibly to obtain
	// all camera possibilities such as supported scene mode, flash mode and
	// etc.
	@Override
	public void onCameraCreate() {

		String defaultQuickControl1 = "";
		String defaultQuickControl2 = "";
		String defaultQuickControl3 = "";
		String defaultQuickControl4 = "";

		// Remove buttons from previous camera start
		removeAllViews(topMenuButtons);
		removeAllViews(topMenuPluginButtons);

		mEVSupported = false;
		mSceneModeSupported = false;
		mWBSupported = false;
		mFocusModeSupported = false;
		mFlashModeSupported = false;
		mISOSupported = false;
		mMeteringAreasSupported = false;
		mCameraChangeSupported = false;
		
		mEVLockSupported = false;
		mWBLockSupported = false;

		activeScene.clear();
		activeWB.clear();
		activeFocus.clear();
		activeFlash.clear();
		activeISO.clear();
		activeMetering.clear();

		activeSceneNames.clear();
		activeWBNames.clear();
		activeFocusNames.clear();
		activeFlashNames.clear();
		activeISONames.clear();
		activeMeteringNames.clear();

		removeAllQuickViews();
		initDefaultQuickControls();
		
		//ViewParent p = quickControl3.getParent();

		createPluginTopMenuButtons();
		
		if(CameraController.getInstance().isExposureLockSupported())
			mEVLockSupported = true;
		if(CameraController.getInstance().isWhiteBalanceLockSupported())
			mWBLockSupported = true;
		
		// Create Exposure compensation button and slider with supported values
		if (CameraController.getInstance().isExposureCompensationSupported()) {
			mEVSupported = true;
			defaultQuickControl1 = String.valueOf(MODE_EV);

			float ev_step = CameraController.getInstance().getExposureCompensationStep();

			int minValue = CameraController.getInstance().getMinExposureCompensation();
			int maxValue = CameraController.getInstance().getMaxExposureCompensation();

			SeekBar evBar = (SeekBar) guiView.findViewById(R.id.evSeekBar);
			if (evBar != null) {
				int initValue = preferences.getInt(MainScreen.sEvPref, 0);
				evBar.setMax(maxValue - minValue);
				evBar.setProgress(initValue + maxValue);

				TextView leftText = (TextView) guiView
						.findViewById(R.id.seekBarLeftText);
				TextView rightText = (TextView) guiView
						.findViewById(R.id.seekBarRightText);

				int minValueReal = Math.round(minValue * ev_step);
				int maxValueReal = Math.round(maxValue * ev_step);
				String minString = String.valueOf(minValueReal);
				String maxString = String.valueOf(maxValueReal);

				if (minValueReal > 0)
					minString = "+" + minString;
				if (maxValueReal > 0)
					maxString = "+" + maxString;

				leftText.setText(minString);
				rightText.setText(maxString);

				mEV = initValue;
				CameraController.getInstance().setCameraExposureCompensation(mEV);

				evBar.setOnSeekBarChangeListener(this);
			}

			RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_EV);
			but.setImageResource(icon_ev);
		}
		else
			mEVSupported = false;

		// Create Scene mode button and adding supported scene modes
		byte[] supported_scene = CameraController.getInstance().getSupportedSceneModes();
		if (supported_scene != null && supported_scene.length > 0 && activeScene != null)
		{
			for(byte scene_name : supported_scene)
			{
				if(scene_name != CameraParameters.SCENE_MODE_NIGHT)
					activeScene.add(SceneModeButtons.get(Integer.valueOf(scene_name)));
				activeSceneNames.add(Integer.valueOf(scene_name));
			}
			
			if(activeSceneNames.size() > 0)
			{
				mSceneModeSupported = true;
				scenemodeAdapter.Elements = activeScene;
				GridView gridview = (GridView) guiView
						.findViewById(R.id.scenemodeGrid);
				gridview.setAdapter(scenemodeAdapter);
				
				int initValue = preferences.getInt(MainScreen.sSceneModePref,
						MainScreen.sDefaultValue);
				if (!activeSceneNames.contains(initValue)) {
					if (CameraController.isFrontCamera())
						initValue = activeSceneNames.get(0);
					else
						initValue = CameraParameters.SCENE_MODE_AUTO;
				}
	
				setButtonSelected(SceneModeButtons, initValue);
				setCameraParameterValue(MODE_SCENE, initValue);
	
				if (icons_scene!=null && icons_scene.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons
						.get(MODE_SCENE);
					int icon_id = icons_scene.get(initValue);
					but.setImageResource(icon_id);
				}
	
				CameraController.getInstance().setCameraSceneMode(mSceneMode);
			}
			else
			{
				mSceneModeSupported = false;
				mSceneMode = -1;
			}
		} 
		else
		{
			mSceneModeSupported = false;
			mSceneMode = -1;
		}

		// Create White Balance mode button and adding supported white balances
		byte[] supported_wb = CameraController.getInstance().getSupportedWhiteBalance();
		if (supported_wb != null && supported_wb.length > 0 && activeWB != null)
		{
			for(byte wb_name : supported_wb)
			{				
				activeWB.add(WBModeButtons.get(Integer.valueOf(wb_name)));
				activeWBNames.add(Integer.valueOf(wb_name));
			}
			
			if(activeWBNames.size() > 0)
			{
				mWBSupported = true;
				
				wbmodeAdapter.Elements = activeWB;
				GridView gridview = (GridView) guiView.findViewById(R.id.wbGrid);
				gridview.setAdapter(wbmodeAdapter);
	
				int initValue = preferences.getInt(MainScreen.sWBModePref, MainScreen.sDefaultValue);
				if (!activeWBNames.contains(initValue))
				{
					if (CameraController.isFrontCamera())
						initValue = activeWBNames.get(0);
					else
						initValue = MainScreen.sDefaultValue;
				}
				setButtonSelected(WBModeButtons, initValue);
				setCameraParameterValue(MODE_WB, initValue);
	
				if (icons_wb!=null && icons_wb.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_WB);
					int icon_id = icons_wb.get(initValue);
					but.setImageResource(icon_id);
				}
	
				CameraController.getInstance().setCameraWhiteBalance(mWB);
			}
			else
			{
				mWBSupported = false;
				mWB = -1;
			}
		}
		else
		{
			mWBSupported = false;
			mWB = -1;
		}

		// Create Focus mode button and adding supported focus modes
		final byte[] supported_focus = CameraController.getInstance().getSupportedFocusModes();
		if (supported_focus != null && supported_focus.length > 0 && activeFocus != null)
		{
			for(byte focus_name : supported_focus)
			{				
				activeFocus.add(FocusModeButtons.get(Integer.valueOf(focus_name)));
				activeFocusNames.add(Integer.valueOf(focus_name));
			}
			
			if(activeFocusNames.size() > 0)
			{
				mFocusModeSupported = true;
				defaultQuickControl3 = String.valueOf(MODE_FOCUS);
				
				if(CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_AUTO) || 
				   CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_MACRO))
				{
					LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
					View paramMode = inflator.inflate(
							R.layout.gui_almalence_quick_control_grid_element, null,
							false);

					String aflock_name = MainScreen.thiz.getResources().getString(R.string.focusAFLock);
					((ImageView) paramMode.findViewById(R.id.imageView))
							.setImageResource(R.drawable.gui_almalence_settings_focus_aflock);
					((TextView) paramMode.findViewById(R.id.textView))
							.setText(aflock_name);
					
					paramMode.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v)
						{
							MainScreen.setAutoFocusLock(true);
							try {
								RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_FOCUS);								
								but.setImageResource(R.drawable.gui_almalence_settings_focus_aflock);
							} catch (Exception e) {
								e.printStackTrace();
								Log.e("set AF-L failed", "icons_focus.get exception: " + e.getMessage());
							}
							
							mFocusMode = focusAfLock;
							
							int AFMode = -1;
							if(CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_AUTO))
								AFMode = CameraParameters.AF_MODE_AUTO;
							else if(CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_MACRO))
								AFMode = CameraParameters.AF_MODE_MACRO;
							else
								AFMode = supported_focus[0];
							
//							CameraController.getInstance().setCameraFocusMode(AFMode);
							
							preferences.edit().putInt(CameraController.isFrontCamera() ? MainScreen.sRearFocusModePref : MainScreen.sFrontFocusModePref, AFMode).commit();
							
							Message msg = new Message();
							msg.arg1 = PluginManager.MSG_FOCUS_CHANGED;
							msg.what = PluginManager.MSG_BROADCAST;
							MainScreen.H.sendMessage(msg);
							
							initSettingsMenu();
							hideSecondaryMenus();
							unselectPrimaryTopMenuButtons(-1);
							
							guiView.findViewById(R.id.topPanel).setVisibility(
									View.VISIBLE);
							quickControlsVisible = false;
						}						
					});
					
					FocusModeButtons.put(focusAfLock, paramMode);
					activeFocus.add(FocusModeButtons.get(focusAfLock));
					activeFocusNames.add(focusAfLock);
				}

				focusmodeAdapter.Elements = activeFocus;
				GridView gridview = (GridView) guiView
						.findViewById(R.id.focusmodeGrid);
				gridview.setAdapter(focusmodeAdapter);
	
				int initValue = preferences.getInt(CameraController.isFrontCamera() ? MainScreen.sRearFocusModePref
						: MainScreen.sFrontFocusModePref, MainScreen.sDefaultFocusValue);
				if (!activeFocusNames.contains(initValue)) {
					if (activeFocusNames.contains(MainScreen.sDefaultValue))
						initValue = MainScreen.sDefaultValue;
					else
						initValue = activeFocusNames.get(0);
				}
				
				setButtonSelected(FocusModeButtons, initValue);
				setCameraParameterValue(MODE_FOCUS, initValue);
	
				if (icons_focus!=null && icons_focus.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons
							.get(MODE_FOCUS);
					try {
						int icon_id = icons_focus.get(initValue);
						but.setImageResource(icon_id);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("onCameraCreate", "icons_focus.get exception: " + e.getMessage());
					}
				}
				
				if(mFocusMode == focusAfLock)
				{
					int AFMode = -1;
					if(CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_AUTO))
						AFMode = CameraParameters.AF_MODE_AUTO;
					else if(CameraController.isModeAvailable(supported_focus, CameraParameters.AF_MODE_MACRO))
						AFMode = CameraParameters.AF_MODE_MACRO;
					else
						AFMode = supported_focus[0];
					
					CameraController.getInstance().setCameraFocusMode(AFMode);
				}
				else					
					CameraController.getInstance().setCameraFocusMode(mFocusMode);
			}
			else
			{
				mFocusModeSupported = false;
				mFocusMode = -1;
			}
		}
		else
		{
			mFocusMode = -1;
			mFocusModeSupported = false;
		}

		// Create Flash mode button and adding supported flash modes
		byte[] supported_flash = CameraController.getInstance().getSupportedFlashModes();
		if (supported_flash != null && supported_flash.length > 0 && activeFlash != null &&
				!(supported_flash.length == 1 && CameraController.isModeAvailable(supported_flash, CameraParameters.FLASH_MODE_OFF)))
		{			
			for(byte flash_name : supported_flash)
			{				
				activeFlash.add(FlashModeButtons.get(Integer.valueOf(flash_name)));
				activeFlashNames.add(Integer.valueOf(flash_name));
			}
			
			if(activeFlashNames.size() > 0)
			{
				mFlashModeSupported = true;
				defaultQuickControl2 = String.valueOf(MODE_FLASH);

				flashmodeAdapter.Elements = activeFlash;
				GridView gridview = (GridView) guiView
						.findViewById(R.id.flashmodeGrid);
				//gridview.setNumColumns(activeFlash.size() > 9 ? 4 : 3);
				gridview.setAdapter(flashmodeAdapter);
	
				int initValue = preferences.getInt(MainScreen.sFlashModePref,
						MainScreen.sDefaultFlashValue);
				if (!activeFlashNames.contains(initValue)) {
					if (CameraController.getInstance().isFrontCamera())
						initValue = activeFlashNames.get(0);
					else
						initValue = MainScreen.sDefaultFlashValue;
				}
				setButtonSelected(FlashModeButtons, initValue);
				setCameraParameterValue(MODE_FLASH, initValue);
	
				if (icons_flash!=null && icons_flash.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons
							.get(MODE_FLASH);
					int icon_id = icons_flash.get(initValue);
					but.setImageResource(icon_id);
				}
	
				
				CameraController.getInstance().setCameraFlashMode(mFlashMode);
			}
			else
			{
				mFlashModeSupported = false;
				mFlashMode = -1;	
			}
		}
		else 
		{
			mFlashModeSupported = false;
			mFlashMode = -1;
		}

		// Create ISO button and adding supported ISOs
		byte[] supported_iso = CameraController.getInstance().getSupportedISO();
		if ((supported_iso != null && supported_iso.length > 0 && activeISO != null) ||
				(CameraController.getInstance().isISOSupported() && activeISO != null))
		{
			if(supported_iso != null)
				for(byte iso_name : supported_iso)
				{				
					activeISO.add(ISOButtons.get(Integer.valueOf(iso_name)));
					activeISONames.add(Integer.valueOf(iso_name));
				}
			else
			{
				for(String iso_name : CameraController.getIsoDefaultList())
				{
					activeISO.add(ISOButtons.get(CameraController.getIsoKey().get(iso_name)));
					activeISONames.add(CameraController.getIsoKey().get(iso_name));
				}
			}
			
			if(activeISONames.size() > 0)
			{
				mISOSupported = true;

				isoAdapter.Elements = activeISO;
				GridView gridview = (GridView) guiView.findViewById(R.id.isoGrid);
				//gridview.setNumColumns(activeISO.size() > 9 ? 4 : 3);
				gridview.setAdapter(isoAdapter);
	
				int initValue = preferences.getInt(MainScreen.sISOPref, MainScreen.sDefaultValue);
				if (!activeISONames.contains(initValue)) {
					if (CameraController.isFrontCamera())
						initValue = activeISONames.get(0);
					else
						initValue = MainScreen.sDefaultValue;
	
					preferences.edit().putInt(MainScreen.sISOPref, initValue).commit();
				}
				setButtonSelected(ISOButtons, initValue);
				setCameraParameterValue(MODE_ISO, initValue);
	
				if (icons_iso!=null && icons_iso.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons
							.get(MODE_ISO);
					int icon_id = icons_iso.get(initValue);
					but.setImageResource(icon_id);
				}
				CameraController.getInstance().setCameraISO(mISO);
			}
			else
			{
				mISOSupported = false;
				mISO = -1;	
			}
		}		
		else
		{
			mISOSupported = false;
			mISO = -1;
		}
		
		int iMeteringAreasSupported = CameraController.getInstance().getMaxNumMeteringAreas();
		if(iMeteringAreasSupported > 0)
		{
			Collection<Integer> unsorted_keys = names_metering.keySet();
			List<Integer> keys = Util.asSortedList(unsorted_keys);
			Iterator<Integer> it = keys.iterator();
			while (it.hasNext())
			{
				int metering_name = it.next();
				activeMetering.add(MeteringModeButtons.get(metering_name));
				activeMeteringNames.add(metering_name);
			}
			
			if(activeMeteringNames.size() > 0)
			{
				this.mMeteringAreasSupported = true;

				meteringmodeAdapter.Elements = activeMetering;
				GridView gridview = (GridView) guiView
						.findViewById(R.id.meteringmodeGrid);
				gridview.setAdapter(meteringmodeAdapter);
	
				int initValue = preferences.getInt(MainScreen.sMeteringModePref,
						MainScreen.sDefaultValue);
				if (!activeMeteringNames.contains(initValue))
						initValue = activeMeteringNames.get(0);
				
				setButtonSelected(MeteringModeButtons, initValue);
				setCameraParameterValue(MODE_MET, initValue);
	
				if (icons_metering!=null && icons_metering.containsKey(initValue))
				{
					RotateImageView but = (RotateImageView) topMenuButtons
							.get(MODE_MET);
					int icon_id = icons_metering.get(initValue);
					but.setImageResource(icon_id);
				}
				
				MainScreen.thiz.setCameraMeteringMode(mMeteringMode);
			}
			else
			{
				mMeteringAreasSupported = false;
				mMeteringMode = -1;	
			}
		}
		else
		{
			this.mMeteringAreasSupported = false;
			this.mMeteringMode = -1;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			addCameraChangeButton();
			defaultQuickControl4 = String.valueOf(MODE_CAM);
		} else
			mCameraChangeSupported = false;
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		String qc1 = prefs.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton1),
				defaultQuickControl1);
		String qc2 = prefs.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton2),
				defaultQuickControl2);
		String qc3 = prefs.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton3),
				defaultQuickControl3);
		String qc4 = prefs.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton4),
				defaultQuickControl4);

		quickControl1 = isCameraParameterSupported(qc1) ? getQuickControlButton(
				qc1, quickControl1) : getFreeQuickControlButton(qc1, qc2, qc3,
				qc4, quickControl1);
		quickControl2 = isCameraParameterSupported(qc2) ? getQuickControlButton(
				qc2, quickControl2) : getFreeQuickControlButton(qc1, qc2, qc3,
				qc4, quickControl2);
		quickControl3 = isCameraParameterSupported(qc3) ? getQuickControlButton(
				qc3, quickControl3) : getFreeQuickControlButton(qc1, qc2, qc3,
				qc4, quickControl3);
		quickControl4 = isCameraParameterSupported(qc4) ? getQuickControlButton(
				qc4, quickControl4) : getFreeQuickControlButton(qc1, qc2, qc3,
				qc4, quickControl4);

		try
		{				
			((LinearLayout) guiView.findViewById(R.id.paramsLayout))
					.addView(quickControl1);
		} catch (Exception e)
		{
			e.printStackTrace();
			Log.e("AlmalenceGUI", "addView exception: " + e.getMessage());
		}
		
		try
		{
			((LinearLayout) guiView.findViewById(R.id.paramsLayout))
					.addView(quickControl2);
		} catch (Exception e)
		{
			e.printStackTrace();
			Log.e("AlmalenceGUI", "addView exception: " + e.getMessage());
		}
		
		try
		{
			((LinearLayout) guiView.findViewById(R.id.paramsLayout))
					.addView(quickControl3);
		} catch (Exception e)
		{
			e.printStackTrace();
			Log.e("AlmalenceGUI", "addView exception: " + e.getMessage());
		}
		
		try
		{
			((LinearLayout) guiView.findViewById(R.id.paramsLayout))
					.addView(quickControl4);
		} catch (Exception e)
		{
			e.printStackTrace();
			Log.e("AlmalenceGUI", "addView exception: " + e.getMessage());
		}

		if (mEVSupported) {
			Message msg = new Message();
			msg.arg1 = PluginManager.MSG_EV_CHANGED;
			msg.what = PluginManager.MSG_BROADCAST;
			MainScreen.H.sendMessage(msg);
		}

		
	}
	
	@Override
	public void onPluginsInitialized()
	{	
		// Hide all opened menu
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		// create and fill drawing slider
		initSettingsMenu();

		Panel.OnPanelListener pListener = new OnPanelListener() {
			public void onPanelOpened(Panel panel) {
				settingsControlsVisible = true;

				if (modeSelectorVisible)
					hideModeList();
				if (quickControlsChangeVisible)
					closeQuickControlsSettings();
				if (isSecondaryMenusVisible())
					hideSecondaryMenus();

				Message msg = new Message();
				msg.arg1 = PluginManager.MSG_CONTROL_LOCKED;
				msg.what = PluginManager.MSG_BROADCAST;
				MainScreen.H.sendMessage(msg);
			}

			public void onPanelClosed(Panel panel) {
				settingsControlsVisible = false;

				Message msg = new Message();
				msg.arg1 = PluginManager.MSG_CONTROL_UNLOCKED;
				msg.what = PluginManager.MSG_BROADCAST;
				MainScreen.H.sendMessage(msg);
				((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false,
						false);
			}
		};

		guiView.findViewById(R.id.topPanel).bringToFront();
		((Panel) guiView.findViewById(R.id.topPanel))
				.setOnPanelListener(pListener);
	}
	

	private boolean isCameraParameterSupported(String param) {
		if (!param.equals("") && topMenuPluginButtons.containsKey(param))
			return true;
		else if (!param.equals("") && com.almalence.util.Util.isNumeric(param))
		{
			int cameraParameter = Integer.valueOf(param);
			switch (cameraParameter) {
			case MODE_EV:
				if (mEVSupported)
					return true;
				else
					return false;
			case MODE_SCENE:
				if (mSceneModeSupported)
					return true;
				else
					return false;
			case MODE_WB:
				if (mWBSupported)
					return true;
				else
					return false;
			case MODE_FLASH:
				if (mFlashModeSupported)
					return true;
				else
					return false;
			case MODE_FOCUS:
				if (mFocusModeSupported)
					return true;
				else
					return false;
			case MODE_ISO:
				if (mISOSupported)
					return true;
				else
					return false;
			case MODE_MET:
				if (mMeteringAreasSupported)
					return true;
				else
					return false;
			case MODE_CAM:
				if (mCameraChangeSupported)
					return true;
				else
					return false;
			}
		}

		return false;
	}

	//bInitMenu - by default should be true. if called several simultaneously - all should be false and last - true
	public void disableCameraParameter(CameraParameter iParam, boolean bDisable, boolean bInitMenu) {
		View topMenuView = null;
		switch (iParam) {
		case CAMERA_PARAMETER_EV:
			topMenuView = topMenuButtons.get(MODE_EV);
			isEVEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_SCENE:
			topMenuView = topMenuButtons.get(MODE_SCENE);
			isSceneEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_WB:
			topMenuView = topMenuButtons.get(MODE_WB);
			isWBEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_FOCUS:
			topMenuView = topMenuButtons.get(MODE_FOCUS);
			isFocusEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_FLASH:
			topMenuView = topMenuButtons.get(MODE_FLASH);
			isFlashEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_ISO:
			topMenuView = topMenuButtons.get(MODE_ISO);
			isIsoEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_METERING:
			topMenuView = topMenuButtons.get(MODE_MET);
			isMeteringEnabled = !bDisable;
			break;
		case CAMERA_PARAMETER_CAMERACHANGE:
			topMenuView = topMenuButtons.get(MODE_CAM);
			isCameraChangeEnabled = !bDisable;
			break;
		}

		if (topMenuView != null) {
			correctTopMenuButtonBackground(topMenuView, !bDisable);

			if (bInitMenu)
				initSettingsMenu();
		}

	}

	private void correctTopMenuButtonBackground(View topMenuView,
			boolean isEnabled) {
		if (topMenuView != null) {
			if (!isEnabled) {
				((RotateImageView) topMenuView).setColorFilter(0x50FAFAFA,
						PorterDuff.Mode.DST_IN);
			} else {
				((RotateImageView) topMenuView).clearColorFilter();
			}
		}
	}

	private View getQuickControlButton(String qcID, View defaultView) {
		if (!qcID.equals("") && topMenuPluginButtons.containsKey(qcID)) {
			Plugin plugin = PluginManager.getInstance().getPlugin(qcID);
			RotateImageView view = (RotateImageView) topMenuPluginButtons
					.get(qcID);
			LinearLayout paramsLayout = (LinearLayout) MainScreen.thiz
					.findViewById(R.id.paramsLayout);
//			int reqWidth = paramsLayout.getWidth() / 4;
//			int reqHeight = paramsLayout.getHeight();

			view.setImageResource(plugin.getQuickControlIconID());
			return view;
		} else if (!qcID.equals("")
				&& topMenuButtons.containsKey(Integer.valueOf(qcID)))
			return topMenuButtons.get(Integer.valueOf(qcID));

		return defaultView;
	}

	// Method for finding a button for a top menu which not yet represented in
	// that top menu
	private View getFreeQuickControlButton(String qc1, String qc2, String qc3,
			String qc4, View emptyView) {
		Set<Integer> topMenuButtonsKeys = topMenuButtons.keySet();
		Iterator<Integer> topMenuButtonsIterator = topMenuButtonsKeys
				.iterator();

		Set<String> topMenuPluginButtonsKeys = topMenuPluginButtons.keySet();
		Iterator<String> topMenuPluginButtonsIterator = topMenuPluginButtonsKeys
				.iterator();

		// Searching for free button in the top menu buttons list (scene mode,
		// wb, focus, flash, camera switch)
		while (topMenuButtonsIterator.hasNext()) {
			int id1, id2, id3, id4;
			try {
				id1 = Integer.valueOf(qc1);
			} catch (NumberFormatException exp) {
				id1 = -1;
			}
			try {
				id2 = Integer.valueOf(qc2);
			} catch (NumberFormatException exp) {
				id2 = -1;
			}
			try {
				id3 = Integer.valueOf(qc3);
			} catch (NumberFormatException exp) {
				id3 = -1;
			}
			try {
				id4 = Integer.valueOf(qc4);
			} catch (NumberFormatException exp) {
				id4 = -1;
			}

			int buttonID = topMenuButtonsIterator.next();
			View topMenuButton = topMenuButtons.get(buttonID);

			if (topMenuButton != quickControl1
					&& topMenuButton != quickControl2
					&& topMenuButton != quickControl3
					&& topMenuButton != quickControl4 && buttonID != id1
					&& buttonID != id2 && buttonID != id3 && buttonID != id4
					&& isCameraParameterSupported(String.valueOf(buttonID)))
				return topMenuButton;
		}

		// If top menu buttons dosn't have a free button, search in plugin's
		// buttons list
		while (topMenuPluginButtonsIterator.hasNext()) {
			String buttonID = topMenuPluginButtonsIterator.next();
			View topMenuButton = topMenuPluginButtons.get(buttonID);

			if (topMenuButton != quickControl1
					&& topMenuButton != quickControl2
					&& topMenuButton != quickControl3
					&& topMenuButton != quickControl4 && buttonID != qc1
					&& buttonID != qc2 && buttonID != qc3 && buttonID != qc4)
				return topMenuButton;
		}

		// If no button is found create a empty button
		return emptyView;
	}

	void addCameraChangeButton()
	{
		if (CameraController.getNumberOfCameras() > 1)
		{
			mCameraChangeSupported = true;

			RotateImageView but = (RotateImageView) topMenuButtons
					.get(MODE_CAM);
			but.setImageResource(icon_cam);
		} else
			mCameraChangeSupported = false;
	}

	public void rotateSquareViews(final int degree, int duration) {
		if (AlmalenceGUI.mPreviousDeviceOrientation != AlmalenceGUI.mDeviceOrientation
				|| duration == 0) {
			// float mode_rotation = mode0.getRotation();
			int startDegree = AlmalenceGUI.mPreviousDeviceOrientation == 0 ? 0
					: 360 - AlmalenceGUI.mPreviousDeviceOrientation;
			int endDegree = AlmalenceGUI.mDeviceOrientation == 0 ? 0
					: 360 - AlmalenceGUI.mDeviceOrientation;

			int diff = endDegree - startDegree;
			// diff = diff >= 0 ? diff : 360 + diff; // make it in range [0,
			// 359]
			//
			// // Make it in range [-179, 180]. That's the shorted distance
			// between the
			// // two angles
			endDegree = diff > 180 ? endDegree - 360 : diff < -180
					&& endDegree == 0 ? 360 : endDegree;
			
			if (modeSelectorVisible)
				rotateViews(modeViews, startDegree, endDegree, duration);

			if (settingsViews.size() > 0)
			{
				int delay = ((Panel)guiView.findViewById(R.id.topPanel)).isOpen()?duration:0;
				rotateViews(settingsViews, startDegree, endDegree, delay);
			}

			if (quickControlChangeres.size() > 0
					&& this.quickControlsChangeVisible)
				rotateViews(quickControlChangeres, startDegree, endDegree,
						duration);

			//if (guiView.findViewById(R.id.scenemodeLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeScene, startDegree, endDegree, duration);

			//if (guiView.findViewById(R.id.wbLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeWB, startDegree, endDegree, duration);

			//if (guiView.findViewById(R.id.focusmodeLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeFocus, startDegree, endDegree, duration);

			//if (guiView.findViewById(R.id.flashmodeLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeFlash, startDegree, endDegree, duration);

			//if (guiView.findViewById(R.id.isoLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeISO, startDegree, endDegree, duration);
			
			//if (guiView.findViewById(R.id.meteringLayout).getVisibility() == View.VISIBLE)
				rotateViews(activeMetering, startDegree, endDegree, duration);
		}
	}

	private void rotateViews(List<View> views, final float startDegree,
			final float endDegree, long duration) {
		for (int i = 0; i < views.size(); i++) {
			float start = startDegree;
			float end = endDegree;
			final View view = views.get(i);
			
			if(view == null)
				continue;

			duration=0;
			if (duration == 0) {
				view.clearAnimation();
				view.setRotation(endDegree);
			} else {
				start = startDegree - view.getRotation();
				end = endDegree - view.getRotation();

				RotateAnimation animation = new RotateAnimation(start, end,
						view.getWidth() / 2, view.getHeight() / 2);

				animation.setDuration(duration);
				animation.setFillAfter(true);
				animation.setInterpolator(new DecelerateInterpolator());

				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						//view.setRotation(endDegree);
						//view.clearAnimation();						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

				});

				view.clearAnimation();
				view.startAnimation(animation);
			}
		}
	}

	/***************************************************************************************
	 * 
	 * addQuickSetting method
	 * 
	 * type: EV, SCENE, WB, FOCUS, FLASH, CAMERA or MORE
	 * 
	 * Common method for tune quick control's and quick setting view's icon and
	 * text
	 * 
	 * Quick controls and Quick settings views has a similar design but
	 * different behavior
	 * 
	 ****************************************************************************************/
	private void addQuickSetting(SettingsType type, boolean isQuickControl) {
		int icon_id = -1;
		CharSequence icon_text = "";
		boolean isEnabled = true;

		switch (type) {
		case SCENE:
			icon_id = icons_scene.get(mSceneMode);
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_scene);
			isEnabled = isSceneEnabled;
			break;
		case WB:
			icon_id = icons_wb.get(mWB);
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_wb);
			isEnabled = isWBEnabled;
			break;
		case FOCUS:
			try {
				if(mFocusMode == focusAfLock)
					icon_id = R.drawable.gui_almalence_settings_focus_aflock;
				else
					icon_id = icons_focus.get(mFocusMode);
				icon_text = MainScreen.thiz.getResources().getString(
						R.string.settings_mode_focus);
				isEnabled = isFocusEnabled;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("addQuickSetting", "icons_focus.get exception: " + e.getMessage());
			}
			break;
		case FLASH:
			icon_id = icons_flash.get(mFlashMode);
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_flash);
			isEnabled = isFlashEnabled;
			break;
		case ISO:
			icon_id = icons_iso.get(mISO);
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_iso);
			isEnabled = isIsoEnabled;
			break;
		case METERING:
			icon_id = icons_metering.get(mMeteringMode);
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_metering);
			isEnabled = isMeteringEnabled;
			break;			
		case CAMERA: {
			icon_id = icon_cam;
			if (preferences.getBoolean("useFrontCamera", false) == false)
				icon_text = MainScreen.thiz.getResources().getString(
						R.string.settings_mode_rear);
			else
				icon_text = MainScreen.thiz.getResources().getString(
						R.string.settings_mode_front);

			isEnabled = isCameraChangeEnabled;
		}
			break;
		case EV:
			icon_id = icon_ev;
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_exposure);
			isEnabled = isEVEnabled;
			break;
		case MORE:
			icon_id = icon_settings;
			icon_text = MainScreen.thiz.getResources().getString(
					R.string.settings_mode_moresettings);
			break;		
		}

		// Get required size of button
		LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
		View settingView = inflator.inflate(
				R.layout.gui_almalence_quick_control_grid_element, null, false);
		ImageView iconView = (ImageView) settingView
				.findViewById(R.id.imageView);
		iconView.setImageResource(icon_id);
		TextView textView = (TextView) settingView.findViewById(R.id.textView);
		textView.setText(icon_text);

		if (!isEnabled && !isQuickControl) {
			iconView.setColorFilter(MainScreen.mainContext.getResources()
					.getColor(R.color.buttonDisabled), PorterDuff.Mode.DST_IN);
			textView.setTextColor(MainScreen.mainContext.getResources()
					.getColor(R.color.textDisabled));
		}

		// Create onClickListener of right type
		switch (type) {
		case SCENE:
			if (isQuickControl)
				createQuickControlSceneOnClick(settingView);
			else
				createSettingSceneOnClick(settingView);
			break;
		case WB:
			if (isQuickControl)
				createQuickControlWBOnClick(settingView);
			else
				createSettingWBOnClick(settingView);
			break;
		case FOCUS:
			if (isQuickControl)
				createQuickControlFocusOnClick(settingView);
			else
				createSettingFocusOnClick(settingView);
			break;
		case FLASH:
			if (isQuickControl)
				createQuickControlFlashOnClick(settingView);
			else
				createSettingFlashOnClick(settingView);
			break;
		case ISO:
			if (isQuickControl)
				createQuickControlIsoOnClick(settingView);
			else
				createSettingIsoOnClick(settingView);
			break;
		case METERING:
			if (isQuickControl)
				createQuickControlMeteringOnClick(settingView);
			else
				createSettingMeteringOnClick(settingView);
			break;			
		case CAMERA:
			if (isQuickControl)
				createQuickControlCameraChangeOnClick(settingView);
			else
				createSettingCameraOnClick(settingView);
			break;
		case EV:
			if (isQuickControl)
				createQuickControlEVOnClick(settingView);
			else
				createSettingEVOnClick(settingView);
			break;
		case MORE:
			if (isQuickControl)
				return;
			else
				createSettingMoreOnClick(settingView);
			break;		
		}

		if (isQuickControl)
			quickControlChangeres.add(settingView);
		else
			settingsViews.add(settingView);
	}

	private void addPluginQuickSetting(Plugin plugin, boolean isQuickControl) {
		int iconID = plugin.getQuickControlIconID();
		String title = plugin.getQuickControlTitle();

		if (iconID <= 0)
			return;

		LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
		View qcView = inflator.inflate(
				R.layout.gui_almalence_quick_control_grid_element, null, false);
		((ImageView) qcView.findViewById(R.id.imageView))
				.setImageResource(iconID);
		((TextView) qcView.findViewById(R.id.textView)).setText(title);

		createPluginQuickControlOnClick(plugin, qcView, isQuickControl);

		if (isQuickControl)
			quickControlChangeres.add(qcView);
		else
			settingsViews.add(qcView);

		plugin.quickControlView = qcView;
	}

	/***************************************************************************************
	 * 
	 * QUICK CONTROLS CUSTOMIZATION METHODS
	 * 
	 * begin >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	 ****************************************************************************************/
	private void initQuickControlsMenu(View currentView) {
		quickControlChangeres.clear();
		if (quickControlAdapter.Elements != null) {
			quickControlAdapter.Elements.clear();
			quickControlAdapter.notifyDataSetChanged();
		}

		Set<Integer> keys = topMenuButtons.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			Integer id = it.next();
			switch (id) {
			case R.id.evButton:
				if (mEVSupported)
					addQuickSetting(SettingsType.EV, true);
				break;
			case R.id.sceneButton:
				if (mSceneModeSupported)
					addQuickSetting(SettingsType.SCENE, true);
				break;
			case R.id.wbButton:
				if (mWBSupported)
					addQuickSetting(SettingsType.WB, true);
				break;
			case R.id.focusButton:
				if (mFocusModeSupported)
					addQuickSetting(SettingsType.FOCUS, true);
				break;
			case R.id.flashButton:
				if (mFlashModeSupported)
					addQuickSetting(SettingsType.FLASH, true);
				break;
			case R.id.isoButton:
				if (mISOSupported)
					addQuickSetting(SettingsType.ISO, true);
				break;
			case R.id.meteringButton:
				if (mMeteringAreasSupported)
					addQuickSetting(SettingsType.METERING, true);
				break;				
			case R.id.camerachangeButton:
				if (mCameraChangeSupported)
					addQuickSetting(SettingsType.CAMERA, true);
				break;			
			}
		}

		// Add quick conrols from plugins
		initPluginQuickControls(PluginManager.getInstance().getActivePlugins(
				PluginType.ViewFinder));
		initPluginQuickControls(PluginManager.getInstance().getActivePlugins(
				PluginType.Capture));
		initPluginQuickControls(PluginManager.getInstance().getActivePlugins(
				PluginType.Processing));
		initPluginQuickControls(PluginManager.getInstance().getActivePlugins(
				PluginType.Filter));
		initPluginQuickControls(PluginManager.getInstance().getActivePlugins(
				PluginType.Export));

//		DisplayMetrics metrics = new DisplayMetrics();
//		MainScreen.thiz.getWindowManager().getDefaultDisplay()
//				.getMetrics(metrics);
//		int width = metrics.widthPixels;
//		int modeHeight = (int) (width / 3 - 5 * metrics.density);
//
//		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
//				LayoutParams.WRAP_CONTENT, modeHeight);
//
//		for (int i = 0; i < quickControlChangeres.size(); i++) {
//			View setting = quickControlChangeres.get(i);
//			setting.setLayoutParams(params);
//		}

		quickControlAdapter.Elements = quickControlChangeres;
		quickControlAdapter.notifyDataSetChanged();
	}

	private void initPluginQuickControls(List<Plugin> Plugins) {
		if (Plugins.size() > 0) {
			for (int i = 0; i < Plugins.size(); i++) {
				Plugin Plugin = Plugins.get(i);

				if (Plugin == null)
					continue;

				addPluginQuickSetting(Plugin, true);
			}
		}
	}

	private void initPluginSettingsControls(List<Plugin> Plugins) {
		if (Plugins.size() > 0) {
			for (int i = 0; i < Plugins.size(); i++) {
				Plugin Plugin = Plugins.get(i);

				if (Plugin == null)
					continue;

				addPluginQuickSetting(Plugin, false);
			}
		}
	}

	private void createPluginQuickControlOnClick(final Plugin plugin,
			View view, boolean isQuickControl) {
		if (isQuickControl)
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					RotateImageView pluginButton = (RotateImageView) topMenuPluginButtons
							.get(plugin.getID());
					LinearLayout paramsLayout = (LinearLayout) MainScreen.thiz
							.findViewById(R.id.paramsLayout);
//					int reqWidth = paramsLayout.getWidth() / 4;
//					int reqHeight = paramsLayout.getHeight();

					pluginButton.setImageResource(plugin
							.getQuickControlIconID());

					switchViews(currentQuickView, pluginButton, plugin.getID());
					recreateQuickControlsMenu();
					changeCurrentQuickControl(pluginButton);
					initQuickControlsMenu(currentQuickView);
					showQuickControlsSettings();
				}

			});
		else
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try 
					{
						plugin.onQuickControlClick();
	
						int icon_id = plugin.getQuickControlIconID();
						String title = plugin.getQuickControlTitle();
						Drawable icon = MainScreen.mainContext.getResources()
								.getDrawable(icon_id);
						((ImageView) v.findViewById(R.id.imageView))
								.setImageResource(icon_id);
						((TextView) v.findViewById(R.id.textView)).setText(title);
	
						RotateImageView pluginButton = (RotateImageView) topMenuPluginButtons
								.get(plugin.getID());
						pluginButton.setImageDrawable(icon);
	
						initSettingsMenu();
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("Almalence GUI", "createPluginQuickControlOnClick exception" + e.getMessage());
					}
				}
			});
	}

	private void createQuickControlEVOnClick(View ev) {
		ev.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView ev = (RotateImageView) topMenuButtons
						.get(MODE_EV);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icon_ev);
				ev.setImageDrawable(icon);

				switchViews(currentQuickView, ev, String.valueOf(MODE_EV));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(ev);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlSceneOnClick(View scene) {
		scene.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView scene = (RotateImageView) topMenuButtons
						.get(MODE_SCENE);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icons_scene.get(mSceneMode));
				scene.setImageDrawable(icon);

				switchViews(currentQuickView, scene, String.valueOf(MODE_SCENE));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(scene);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlWBOnClick(View wb) {
		wb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView wb = (RotateImageView) topMenuButtons
						.get(MODE_WB);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icons_wb.get(mWB));
				wb.setImageDrawable(icon);

				switchViews(currentQuickView, wb, String.valueOf(MODE_WB));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(wb);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlFocusOnClick(View focus) {
		focus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView focus = (RotateImageView) topMenuButtons
						.get(MODE_FOCUS);
				try {
					Drawable icon = MainScreen.mainContext.getResources()
							.getDrawable(icons_focus.get(mFocusMode));
					focus.setImageDrawable(icon);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("createQuickControlFocusOnClick", "icons_focus.get exception: " + e.getMessage());
				}

				switchViews(currentQuickView, focus, String.valueOf(MODE_FOCUS));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(focus);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlFlashOnClick(View flash) {
		flash.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView flash = (RotateImageView) topMenuButtons
						.get(MODE_FLASH);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icons_flash.get(mFlashMode));
				flash.setImageDrawable(icon);

				switchViews(currentQuickView, flash, String.valueOf(MODE_FLASH));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(flash);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlIsoOnClick(View iso) {
		iso.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView iso = (RotateImageView) topMenuButtons
						.get(MODE_ISO);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icons_iso.get(mISO));
				iso.setImageDrawable(icon);

				switchViews(currentQuickView, iso, String.valueOf(MODE_ISO));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(iso);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}
	
	private void createQuickControlMeteringOnClick(View metering) {
		metering.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView metering = (RotateImageView) topMenuButtons
						.get(MODE_MET);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icons_metering.get(mMeteringMode));
				metering.setImageDrawable(icon);

				switchViews(currentQuickView, metering, String.valueOf(MODE_MET));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(metering);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}

	private void createQuickControlCameraChangeOnClick(View cameraChange) {
		cameraChange.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RotateImageView cam = (RotateImageView) topMenuButtons
						.get(MODE_CAM);
				Drawable icon = MainScreen.mainContext.getResources()
						.getDrawable(icon_cam);
				cam.setImageDrawable(icon);

				switchViews(currentQuickView, cam, String.valueOf(MODE_CAM));
				recreateQuickControlsMenu();
				changeCurrentQuickControl(cam);
				initQuickControlsMenu(currentQuickView);
				showQuickControlsSettings();
			}

		});
	}
	

	public void changeCurrentQuickControl(View newCurrent) {
		if (currentQuickView != null)
			currentQuickView.setBackgroundDrawable(MainScreen.mainContext
					.getResources().getDrawable(
							R.drawable.transparent_background));

		currentQuickView = newCurrent;
		newCurrent.setBackgroundDrawable(MainScreen.mainContext.getResources()
				.getDrawable(R.drawable.layout_border_qc_button));
		((RotateImageView) newCurrent).setBackgroundEnabled(true);
	}

	private void showQuickControlsSettings() {
		unselectPrimaryTopMenuButtons(-1);
		hideSecondaryMenus();

		GridView gridview = (GridView) guiView.findViewById(R.id.qcGrid);
		gridview.setAdapter(quickControlAdapter);

		// final int degree = AlmalenceGUI.mDeviceOrientation >= 0 ?
		// AlmalenceGUI.mDeviceOrientation % 360
		// : AlmalenceGUI.mDeviceOrientation % 360 + 360;
		// rotateSquareViews(degree, 0);

		((RelativeLayout) guiView.findViewById(R.id.qcLayout))
				.setVisibility(View.VISIBLE);
		//(guiView.findViewById(R.id.qcGrid)).setVisibility(View.VISIBLE);

		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.setBackgroundDrawable(MainScreen.mainContext.getResources()
						.getDrawable(R.drawable.blacktransparentlayertop));

		Set<Integer> topmenu_keys = topMenuButtons.keySet();
		Iterator<Integer> it = topmenu_keys.iterator();
		while (it.hasNext()) {
			int key = it.next();
			if (currentQuickView != topMenuButtons.get(key)) {
				((RotateImageView) topMenuButtons.get(key))
						.setBackgroundEnabled(true);
				topMenuButtons.get(key).setBackgroundDrawable(
						MainScreen.mainContext.getResources().getDrawable(
								R.drawable.transparent_background));
			}
		}

		quickControlsChangeVisible = true;

		final int degree = AlmalenceGUI.mDeviceOrientation >= 0 ? AlmalenceGUI.mDeviceOrientation % 360
				: AlmalenceGUI.mDeviceOrientation % 360 + 360;
		rotateSquareViews(degree, 0);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_LOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void closeQuickControlsSettings() {
		RelativeLayout gridview = (RelativeLayout) guiView
				.findViewById(R.id.qcLayout);
		gridview.setVisibility(View.INVISIBLE);
		//(guiView.findViewById(R.id.qcGrid)).setVisibility(View.INVISIBLE);
		quickControlsChangeVisible = false;

		currentQuickView.setBackgroundDrawable(MainScreen.mainContext
				.getResources().getDrawable(R.drawable.transparent_background));
		currentQuickView = null;

		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.setBackgroundDrawable(MainScreen.mainContext.getResources()
						.getDrawable(R.drawable.blacktransparentlayertop));

		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_EV), isEVEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_SCENE), isSceneEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_WB), isWBEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_FOCUS), isFocusEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_FLASH), isFlashEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_ISO), isIsoEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_MET), isMeteringEnabled);
		correctTopMenuButtonBackground(
				MainScreen.thiz.findViewById(MODE_CAM), isCameraChangeEnabled);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_UNLOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);

		guiView.findViewById(R.id.topPanel).setVisibility(View.VISIBLE);
	}

	/***************************************************************************************
	 * 
	 * QUICK CONTROLS CUSTOMIZATION METHODS
	 * 
	 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< end
	 ***************************************************************************************/

	/***************************************************************************************
	 * 
	 * SETTINGS MENU METHODS
	 * 
	 * begin >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	 ****************************************************************************************/
	private void initSettingsMenu() {
		// Clear view list to recreate all settings buttons
		settingsViews.clear();
		if (settingsAdapter.Elements != null) {
			settingsAdapter.Elements.clear();
			settingsAdapter.notifyDataSetChanged();
		}

		// Obtain all theoretical buttons we know
		Set<Integer> keys = topMenuButtons.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			// If such camera feature is supported then add a button to settings
			// menu
			Integer id = it.next();
			switch (id) {
			case R.id.evButton:
				if (mEVSupported)
					addQuickSetting(SettingsType.EV, false);
				break;
			case R.id.sceneButton:
				if (mSceneModeSupported)
					addQuickSetting(SettingsType.SCENE, false);
				break;
			case R.id.wbButton:
				if (mWBSupported)
					addQuickSetting(SettingsType.WB, false);
				break;
			case R.id.focusButton:
				if (mFocusModeSupported)
					addQuickSetting(SettingsType.FOCUS, false);
				break;
			case R.id.flashButton:
				if (mFlashModeSupported)
					addQuickSetting(SettingsType.FLASH, false);
				break;
			case R.id.isoButton:
				if (mISOSupported)
					addQuickSetting(SettingsType.ISO, false);
				break;
			case R.id.meteringButton:
				if (mMeteringAreasSupported)
					addQuickSetting(SettingsType.METERING, false);
				break;				
			case R.id.camerachangeButton:
				if (mCameraChangeSupported)
					addQuickSetting(SettingsType.CAMERA, false);
				break;			
			}
		}

		// Add quick conrols from plugins
		initPluginSettingsControls(PluginManager.getInstance()
				.getActivePlugins(PluginType.ViewFinder));
		initPluginSettingsControls(PluginManager.getInstance()
				.getActivePlugins(PluginType.Capture));
		initPluginSettingsControls(PluginManager.getInstance()
				.getActivePlugins(PluginType.Processing));
		initPluginSettingsControls(PluginManager.getInstance()
				.getActivePlugins(PluginType.Filter));
		initPluginSettingsControls(PluginManager.getInstance()
				.getActivePlugins(PluginType.Export));

		// The very last control is always MORE SETTINGS
		addQuickSetting(SettingsType.MORE, false);

		settingsAdapter.Elements = settingsViews;

		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int modeHeight = (int) (width / 3 - 5 * metrics.density);

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.WRAP_CONTENT, modeHeight);

//		for (int i = 0; i < settingsViews.size(); i++) {
//			View setting = settingsViews.get(i);
//			setting.setLayoutParams(params);
//		}

		final int degree = AlmalenceGUI.mDeviceOrientation >= 0 ? AlmalenceGUI.mDeviceOrientation % 360
				: AlmalenceGUI.mDeviceOrientation % 360 + 360;
		rotateSquareViews(degree, 0);

		GridView gridview = (GridView) guiView.findViewById(R.id.settingsGrid);
		gridview.setAdapter(settingsAdapter);
		settingsAdapter.notifyDataSetChanged();
	}

	private void createSettingSceneOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (!isSceneEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.BOTTOM,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				byte[] supported_scene = CameraController.getInstance().getSupportedSceneModes();
				if (supported_scene == null)
					return;
				if (supported_scene.length > 0)
				{
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.scenemodeLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_SCENE);
					}
					else
						hideSecondaryMenus();
				} else {
					
					/*Old code. Used to change scene mode 'on the fly'*/
					
//					String newSceneMode;
//					ListIterator<String> it = supported_scene
//							.listIterator(supported_scene.indexOf(mSceneMode));
//					it.next();
//					if (it.hasNext())
//						newSceneMode = it.next();
//					else
//						newSceneMode = supported_scene.get(0);
//
//					Drawable icon = MainScreen.mainContext.getResources()
//							.getDrawable(icons_scene.get(newSceneMode));
//					((ImageView) v.findViewById(R.id.imageView))
//							.setImageDrawable(icon);
//					setSceneMode(newSceneMode);
				}
			}
		});
	}

	private void createSettingWBOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isWBEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				byte[] supported_wb = CameraController.getInstance().getSupportedWhiteBalance();
				if (supported_wb == null)
					return;
				if (supported_wb.length > 0)
				{
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.wbLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_WB);
					}

					else
						hideSecondaryMenus();
				} else {
					
					/*Old code. Used to change wb mode 'on the fly'*/
					
//					String newWBMode;
//					ListIterator<String> it = supported_wb
//							.listIterator(supported_wb.indexOf(mWB));
//					it.next();
//					if (it.hasNext())
//						newWBMode = it.next();
//					else
//						newWBMode = supported_wb.get(0);
//
//					((ImageView) v.findViewById(R.id.imageView))
//							.setImageResource(icons_wb.get(newWBMode));
//					setWhiteBalance(newWBMode);
				}
			}
		});
	}

	private void createSettingFocusOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isFocusEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				byte[] supported_focus = CameraController.getInstance().getSupportedFocusModes();
				if (supported_focus == null)
					return;
				if (supported_focus.length > 0)
				{
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.focusmodeLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_FOCUS);
					}
					else
						hideSecondaryMenus();
				} else {
					
					/*Old code. Used to change focus mode 'on the fly'*/
					
//					String newFocusMode;
//					ListIterator<String> it = supported_focus
//							.listIterator(supported_focus.indexOf(mFocusMode));
//					it.next();
//					if (it.hasNext())
//						newFocusMode = it.next();
//					else
//						newFocusMode = supported_focus.get(0);
//
//					try {
//						Drawable icon = MainScreen.mainContext.getResources()
//								.getDrawable(icons_focus.get(newFocusMode));
//						((ImageView) v.findViewById(R.id.imageView)).setImageDrawable(icon);
//					} catch (Exception e) {
//						e.printStackTrace();
//						Log.e("createSettingFocusOnClick", "icons_focus.get exception: " + e.getMessage());
//					}
//					
//					setFocusMode(newFocusMode);
				}
			}
		});
	}

	private void createSettingFlashOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isFlashEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				byte[] supported_flash = CameraController.getInstance().getSupportedFlashModes();
				if (supported_flash == null)
					return;
				if (supported_flash.length > 0)
				{
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.flashmodeLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_FLASH);
					}
					else
						hideSecondaryMenus();
				} else {
					
					/*Old code. Used to change flash mode 'on the fly'*/
					
//					String newFlashMode;
//					ListIterator<String> it = supported_flash
//							.listIterator(supported_flash.indexOf(mFlashMode));
//					it.next();
//					if (it.hasNext())
//						newFlashMode = it.next();
//					else
//						newFlashMode = supported_flash.get(0);
//
//					Drawable icon = MainScreen.mainContext.getResources()
//							.getDrawable(icons_flash.get(newFlashMode));
//					((ImageView) v.findViewById(R.id.imageView))
//							.setImageDrawable(icon);
//					setFlashMode(newFlashMode);
				}
			}
		});
	}

	private void createSettingIsoOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isIsoEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				byte[] supported_iso = CameraController.getInstance().getSupportedISO();
				if ((supported_iso != null && supported_iso.length > 0) ||
				    CameraController.getInstance().isISOSupported())
				{					
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.isoLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_ISO);
					}
					else
						hideSecondaryMenus();
				}
				else
				{
					
					/*Old code. Used to change iso mode 'on the fly'*/
					
//					String newISO;
//					ListIterator<String> it = supported_iso
//							.listIterator(supported_iso.indexOf(mISO));
//					it.next();
//					if (it.hasNext())
//						newISO = it.next();
//					else
//						newISO = supported_iso.get(0);
//
//					Drawable icon = MainScreen.mainContext.getResources()
//							.getDrawable(icons_iso.get(newISO));
//					((ImageView) v.findViewById(R.id.imageView))
//							.setImageDrawable(icon);
//					setISO(newISO);
				}
			}
		});
	}
	
	private void createSettingMeteringOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isMeteringEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				int iMeteringAreasSupported = CameraController.getInstance().getMaxNumMeteringAreas();
				if (iMeteringAreasSupported > 0)
				{					
					if(iScreenType == 0)
						((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
					
					if(guiView.findViewById(R.id.meteringLayout).getVisibility() != View.VISIBLE)
					{
						hideSecondaryMenus();					
						showParams(MODE_MET);
					}
					else
						hideSecondaryMenus();
				}
			}
		});
	}
	

	private void createSettingCameraOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isCameraChangeEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				CameraSwitched(true);
			}

		});
	}

	private void createSettingEVOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isEVEnabled) {
					showToast(
							null,
							Toast.LENGTH_SHORT,
							Gravity.CENTER,
							MainScreen.thiz.getResources().getString(
									R.string.settings_not_available), true,
							false);
					return;
				}
				if(iScreenType == 0)
					((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false, false);
				
				if(guiView.findViewById(R.id.evLayout).getVisibility() != View.VISIBLE)
				{
					hideSecondaryMenus();					
					showParams(MODE_EV);
				}
				else
					hideSecondaryMenus();
			}
		});
	}

	private void createSettingMoreOnClick(View settingView) {
		settingView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PluginManager.getInstance().onShowPreferences();
				Intent settingsActivity = new Intent(MainScreen.mainContext,
						Preferences.class);
				MainScreen.thiz.startActivity(settingsActivity);
				((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false,
						false);
			}
		});
	}

	/***************************************************************************************
	 * 
	 * SETTINGS MENU METHODS
	 * 
	 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< end
	 ***************************************************************************************/

	private void CameraSwitched(boolean restart) {
		if (PluginManager.getInstance().getProcessingCounter() != 0)
			return;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		boolean isFrontCamera = prefs.getBoolean("useFrontCamera", false);
		prefs.edit().putBoolean("useFrontCamera", !isFrontCamera).commit();

		if (restart == true) {
			MainScreen.thiz.PauseMain();
			MainScreen.thiz.ResumeMain();
		}
	}

	private void showModeList() {
		unselectPrimaryTopMenuButtons(-1);
		hideSecondaryMenus();

		initModeList();
		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int modeHeightByWidth = (int) (width / 3 - 5 * metrics.density);
		int modeHeightByDimen = Math.round(MainScreen.thiz.getResources()
				.getDimension(R.dimen.gridModeImageSize)
				+ MainScreen.thiz.getResources().getDimension(
						R.dimen.gridModeTextLayoutSize));

		int modeHeight = modeHeightByDimen > modeHeightByWidth ? modeHeightByWidth
				: modeHeightByDimen;

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.WRAP_CONTENT, modeHeight);

		for (int i = 0; i < modeViews.size(); i++) {
			View mode = modeViews.get(i);
			mode.setLayoutParams(params);
		}

		GridView gridview = (GridView) guiView.findViewById(R.id.modeGrid);
		gridview.setAdapter(modeAdapter);
		
		gridview.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Log.e("AlmalenceGUI", "ModeGridView onTouch! Action " + event.getAction());
				return false;
			}
			
		});

		((RelativeLayout) guiView.findViewById(R.id.modeLayout))
				.setVisibility(View.VISIBLE);
		(guiView.findViewById(R.id.modeGrid)).setVisibility(View.VISIBLE);

		guiView.findViewById(R.id.modeLayout).bringToFront();

		modeSelectorVisible = true;

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_LOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void hideModeList() {
		RelativeLayout gridview = (RelativeLayout) guiView
				.findViewById(R.id.modeLayout);

		Animation gone = AnimationUtils.loadAnimation(MainScreen.thiz,
				R.anim.gui_almalence_modelist_invisible);
		gone.setFillAfter(true);

		gridview.setAnimation(gone);
		(guiView.findViewById(R.id.modeGrid)).setAnimation(gone);

		gridview.setVisibility(View.GONE);
		(guiView.findViewById(R.id.modeGrid)).setVisibility(View.GONE);

		gone.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				guiView.findViewById(R.id.modeLayout).clearAnimation();
				guiView.findViewById(R.id.modeGrid).clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		modeSelectorVisible = false;

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_UNLOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	@Override
	public void onHardwareShutterButtonPressed() {
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);
		lockControls = true;
		PluginManager.getInstance().OnShutterClick();
	}

	@Override
	public void onHardwareFocusButtonPressed() {
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);
		// lockControls = true;
		PluginManager.getInstance().OnFocusButtonClick();
	}

	private void shutterButtonPressed() {
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);
		lockControls = true;
		PluginManager.getInstance().OnShutterClick();
	}

	private void infoSlide(boolean toLeft, float XtoVisible, float XtoInvisible) {
		if ((infoSet == INFO_ALL & !toLeft) && isAnyViewOnViewfinder())
			infoSet = INFO_PARAMS;
		else if (infoSet == INFO_ALL && !isAnyViewOnViewfinder())
			infoSet = INFO_NO;
		else if (isAnyViewOnViewfinder())
			infoSet = (infoSet + 1 * (toLeft ? 1 : -1)) % 4;
		else
			infoSet = INFO_ALL;
		setInfo(toLeft, XtoVisible, XtoInvisible, true);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		prefs.edit().putInt(MainScreen.sDefaultInfoSetPref, infoSet).commit();
	}

	private void setInfo(boolean toLeft, float XtoVisible, float XtoInvisible,
			boolean isAnimate) {
		int pluginzoneWidth = guiView.findViewById(R.id.pluginsLayout)
				.getWidth();
		int infozoneWidth = guiView.findViewById(R.id.infoLayout).getWidth();
		int screenWidth = pluginzoneWidth + infozoneWidth;

		AnimationSet rlinvisible = new AnimationSet(true);
		rlinvisible.setInterpolator(new DecelerateInterpolator());

		AnimationSet rlvisible = new AnimationSet(true);
		rlvisible.setInterpolator(new DecelerateInterpolator());

		AnimationSet lrinvisible = new AnimationSet(true);
		lrinvisible.setInterpolator(new DecelerateInterpolator());

		AnimationSet lrvisible = new AnimationSet(true);
		lrvisible.setInterpolator(new DecelerateInterpolator());

		int duration_invisible = 0;
		duration_invisible = isAnimate ? com.almalence.util.Util
				.clamp(Math.abs(Math.round(((toLeft ? XtoVisible
						: (screenWidth - XtoVisible)) * 500) / screenWidth)),
						10, 500) : 0;
		
		int duration_visible = 0; 
		duration_visible = isAnimate ? com.almalence.util.Util
				.clamp(Math.abs(Math.round(((toLeft ? XtoInvisible
						: (screenWidth - XtoInvisible)) * 500) / screenWidth)),
						10, 500) : 0;
		

		Animation invisible_alpha = new AlphaAnimation(1, 0);
		invisible_alpha.setDuration(duration_invisible);
		invisible_alpha.setRepeatCount(0);

		Animation visible_alpha = new AlphaAnimation(0, 1);
		visible_alpha.setDuration(duration_visible);
		visible_alpha.setRepeatCount(0);

		Animation rlinvisible_translate = new TranslateAnimation(XtoInvisible,
				-screenWidth, 0, 0);
		rlinvisible_translate.setDuration(duration_invisible);
		rlinvisible_translate.setFillAfter(true);

		Animation lrinvisible_translate = new TranslateAnimation(XtoInvisible,
				screenWidth, 0, 0);
		lrinvisible_translate.setDuration(duration_invisible);
		lrinvisible_translate.setFillAfter(true);

		Animation rlvisible_translate = new TranslateAnimation(XtoVisible, 0,
				0, 0);
		rlvisible_translate.setDuration(duration_visible);
		rlvisible_translate.setFillAfter(true);

		Animation lrvisible_translate = new TranslateAnimation(XtoVisible, 0,
				0, 0);
		lrvisible_translate.setDuration(duration_visible);
		lrvisible_translate.setFillAfter(true);

		// Add animations to appropriate set
		rlinvisible.addAnimation(invisible_alpha);
		rlinvisible.addAnimation(rlinvisible_translate);

		// rlvisible.addAnimation(visible_alpha);
		rlvisible.addAnimation(rlvisible_translate);

		lrinvisible.addAnimation(invisible_alpha);
		lrinvisible.addAnimation(lrinvisible_translate);

		// lrvisible.addAnimation(visible_alpha);
		lrvisible.addAnimation(lrvisible_translate);

		int[] zonesVisibility = new int[3];
		switch (infoSet)
		{
			case INFO_ALL:
			{
				zonesVisibility[0] = View.VISIBLE;
				zonesVisibility[1] = View.VISIBLE;
				zonesVisibility[2] = View.VISIBLE;
				break;
			}
	
			case INFO_NO:
			{
				zonesVisibility[0] = View.GONE;
				zonesVisibility[1] = View.GONE;
				zonesVisibility[2] = View.GONE;				
				break;
			}
	
			case INFO_GRID:
			{
				zonesVisibility[0] = View.GONE;
				zonesVisibility[1] = View.GONE;
				zonesVisibility[2] = View.VISIBLE;
				break;
			}
			
			case INFO_PARAMS:
			{
				zonesVisibility[0] = View.VISIBLE;
				zonesVisibility[1] = View.GONE;
				zonesVisibility[2] = View.GONE;
				break;
			}
		}
		applyZonesVisibility(zonesVisibility, isAnimate, toLeft, rlvisible, lrvisible, rlinvisible, lrinvisible);

		rlinvisible.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				clearLayoutsAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		lrinvisible.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				clearLayoutsAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		// checks preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		Editor prefsEditor = prefs.edit();
		prefsEditor.putInt(MainScreen.sDefaultInfoSetPref, infoSet);
		prefsEditor.commit();
	}
	
	private void applyZonesVisibility(int[] zonesVisibility, boolean isAnimate, boolean toLeft, AnimationSet rlvisible, AnimationSet lrvisible, AnimationSet rlinvisible, AnimationSet lrinvisible)
	{
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		if(guiView.findViewById(R.id.paramsLayout).getVisibility() != zonesVisibility[0])
		{
			if (isAnimate)
				guiView.findViewById(R.id.paramsLayout).startAnimation(
						toLeft ? (zonesVisibility[0] == View.VISIBLE? rlvisible : rlinvisible) : (zonesVisibility[0] == View.VISIBLE? lrvisible : lrinvisible));
			guiView.findViewById(R.id.paramsLayout).setVisibility(zonesVisibility[0]);
			((Panel) guiView.findViewById(R.id.topPanel)).reorder(zonesVisibility[0] == View.GONE,
					true);
		}
		
		if(guiView.findViewById(R.id.pluginsLayout).getVisibility() != zonesVisibility[1])
		{
			if (isAnimate)
				guiView.findViewById(R.id.pluginsLayout).startAnimation(
						toLeft ? (zonesVisibility[1] == View.VISIBLE? rlvisible : rlinvisible) : (zonesVisibility[1] == View.VISIBLE? lrvisible : lrinvisible));
			guiView.findViewById(R.id.pluginsLayout).setVisibility(zonesVisibility[1]);
	
			if (isAnimate)
				guiView.findViewById(R.id.infoLayout).startAnimation(
						toLeft ? (zonesVisibility[1] == View.VISIBLE? rlvisible : rlinvisible) : (zonesVisibility[1] == View.VISIBLE? lrvisible : lrinvisible));
			guiView.findViewById(R.id.infoLayout).setVisibility(zonesVisibility[1]);
		}
		
		if(guiView.findViewById(R.id.fullscreenLayout).getVisibility() != zonesVisibility[2])
		{
			if (isAnimate)
				guiView.findViewById(R.id.fullscreenLayout).startAnimation(
						toLeft ? (zonesVisibility[2] == View.VISIBLE? rlvisible : rlinvisible) : (zonesVisibility[2] == View.VISIBLE? lrvisible : lrinvisible));
			guiView.findViewById(R.id.fullscreenLayout).setVisibility(zonesVisibility[2]);
		}
	}
	
	private void clearLayoutsAnimation()
	{
		guiView.findViewById(R.id.paramsLayout).clearAnimation();
		guiView.findViewById(R.id.pluginsLayout).clearAnimation();
		guiView.findViewById(R.id.fullscreenLayout).clearAnimation();
		guiView.findViewById(R.id.infoLayout).clearAnimation();
	}

	// Method used by quick controls customization feature. Swaps current quick
	// control with selected from qc grid.
	private void switchViews(View currentView, View newView, String qcID) {
		if (currentView == newView)
			return;

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);

		int currentQCNumber = -1;
		String currentViewID = "";
		if (currentView == quickControl1) {
			currentViewID = pref.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton1), "");
			currentQCNumber = 1;
		} else if (currentView == quickControl2) {
			currentViewID = pref.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton2), "");
			currentQCNumber = 2;
		} else if (currentView == quickControl3) {
			currentViewID = pref.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton3), "");
			currentQCNumber = 3;
		} else if (currentView == quickControl4) {
			currentViewID = pref.getString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton4), "");
			currentQCNumber = 4;
		}

		if (newView == quickControl1) {
			quickControl1 = currentView;
			pref.edit().putString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton1), currentViewID)
					.commit();
		} else if (newView == quickControl2) {
			quickControl2 = currentView;
			pref.edit().putString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton2), currentViewID)
					.commit();
		} else if (newView == quickControl3) {
			quickControl3 = currentView;
			pref.edit().putString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton3), currentViewID)
					.commit();
		} else if (newView == quickControl4) {
			quickControl4 = currentView;
			pref.edit().putString(MainScreen.thiz.getResources().getString(R.string.Preference_QuickControlButton4), currentViewID)
					.commit();
		} else {
			if (currentView.getParent() != null)
				((ViewGroup) currentView.getParent()).removeView(currentView);
		}

		switch (currentQCNumber) {
		case 1:
			quickControl1 = newView;
			pref.edit().putString("quickControlButton1", qcID).commit();
			break;
		case 2:
			quickControl2 = newView;
			pref.edit().putString("quickControlButton2", qcID).commit();
			break;
		case 3:
			quickControl3 = newView;
			pref.edit().putString("quickControlButton3", qcID).commit();
			break;
		case 4:
			quickControl4 = newView;
			pref.edit().putString("quickControlButton4", qcID).commit();
			break;
		}
	}

	private void recreateQuickControlsMenu() {
		removeAllViews(topMenuButtons);
		removeAllViews(topMenuPluginButtons);
		removeAllQuickViews();

		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.addView(quickControl1);
		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.addView(quickControl2);
		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.addView(quickControl3);
		((LinearLayout) guiView.findViewById(R.id.paramsLayout))
				.addView(quickControl4);
	}

	private void removeAllQuickViews() {
		if (quickControl1 != null && quickControl1.getParent() != null)
			((ViewGroup) quickControl1.getParent()).removeView(quickControl1);
		if (quickControl2 != null && quickControl2.getParent() != null)
			((ViewGroup) quickControl2.getParent()).removeView(quickControl2);
		if (quickControl3 != null && quickControl3.getParent() != null)
			((ViewGroup) quickControl3.getParent()).removeView(quickControl3);
		if (quickControl4 != null && quickControl4.getParent() != null)
			((ViewGroup) quickControl4.getParent()).removeView(quickControl4);
	}

	private void removeAllViews(Map<?, View> buttons) {
		Collection<View> button_set = buttons.values();
		Iterator<View> it = button_set.iterator();
		while (it.hasNext()) {
			View view = it.next();
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);
		}
	}

	private void removePluginViews() {
		List<View> pluginsView = new ArrayList<View>();
		RelativeLayout pluginsLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.pluginsLayout);
		for (int i = 0; i < pluginsLayout.getChildCount(); i++)
			pluginsView.add(pluginsLayout.getChildAt(i));

		for (int j = 0; j < pluginsView.size(); j++) {
			View view = pluginsView.get(j);
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);

			pluginsLayout.removeView(view);
		}

		List<View> fullscreenView = new ArrayList<View>();
		RelativeLayout fullscreenLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.fullscreenLayout);
		for (int i = 0; i < fullscreenLayout.getChildCount(); i++)
			fullscreenView.add(fullscreenLayout.getChildAt(i));

		for (int j = 0; j < fullscreenView.size(); j++) {
			View view = fullscreenView.get(j);
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);

			fullscreenLayout.removeView(view);
		}

		// List<View> specialPluginsView = new ArrayList<View>();
		// RelativeLayout specialPluginsLayout = (RelativeLayout)
		// MainScreen.thiz
		// .findViewById(R.id.specialPluginsLayout);
		// for (int i = 0; i < specialPluginsLayout.getChildCount(); i++)
		// specialPluginsView.add(specialPluginsLayout.getChildAt(i));
		//
		// for (int j = 0; j < specialPluginsView.size(); j++) {
		// View view = specialPluginsView.get(j);
		// if (view.getParent() != null)
		// ((ViewGroup) view.getParent()).removeView(view);
		//
		// specialPluginsLayout.removeView(view);
		// }

		List<View> infoView = new ArrayList<View>();
		LinearLayout infoLayout = (LinearLayout) MainScreen.thiz
				.findViewById(R.id.infoLayout);
		for (int i = 0; i < infoLayout.getChildCount(); i++)
			infoView.add(infoLayout.getChildAt(i));

		for (int j = 0; j < infoView.size(); j++) {
			View view = infoView.get(j);
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);

			infoLayout.removeView(view);
		}
	}

	public boolean onLongClick(View v) {
		if (quickControlsChangeVisible)
			return true;

		if (modeSelectorVisible)
			return true;
		
		if(shutterButton == v)
		{
			//AEAWLock();
		}
		else
		{
			changeCurrentQuickControl(v);
	
			initQuickControlsMenu(v);
			showQuickControlsSettings();
			guiView.findViewById(R.id.topPanel).setVisibility(View.GONE);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		hideSecondaryMenus();
		if (!quickControlsChangeVisible) {
			if (topMenuPluginButtons.containsValue(v)) {
				Set<String> pluginIDset = topMenuPluginButtons.keySet();
				Iterator<String> it = pluginIDset.iterator();
				while (it.hasNext()) {
					String pluginID = it.next();
					if (v == topMenuPluginButtons.get(pluginID)) {
						Plugin plugin = PluginManager.getInstance().getPlugin(
								pluginID);
						plugin.onQuickControlClick();

						int icon_id = plugin.getQuickControlIconID();
						Drawable icon = MainScreen.mainContext.getResources()
								.getDrawable(icon_id);
						((RotateImageView) v).setImageDrawable(icon);

						initSettingsMenu();
						break;
					}
				}
			}
			return;
		}

		changeCurrentQuickControl(v);

		initQuickControlsMenu(v);
		showQuickControlsSettings();
	}

	@Override
	public void onButtonClick(View button) {
		// hide hint screen
		if (guiView.findViewById(R.id.hintLayout).getVisibility() == View.VISIBLE)
			guiView.findViewById(R.id.hintLayout).setVisibility(View.INVISIBLE);

		if (guiView.findViewById(R.id.mode_help).getVisibility() ==  View.VISIBLE)
			guiView.findViewById(R.id.mode_help).setVisibility(View.INVISIBLE);
		
		int id = button.getId();
		if (lockControls && ((R.id.buttonShutter != id)))
			return;

		// 1. if quick settings slider visible - lock everything
		// 2. if modes visible - allow only selectmode button
		// 3. if change quick controls visible - allow only OK button
		// if (settingsControlsVisible && ((R.id.buttonSelectMode != id) ||
		// (R.id.buttonShutter != id) || (R.id.buttonGallery != id) ))
		// hideModeList();
		//
		if (settingsControlsVisible || quickControlsChangeVisible
				|| (modeSelectorVisible && (R.id.buttonSelectMode != id))) {
			if (quickControlsChangeVisible) {// if change control visible and
												// quick control button pressed
				if ((button != quickControl1) && (button != quickControl2)
						&& (button != quickControl3)
						&& (button != quickControl4))
					closeQuickControlsSettings();
			}
			if (settingsControlsVisible) {
				((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false,
						true);
				return;
			}
			if (modeSelectorVisible) {
				hideModeList();
				return;
			}
		}

		switch (id) {
		// BOTTOM BUTTONS - Modes, Shutter
		case R.id.buttonSelectMode: {
			if (quickControlsChangeVisible || settingsControlsVisible)
				break;

			if (!modeSelectorVisible)
				showModeList();
			else
				hideModeList();
		}
			break;

		case R.id.buttonShutter: {
			if (quickControlsChangeVisible || settingsControlsVisible)
				break;

			shutterButtonPressed();
		}
			break;

		case R.id.buttonGallery: {
			if (quickControlsChangeVisible || settingsControlsVisible)
				break;

			openGallery();
			break;
		}

		// TOP MENU BUTTONS - Scene mode, white balance, focus mode, flash mode,
		// settings
		case R.id.evButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isEVEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			LinearLayout Layout = (LinearLayout) guiView
					.findViewById(R.id.evLayout);
			if (Layout.getVisibility() == View.GONE) {
				unselectPrimaryTopMenuButtons(MODE_EV);
				hideSecondaryMenus();
				showParams(MODE_EV);
				quickControlsVisible = true;
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;
		case R.id.sceneButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isSceneEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.scenemodeLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_SCENE);
				hideSecondaryMenus();
				showParams(MODE_SCENE);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;
		case R.id.wbButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isWBEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.wbLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_WB);
				hideSecondaryMenus();
				showParams(MODE_WB);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;
		case R.id.focusButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isFocusEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.focusmodeLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_FOCUS);
				hideSecondaryMenus();
				showParams(MODE_FOCUS);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;
		case R.id.flashButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			// hideSecondaryMenus();
			if (!isFlashEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.flashmodeLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_FLASH);
				hideSecondaryMenus();
				showParams(MODE_FLASH);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
			// // switch flash parameters by touch
			// List<String> supported_flash = activeFlashNames;
			//
			// int idx = supported_flash.indexOf(mFlashMode);
			// idx++;
			// if (supported_flash.size() <= idx)
			// idx = 0;
			//
			// String newFlashMode = supported_flash.get(idx);
			// setFlashMode(newFlashMode);
		}
			break;
		case R.id.isoButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isIsoEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.isoLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_ISO);
				hideSecondaryMenus();
				showParams(MODE_ISO);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;
		case R.id.meteringButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			if (!isMeteringEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			RelativeLayout Layout = (RelativeLayout) guiView
					.findViewById(R.id.meteringLayout);
			if (Layout.getVisibility() == View.GONE) {
				quickControlsVisible = true;
				unselectPrimaryTopMenuButtons(MODE_MET);
				hideSecondaryMenus();
				showParams(MODE_MET);
			} else {
				quickControlsVisible = false;
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
			}
		}
			break;			
		case R.id.camerachangeButton: {
			if (quickControlsChangeVisible) {
				changeCurrentQuickControl(button);
				initQuickControlsMenu(button);
				showQuickControlsSettings();
				break;
			}

			unselectPrimaryTopMenuButtons(-1);
			hideSecondaryMenus();

			if (!isCameraChangeEnabled) {
				showToast(
						null,
						Toast.LENGTH_SHORT,
						Gravity.CENTER,
						MainScreen.thiz.getResources().getString(
								R.string.settings_not_available), true, false);
				break;
			}

			CameraSwitched(true);
		}
			break;		

		// EXPOSURE COMPENSATION BUTTONS (-\+)
		case R.id.evMinusButton: {
			expoMinus();
		}
			break;
		case R.id.evPlusButton: {
			expoPlus();
		}
			break;
		}
		this.initSettingsMenu();
	}

	private void setSceneMode(int newMode)
	{
		if(newMode != -1 && SceneModeButtons.containsKey(newMode))
		{
			CameraController.getInstance().setCameraSceneMode(newMode);
			mSceneMode = newMode;
		}
		else if(SceneModeButtons.containsKey(CameraParameters.SCENE_MODE_AUTO))
		{
			CameraController.getInstance().setCameraSceneMode(CameraParameters.SCENE_MODE_AUTO);
			mSceneMode = CameraParameters.SCENE_MODE_AUTO;	
		}
		else if(CameraController.getInstance().getSupportedSceneModes() != null)
		{
			CameraController.getInstance().setCameraSceneMode(CameraController.getInstance().getSupportedSceneModes()[0]);
			mSceneMode = CameraController.getInstance().getSupportedSceneModes()[0];
		}

		// After change scene mode it may be changed other stuff such as
		// flash, wb, focus mode.
		// Need to get this information and update state of current
		// parameters.						
		int wbNew = CameraController.getInstance().getWBMode();
		int flashNew = CameraController.getInstance().getFlashMode();
		int focusNew = CameraController.getInstance().getFocusMode();
		int isoNew = CameraController.getInstance().getISOMode();
		
		// Save new params value
		if(wbNew != -1 && WBModeButtons.containsKey(wbNew))
			mWB = wbNew;
		else if(WBModeButtons.containsKey(CameraParameters.WB_MODE_AUTO))
			mWB = CameraParameters.WB_MODE_AUTO;
		else if(CameraController.getInstance().isWhiteBalanceSupported())
			mWB = CameraController.getInstance().getSupportedWhiteBalance()[0];
		else
			mWB = -1;
		
		if(focusNew != -1 && FocusModeButtons.containsKey(focusNew))
			mFocusMode = focusNew;
		else if(FocusModeButtons.containsKey(CameraParameters.AF_MODE_AUTO))
			mFocusMode = CameraParameters.AF_MODE_AUTO;
		else if(CameraController.getInstance().isFocusModeSupported())
			mFocusMode = CameraController.getInstance().getSupportedFocusModes()[0];
		else
			mFocusMode = -1;
		
		if(flashNew != -1 && FlashModeButtons.containsKey(flashNew))
			mFlashMode = flashNew;
		else if(FocusModeButtons.containsKey(CameraParameters.FLASH_MODE_AUTO))
			mFlashMode = CameraParameters.FLASH_MODE_AUTO;
		else if(CameraController.getInstance().isFlashModeSupported())
			mFlashMode = CameraController.getInstance().getSupportedFlashModes()[0];
		else
			mFlashMode = -1;
		
		if(isoNew != -1 && ISOButtons.containsKey(isoNew))
			mISO = isoNew;
		else if(ISOButtons.containsKey(CameraParameters.ISO_AUTO))
			mISO = CameraParameters.ISO_AUTO;
		else if(CameraController.getInstance().getSupportedISO() != null)
			mISO = CameraController.getInstance().getSupportedISO()[0];
		else
			mISO = -1;

		// Set appropriate params buttons pressed
		setButtonSelected(SceneModeButtons, mSceneMode);
		setButtonSelected(WBModeButtons, mWB);
		setButtonSelected(FocusModeButtons, mFocusMode);
		setButtonSelected(FlashModeButtons, mFlashMode);
		setButtonSelected(ISOButtons, mISO);

		// Update icons for other camera parameter buttons
		RotateImageView but = null;
		int icon_id = -1;
		if (mWB != -1)
		{
			but = (RotateImageView) topMenuButtons.get(MODE_WB);
			icon_id = icons_wb.get(mWB);
			but.setImageResource(icon_id);
			preferences.edit().putInt(MainScreen.sWBModePref, mWB).commit();

			Message msg = new Message();
			msg.arg1 = PluginManager.MSG_WB_CHANGED;
			msg.what = PluginManager.MSG_BROADCAST;
			MainScreen.H.sendMessage(msg);
		}
		if (mFocusMode != -1)
		{				
			try {
				but = (RotateImageView) topMenuButtons.get(MODE_FOCUS);
				icon_id = icons_focus.get(mFocusMode);
				but.setImageResource(icon_id);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("setSceneMode", "icons_focus.get exception: " + e.getMessage());
			}
			
			preferences
					.edit()
					.putInt(
							CameraController.isFrontCamera() ? MainScreen.sRearFocusModePref
									: MainScreen.sFrontFocusModePref, mFocusMode)
					.commit();

			Message msg = new Message();
			msg.arg1 = PluginManager.MSG_FOCUS_CHANGED;
			msg.what = PluginManager.MSG_BROADCAST;
			MainScreen.H.sendMessage(msg);
		}
		if (mFlashMode != -1)
		{
			but = (RotateImageView) topMenuButtons.get(MODE_FLASH);
			icon_id = icons_flash.get(mFlashMode);
			but.setImageResource(icon_id);
			preferences.edit().putInt(MainScreen.sFlashModePref, mFlashMode)
					.commit();

			Message msg = new Message();
			msg.arg1 = PluginManager.MSG_FLASH_CHANGED;
			msg.what = PluginManager.MSG_BROADCAST;
			MainScreen.H.sendMessage(msg);
		}
		if (mISO != -1)
		{
			but = (RotateImageView) topMenuButtons.get(MODE_ISO);
			icon_id = icons_iso.get(mISO);
			but.setImageResource(icon_id);
			preferences.edit().putInt(MainScreen.sISOPref, mISO).commit();

			Message msg = new Message();
			msg.arg1 = PluginManager.MSG_ISO_CHANGED;
			msg.what = PluginManager.MSG_BROADCAST;
			MainScreen.H.sendMessage(msg);
		}
		preferences.edit().putInt(MainScreen.sSceneModePref, newMode).commit();


		but = (RotateImageView) topMenuButtons.get(MODE_SCENE);
		icon_id = icons_scene.get(mSceneMode);
		but.setImageResource(icon_id);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_SCENE_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void setWhiteBalance(int newMode)
	{
		if (newMode != -1)
		{
			if ((mSceneMode != CameraParameters.SCENE_MODE_AUTO || mWB != newMode)
					&& CameraController.getInstance().isSceneModeSupported())
			{
				setSceneMode(CameraParameters.SCENE_MODE_AUTO);
			}
			
			CameraController.getInstance().setCameraWhiteBalance(newMode);
			
			mWB = newMode;
			setButtonSelected(WBModeButtons, mWB);

			preferences.edit().putInt(MainScreen.sWBModePref, newMode).commit();
		}

		RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_WB);
		int icon_id = icons_wb.get(mWB);
		but.setImageResource(icon_id);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_WB_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void setFocusMode(int newMode)
	{
		if (newMode != -1 )
		{
			if (mSceneMode != CameraParameters.SCENE_MODE_AUTO && mFocusMode != CameraParameters.AF_MODE_AUTO)
			{
				if (CameraController.getInstance().isSceneModeSupported())
					setSceneMode(CameraParameters.SCENE_MODE_AUTO);
			}

			CameraController.getInstance().setCameraFocusMode(newMode);
			
			mFocusMode = newMode;
			setButtonSelected(FocusModeButtons, mFocusMode);

			preferences.edit().putInt(CameraController.isFrontCamera() ? MainScreen.sRearFocusModePref : MainScreen.sFrontFocusModePref, newMode).commit();
		}

		try {
			RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_FOCUS);
			int icon_id = icons_focus.get(mFocusMode);
			but.setImageResource(icon_id);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("setFocusMode", "icons_focus.get exception: " + e.getMessage());
		}
		
		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_FOCUS_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);
		
		MainScreen.setAutoFocusLock(false);
	}

	private void setFlashMode(int newMode)
	{
		if (newMode != -1)
		{
			if (mSceneMode != CameraParameters.SCENE_MODE_AUTO && mFlashMode != CameraParameters.FLASH_MODE_AUTO
					&& CameraController.getInstance().isSceneModeSupported())
				setSceneMode(CameraParameters.SCENE_MODE_AUTO);

			CameraController.getInstance().setCameraFlashMode(newMode);
			mFlashMode = newMode;
			setButtonSelected(FlashModeButtons, mFlashMode);

			preferences.edit().putInt(MainScreen.sFlashModePref, newMode).commit();
		}

		RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_FLASH);
		int icon_id = icons_flash.get(mFlashMode);
		but.setImageResource(icon_id);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_FLASH_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void setISO(int newMode)
	{
		if (newMode != -1)
		{
			if (mSceneMode != CameraParameters.SCENE_MODE_AUTO && mFlashMode != CameraParameters.FLASH_MODE_AUTO
					&& CameraController.getInstance().isSceneModeSupported())
				setSceneMode(CameraParameters.SCENE_MODE_AUTO);

			CameraController.getInstance().setCameraISO(newMode);
			mISO = newMode;
			setButtonSelected(ISOButtons, mISO);

			preferences.edit().putInt(MainScreen.sISOPref, newMode).commit();
		}

		RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_ISO);
		int icon_id = icons_iso.get(mISO);
		but.setImageResource(icon_id);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_ISO_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}
	
	private void setMeteringMode(int newMode)
	{
		if (newMode != -1 && mMeteringMode != newMode)
		{
			mMeteringMode = newMode;
			setButtonSelected(MeteringModeButtons, mMeteringMode);

			preferences.edit().putInt(MainScreen.sMeteringModePref, newMode).commit();
			MainScreen.thiz.setCameraMeteringMode(newMode);
		}

		RotateImageView but = (RotateImageView) topMenuButtons.get(MODE_MET);
		int icon_id = icons_metering.get(mMeteringMode);
		but.setImageResource(icon_id);

		initSettingsMenu();
		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);
	}

	// Hide all pop-up layouts
	private void unselectPrimaryTopMenuButtons(int iTopMenuButtonSelected)
	{
		Set<Integer> keys = topMenuButtons.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			Integer it_button = it.next();
			(topMenuButtons.get(it_button)).setPressed(false);
			(topMenuButtons.get(it_button)).setSelected(false);
		}

		if ((iTopMenuButtonSelected > -1)
				&& topMenuButtons.containsKey(iTopMenuButtonSelected)) {
			RotateImageView pressed_button = (RotateImageView) topMenuButtons
					.get(iTopMenuButtonSelected);
			pressed_button.setPressed(false);
			pressed_button.setSelected(true);
		}
	}
	
	private int findTopMenuButtonIndex(View view)
	{
		Set<Integer> keys = topMenuButtons.keySet();
		Iterator<Integer> it = keys.iterator();
		Integer pressed_button = -1;
		while (it.hasNext()) {
			Integer it_button = it.next();
			View v = topMenuButtons.get(it_button);
			if (v == view) {
				pressed_button = it_button;
				break;
			}
		}
		
		return pressed_button;
	}

	private void topMenuButtonPressed(int iTopMenuButtonPressed)
	{
		Set<Integer> keys = topMenuButtons.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			Integer it_button = it.next();
			if (isTopMenuButtonEnabled(it_button))
				(topMenuButtons.get(it_button))
						.setBackgroundDrawable(MainScreen.mainContext
								.getResources().getDrawable(
										R.drawable.transparent_background));
		}

		if ((iTopMenuButtonPressed > -1 && isTopMenuButtonEnabled(iTopMenuButtonPressed))
				&& topMenuButtons.containsKey(iTopMenuButtonPressed)) {
			RotateImageView pressed_button = (RotateImageView) topMenuButtons
					.get(iTopMenuButtonPressed);
			// pressed_button.setBackgroundEnabled(true);
			pressed_button
					.setBackgroundDrawable(MainScreen.mainContext
							.getResources()
							.getDrawable(
									R.drawable.almalence_gui_button_background_pressed));

		}
	}

	public boolean isTopMenuButtonEnabled(int iTopMenuButtonKey) {
		boolean isEnabled = false;
		switch (iTopMenuButtonKey) {
		case MODE_SCENE:
			if (isSceneEnabled)
				isEnabled = true;
			break;
		case MODE_WB:
			if (isWBEnabled)
				isEnabled = true;
			break;
		case MODE_FOCUS:
			if (isFocusEnabled)
				isEnabled = true;
			break;
		case MODE_FLASH:
			if (isFlashEnabled)
				isEnabled = true;
			break;
		case MODE_EV:
			if (isEVEnabled)
				isEnabled = true;
			break;
		case MODE_ISO:
			if (isIsoEnabled)
				isEnabled = true;
			break;
		case MODE_MET:
			if (isMeteringEnabled)
				isEnabled = true;
			break;			
		case MODE_CAM:
			if (isCameraChangeEnabled)
				isEnabled = true;
			break;		
		}

		return isEnabled;
	}

	// Hide all pop-up layouts
	@Override
	public void hideSecondaryMenus() {
		if (!isSecondaryMenusVisible())
			return;

		guiView.findViewById(R.id.evLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.scenemodeLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.wbLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.focusmodeLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.flashmodeLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.isoLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.meteringLayout).setVisibility(View.GONE);

		guiView.findViewById(R.id.modeLayout).setVisibility(View.GONE);
		guiView.findViewById(R.id.vfLayout).setVisibility(View.GONE);

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_UNLOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	public boolean isSecondaryMenusVisible() {
		if (guiView.findViewById(R.id.evLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.scenemodeLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.wbLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.focusmodeLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.flashmodeLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.isoLayout).getVisibility() == View.VISIBLE
				|| guiView.findViewById(R.id.meteringLayout).getVisibility() == View.VISIBLE)
			return true;
		return false;
	}

	// Decide what layout to show when some main's parameters button is clicked
	private void showParams(int iButton) {
		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int modeHeightByWidth = (int) (width / 3 - 5 * metrics.density);
		int modeHeightByDimen = Math.round(MainScreen.thiz.getResources()
				.getDimension(R.dimen.gridImageSize)
				+ MainScreen.thiz.getResources().getDimension(
						R.dimen.gridTextLayoutHeight));

		int modeHeight = modeHeightByDimen > modeHeightByWidth ? modeHeightByWidth
				: modeHeightByDimen;
		
		//TODO: use this size calculation to get real square view
//		int paramSize = Math.round(MainScreen.thiz.getResources()
//				.getDimension(R.dimen.gridElementSize));

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.WRAP_CONTENT, modeHeight);

		List<View> views = null;
		switch (iButton) {
		case MODE_SCENE:
			views = activeScene;
			break;
		case MODE_WB:
			views = activeWB;
			break;
		case MODE_FOCUS:
			views = activeFocus;
			break;
		case MODE_FLASH:
			views = activeFlash;
			break;
		case MODE_ISO:
			views = activeISO;
			break;
		case MODE_MET:
			views = activeMetering;
			break;			
		}

		if (views != null) {
			for (int i = 0; i < views.size(); i++) {
				View param = views.get(i);
				if(param != null)
					param.setLayoutParams(params);
			}
		}

		switch (iButton) {
		case MODE_EV:
			guiView.findViewById(R.id.evLayout).setVisibility(View.VISIBLE);
			break;
		case MODE_SCENE:
			guiView.findViewById(R.id.scenemodeLayout).setVisibility(
					View.VISIBLE);
			break;
		case MODE_WB:
			guiView.findViewById(R.id.wbLayout).setVisibility(View.VISIBLE);
			break;
		case MODE_FOCUS:
			guiView.findViewById(R.id.focusmodeLayout).setVisibility(
					View.VISIBLE);
			break;
		case MODE_FLASH:
			guiView.findViewById(R.id.flashmodeLayout).setVisibility(
					View.VISIBLE);
			break;
		case MODE_ISO:
			guiView.findViewById(R.id.isoLayout).setVisibility(View.VISIBLE);
			break;
		case MODE_MET:
			guiView.findViewById(R.id.meteringLayout).setVisibility(View.VISIBLE);
			break;			
		}

		quickControlsVisible = true;

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_CONTROL_LOCKED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	private void setButtonSelected(Map<Integer, View> buttonsList,
			int mode_id) {
		Set<Integer> keys = buttonsList.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			int it_button = it.next();
			((RelativeLayout) buttonsList.get(it_button)).setPressed(false);
		}

		if (buttonsList.containsKey(mode_id)) {
			RelativeLayout pressed_button = (RelativeLayout) buttonsList
					.get(mode_id);
			pressed_button.setPressed(false);
		}
	}

	private void setCameraParameterValue(int iParameter, int sValue) {
		switch (iParameter) {
		case MODE_SCENE:
			mSceneMode = sValue;
			break;
		case MODE_WB:
			mWB = sValue;
			break;
		case MODE_FOCUS:
			mFocusMode = sValue;
			break;
		case MODE_FLASH:
			mFlashMode = sValue;
			break;
		case MODE_ISO:
			mISO = sValue;
			break;
		case MODE_MET:
			mMeteringMode = sValue;
			break;			
		}
	}

	/************************************************************************************
	 * 
	 * Methods for adding Viewfinder plugin's controls and informational
	 * controls from other plugins
	 * 
	 ***********************************************************************************/
	private void addInfoControl(View info_control) {
		// Calculate appropriate size of added plugin's view
		android.widget.LinearLayout.LayoutParams viewLayoutParams = (android.widget.LinearLayout.LayoutParams) info_control
				.getLayoutParams();
		viewLayoutParams = this.getTunedLinearLayoutParams(info_control,
				viewLayoutParams, iInfoViewMaxWidth, iInfoViewMaxHeight);

		((LinearLayout) guiView.findViewById(R.id.infoLayout)).addView(
				info_control, viewLayoutParams);
	}

	// Public interface for all plugins
	// Automatically decide where to put view and correct view's size if
	// necessary
	@Override
	protected void addPluginViews(Map<View, Plugin.ViewfinderZone> views_map) {
		Set<View> view_set = views_map.keySet();
		Iterator<View> it = view_set.iterator();
		while (it.hasNext()) {
			
			try {
				View view = it.next();
				Plugin.ViewfinderZone desire_zone = views_map.get(view);
	
				android.widget.RelativeLayout.LayoutParams viewLayoutParams = (android.widget.RelativeLayout.LayoutParams) view
						.getLayoutParams();
				viewLayoutParams = this.getTunedPluginLayoutParams(view,
						desire_zone, viewLayoutParams);
	
				if (viewLayoutParams == null) // No free space on plugin's layout
					return;
	
				view.setLayoutParams(viewLayoutParams);
				if (desire_zone == Plugin.ViewfinderZone.VIEWFINDER_ZONE_FULLSCREEN
						|| desire_zone == Plugin.ViewfinderZone.VIEWFINDER_ZONE_CENTER)
					((RelativeLayout) guiView.findViewById(R.id.fullscreenLayout))
							.addView(view, 0,
									(ViewGroup.LayoutParams) viewLayoutParams);
				else
					((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
							.addView(view, viewLayoutParams);
			}
			catch (Exception e) {
				e.printStackTrace();
				Log.e("Almalence GUI", "addPluginViews exception: " + e.getMessage());
			}
		}
	}

	@Override
	public void addViewQuick(View view, Plugin.ViewfinderZone desire_zone) {
		android.widget.RelativeLayout.LayoutParams viewLayoutParams = (android.widget.RelativeLayout.LayoutParams) view
				.getLayoutParams();
		viewLayoutParams = this.getTunedPluginLayoutParams(view, desire_zone,
				viewLayoutParams);

		if (viewLayoutParams == null) // No free space on plugin's layout
			return;

		view.setLayoutParams(viewLayoutParams);
		if (desire_zone == Plugin.ViewfinderZone.VIEWFINDER_ZONE_FULLSCREEN
				|| desire_zone == Plugin.ViewfinderZone.VIEWFINDER_ZONE_CENTER)
			((RelativeLayout) guiView.findViewById(R.id.fullscreenLayout))
					.addView(view, 0, (ViewGroup.LayoutParams) viewLayoutParams);
		else
			((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
					.addView(view, viewLayoutParams);
	}

	@Override
	protected void removePluginViews(Map<View, Plugin.ViewfinderZone> views_map) {
		Set<View> view_set = views_map.keySet();
		Iterator<View> it = view_set.iterator();
		while (it.hasNext()) {
			View view = it.next();
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);

			((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
					.removeView(view);
		}
	}

	@Override
	public void removeViewQuick(View view) {
		if (view == null)
			return;
		if (view.getParent() != null)
			((ViewGroup) view.getParent()).removeView(view);

		((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
				.removeView(view);
	}

	/* Private section for adding plugin's views */

	// INFO VIEW SECTION
	@Override
	protected void addInfoView(View view,
			android.widget.LinearLayout.LayoutParams viewLayoutParams) {
		if (((LinearLayout) guiView.findViewById(R.id.infoLayout))
				.getChildCount() != 0)
			viewLayoutParams.topMargin = 4;

		((LinearLayout) guiView.findViewById(R.id.infoLayout)).addView(view,
				viewLayoutParams);
	}

	@Override
	protected void removeInfoView(View view) {
		if (view.getParent() != null)
			((ViewGroup) view.getParent()).removeView(view);
		((LinearLayout) guiView.findViewById(R.id.infoLayout)).removeView(view);
	}

	protected boolean isAnyViewOnViewfinder() {
		RelativeLayout pluginsLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.pluginsLayout);
		LinearLayout infoLayout = (LinearLayout) MainScreen.thiz
				.findViewById(R.id.infoLayout);
		RelativeLayout fullScreenLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.fullscreenLayout);

		return pluginsLayout.getChildCount() > 0
				|| infoLayout.getChildCount() > 0
				|| fullScreenLayout.getChildCount() > 0;
	}

	// selected mode - to use in onClick
	public Mode tmpActiveMode;

	// controls if info about new mode shown or not. to prevent from double info
	// private boolean modeChangedShown=false;
	private void initModeList() {
		modeViews.clear();
		buttonModeViewAssoc.clear();

		List<Mode> hash = ConfigParser.getInstance().getList();
		Iterator<Mode> it = hash.iterator();

		int mode_number = 0;
		while (it.hasNext()) {
			Mode tmp = it.next();

			LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();
			View mode = inflator.inflate(
					R.layout.gui_almalence_select_mode_grid_element, null,
					false);
			// set some mode icon
			((ImageView) mode.findViewById(R.id.modeImage))
					.setImageResource(MainScreen.thiz.getResources()
							.getIdentifier(tmp.icon, "drawable",
									MainScreen.thiz.getPackageName()));

			int id = MainScreen.thiz.getResources().getIdentifier(tmp.modeName,
					"string", MainScreen.thiz.getPackageName());
			String modename = MainScreen.thiz.getResources().getString(id);

			//final boolean isFirstMode = mode_number == 0? true : false;
			((TextView) mode.findViewById(R.id.modeText)).setText(modename);
			if (mode_number==0)
			mode.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_CANCEL)// && isFirstMode)
					{
						return changeMode(v);
					}
						return false;
				}
				
			});
			mode.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					changeMode(v);
				}
			});
			buttonModeViewAssoc.put(mode, tmp.modeID);
			modeViews.add(mode);

			//select active mode in grid with frame
			if (PluginManager.getInstance().getActiveModeID() == tmp.modeID) {
				mode.findViewById(R.id.modeSelectLayout2).setBackgroundResource(
						R.drawable.thumbnail_background_selected_inner);
			}
			
			mode_number++;
		}

		modeAdapter.Elements = modeViews;
	}
	
	private boolean changeMode(View v)
	{
		hideModeList();

		// get mode associated with pressed button
		String key = buttonModeViewAssoc.get(v);
		Mode mode = ConfigParser.getInstance().getMode(key);
		// if selected the same mode - do not reinitialize camera
		// and other objects.
		if (PluginManager.getInstance().getActiveModeID() == mode.modeID)
			return false;

		tmpActiveMode = mode;

		if (mode.modeID.equals("video"))
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
			if (prefs.getBoolean("videoStartStandardPref", false))
			{
				PluginManager.getInstance().onPause(true);
				Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
			    //MainScreen.thiz.startActivityForResult(intent, 111);
				MainScreen.thiz.startActivity(intent);
			    return true;
			}
		}
		
		// <!-- -+-
		if (!MainScreen.thiz.checkLaunches(tmpActiveMode))
			return false;
		//-+- -->
		
		new CountDownTimer(100, 100) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				PluginManager.getInstance().switchMode(
						tmpActiveMode);
			}
		}.start();

		// set modes icon inside mode selection icon
		Bitmap bm = null;
		Bitmap iconBase = BitmapFactory.decodeResource(
				MainScreen.mainContext.getResources(),
				R.drawable.gui_almalence_select_mode);
		int id = MainScreen.thiz.getResources().getIdentifier(
				mode.icon, "drawable",
				MainScreen.thiz.getPackageName());
		Bitmap iconOverlay = BitmapFactory.decodeResource(
				MainScreen.mainContext.getResources(),
				MainScreen.thiz.getResources().getIdentifier(
						mode.icon, "drawable",
						MainScreen.thiz.getPackageName()));
		iconOverlay = Bitmap.createScaledBitmap(iconOverlay,
				(int) (iconBase.getWidth() / 1.8),
				(int) (iconBase.getWidth() / 1.8), false);

		bm = mergeImage(iconBase, iconOverlay);
		bm = Bitmap
				.createScaledBitmap(
						bm,
						(int) (MainScreen.mainContext
								.getResources()
								.getDimension(R.dimen.mainButtonHeightSelect)),
						(int) (MainScreen.mainContext
								.getResources()
								.getDimension(R.dimen.mainButtonHeightSelect)),
						false);
		((RotateImageView) guiView
				.findViewById(R.id.buttonSelectMode))
				.setImageBitmap(bm);

		int rid = MainScreen.thiz.getResources().getIdentifier(
				tmpActiveMode.howtoText, "string",
				MainScreen.thiz.getPackageName());
		String howto = "";
		if (rid != 0)
			howto = MainScreen.thiz.getResources().getString(rid);
		// show toast on mode changed
		showToast(
				v,
				Toast.LENGTH_SHORT,
				Gravity.CENTER,
				((TextView) v.findViewById(R.id.modeText))
						.getText()
						+ " "
						+ MainScreen.thiz.getResources().getString(
								R.string.almalence_gui_selected)
						+ (tmpActiveMode.howtoText.isEmpty() ? ""
								: "\n") + howto, false, true);
		return false;
	}
	

	public void showToast(final View v, final int showLength,
			final int gravity, final String toastText,
			final boolean withBackground, final boolean startOffset) {
		MainScreen.thiz.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final RelativeLayout modeLayout = (RelativeLayout) guiView
						.findViewById(R.id.changeModeToast);

				DisplayMetrics metrics = new DisplayMetrics();
				MainScreen.thiz.getWindowManager().getDefaultDisplay()
						.getMetrics(metrics);
				int screen_height = metrics.heightPixels;
				int screen_width = metrics.widthPixels;

				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) modeLayout
						.getLayoutParams();
				int[] rules = lp.getRules();
				if (gravity == Gravity.CENTER
						|| (mDeviceOrientation != 0 && mDeviceOrientation != 360))
					lp.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
				else {
					rules[RelativeLayout.CENTER_IN_PARENT] = 0;
					lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);

					View shutter = guiView.findViewById(R.id.buttonShutter);
					int shutter_height = shutter.getHeight()
							+ shutter.getPaddingBottom();
					lp.setMargins(
							0,
							(int) (screen_height
									- MainScreen.thiz.getResources()
											.getDimension(
													R.dimen.paramsLayoutHeight) - shutter_height),
							0, shutter_height);
				}

				if (withBackground)
					modeLayout.setBackgroundDrawable(MainScreen.thiz
							.getResources().getDrawable(
									R.drawable.almalence_gui_toast_background));
				else
					modeLayout.setBackgroundDrawable(null);
				RotateImageView imgView = (RotateImageView) modeLayout
						.findViewById(R.id.selectModeIcon);
				TextView text = (TextView) modeLayout
						.findViewById(R.id.selectModeText);

				if (v != null) {
					RelativeLayout.LayoutParams pm = (RelativeLayout.LayoutParams) imgView
							.getLayoutParams();
					pm.width = (int) MainScreen.thiz.getResources()
							.getDimension(R.dimen.mainButtonHeight);
					pm.height = (int) MainScreen.thiz.getResources()
							.getDimension(R.dimen.mainButtonHeight);
					imgView.setImageDrawable(((ImageView) v
							.findViewById(R.id.modeImage)).getDrawable());
				} else {
					RelativeLayout.LayoutParams pm = (RelativeLayout.LayoutParams) imgView
							.getLayoutParams();
					pm.width = 0;
					pm.height = 0;
				}
				text.setText("  " + toastText);
				text.setTextSize(16);
				text.setTextColor(Color.WHITE);

				text.setMaxWidth((int) Math.round(screen_width * 0.7));

				Animation visible_alpha = new AlphaAnimation(0, 1);
				visible_alpha.setStartOffset(startOffset ? 2000 : 0);
				visible_alpha.setDuration(1000);
				visible_alpha.setRepeatCount(0);

				final Animation invisible_alpha = new AlphaAnimation(1, 0);
				invisible_alpha
						.setStartOffset(showLength == Toast.LENGTH_SHORT ? 1000
								: 3000);
				invisible_alpha.setDuration(1000);
				invisible_alpha.setRepeatCount(0);

				visible_alpha.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						modeLayout.startAnimation(invisible_alpha);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});

				invisible_alpha.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						modeLayout.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});

				modeLayout.setRotation(-mDeviceOrientation);
				modeLayout.setVisibility(View.VISIBLE);
				modeLayout.bringToFront();
				modeLayout.startAnimation(visible_alpha);
			}
		});
	}

	// helper function to draw one bitmap on another
	private Bitmap mergeImage(Bitmap base, Bitmap overlay) {
		int adWDelta = (int) (base.getWidth() - overlay.getWidth()) / 2;
		int adHDelta = (int) (base.getHeight() - overlay.getHeight()) / 2;

		Bitmap mBitmap = Bitmap.createBitmap(base.getWidth(), base.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		canvas.drawBitmap(base, 0, 0, null);
		canvas.drawBitmap(overlay, adWDelta, adHDelta, null);

		return mBitmap;
	}

	// Supplementary methods to find suitable size for plugin's view
	// For LINEARLAYOUT
	private android.widget.RelativeLayout.LayoutParams getTunedRelativeLayoutParams(
			View view, android.widget.RelativeLayout.LayoutParams currParams,
			int goodWidth, int goodHeight) {
		int viewHeight, viewWidth;

		if (currParams != null) {
			viewHeight = currParams.height;
			viewWidth = currParams.width;

			if ((viewHeight > goodHeight || viewHeight <= 0) && goodHeight > 0)
				viewHeight = goodHeight;

			if ((viewWidth > goodWidth || viewWidth <= 0) && goodWidth > 0)
				viewWidth = goodWidth;

			currParams.width = viewWidth;
			currParams.height = viewHeight;

			view.setLayoutParams(currParams);
		} else {
			currParams = new android.widget.RelativeLayout.LayoutParams(
					goodWidth, goodHeight);
			view.setLayoutParams(currParams);
		}

		return currParams;
	}

	// For RELATIVELAYOUT
	private android.widget.LinearLayout.LayoutParams getTunedLinearLayoutParams(
			View view, android.widget.LinearLayout.LayoutParams currParams,
			int goodWidth, int goodHeight) {
		if (currParams != null) {
			int viewHeight = currParams.height;
			int viewWidth = currParams.width;

			if ((viewHeight > goodHeight || viewHeight <= 0) && goodHeight > 0)
				viewHeight = goodHeight;

			if ((viewWidth > goodWidth || viewWidth <= 0) && goodWidth > 0)
				viewWidth = goodWidth;

			currParams.width = viewWidth;
			currParams.height = viewHeight;

			view.setLayoutParams(currParams);
		} else {
			currParams = new android.widget.LinearLayout.LayoutParams(
					goodWidth, goodHeight);
			view.setLayoutParams(currParams);
		}

		return currParams;
	}

	/************************************************************************************************
	 * >>>>>>>>>
	 * 
	 * DEFINITION OF PLACE ON LAYOUT FOR PLUGIN'S VIEWS
	 * 
	 * >>>>>>>>> BEGIN
	 ***********************************************************************************************/

	protected Plugin.ViewfinderZone zones[] = {
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_TOP_LEFT,
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_TOP_RIGHT,
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_CENTER_RIGHT,
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_BOTTOM_RIGHT,
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_BOTTOM_LEFT,
			Plugin.ViewfinderZone.VIEWFINDER_ZONE_CENTER_LEFT };

	protected android.widget.RelativeLayout.LayoutParams getTunedPluginLayoutParams(
			View view, Plugin.ViewfinderZone desire_zone,
			android.widget.RelativeLayout.LayoutParams currParams) {
		int left = 0, right = 0, top = 0, bottom = 0;

		RelativeLayout pluginLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.pluginsLayout);

		if (currParams == null)
			currParams = new android.widget.RelativeLayout.LayoutParams(
					getMinPluginViewWidth(), getMinPluginViewHeight());

		switch (desire_zone) {
		case VIEWFINDER_ZONE_TOP_LEFT: {
			left = 0;
			right = currParams.width;
			top = 0;
			bottom = currParams.height;

			currParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		}
			break;
		case VIEWFINDER_ZONE_TOP_RIGHT: {
			left = pluginLayout.getWidth() - currParams.width;
			right = pluginLayout.getWidth();
			top = 0;
			bottom = currParams.height;

			currParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
			break;
		case VIEWFINDER_ZONE_CENTER_LEFT: {
			left = 0;
			right = currParams.width;
			top = pluginLayout.getHeight() / 2 - currParams.height / 2;
			bottom = pluginLayout.getHeight() / 2 + currParams.height / 2;

			currParams.addRule(RelativeLayout.CENTER_VERTICAL);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		}
			break;
		case VIEWFINDER_ZONE_CENTER_RIGHT: {
			left = pluginLayout.getWidth() - currParams.width;
			right = pluginLayout.getWidth();
			top = pluginLayout.getHeight() / 2 - currParams.height / 2;
			bottom = pluginLayout.getHeight() / 2 + currParams.height / 2;

			currParams.addRule(RelativeLayout.CENTER_VERTICAL);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
			break;
		case VIEWFINDER_ZONE_BOTTOM_LEFT: {
			left = 0;
			right = currParams.width;
			top = pluginLayout.getHeight() - currParams.height;
			bottom = pluginLayout.getHeight();

			currParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		}
			break;
		case VIEWFINDER_ZONE_BOTTOM_RIGHT: {
			left = pluginLayout.getWidth() - currParams.width;
			right = pluginLayout.getWidth();
			top = pluginLayout.getHeight() - currParams.height;
			bottom = pluginLayout.getHeight();

			currParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
			break;
		case VIEWFINDER_ZONE_FULLSCREEN: {
			currParams.width = MainScreen.thiz.getPreviewSize() != null ? MainScreen.thiz
					.getPreviewSize().getWidth() : 0;
			currParams.height = MainScreen.thiz.getPreviewSize() != null ? MainScreen.thiz
					.getPreviewSize().getHeight() : 0;
			currParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			// if(currParams.width != currParams.height)
			// {
			// currParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
			// }
			// else
			// {
			// currParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
			// }

			return currParams;
		}
		case VIEWFINDER_ZONE_CENTER: {
			if (currParams.width > iCenterViewMaxWidth)
				currParams.width = iCenterViewMaxWidth;

			if (currParams.height > iCenterViewMaxHeight)
				currParams.height = iCenterViewMaxHeight;

			currParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			// if(currParams.width != currParams.height)
			// {
			// currParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			// }
			// else
			// {
			// currParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
			// int[] rules = currParams.getRules();
			// rules[RelativeLayout.CENTER_IN_PARENT] = 0;
			// }

			return currParams;
		}
		}

		return findFreeSpaceOnLayout(new Rect(left, top, right, bottom),
				currParams, desire_zone);
	}

	protected android.widget.RelativeLayout.LayoutParams findFreeSpaceOnLayout(
			Rect currRect,
			android.widget.RelativeLayout.LayoutParams currParams,
			Plugin.ViewfinderZone currZone) {
		boolean isFree = true;
		Rect childRect;

		// int child_left, child_right, child_top, child_bottom;
		View pluginLayout = MainScreen.thiz.findViewById(R.id.pluginsLayout);

		// Looking in all zones at clockwise direction for free place
		for (int j = Plugin.ViewfinderZone.getInt(currZone), counter = 0; j < zones.length; j++, counter++) {
			isFree = true;
			// Check intersections with already added views
			for (int i = 0; i < ((ViewGroup) pluginLayout).getChildCount(); ++i) {
				View nextChild = ((ViewGroup) pluginLayout).getChildAt(i);

				currRect = getPluginViewRectInZone(currParams, zones[j]);
				childRect = getPluginViewRect(nextChild);

				if (currRect.intersect(childRect)) {
					isFree = false;
					break;
				}
			}

			if (isFree) // Free zone has found
			{
				if (currZone == zones[j]) // Current zone is free
					return currParams;
				else {
					int rules[] = currParams.getRules();
					for (int i = 0; i < rules.length; i++)
						currParams.addRule(i, 0);

					switch (zones[j]) {
					case VIEWFINDER_ZONE_TOP_LEFT: {
						currParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					}
						break;
					case VIEWFINDER_ZONE_TOP_RIGHT: {
						currParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					}
						break;
					case VIEWFINDER_ZONE_CENTER_LEFT: {
						currParams.addRule(RelativeLayout.CENTER_VERTICAL);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					}
						break;
					case VIEWFINDER_ZONE_CENTER_RIGHT: {
						currParams.addRule(RelativeLayout.CENTER_VERTICAL);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					}
						break;
					case VIEWFINDER_ZONE_BOTTOM_LEFT: {
						currParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					}
						break;
					case VIEWFINDER_ZONE_BOTTOM_RIGHT: {
						currParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						currParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					}
						break;
					default:
						break;
					}

					return currParams;
				}
			} else {
				if (counter == zones.length - 1) // Already looked in all zones
													// and they are all full
					return null;

				if (j == zones.length - 1) // If we started not from a first
											// zone (top-left)
					j = -1;
			}
		}

		return null;
	}

	protected Rect getPluginViewRectInZone(
			RelativeLayout.LayoutParams currParams, Plugin.ViewfinderZone zone) {
		int left = -1, right = -1, top = -1, bottom = -1;

		RelativeLayout pluginLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.pluginsLayout);

		int viewWidth = currParams.width;
		int viewHeight = currParams.height;

		int layoutWidth = pluginLayout.getWidth();
		int layoutHeight = pluginLayout.getHeight();

		switch (zone) {
		case VIEWFINDER_ZONE_TOP_LEFT: {
			left = 0;
			right = viewWidth;
			top = 0;
			bottom = viewHeight;
		}
			break;
		case VIEWFINDER_ZONE_TOP_RIGHT: {
			left = layoutWidth - viewWidth;
			right = layoutWidth;
			top = 0;
			bottom = viewHeight;
		}
			break;
		case VIEWFINDER_ZONE_CENTER_LEFT: {
			left = 0;
			right = viewWidth;
			top = layoutHeight / 2 - viewHeight / 2;
			bottom = layoutHeight / 2 + viewHeight / 2;
		}
			break;
		case VIEWFINDER_ZONE_CENTER_RIGHT: {
			left = layoutWidth - viewWidth;
			right = layoutWidth;
			top = layoutHeight / 2 - viewHeight / 2;
			bottom = layoutHeight / 2 + viewHeight / 2;
		}
			break;
		case VIEWFINDER_ZONE_BOTTOM_LEFT: {
			left = 0;
			right = viewWidth;
			top = layoutHeight - viewHeight;
			bottom = layoutHeight;
		}
			break;
		case VIEWFINDER_ZONE_BOTTOM_RIGHT: {
			left = layoutWidth - viewWidth;
			right = layoutWidth;
			top = layoutHeight - viewHeight;
			bottom = layoutHeight;
		}
			break;
		default:
			break;
		}

		return new Rect(left, top, right, bottom);
	}

	protected Rect getPluginViewRect(View view) {
		int left = -1, right = -1, top = -1, bottom = -1;

		RelativeLayout pluginLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.pluginsLayout);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		int rules[] = lp.getRules();

		int viewWidth = lp.width;
		int viewHeight = lp.height;

		int layoutWidth = pluginLayout.getWidth();
		int layoutHeight = pluginLayout.getHeight();

		// Get X coordinates
		if (rules[RelativeLayout.ALIGN_PARENT_LEFT] != 0) {
			left = 0;
			right = viewWidth;
		} else if (rules[RelativeLayout.ALIGN_PARENT_RIGHT] != 0) {
			left = layoutWidth - viewWidth;
			right = layoutWidth;
		}

		// Get Y coordinates
		if (rules[RelativeLayout.ALIGN_PARENT_TOP] != 0) {
			top = 0;
			bottom = viewHeight;
		} else if (rules[RelativeLayout.ALIGN_PARENT_BOTTOM] != 0) {
			top = layoutHeight - viewHeight;
			bottom = layoutHeight;
		} else if (rules[RelativeLayout.CENTER_VERTICAL] != 0) {
			top = layoutHeight / 2 - viewHeight / 2;
			bottom = layoutHeight / 2 + viewHeight / 2;
		}

		return new Rect(left, top, right, bottom);
	}

	// Supplementary methods for getting appropriate sizes for plugin's views
	@Override
	public int getMaxPluginViewHeight() {
		return ((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
				.getHeight() / 3;
	}

	@Override
	public int getMaxPluginViewWidth() {
		return ((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
				.getWidth() / 2;
	}

	@Override
	public int getMinPluginViewHeight() {
		return ((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
				.getHeight() / 12;
	}

	@Override
	public int getMinPluginViewWidth() {
		return ((RelativeLayout) guiView.findViewById(R.id.pluginsLayout))
				.getWidth() / 4;
	}

	/************************************************************************************************
	 * <<<<<<<<<<
	 * 
	 * DEFINITION OF PLACE ON LAYOUT FOR PLUGIN'S VIEWS
	 * 
	 * <<<<<<<<<< END
	 ***********************************************************************************************/

	private static float X = 0;

	private static float Xprev = 0;

	private static float Xoffset = 0;

	private static float XtoLeftInvisible = 0;
	private static float XtoLeftVisible = 0;

	private static float XtoRightInvisible = 0;
	private static float XtoRightVisible = 0;

	private MotionEvent prevEvent;
	private MotionEvent downEvent;
	private boolean scrolling = false;

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// hide hint screen
		if (guiView.findViewById(R.id.hintLayout).getVisibility() == View.VISIBLE) {
			if (event.getAction() == MotionEvent.ACTION_UP)
				guiView.findViewById(R.id.hintLayout).setVisibility(
						View.INVISIBLE);
			return true;
		}

		if (view == (LinearLayout) guiView.findViewById(R.id.evLayout)
				|| lockControls)
			return true;

		// to possibly slide-out top panel
		if (view == MainScreen.thiz.preview
				|| view == (View) MainScreen.thiz
						.findViewById(R.id.mainLayout1))
			((Panel) guiView.findViewById(R.id.topPanel)).touchListener
					.onTouch(view, event);

		else if (view.getParent() == (View) MainScreen.thiz
				.findViewById(R.id.paramsLayout) && !quickControlsChangeVisible) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				downEvent = MotionEvent.obtain(event);
				prevEvent = MotionEvent.obtain(event);
				scrolling = false;

				topMenuButtonPressed(findTopMenuButtonIndex(view));

				return false;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				topMenuButtonPressed(-1);
				if (scrolling == true)
					((Panel) guiView.findViewById(R.id.topPanel)).touchListener
							.onTouch(view, event);
				scrolling = false;
				if (prevEvent == null || downEvent == null)
					return false;
				if (prevEvent.getAction() == MotionEvent.ACTION_DOWN)
					return false;
				if (prevEvent.getAction() == MotionEvent.ACTION_MOVE) {
					if ((event.getY() - downEvent.getY()) < 50)
						return false;
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE
					&& scrolling == false) {
				if (downEvent == null)
					return false;
				if ((event.getY() - downEvent.getY()) < 50)
					return false;
				else {
					scrolling = true;
					((Panel) guiView.findViewById(R.id.topPanel)).touchListener
							.onTouch(view, downEvent);
				}
			}
			((Panel) guiView.findViewById(R.id.topPanel)).touchListener
					.onTouch(view, event);
		}

		// to allow quickControl's to process onClick, onLongClick
		if (view.getParent() == (View) MainScreen.thiz
				.findViewById(R.id.paramsLayout)) {
			return false;
		}

		boolean isMenuOpened = false;
		if (quickControlsChangeVisible || modeSelectorVisible
				|| settingsControlsVisible || isSecondaryMenusVisible())
			isMenuOpened = true;

		if (quickControlsChangeVisible
				&& view.getParent() != (View) MainScreen.thiz
						.findViewById(R.id.paramsLayout))
			closeQuickControlsSettings();

		if (modeSelectorVisible)
			hideModeList();

		hideSecondaryMenus();
		unselectPrimaryTopMenuButtons(-1);

		if (settingsControlsVisible)
			return true;
		else if (!isMenuOpened)
			// call onTouch of active vf and capture plugins
			PluginManager.getInstance().onTouch(view, event);

		RelativeLayout pluginLayout = (RelativeLayout) guiView
				.findViewById(R.id.pluginsLayout);
		RelativeLayout fullscreenLayout = (RelativeLayout) guiView
				.findViewById(R.id.fullscreenLayout);
		LinearLayout paramsLayout = (LinearLayout) guiView
				.findViewById(R.id.paramsLayout);
		LinearLayout infoLayout = (LinearLayout) guiView
				.findViewById(R.id.infoLayout);
		// OnTouch listener to show info and sliding grids
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			X = event.getX();
			Xoffset = X;
			Xprev = X;

			pluginLayout.clearAnimation();
			fullscreenLayout.clearAnimation();
			paramsLayout.clearAnimation();
			infoLayout.clearAnimation();

			topMenuButtonPressed(findTopMenuButtonIndex(view));

			return true;
		}
		case MotionEvent.ACTION_UP: {
			float difX = event.getX();
			if ((X > difX) && (X - difX > 100)) {
				//Log.i("AlamlenceGUI", "Move left");
				sliderLeftEvent();
				return true;
			} else if (X < difX && (difX - X > 100)) {
				//Log.i("AlamlenceGUI", "Move right");
				sliderRightEvent();
				return true;
			}

			pluginLayout.clearAnimation();
			fullscreenLayout.clearAnimation();
			paramsLayout.clearAnimation();
			infoLayout.clearAnimation();

			topMenuButtonPressed(-1);

			break;
		}
		case MotionEvent.ACTION_MOVE: {
			int pluginzoneWidth = guiView.findViewById(R.id.pluginsLayout)
					.getWidth();
			int infozoneWidth = guiView.findViewById(R.id.infoLayout)
					.getWidth();
			int screenWidth = pluginzoneWidth + infozoneWidth;

			float difX = event.getX();

			Animation in_animation;
			Animation out_animation;
			Animation reverseout_animation;
			boolean toLeft;
			if (difX > Xprev) {
				out_animation = new TranslateAnimation(Xprev - Xoffset, difX
						- Xoffset, 0, 0);
				out_animation.setDuration(10);
				out_animation.setInterpolator(new LinearInterpolator());
				out_animation.setFillAfter(true);

				in_animation = new TranslateAnimation(Xprev - Xoffset
						- screenWidth, difX - Xoffset - screenWidth, 0, 0);
				in_animation.setDuration(10);
				in_animation.setInterpolator(new LinearInterpolator());
				in_animation.setFillAfter(true);

				reverseout_animation = new TranslateAnimation(difX
						+ (screenWidth - Xoffset), Xprev
						+ (screenWidth - Xoffset), 0, 0);
				reverseout_animation.setDuration(10);
				reverseout_animation.setInterpolator(new LinearInterpolator());
				reverseout_animation.setFillAfter(true);

				toLeft = false;

				XtoRightInvisible = difX - Xoffset;
				XtoRightVisible = difX - Xoffset - screenWidth;
			} else {
				out_animation = new TranslateAnimation(difX - Xoffset, Xprev
						- Xoffset, 0, 0);
				out_animation.setDuration(10);
				out_animation.setInterpolator(new LinearInterpolator());
				out_animation.setFillAfter(true);

				in_animation = new TranslateAnimation(screenWidth
						+ (Xprev - Xoffset), screenWidth + (difX - Xoffset), 0,
						0);
				in_animation.setDuration(10);
				in_animation.setInterpolator(new LinearInterpolator());
				in_animation.setFillAfter(true);

				reverseout_animation = new TranslateAnimation(Xprev - Xoffset
						- screenWidth, difX - Xoffset - screenWidth, 0, 0);
				reverseout_animation.setDuration(10);
				reverseout_animation.setInterpolator(new LinearInterpolator());
				reverseout_animation.setFillAfter(true);

				toLeft = true;

				XtoLeftInvisible = Xprev - Xoffset;
				XtoLeftVisible = screenWidth + (difX - Xoffset);
			}

			switch (infoSet) {
				case INFO_ALL: {
					pluginLayout.startAnimation(out_animation);
					fullscreenLayout.startAnimation(out_animation);
					infoLayout.startAnimation(out_animation);
					if ((difX < X) || !isAnyViewOnViewfinder())
						paramsLayout.startAnimation(out_animation);
				}
					break;
				case INFO_NO: {
					if ((toLeft && difX < X) || (!toLeft && difX > X))
						fullscreenLayout.startAnimation(in_animation);
					else
						paramsLayout.startAnimation(reverseout_animation);
					if (!toLeft && isAnyViewOnViewfinder()) {
						pluginLayout.startAnimation(in_animation);
						fullscreenLayout.startAnimation(in_animation);
						infoLayout.startAnimation(in_animation);
					} else if (toLeft && difX > X && isAnyViewOnViewfinder()) {
						pluginLayout.startAnimation(reverseout_animation);
						paramsLayout.startAnimation(reverseout_animation);
						infoLayout.startAnimation(reverseout_animation);
					}
				}
					break;
				case INFO_GRID: {
					if (difX > X)//to INFO_NO
						fullscreenLayout.startAnimation(out_animation);
					else//to INFO_PARAMS
					{
						fullscreenLayout.startAnimation(out_animation);
						paramsLayout.startAnimation(in_animation);
					}
				}
					break;
				case INFO_PARAMS: {
					fullscreenLayout.startAnimation(in_animation);
					if (difX > X)
						paramsLayout.startAnimation(out_animation);
					if (toLeft) {
						pluginLayout.startAnimation(in_animation);
						infoLayout.startAnimation(in_animation);
					} else if (difX < X) {
						pluginLayout.startAnimation(reverseout_animation);
						infoLayout.startAnimation(reverseout_animation);
					}
				}
					break;
			}

			Xprev = Math.round(difX);

		}
			break;
		}
		return false;
	}

	private int getRelativeLeft(View myView) {
		if (myView.getParent() == myView.getRootView())
			return myView.getLeft();
		else
			return myView.getLeft()
					+ getRelativeLeft((View) myView.getParent());
	}

	private int getRelativeTop(View myView) {
		if (myView.getParent() == myView.getRootView())
			return myView.getTop();
		else
			return myView.getTop() + getRelativeTop((View) myView.getParent());
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
//		Camera camera = CameraController.getInstance().getCamera();
//		if (null == camera)
//			return;
		
//		Camera.Parameters params = CameraController.getInstance().getCameraParameters();
//		params.setExposureCompensation(iEv);
//		CameraController.getInstance().setCameraParameters(params);
		
		int iEv = progress - CameraController.getInstance().getMaxExposureCompensation();
		CameraController.getInstance().setCameraExposureCompensation(iEv);

		preferences.edit().putInt(MainScreen.sEvPref, iEv).commit();

		mEV = iEv;

		Message msg = new Message();
		msg.arg1 = PluginManager.MSG_EV_CHANGED;
		msg.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(msg);
	}

	@Override
	public void onVolumeBtnExpo(int keyCode)
	{
		if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
			expoMinus();
		else if ( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
			expoPlus();	
	}
	
	private void expoMinus()
	{
		SeekBar evBar = (SeekBar) guiView.findViewById(R.id.evSeekBar);
		if (evBar != null) 
		{
			int minValue = CameraController.getInstance().getMinExposureCompensation();
			int step = 1;
			int currProgress = evBar.getProgress();
			int iEv = currProgress - step;
			if (iEv < 0)
				iEv = 0;
//			Camera camera = CameraController.getInstance().getCamera();
//			if (null != camera) {
//				Camera.Parameters params = CameraController.getInstance().getCameraParameters();
//				params.setExposureCompensation(iEv + minValue);
//				CameraController.getInstance().setCameraParameters(params);
//			}
			
			CameraController.getInstance().setCameraExposureCompensation(iEv + minValue);

			preferences
					.edit()
					.putInt(MainScreen.sEvPref,
							Math.round((iEv + minValue)
									* CameraController.getInstance()
											.getExposureCompensationStep()))
					.commit();

			evBar.setProgress(iEv);
		}
	}
	
	private void expoPlus()
	{
		SeekBar evBar = (SeekBar) guiView.findViewById(R.id.evSeekBar);
		if (evBar != null) {
			int minValue = CameraController.getInstance().getMinExposureCompensation();
			int maxValue = CameraController.getInstance().getMaxExposureCompensation();

			int step = 1;

			int currProgress = evBar.getProgress();
			int iEv = currProgress + step;
			if (iEv > maxValue - minValue)
				iEv = maxValue - minValue;
//			Camera camera = CameraController.getInstance().getCamera();
//			if (null != camera) {
//				Camera.Parameters params = CameraController.getInstance().getCameraParameters();
//				params.setExposureCompensation(iEv + minValue);
//				CameraController.getInstance().setCameraParameters(params);
//			}
			
			CameraController.getInstance().setCameraExposureCompensation(iEv + minValue);

			preferences
					.edit()
					.putInt(MainScreen.sEvPref,
							Math.round((iEv + minValue)
									* CameraController.getInstance()
											.getExposureCompensationStep()))
					.commit();

			evBar.setProgress(iEv);
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	// Slider events handler

	private void sliderLeftEvent() {
		infoSlide(true, XtoLeftVisible, XtoLeftInvisible);
	}

	private void sliderRightEvent() {
		infoSlide(false, XtoRightVisible, XtoRightInvisible);
	}

	// shutter icons setter
	public void setShutterIcon(ShutterButton id) {
		RotateImageView mainButton = ((RotateImageView) guiView.findViewById(R.id.buttonShutter));
		RotateImageView additionalButton = ((RotateImageView) guiView.findViewById(R.id.buttonShutterAdditional));
		LinearLayout buttonShutterContainer = (LinearLayout) guiView.findViewById(R.id.buttonShutterContainer);
		
		// photo
		if (id == ShutterButton.DEFAULT) {
			buttonShutterContainer.setOrientation(LinearLayout.VERTICAL);
			buttonShutterContainer.setPadding(0, 0, 0, 0);
			
			additionalButton.setVisibility(View.GONE);
			
			mainButton.setImageResource(R.drawable.button_shutter);
			int dp = (int) MainScreen.thiz.getResources().getDimension(R.dimen.shutterHeight);
			mainButton.getLayoutParams().width = dp;
			mainButton.getLayoutParams().height = dp;
		} 
		// video
		else {
			buttonShutterContainer.setOrientation(LinearLayout.HORIZONTAL);
			buttonShutterContainer.setPadding(0, Util.dpToPixel(15), 0, 0);
			
			additionalButton.setVisibility(View.VISIBLE);

			int dp = (int) MainScreen.thiz.getResources().getDimension(R.dimen.videoShutterHeight);
			mainButton.getLayoutParams().width = dp;
			mainButton.getLayoutParams().height = dp;
			
			if (id == ShutterButton.RECORDER_START) {
				mainButton.setImageResource(R.drawable.gui_almalence_shutter_video_off);
				additionalButton.setImageResource(R.drawable.gui_almalence_shutter_video_pause);
			}
			else if (id == ShutterButton.RECORDER_STOP) {
				mainButton.setImageResource(R.drawable.gui_almalence_shutter_video_stop);
				additionalButton.setImageResource(R.drawable.gui_almalence_shutter_video_pause);
			} else if (id == ShutterButton.RECORDER_PAUSED) {
				additionalButton.setImageResource(R.drawable.gui_almalence_shutter_video_pause_red);
				mainButton.setImageResource(R.drawable.gui_almalence_shutter_video_stop_red);
			}
			else if (id == ShutterButton.RECORDER_RECORDING) {
				mainButton.setImageResource(R.drawable.gui_almalence_shutter_video_stop_red);
			}
		}
	}	

	public boolean onKeyDown(boolean isFromMain, int keyCode, KeyEvent event) {
		// hide hint screen
		if (guiView.findViewById(R.id.hintLayout).getVisibility() == View.VISIBLE)
			guiView.findViewById(R.id.hintLayout).setVisibility(View.INVISIBLE);
		
		if (guiView.findViewById(R.id.mode_help).getVisibility() ==  View.VISIBLE)
		{
			guiView.findViewById(R.id.mode_help).setVisibility(View.INVISIBLE);
			return true;
		}

		int res = 0;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (quickControlsChangeVisible) {
				closeQuickControlsSettings();
				res++;
				guiView.findViewById(R.id.topPanel).setVisibility(View.VISIBLE);
			} else if (settingsControlsVisible) {
				((Panel) guiView.findViewById(R.id.topPanel)).setOpen(false,
						true);
				res++;
			} else if (modeSelectorVisible) {
				hideModeList();
				res++;
			} else if (quickControlsVisible) {
				unselectPrimaryTopMenuButtons(-1);
				hideSecondaryMenus();
				res++;
				guiView.findViewById(R.id.topPanel).setVisibility(View.VISIBLE);
				quickControlsVisible = false;
			}
			if (((RelativeLayout) guiView.findViewById(R.id.viewPagerLayout)).getVisibility() == View.VISIBLE)
			{
				hideStore();
				res++;
			}
		}

		if (keyCode == KeyEvent.KEYCODE_CAMERA /*
												 * || keyCode ==
												 * KeyEvent.KEYCODE_DPAD_CENTER
												 */) {
			if (settingsControlsVisible || quickControlsChangeVisible
					|| modeSelectorVisible) {
				if (quickControlsChangeVisible)
					closeQuickControlsSettings();
				if (settingsControlsVisible) {
					((Panel) guiView.findViewById(R.id.topPanel)).setOpen(
							false, true);
					return false;
				}
				if (modeSelectorVisible) {
					hideModeList();
					return false;
				}
			}
			shutterButtonPressed();
			return true;
		}

		// check if back button pressed and processing is in progress
		if (res == 0)
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (PluginManager.getInstance().getProcessingCounter() != 0) {
					// splash screen about processing
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainScreen.thiz)
							.setTitle("Processing...")
							.setMessage(
									MainScreen.thiz.getResources().getString(
											R.string.processing_not_finished))
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											// continue with delete
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}

		return (res > 0 ? true : false);
	}

	private void openGallery() {
		if (mThumbnail == null)
			return;

		Uri uri = this.mThumbnail.getUri();

		Message message = new Message();
		message.arg1 = PluginManager.MSG_STOP_CAPTURE;
		message.what = PluginManager.MSG_BROADCAST;
		MainScreen.H.sendMessage(message);

		if (!isUriValid(uri, MainScreen.thiz.getContentResolver())) {
			//prevent problems when image saved to specified location - for some reasons it doesn't work
			//Log.e("AlmalenceGUI", "Uri invalid. uri=" + uri);
//			return;
		}

		try {
			MainScreen.thiz.startActivity(new Intent(Intent.ACTION_VIEW,
					uri));
//			MainScreen.thiz.startActivity(new Intent(
//					"com.android.camera.action.REVIEW", uri));
		} catch (ActivityNotFoundException ex) {
			try {
				MainScreen.thiz.startActivity(new Intent(Intent.ACTION_VIEW,
						uri));
			} catch (ActivityNotFoundException e) {
				Log.e("AlmalenceGUI", "review image fail. uri=" + uri, e);
			}
		}
	}

	public static boolean isUriValid(Uri uri, ContentResolver resolver) {
		if (uri == null)
			return false;

		try {
			ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
			if (pfd == null) {
				Log.e("AlmalenceGUI", "Fail to open URI. URI=" + uri);
				return false;
			}
			pfd.close();
		} catch (IOException ex) {
			return false;
		}

		return true;
	}
	
	@Override
	public void onCaptureFinished()
	{
//		AeUnlock();
//		AwUnlock();
	}

	// called to set any indication when export plugin work finished.
	@Override
	public void onExportFinished() {
		// stop animation
		if (processingAnim != null) {
			processingAnim.clearAnimation();
			processingAnim.setVisibility(View.GONE);
		}
		RelativeLayout rl = (RelativeLayout) guiView
				.findViewById(R.id.blockingLayout);
		if (rl.getVisibility() == View.VISIBLE) {
			rl.setVisibility(View.GONE);
		}

		updateThumbnailButton();
		thumbnailView.invalidate();

		//Log.e("AlmalenceGUI", "processing conter = " + PluginManager.getInstance().getProcessingCounter());
		if (0 != PluginManager.getInstance().getProcessingCounter()) {
			new CountDownTimer(10, 10) {
				public void onTick(long millisUntilFinished) {
				}

				public void onFinish() {
					startProcessingAnimation();
				}
			}.start();
		}
	}

	@Override
	public void onPostProcessingStarted() {
		guiView.findViewById(R.id.buttonGallery).setEnabled(false);
		guiView.findViewById(R.id.buttonShutter).setEnabled(false);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(false);
		guiView.findViewById(R.id.postprocessingLayout).setVisibility(
				View.VISIBLE);
		guiView.findViewById(R.id.postprocessingLayout).bringToFront();
		List<Plugin> processingPlugins = PluginManager.getInstance()
				.getActivePlugins(PluginType.Processing);
		if (processingPlugins.size() > 0) {
			View postProcessingView = processingPlugins.get(0)
					.getPostProcessingView();
			if (postProcessingView != null)
				((RelativeLayout) guiView
						.findViewById(R.id.postprocessingLayout))
						.addView(postProcessingView);
		}
	}

	@Override
	public void onPostProcessingFinished() {
		List<View> postprocessingView = new ArrayList<View>();
		RelativeLayout pluginsLayout = (RelativeLayout) MainScreen.thiz
				.findViewById(R.id.postprocessingLayout);
		for (int i = 0; i < pluginsLayout.getChildCount(); i++)
			postprocessingView.add(pluginsLayout.getChildAt(i));

		for (int j = 0; j < postprocessingView.size(); j++) {
			View view = postprocessingView.get(j);
			if (view.getParent() != null)
				((ViewGroup) view.getParent()).removeView(view);

			pluginsLayout.removeView(view);
		}

		guiView.findViewById(R.id.postprocessingLayout)
				.setVisibility(View.GONE);
		guiView.findViewById(R.id.buttonGallery).setEnabled(true);
		guiView.findViewById(R.id.buttonShutter).setEnabled(true);
		guiView.findViewById(R.id.buttonSelectMode).setEnabled(true);

		updateThumbnailButton();
		thumbnailView.invalidate();
	}

	private updateThumbnailButtonTask t = null;
	public void updateThumbnailButton() {
		
		t = new updateThumbnailButtonTask(MainScreen.thiz);
        t.execute();
        
        new CountDownTimer(3000, 3000) {
			public void onTick(long millisUntilFinished) {}

			public void onFinish() {
				try{
					if (t != null && t.getStatus() != AsyncTask.Status.FINISHED)
					{
//						Log.e("AlmalenceGUI", "Trying to stop thumbnail");
						t.cancel(true);
					}
//					Log.e("AlmalenceGUI", "Thumbnail stopped");
				}catch(Exception e)
				{
					Log.e("AlmalenceGUI", "Can't stop thumbnail processing");
				}
			}
		}.start();
	}
	
	private class updateThumbnailButtonTask extends AsyncTask <Void, Void, Void>
	{
		public updateThumbnailButtonTask(Context context) 
		{
		}
			
		@Override
		protected void onPreExecute() {
		// do nothing.
		}
		
		@Override
		protected Void doInBackground(Void... params) 
		{
			mThumbnail = Thumbnail.getLastThumbnail(MainScreen.thiz.getContentResolver());
			return null;
		}

		@Override
		protected void onPostExecute(Void v) 
		{
			if (mThumbnail != null) {
				final Bitmap bitmap = mThumbnail.getBitmap();

				if (bitmap != null) {
					if (bitmap.getHeight() > 0 && bitmap.getWidth() > 0) {
						System.gc();

						try {
							Bitmap bm = Thumbnail
									.getRoundedCornerBitmap(
											bitmap,
											(int) (MainScreen.mainContext
													.getResources()
													.getDimension(R.dimen.mainButtonHeight)*1.2),
													(int) (MainScreen.mainContext
															.getResources()
															.getDimension(R.dimen.mainButtonHeight)/1.1));

							thumbnailView.setImageBitmap(bm);
						} catch (Exception e) {
							Log.v("AlmalenceGUI", "Can't set thumbnail");
						}
					}
				}
			} else {
				try {
					Bitmap bitmap = Bitmap.createBitmap(96, 96, Config.ARGB_8888);
					Canvas canvas = new Canvas(bitmap);
					canvas.drawColor(Color.BLACK);
					Bitmap bm = Thumbnail.getRoundedCornerBitmap(bitmap,
							(int) (MainScreen.mainContext.getResources()
									.getDimension(R.dimen.mainButtonHeight)*1.2),
									(int) (MainScreen.mainContext
											.getResources()
											.getDimension(R.dimen.mainButtonHeight)/1.1));
					thumbnailView.setImageBitmap(bm);
				} catch (Exception e) {
					Log.v("AlmalenceGUI", "Can't set thumbnail");
				}
			}
		}
	}
	
//	public void updateThumbnailButton() {
//		this.mThumbnail = Thumbnail.getLastThumbnail(MainScreen.thiz
//				.getContentResolver());
//
//		if (this.mThumbnail != null) {
//			final Bitmap bitmap = this.mThumbnail.getBitmap();
//
//			if (bitmap != null) {
//				if (bitmap.getHeight() > 0 && bitmap.getWidth() > 0) {
//					System.gc();
//
//					try {
//						Bitmap bm = Thumbnail
//								.getRoundedCornerBitmap(
//										bitmap,
//										(int) (MainScreen.mainContext
//												.getResources()
//												.getDimension(R.dimen.mainButtonHeight)),
//										(int) ((15.0f)));
//
//						thumbnailView.setImageBitmap(bm);
//					} catch (Exception e) {
//						Log.v("AlmalenceGUI", "Can't set thumbnail");
//					}
//				}
//			}
//		} else {
//			try {
//				Bitmap bitmap = Bitmap.createBitmap(96, 96, Config.ARGB_8888);
//				Canvas canvas = new Canvas(bitmap);
//				canvas.drawColor(Color.BLACK);
//				Bitmap bm = Thumbnail.getRoundedCornerBitmap(bitmap,
//						(int) (MainScreen.mainContext.getResources()
//								.getDimension(R.dimen.mainButtonHeight)),
//						(int) ((15.0f)));
//				thumbnailView.setImageBitmap(bm);
//			} catch (Exception e) {
//				Log.v("AlmalenceGUI", "Can't set thumbnail");
//			}
//		}
//	}

	private ImageView processingAnim;

	public void startProcessingAnimation() {
		if(processingAnim != null && processingAnim.getVisibility() == View.VISIBLE)
			return;

		processingAnim = ((ImageView) guiView
				.findViewById(R.id.buttonGallery2));
		processingAnim.setVisibility(View.VISIBLE);

		int height = (int)MainScreen.thiz.getResources().getDimension(R.dimen.paramsLayoutHeightScanner);
		int width = (int)MainScreen.thiz.getResources().getDimension(R.dimen.paramsLayoutHeightScanner);
		Animation rotation = new RotateAnimation(0, 360, width/2, height/2);
		rotation.setDuration(800);
		rotation.setInterpolator(new LinearInterpolator());
		rotation.setRepeatCount(100);

		processingAnim.startAnimation(rotation);
	}

	public void processingBlockUI() {
		RelativeLayout rl = (RelativeLayout) guiView
				.findViewById(R.id.blockingLayout);
		if (rl.getVisibility() == View.GONE) {
			rl.setVisibility(View.VISIBLE);
			rl.bringToFront();

			guiView.findViewById(R.id.buttonGallery).setEnabled(false);
			guiView.findViewById(R.id.buttonShutter).setEnabled(false);
			guiView.findViewById(R.id.buttonSelectMode).setEnabled(false);
		}
	}

	// capture indication - will play shutter icon opened/closed
	private boolean captureIndication = true;
	private boolean isIndicationOn = false;

	public void startContinuousCaptureIndication() {
		captureIndication = true;
		new CountDownTimer(200, 200) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				if (captureIndication) {
					if (isIndicationOn) {
						((RotateImageView) guiView
								.findViewById(R.id.buttonShutter))
								.setImageResource(R.drawable.gui_almalence_shutter);
						isIndicationOn = false;
					} else {
						((RotateImageView) guiView
								.findViewById(R.id.buttonShutter))
								.setImageResource(R.drawable.gui_almalence_shutter_pressed);
						isIndicationOn = true;
					}
					startContinuousCaptureIndication();
				}
			}
		}.start();
	}

	public void stopCaptureIndication() {
		captureIndication = false;
		((RotateImageView) guiView.findViewById(R.id.buttonShutter))
				.setImageResource(R.drawable.button_shutter);
	}

	public void showCaptureIndication() {
		new CountDownTimer(400, 200) {
			public void onTick(long millisUntilFinished) {
				((RotateImageView) guiView.findViewById(R.id.buttonShutter))
						.setImageResource(R.drawable.gui_almalence_shutter_pressed);
			}

			public void onFinish() {
				((RotateImageView) guiView.findViewById(R.id.buttonShutter))
						.setImageResource(R.drawable.button_shutter);
			}
		}.start();
	}
	
	

	@Override
	public void onCameraSetup() {		
	}

	@Override
	public void menuButtonPressed() {
	}

	@Override
	public void addMode(View mode) {
	}

	@Override
	public void SetModeSelected(View v) {
	}

	@Override
	public void hideModes() {
	}

	@Override
	public int getMaxModeViewWidth() {
		return -1;
	}

	@Override
	public int getMaxModeViewHeight() {
		return -1;
	}

	// @Override
	// public void autoFocus(){}
	// @Override
	// public void onAutoFocus(boolean focused, Camera paramCamera){}

	@Override
	@TargetApi(14)
	public void setFocusParameters() {
	}
	
	
	//mode help procedure
	@Override
	public void showHelp(String modeName, String text, int imageID, String Prefs)
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		boolean needToShow = prefs.getBoolean(Prefs, true);
		
		//check show help settings
		MainScreen.showHelp = prefs.getBoolean("showHelpPrefCommon", true);
		if (false == needToShow || MainScreen.showHelp == false)
			return;
		
		if (guiView.findViewById(R.id.postprocessingLayout).getVisibility() == View.VISIBLE)
			return;
		
		final String preference = Prefs;
		
		final View help = guiView.findViewById(R.id.mode_help);
		ImageView helpImage = (ImageView)guiView.findViewById(R.id.helpImage);
		helpImage.setImageResource(imageID);
		TextView helpText = (TextView)guiView.findViewById(R.id.helpText);
		helpText.setText(text);
		
//		final CheckBox ck = (CheckBox)guiView.findViewById(R.id.helpCheckBox);
//		ck.setChecked(false);
		
		TextView helpTextModeName = (TextView)guiView.findViewById(R.id.helpTextModeName);
		helpTextModeName.setText(modeName);
		
		Button button = (Button)guiView.findViewById(R.id.buttonOk);
		button.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				help.setVisibility(View.GONE);
				
//				if (ck.isChecked())
//				{
//					Editor prefsEditor = prefs.edit();
//					//prefsEditor.putBoolean("showHelpPrefCommon", false);
//					//reset all show help settings
////					if (MainScreen.showHelp)
////					{
////						prefsEditor.putBoolean("droShowHelp", true);
////						prefsEditor.putBoolean("sequenceRemovalShowHelp", true);
////						prefsEditor.putBoolean("panoramaShowHelp", true);
////						prefsEditor.putBoolean("groupshotRemovalShowHelp", true);
////						prefsEditor.putBoolean("objectRemovalShowHelp", true);
////					}
//					
//					prefsEditor.putBoolean(preference, false);
//					prefsEditor.commit();
//				}				
			}
		});
		
		Button buttonDontShow = (Button)guiView.findViewById(R.id.buttonDontShow);
		buttonDontShow.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				help.setVisibility(View.GONE);
				
//				if (ck.isChecked())
				{
					Editor prefsEditor = prefs.edit();
					//prefsEditor.putBoolean("showHelpPrefCommon", false);
					//reset all show help settings
//					if (MainScreen.showHelp)
//					{
//						prefsEditor.putBoolean("droShowHelp", true);
//						prefsEditor.putBoolean("sequenceRemovalShowHelp", true);
//						prefsEditor.putBoolean("panoramaShowHelp", true);
//						prefsEditor.putBoolean("groupshotRemovalShowHelp", true);
//						prefsEditor.putBoolean("objectRemovalShowHelp", true);
//					}
					
					prefsEditor.putBoolean(preference, false);
					prefsEditor.commit();
				}				
			}
		});
		
		help.setVisibility(View.VISIBLE);
		help.bringToFront();
	}
	
	boolean swChecked = false;
	String[] stringInterval = {"3", "5", "10", "15", "30", "60"};
	public void SelfTimerDialog()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		int interval = prefs.getInt(MainScreen.sDelayedCaptureIntervalPref, 0);
		swChecked = prefs.getBoolean(MainScreen.sSWCheckedPref, false);		
		
		final Dialog d = new Dialog(MainScreen.thiz);
        d.setTitle("Self timer settings");
        d.setContentView(R.layout.selftimer_dialog);
        final Button bSet = (Button) d.findViewById(R.id.button1);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np.setMaxValue(5);
        np.setMinValue(0);
        np.setValue(interval);
        np.setDisplayedValues(stringInterval);
        np.setWrapSelectorWheel(false);
        
        final CheckBox flashCheckbox = (CheckBox) d.findViewById(R.id.flashCheckbox);
        boolean flash = prefs.getBoolean(MainScreen.sDelayedFlashPref, false);
        flashCheckbox.setChecked(flash);
        
        final CheckBox soundCheckbox = (CheckBox) d.findViewById(R.id.soundCheckbox);
        boolean sound = prefs.getBoolean(MainScreen.sDelayedSoundPref, false);
        soundCheckbox.setChecked(sound);
        
//        final NumberPicker np2 = (NumberPicker) d.findViewById(R.id.numberPicker2);
//        np2.setMaxValue(2);
//        np2.setMinValue(0);
//        np2.setValue(measurementVal);
//        np2.setWrapSelectorWheel(false);
//        np2.setDisplayedValues(stringMeasurement);
        
        final Switch sw = (Switch) d.findViewById(R.id.selftimer_switcher);
        
        //disable/enable controls in dialog
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				if (false == sw.isChecked())
		        {
//		        	np2.setEnabled(false);
		        	np.setEnabled(false);
		        	flashCheckbox.setEnabled(false);
		        	soundCheckbox.setEnabled(false);
		        	swChecked = false;
//		        	bSet.setEnabled(false);
		        }
				else
				{
//					np2.setEnabled(true);
		        	np.setEnabled(true);
		        	flashCheckbox.setEnabled(true);
		        	soundCheckbox.setEnabled(true);
		        	swChecked = true;
		        	bSet.setEnabled(true);
				}
			}
		});
        
        //disable control in dialog by default
        if (false == swChecked)
        {
        	sw.setChecked(false);
        	flashCheckbox.setEnabled(false);
        	soundCheckbox.setEnabled(false);
//        	np2.setEnabled(false);
        	np.setEnabled(false);
        	bSet.setEnabled(false);
        }
        else
        {
//        	np2.setEnabled(true);
        	np.setEnabled(true);
        	flashCheckbox.setEnabled(true);
        	soundCheckbox.setEnabled(true);
        	bSet.setEnabled(true);
        	sw.setChecked(true);
        }
        
        //set button in dialog pressed
        bSet.setOnClickListener(new OnClickListener()
        {
         @Override
         public void onClick(View v) {
             d.dismiss();
             int interval = 0;
             Editor prefsEditor = prefs.edit();
 			
             if (swChecked == true)
             {
//            	 measurementVal = np2.getValue();
            	 interval  = np.getValue();
//            	 timeLapseButton.setImageResource(R.drawable.plugin_capture_video_timelapse_active);
             }
             else
             {
            	 interval = 0;
//            	 timeLapseButton.setImageResource(R.drawable.plugin_capture_video_timelapse_inactive);
             }
             int real_int = Integer.parseInt(stringInterval[np.getValue()]);
             prefsEditor.putBoolean(MainScreen.sSWCheckedPref, swChecked);
             if (swChecked)
            	 prefsEditor.putInt(MainScreen.sDelayedCapturePref, real_int);
             else
             {
            	 prefsEditor.putInt(MainScreen.sDelayedCapturePref, 0);
            	 real_int = 0;
             }
             prefsEditor.putBoolean(MainScreen.sDelayedFlashPref, flashCheckbox.isChecked());
             prefsEditor.putBoolean(MainScreen.sDelayedSoundPref, soundCheckbox.isChecked());
             prefsEditor.putInt(MainScreen.sDelayedCaptureIntervalPref, interval);
             prefsEditor.commit();

             updateTimelapseButton(real_int);
          }    
         });
      d.show();
	}
	
	private RotateImageView timeLapseButton =null;
	private void AddSelfTimerControl(boolean needToShow)
	{
		View selfTimerControl =null;
		// Calculate right sizes for plugin's controls
		DisplayMetrics metrics = new DisplayMetrics();
		MainScreen.thiz.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		float fScreenDensity = metrics.density;
    			
    	int iIndicatorSize = (int) (MainScreen.mainContext.getResources()
				.getInteger(R.integer.infoControlHeight) * fScreenDensity);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iIndicatorSize, iIndicatorSize);
		int topMargin = MainScreen.thiz.findViewById(R.id.paramsLayout).getHeight() + (int)MainScreen.thiz.getResources().getDimension(R.dimen.viewfinderViewsMarginTop);
		params.setMargins((int)(2*MainScreen.guiManager.getScreenDensity()), topMargin, 0, 0);
		
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		
		((RelativeLayout)MainScreen.thiz.findViewById(R.id.specialPluginsLayout2)).requestLayout();		
		
		LayoutInflater inflator = MainScreen.thiz.getLayoutInflater();		
		selfTimerControl = inflator.inflate(R.layout.selftimer_capture_layout, null, false);
		selfTimerControl.setVisibility(View.VISIBLE);
		
		removeViews(selfTimerControl, R.id.specialPluginsLayout2);
		
		if (needToShow == false || PluginManager.getInstance().getActivePlugins(PluginType.Capture).get(0).delayedCaptureSupported()==false)
		{
			return; 
		}
		
		timeLapseButton = (RotateImageView)selfTimerControl.findViewById(R.id.buttonSelftimer);
		
		timeLapseButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {			
				SelfTimerDialog();
			}
			
		});
		
		List<View> specialView2 = new ArrayList<View>();
		RelativeLayout specialLayout2 = (RelativeLayout)MainScreen.thiz.findViewById(R.id.specialPluginsLayout2);
		for(int i = 0; i < specialLayout2.getChildCount(); i++)
			specialView2.add(specialLayout2.getChildAt(i));

		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.height = (int)MainScreen.thiz.getResources().getDimension(R.dimen.videobuttons_size);
		
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);		
		
		((RelativeLayout)MainScreen.thiz.findViewById(R.id.specialPluginsLayout2)).addView(selfTimerControl, params);
		
		selfTimerControl.setLayoutParams(params);
		selfTimerControl.requestLayout();
		
		((RelativeLayout)MainScreen.thiz.findViewById(R.id.specialPluginsLayout2)).requestLayout();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		int delayInterval = prefs.getInt(MainScreen.sDelayedCapturePref, 0);
		updateTimelapseButton(delayInterval);
	}
	
	private void updateTimelapseButton(int delayInterval)
	{
		switch (delayInterval)
        {
         case 0:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer_control);
        	 break;
         case 3:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer3_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer3_control);
        	 break;
         case 5:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer5_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer5_control);
        	 break;
         case 10:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer10_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer10_control);
        	 break;
         case 15:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer15_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer15_control);
        	 break;
         case 30:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer30_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer30_control);
        	 break;
         case 60:
        	 if (swChecked)
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer60_controlcative);
        	 else
        		 timeLapseButton.setImageResource(R.drawable.gui_almalence_mode_selftimer60_control);
        	 break;         
         }
	}
}
