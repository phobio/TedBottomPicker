package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.util.MimeTypeUtil;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private ArrayList<PickerTile> pickerTiles;
    private Context context;
    private TedBottomPicker bottomPicker;
    private OnItemClickListener onItemClickListener;
    private ArrayList<Uri> selectedUriList;
    @TedBottomPicker.Builder.MediaType
    private int selectedMediaType;

    public GalleryAdapter(
            @NonNull Context context,
            @NonNull TedBottomPicker picker,
            @TedBottomPicker.Builder.MediaType int selectedMediaType
    ) {

        this.context = context;
        this.bottomPicker = picker;

        pickerTiles = new ArrayList<>();
        selectedUriList = new ArrayList<>();

        if (bottomPicker.isShowCamera()) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (bottomPicker.isShowGallery()) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }
        this.selectedMediaType = selectedMediaType;
        Cursor cursor = null;
        try {
            String[] columns;
            String orderBy;
            Uri uri;
            if (selectedMediaType == TedBottomPicker.Builder.MediaType.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Images.Media.DATA};
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
            } else {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Video.VideoColumns.DATA};
                orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC";
            }

            cursor = context.getApplicationContext().getContentResolver().query(uri, columns, null, null, orderBy);
            //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


            if (cursor != null) {

                int count = 0;
                while (cursor.moveToNext() && count < bottomPicker.getPreviewMaxCount()) {

                    String dataIndex;
                    if (selectedMediaType == TedBottomPicker.Builder.MediaType.IMAGE) {
                        dataIndex = MediaStore.Images.Media.DATA;
                    }else{
                        dataIndex = MediaStore.Video.VideoColumns.DATA;
                    }
                    String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    File imageFile = new File(imageLocation);
                    pickerTiles.add(new PickerTile(Uri.fromFile(imageFile)));
                    count++;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    public void setSelectedUriList(ArrayList<Uri> selectedUriList, Uri uri) {
        this.selectedUriList = selectedUriList;

        int position = -1;


        PickerTile pickerTile;
        for (int i = 0; i < pickerTiles.size(); i++) {
            pickerTile = pickerTiles.get(i);
            if (pickerTile.isImageTile() && Objects.equals(pickerTile.getImageUri(), uri)) {
                position = i;
                break;
            }
        }

        if (position > 0) {
            notifyItemChanged(position);
        }

    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, final int position) {

        PickerTile pickerTile = getItem(position);

        boolean isSelected = false;

        if (pickerTile.isCameraTile()) {
            holder.iv_thumbnail.setBackgroundResource(bottomPicker.getCameraTileBackgroundResId());
            Drawable cameraDrawable =
                    (TedBottomPicker.Builder.MediaType.VIDEO == selectedMediaType
                            && bottomPicker.getVideoCameraTileDrawable() != null) ?
                            bottomPicker.getVideoCameraTileDrawable() : bottomPicker.getCameraTileDrawable();
            holder.iv_thumbnail.setImageDrawable(cameraDrawable);
        } else if (pickerTile.isGalleryTile()) {
            holder.iv_thumbnail.setBackgroundResource(bottomPicker.getGalleryTileBackgroundResId());
            holder.iv_thumbnail.setImageDrawable(bottomPicker.getGalleryTileDrawable());

        } else {
            holder.tv_timestamp.setVisibility(View.GONE);
            Uri uri = pickerTile.getImageUri();
            if (uri == null)
                return;
            String type = MimeTypeUtil.getMimeType(context, uri);
            if (MimeTypeUtil.MimeType.VIDEO.equals(type)){
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(context, uri);
                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long timeInMillisec = Long.parseLong(time);
                    retriever.release();

                    if (timeInMillisec > 0) {
                        String timeString = String.format(context.getString(R.string.time_string),
                                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec),
                                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec))
                        );
                        holder.tv_timestamp.setText(timeString);
                        holder.tv_timestamp.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Glide.with(context)
                    .load(uri)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().centerCrop()
                            .placeholder(R.drawable.ic_gallery)
                            .error(R.drawable.img_error))
                    .into(holder.iv_thumbnail);

            isSelected = selectedUriList.contains(uri);
        }


        if (holder.root != null) {

            Drawable foregroundDrawable;

            if (bottomPicker.getSelectedForegroundDrawable() != null) {
                foregroundDrawable = bottomPicker.getSelectedForegroundDrawable();
            } else {
                foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected);
            }

            ((FrameLayout) holder.root).setForeground(isSelected ? foregroundDrawable : null);
        }


        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        final Uri imageUri;
        final
        @TileType
        int tileType;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isGalleryTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }

        public boolean isImageTile() {
            return tileType == IMAGE;
        }

        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        public boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        @IntDef({IMAGE, CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareFrameLayout root;


        TedSquareImageView iv_thumbnail;
        TextView tv_timestamp;

        public GalleryViewHolder(View view) {
            super(view);
            root = view.findViewById(R.id.root);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
            tv_timestamp = view.findViewById(R.id.tv_timestamp);

        }

    }


}
