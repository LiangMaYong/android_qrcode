# android_qrcode
This is Android QRScan library.

## Get start

Activity:
```
public class QRScanActivity extends Activity {

    private DecodeScanView scanView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        scanView = (DecodeScanView) findViewById(R.id.scanView);
        scanView.setOnDecodeScanListener(new DecodeScanView.OnDecodeScanListener() {
            @Override
            public boolean handleDecode(Result result, Bitmap barcode) {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanView.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.onPause(this);
    }

    @Override
    protected void onDestroy() {
        scanView.onDestroy(this);
        super.onDestroy();
    }
}
```
Permission:
```
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.FLASHLIGHT"/>
<uses-feature android:name="android.hardware.camera"/>
<uses-feature android:name="android.hardware.camera.autofocus"/>
```
## LICENSE
```
Copyright 2016 LiangMaYong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```