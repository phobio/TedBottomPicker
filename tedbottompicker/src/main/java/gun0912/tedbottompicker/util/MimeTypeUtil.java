package gun0912.tedbottompicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

public class MimeTypeUtil {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MimeType.IMAGE, MimeType.VIDEO, MimeType.WILDCARD})
    public @interface MimeType {
        String IMAGE = "image/jpeg";
        String VIDEO = "video/mp4";
        String WILDCARD = "*/*";
    }

    @Nullable
    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


}
