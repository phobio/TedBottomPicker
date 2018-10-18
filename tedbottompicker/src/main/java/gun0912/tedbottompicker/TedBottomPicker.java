package gun0912.tedbottompicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedonactivityresult.TedOnActivityResult;
import com.gun0912.tedonactivityresult.listener.OnActivityResultListener;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import gun0912.tedbottompicker.adapter.GalleryAdapter;
import gun0912.tedbottompicker.util.MimeTypeUtil;
import gun0912.tedbottompicker.util.RealPathUtil;

//@SuppressWarnings({"WeakerAccess", "unused"})
public class TedBottomPicker extends BottomSheetDialogFragment {

    private static final String TAG = "TedBottomPicker";
    private static final String EXTRA_CAMERA_IMAGE_URI = "camera_image_uri";
    private static final String EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri";
    private static final String EXTRA_PREVIEW_MAX_COUNT = "preview_max_count";
    private static final String EXTRA_SPACING = "spacing";
    private static final String EXTRA_INCLUDE_EDGE_SPACING = "include_edge_spacing";
    private static final String EXTRA_SHOW_CAMERA = "show_camera";
    private static final String EXTRA_PEEK_HEIGHT = "peek_height";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_SHOW_TITLE = "show_title";
    private static final String EXTRA_SELECT_MAX_COUNT = "select_max_count";
    private static final String EXTRA_SELECT_MIN_COUNT = "select_min_count";
    private static final String EXTRA_COMPLETE_BUTTON_TEXT = "complete_button_text";
    private static final String EXTRA_EMPTY_SELECTION_TEXT = "empty_selection_text";
    private static final String EXTRA_SELECT_MAX_COUNT_ERROR_TEXT = "select_max_count_error_text";
//    private static final String EXTRA_SELECT_MIN_COUNT_ERROR_TEXT = "select_min_count_error_text";
    private static final String EXTRA_MEDIA_TYPES = "media_types";
    private static final String EXTRA_TITLE_BACKGROUND_RES_ID = "title_background_res_id";
    private static final String EXTRA_MEDIA_SELECTOR_TEXT_COLOR = "media_selector_text_color";
    private static final String EXTRA_CAMERA_TILE_RES_ID = "camera_tile_res_id";
    private static final String EXTRA_VIDEO_CAMERA_TILE_RES_ID = "video_tile_res_id";
    private static final String EXTRA_GALLERY_TILE_RES_ID = "gallery_tile_res_id";
    private static final String EXTRA_CAMERA_TILE_BG = "camera_tile_background_res_id";
    private static final String EXTRA_GALLERY_TILE_BG = "gallery_tile_background_res_id";

    private GalleryAdapter imageGalleryAdapter;
    private View view_title_container;
    private TextView tv_title;
    private Button btn_done;
    private Button btn_video;
    private Button btn_photo;
    private FrameLayout selected_photos_container_frame;
    private LinearLayout selected_photos_container;
    private TextView selected_photos_empty;
    private RecyclerView rc_gallery;

    @Nullable private Drawable cameraTileDrawable;
    @Nullable private Drawable videoCameraTileDrawable;
    @Nullable private Drawable galleryTileDrawable;
//    @Nullable private Drawable deSelectIconDrawable;
//    @Nullable private Drawable selectedForegroundDrawable;

    @Nullable private OnImageSelectedListener onImageSelectedListener;
//    private OnMultiImageSelectedListener onMultiImageSelectedListener;
//    private OnErrorListener onErrorListener;

    @NonNull final private ArrayList<Uri> selectedUriList = new ArrayList<>();
//    @Nullable private Uri selectedUri;
    @Nullable private Uri cameraImageUri;
    private @Builder.MediaType int selectedMediaType;

    // start params
    private int previewMaxCount = 25;
    private boolean includeEdgeSpacing = false;
    private boolean showCamera = true;
    private boolean showGallery = true;
    private int peekHeight = -1;
    @Nullable private String title;
    private boolean showTitle = true;
    private int spacing;
    private int selectMaxCount = Integer.MAX_VALUE;
    private int selectMinCount = 0;
    @Nullable private String completeButtonText;
    @Nullable private String emptySelectionText;
    @Nullable private String selectMaxCountErrorText;
//    @Nullable private String selectMinCountErrorText;
    @Nullable private @Builder.MediaType int[] mediaTypes;
    private @DrawableRes int titleBackgroundResId;
    private @DrawableRes int mediaSelectorTextColor = R.drawable.selector_btn_text_color;
    private @DrawableRes int cameraTileResId;
    private @DrawableRes int videoCameraTileResId;
    private @DrawableRes int galleryTileResId;
    private @DrawableRes int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
    private @DrawableRes int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;

    // end params

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG, "onStateChanged() newState: " + newState);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            Log.d(TAG, "onSlide() slideOffset: " + slideOffset);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSavedInstanceState(savedInstanceState);
        Context context = requireContext();
        cameraTileDrawable = ContextCompat.getDrawable(context, cameraTileResId);
        videoCameraTileDrawable = ContextCompat.getDrawable(context, videoCameraTileResId);
        galleryTileDrawable = ContextCompat.getDrawable(context, galleryTileResId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        if (activity instanceof OnImageSelectedListener) {
            onImageSelectedListener = ((OnImageSelectedListener) activity);
        }
    }

    private void setupSavedInstanceState(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            initWithBundle(bundle);
        }

        if (savedInstanceState == null) {
            cameraImageUri = null;
//            tempUriList = selectedUriList;
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI);
//            tempUriList = savedInstanceState.getParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI);
        }
    }

    private void initWithBundle(@NonNull Bundle bundle) {
        this.previewMaxCount = bundle.getInt(EXTRA_PREVIEW_MAX_COUNT, this.previewMaxCount);
        this.spacing = bundle.getInt(EXTRA_SPACING);
        this.includeEdgeSpacing = bundle.getBoolean(EXTRA_INCLUDE_EDGE_SPACING,
                this.includeEdgeSpacing);
        this.showCamera = bundle.getBoolean(EXTRA_SHOW_CAMERA, this.showCamera);
        this.peekHeight = bundle.getInt(EXTRA_PEEK_HEIGHT, this.peekHeight);
        this.title = bundle.getString(EXTRA_TITLE);
        this.showTitle = bundle.getBoolean(EXTRA_SHOW_TITLE, this.showTitle);
        this.selectMaxCount = bundle.getInt(EXTRA_SELECT_MAX_COUNT, this.selectMaxCount);
        this.selectMinCount = bundle.getInt(EXTRA_SELECT_MIN_COUNT, this.selectMinCount);
        this.completeButtonText = bundle.getString(EXTRA_COMPLETE_BUTTON_TEXT);
        this.emptySelectionText = bundle.getString(EXTRA_EMPTY_SELECTION_TEXT);
        this.selectMaxCountErrorText = bundle.getString(EXTRA_SELECT_MAX_COUNT_ERROR_TEXT);
//        this.selectMinCountErrorText = bundle.getString(EXTRA_SELECT_MIN_COUNT_ERROR_TEXT);
        this.mediaTypes = bundle.getIntArray(EXTRA_MEDIA_TYPES);
        this.titleBackgroundResId = bundle.getInt(EXTRA_TITLE_BACKGROUND_RES_ID);
        this.mediaSelectorTextColor = bundle.getInt(EXTRA_MEDIA_SELECTOR_TEXT_COLOR,
                this.mediaSelectorTextColor);
        this.cameraTileResId = bundle.getInt(EXTRA_CAMERA_TILE_RES_ID);
        this.videoCameraTileResId = bundle.getInt(EXTRA_VIDEO_CAMERA_TILE_RES_ID);
        this.galleryTileResId = bundle.getInt(EXTRA_GALLERY_TILE_RES_ID);
        this.cameraTileBackgroundResId = bundle.getInt(EXTRA_CAMERA_TILE_BG,
                this.cameraTileBackgroundResId);
        this.galleryTileBackgroundResId = bundle.getInt(EXTRA_GALLERY_TILE_BG,
                this.galleryTileBackgroundResId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri);
        outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, selectedUriList);
        super.onSaveInstanceState(outState);
    }

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.tedbottompicker_content_view, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            if (peekHeight > 0) {
                ((BottomSheetBehavior) behavior).setPeekHeight(peekHeight);
            }
        }

        initView(contentView);

        setTitle();
        initMediaToggle();
        setRecyclerView();
        setSelectionView();

        if (onImageSelectedListener != null && cameraImageUri != null) {
            addUri(cameraImageUri);
        }
//        } else if (onMultiImageSelectedListener != null && tempUriList != null) {
//            for (Uri uri : tempUriList) {
//                addUri(uri);
//            }
//        }

        setDoneButton();
        checkMultiMode();
    }

    private void setSelectionView() {
        if (emptySelectionText != null) {
            selected_photos_empty.setText(emptySelectionText);
        }
    }

    private void setDoneButton() {
        if (completeButtonText != null) {
            btn_done.setText(completeButtonText);
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMultiSelectComplete();
            }
        });
    }

    private void onMultiSelectComplete() {
//        if (selectedUriList.size() < selectMinCount) {
//            String message;
//            if (selectMinCountErrorText != null) {
//                message = selectMinCountErrorText;
//            } else {
//                message = String.format(getResources().getString(R.string.select_min_count), selectMinCount);
//            }
//
//            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        onMultiImageSelectedListener.onImagesSelected(selectedUriList);
        dismissAllowingStateLoss();
    }

    private void checkMultiMode() {
        if (!isMultiSelect()) {
            btn_done.setVisibility(View.GONE);
            selected_photos_container_frame.setVisibility(View.GONE);
        }
    }

    private void initMediaToggle() {
        if (mediaTypes != null  && mediaTypes.length > 1) {
            view_title_container.setVisibility(View.VISIBLE);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn_photo.setSelected(false);
                    btn_video.setSelected(false);
                    btn_photo.setTypeface(Typeface.DEFAULT);
                    btn_video.setTypeface(Typeface.DEFAULT);

                    v.setSelected(true);
                    ((Button)v).setTypeface(Typeface.DEFAULT_BOLD);
                    selectedMediaType = (int) v.getTag();
                    updateAdapter();
                }
            };
            btn_photo.setSelected(true);
            btn_photo.setTag(mediaTypes[0]);
            btn_photo.setOnClickListener(clickListener);
            btn_video.setTag(mediaTypes[1]);
            btn_video.setOnClickListener(clickListener);
        } else {
            view_title_container.setVisibility(View.GONE);
        }
        selectedMediaType = mediaTypes[0];
    }

    private void initView(View contentView) {
        view_title_container = contentView.findViewById(R.id.view_title_container);
        rc_gallery = contentView.findViewById(R.id.rc_gallery);
        tv_title = contentView.findViewById(R.id.tv_title);
        btn_done = contentView.findViewById(R.id.btn_done);
        btn_photo = contentView.findViewById(R.id.btn_photo);
        btn_video = contentView.findViewById(R.id.btn_video);

//        View select_media_type_container = contentView.findViewById(R.id.view_media_type_container);
        selected_photos_container_frame = contentView.findViewById(R.id.selected_photos_container_frame);
//        HorizontalScrollView hsv_selected_photos = contentView.findViewById(R.id.hsv_selected_photos);
        selected_photos_container = contentView.findViewById(R.id.selected_photos_container);
        selected_photos_empty = contentView.findViewById(R.id.selected_photos_empty);
    }

    private void setRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rc_gallery.setLayoutManager(gridLayoutManager);
        rc_gallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), spacing, includeEdgeSpacing));
        updateAdapter();
    }

    private void updateAdapter() {
        imageGalleryAdapter = new GalleryAdapter(requireActivity(), this, selectedMediaType);
        rc_gallery.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                GalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);

                switch (pickerTile.getTileType()) {
                    case GalleryAdapter.PickerTile.CAMERA:
                        startCameraIntent();
                        break;
                    case GalleryAdapter.PickerTile.GALLERY:
                        startGalleryIntent();
                        break;
                    case GalleryAdapter.PickerTile.IMAGE:
                        if (pickerTile.getImageUri() != null) {
                            complete(pickerTile.getImageUri());
                        }
                        break;

                    default:
                        errorMessage();
                }
            }
        });
    }

    private void complete(final Uri uri) {
        Log.d(TAG, "selected uri: " + uri.toString());
        //uri = Uri.parse(uri.toString());
        if (isMultiSelect()) {
            if (selectedUriList.contains(uri)) {
                removeImage(uri);
            } else {
                addUri(uri);
            }
        } else {
            if (onImageSelectedListener != null)
                onImageSelectedListener.onImageSelected(uri);
            dismissAllowingStateLoss();
        }
    }

    private void addUri(final Uri uri) {
        Objects.requireNonNull(getActivity());
        if (selectedUriList.size() == selectMaxCount) {
            String message;
            if (selectMaxCountErrorText != null) {
                message = selectMaxCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_max_count), selectMaxCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedUriList.add(uri);

        @SuppressLint("InflateParams")
        final View rootView = LayoutInflater.from(getActivity())
                .inflate(R.layout.tedbottompicker_selected_item, null);

        ImageView thumbnail = rootView.findViewById(R.id.selected_photo);
        ImageView iv_close = rootView.findViewById(R.id.iv_close);
        rootView.setTag(uri);

        selected_photos_container.addView(rootView, 0);

        int px = (int) getResources().getDimension(R.dimen.tedbottompicker_selected_image_height);
        thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));

        Glide.with(getActivity())
                .load(uri)
                .thumbnail(0.1f)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error))
                .into(thumbnail);

//        if (deSelectIconDrawable != null) {
//            iv_close.setImageDrawable(deSelectIconDrawable);
//        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImage(uri);

            }
        });

        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
    }

    private void removeImage(Uri uri) {
        selectedUriList.remove(uri);

        for (int i = 0; i < selected_photos_container.getChildCount(); i++) {
            View childView = selected_photos_container.getChildAt(i);


            if (childView.getTag().equals(uri)) {
                selected_photos_container.removeViewAt(i);
                break;
            }
        }

        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
    }

    private void updateSelectedView() {
        if (selectedUriList.size() == 0) {
            selected_photos_empty.setVisibility(View.VISIBLE);
            selected_photos_container.setVisibility(View.GONE);
        } else {
            selected_photos_empty.setVisibility(View.GONE);
            selected_photos_container.setVisibility(View.VISIBLE);
        }
    }

    private void startCameraIntent() {
        Objects.requireNonNull(getActivity());
        Objects.requireNonNull(getContext());
        Intent cameraInent;
        File mediaFile;

        if (selectedMediaType == Builder.MediaType.IMAGE) {
            cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mediaFile = getImageFile();
        } else {
            cameraInent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            mediaFile = getVideoFile();
        }

        if (mediaFile == null) {
            errorMessage("Can't create file");
            return;
        }

        if (cameraInent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Camera Application");
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(getContext(), getContext()
                .getApplicationContext()
                .getPackageName() + ".provider", mediaFile);

        List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager()
                .queryIntentActivities(cameraInent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            getContext().grantUriPermission(packageName, photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        TedOnActivityResult.with(getActivity())
                .setIntent(cameraInent)
                .setListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            onActivityResultCamera(cameraImageUri);
                        }
                    }
                })
                .startActivityForResult();
    }

    @Nullable
    private File getImageFile() {
        // Create an image file name
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
                    Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            if (!storageDir.exists()) {
                boolean success = storageDir.mkdirs();
                if (!success) return null;
                // TODO
            }

            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );


            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage("Could not create imageFile for camera");
        }


        return imageFile;
    }

    @Nullable
    private File getVideoFile() {
        // Create an image file name
        File videoFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
                    Locale.getDefault()).format(new Date());
            String imageFileName = "VIDEO_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);

            if (!storageDir.exists()) {
                Boolean success = storageDir.mkdirs();
                if (!success) return null;
            }

            videoFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );


            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(videoFile);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage("Could not create imageFile for camera");
        }


        return videoFile;
    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

//        if (onErrorListener == null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
//        } else {
//            onErrorListener.onError(errorMessage);
//        }
    }

    private void startGalleryIntent() {
        Objects.requireNonNull(getActivity());
        Intent galleryIntent;
        if (selectedMediaType == Builder.MediaType.IMAGE) {
            galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        } else {
            galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setDataAndType(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"video/*");
        }

        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Gallery Application");
            return;
        }

        TedOnActivityResult.with(getActivity())
                .setIntent(galleryIntent)
                .setListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            onActivityResultGallery(data);
                        }
                    }
                })
                .startActivityForResult();
    }

    private void errorMessage() {
        errorMessage(null);
    }

    private void setTitle() {

        if (!showTitle) {
            tv_title.setVisibility(View.GONE);

            if (!isMultiSelect()) {
                view_title_container.setVisibility(View.GONE);
            }

            return;
        }

        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
        }

        if (titleBackgroundResId > 0) {
            tv_title.setBackgroundResource(titleBackgroundResId);
        }
    }

    private boolean isMultiSelect() {
        return false;
//        return onMultiImageSelectedListener != null;
    }

    private void onActivityResultCamera(final Uri cameraImageUri) {

        String mimeType = MimeTypeUtil.getMimeType(getActivity(), cameraImageUri);
        MediaScannerConnection.scanFile(getContext(), new String[]{cameraImageUri.getPath()},
                new String[]{mimeType}, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String s, Uri uri) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                        complete(cameraImageUri);
                    }
                });

            }
        });
    }

    private void onActivityResultGallery(Intent data) {
        Uri temp = data.getData();

        if (temp == null) {
            errorMessage();
        }

        String realPath = RealPathUtil.getRealPath(getActivity(), temp);

        Uri selectedImageUri;
        try {
            selectedImageUri = Uri.fromFile(new File(realPath));
        } catch (Exception ex) {
            selectedImageUri = Uri.parse(realPath);
        }

        complete(selectedImageUri);
    }


//    public interface OnMultiImageSelectedListener {
//        void onImagesSelected(ArrayList<Uri> uriList);
//    }

    public interface OnImageSelectedListener {
        void onImageSelected(Uri uri);
    }

//    public interface OnErrorListener {
//        void onError(String message);
//    }

    public int getPreviewMaxCount() {
        return previewMaxCount;
    }

    @Nullable
    public Drawable getCameraTileDrawable() {
        return cameraTileDrawable;
    }

    @Nullable
    public Drawable getVideoCameraTileDrawable() {
        return videoCameraTileDrawable;
    }

    @Nullable
    public Drawable getGalleryTileDrawable() {
        return galleryTileDrawable;
    }

    @Nullable
    public Drawable getSelectedForegroundDrawable() {
//        return selectedForegroundDrawable;
        return null;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public boolean isShowGallery() {
        return showGallery;
    }

    public int getCameraTileBackgroundResId() {
        return cameraTileBackgroundResId;
    }

    public int getGalleryTileBackgroundResId() {
        return galleryTileBackgroundResId;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public static class Builder {
        @NonNull private Context context;
        @NonNull private Bundle bundle;

        public Builder(@NonNull Context context) {
            this.context = context;
            bundle = new Bundle();
            setCameraTile(R.drawable.ic_camera);
            setGalleryTile(R.drawable.ic_gallery);
            setVideoCameraTile(R.drawable.ic_clear);
            setSpacingResId(R.dimen.tedbottompicker_grid_layout_margin);
        }

        public Builder setCameraTile(@DrawableRes int cameraTileResId) {
            bundle.putInt(EXTRA_CAMERA_TILE_RES_ID, cameraTileResId);
            return this;
        }

        public Builder setVideoCameraTile(@DrawableRes int videoCameraTileResId) {
            bundle.putInt(EXTRA_VIDEO_CAMERA_TILE_RES_ID, videoCameraTileResId);
            return this;
        }

        public Builder setGalleryTile(@DrawableRes int galleryTileResId) {
            bundle.putInt(EXTRA_GALLERY_TILE_RES_ID, galleryTileResId);
            return this;
        }

        public Builder setSpacingResId(@DimenRes int dimenResId) {
            int spacing = context.getResources().getDimensionPixelSize(dimenResId);
            bundle.putInt(EXTRA_SPACING, spacing);
            return this;
        }

        public Builder showCameraTile(boolean showCamera) {
            bundle.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
            return this;
        }

        public Builder showGalleryTile(boolean showGallery) {
//            bundle.putBoolean(EXTRA_SHOW);
            return this;
        }

        public Builder setCameraTileBackgroundResId(@ColorRes int resId) {
            bundle.putInt(EXTRA_CAMERA_TILE_BG, resId);
            return this;
        }

        public Builder setGalleryTileBackgroundResId(@ColorRes int resId) {
            bundle.putInt(EXTRA_GALLERY_TILE_BG, resId);
            return this;
        }

        public Builder setTitle(String title) {
            bundle.putString(EXTRA_TITLE, title);
            return this;
        }

        public Builder setTitle(@StringRes int stringResId) {
            String title = context.getResources().getString(stringResId);
            return setTitle(title);
        }

        public Builder showTitle(boolean showTitle) {
            bundle.putBoolean(EXTRA_SHOW_TITLE, showTitle);
            return this;
        }

        public Builder setMediaTypes(@MediaType int[] types){
            bundle.putIntArray(EXTRA_MEDIA_TYPES, types);
            return this;
        }

        public TedBottomPicker create() {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. " +
                        "Did you remember to request it first?");
            }

            TedBottomPicker fragment = new TedBottomPicker();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({MediaType.IMAGE, MediaType.VIDEO})
        public @interface MediaType {
            int IMAGE = 1;
            int VIDEO = 2;
        }

    }

}
