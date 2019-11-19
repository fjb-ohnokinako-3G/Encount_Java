package com.websarva.wings.android.cameraintentsample;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
	/**
	 * 保存された画像のURI。
	 */
	private Uri _imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_camera);

		Button postButton = findViewById(R.id.postButton);

		postButton.setOnClickListener(new View.OnClickListener() {

			// クリック時に呼ばれるメソッド
			@Override
			public void onClick(View view) {
				//写真のパス・名前を指定
				OkHttpPost.uurl = "/sdcard/Download/sample1-s.jpg";
				//コメントをEditTextから取得
				EditText commentInput = findViewById(R.id.commentInput);
				OkHttpPost.cmnt = commentInput.getText().toString();
				//緯度を取得
				OkHttpPost.latitude = "35.703092";
				//経度を取得
				OkHttpPost.longitude = "139.985561";

				OkHttpPost.basyo = _imageUri;

				//val postTask = OkHttpPost();
				//postTask.execute();
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//カメラアプリからの戻りでかつ撮影成功の場合
		if(requestCode == 200 && resultCode == RESULT_OK) {
			//画像を表示するImageViewを取得。
			ImageView ivCamera = findViewById(R.id.ivCamera);
			//フィールドの画像URIをImageViewに設定。
			ivCamera.setImageURI(_imageUri);

			//デバッグ用
			System.out.println(_imageUri);


			/**
			 * ここの処理
			 */
			Uri uuri = getFileSchemeUri(_imageUri);
			System.out.println(uuri.toString());

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	//WRITE_EXTERNAL_STORAGEに対するパーミションダイアログでかつ許可を選択したなら…
		if(requestCode == 2000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			//もう一度カメラアプリを起動。
			ImageView ivCamera = findViewById(R.id.ivCamera);
			onCameraImageClick(ivCamera);
		}
	}

	/**
	 * 画像部分がタップされたときの処理メソッド。
	 */
	public void onCameraImageClick(View view) {
		//WRITE_EXTERNAL_STORAGEの許可が下りていないなら…
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			//WRITE_EXTERNAL_STORAGEの許可を求めるダイアログを表示。その際、リクエストコードを2000に設定。
			String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
			ActivityCompat.requestPermissions(this, permissions, 2000);
			return;
		}

		//日時データを「yyyyMMddHHmmss」の形式に整形するフォーマッタを生成。
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		//現在の日時を取得。
		Date now = new Date(System.currentTimeMillis());
		//取得した日時データを「yyyyMMddHHmmss」形式に整形した文字列を生成。
		String nowStr = dateFormat.format(now);
		//ストレージに格納する画像のファイル名を生成。ファイル名の一意を確保するためにタイムスタンプの値を利用。
		String fileName = "UseCameraActivityPhoto_" + nowStr +".jpg";

		//ContentValuesオブジェクトを生成。
		ContentValues values = new ContentValues();
		//画像ファイル名を設定。
		values.put(MediaStore.Images.Media.TITLE, fileName);
		//画像ファイルの種類を設定。
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

		//ContentResolverオブジェクトを生成。
		ContentResolver resolver = getContentResolver();
		//ContentResolverを使ってURIオブジェクトを生成。
		_imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		//Intentオブジェクトを生成。
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//Extra情報として_imageUriを設定。
		intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
		//アクティビティを起動。
		startActivityForResult(intent, 200);
	}

	/**
	 * URIをFileスキームのURIに変換する.
	 * @param uri 変換前のURI  例) content://media/external/images/media/33
	 * @return 変換後のURI     例) file:///storage/sdcard/test1.jpg
	 */
	private Uri getFileSchemeUri(Uri uri){
		Uri fileSchemeUri = uri;
		String path = getPath(uri);
		fileSchemeUri = Uri.fromFile(new File(path));
		return fileSchemeUri;
	}

	/**
	 * URIからファイルPATHを取得する.
	 * @param uri URI
	 * @return ファイルPATH
	 */
	private String getPath(Uri uri) {
		String path = uri.toString();
		if (path.matches("^file:.*")) {
			return path.replaceFirst("file://", "");
		} else if (!path.matches("^content:.*")) {
			return path;
		}
		Context context = getApplicationContext();
		ContentResolver contentResolver = context.getContentResolver();
		String[] columns = { MediaStore.Images.Media.DATA };
		Cursor cursor = contentResolver.query(uri, columns, null, null, null);
		if (cursor != null){
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				path = cursor.getString(0);
			}
			cursor.close();
		}
		return path;
	}
}
