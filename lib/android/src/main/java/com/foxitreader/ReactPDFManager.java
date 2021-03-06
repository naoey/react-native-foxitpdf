package com.foxitreader;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.foxit.sdk.common.Constants;
import com.foxit.sdk.common.Library;
import com.foxit.uiextensions.config.Config;

public class ReactPDFManager extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = "PDFManager";

    private static boolean isLibraryInited = false;
    private static int mErrorCode = Constants.e_ErrInvalidLicense;
    private static String mLastSn;
    private static String mLastKey;

    public ReactPDFManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void initialize(String foxit_sn, String foxit_key) {
        try {
            if (isLibraryInited == false) {
                mErrorCode = Library.initialize(foxit_sn, foxit_key);
                isLibraryInited = true;
            } else if (!mLastSn.equals(foxit_sn) || !mLastKey.equals(foxit_key)) {
                Library.release();
                mErrorCode = Library.initialize(foxit_sn, foxit_key);
            }
            mLastSn = foxit_sn;
            mLastKey = foxit_key;
        } catch (Exception e) {
            Log.e("FoxitPDF", "Initialization library failed： " + e.getMessage());
        }
    }

    @ReactMethod
    public void openPDF(String src, String password, ReadableMap ui_config) {
        openDoc(src, password, ui_config);
    }

    @ReactMethod
    public void openDocument(String path, String password, ReadableMap ui_config) {
        openDoc(path, password, ui_config);
    }

    private void openDoc(String path, String password, ReadableMap ui_config) {
        ReactApplicationContext reactContext = this.getReactApplicationContext();
        if (mErrorCode != Constants.e_ErrSuccess) {
            String errorMsg = (mErrorCode == Constants.e_ErrInvalidLicense) ? "The license is invalid!" : "Failed to initialize the library!";
            Toast.makeText(reactContext, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(path)) {
            String errorMsg = "File path cannot be empty!";
            Toast.makeText(reactContext, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(reactContext, PDFReaderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("path", path);
        intent.putExtra("password", password);
        if (null != ui_config) {
            intent.putExtra(Config.class.getName(), ui_config.toString());
        }
        reactContext.startActivity(intent);
    }

}
