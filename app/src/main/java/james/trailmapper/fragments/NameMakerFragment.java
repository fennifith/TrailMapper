package james.trailmapper.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import james.trailmapper.R;
import james.trailmapper.data.MapData;
import james.trailmapper.data.PositionData;
import james.trailmapper.utils.ImageUtils;
import james.trailmapper.views.RatioImageView;

public class NameMakerFragment extends MakerFragment {

    private static final int REQUEST_SELECT_IMAGE = 824;
    private static final int REQUEST_TAKE_PICTURE = 543;

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.imageView)
    RatioImageView imageView;

    private boolean isNameComplete;
    private boolean isImageComplete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_name_maker, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @OnTextChanged(R.id.name)
    void setName() {
        String text = name.getText().toString();
        isNameComplete = false;

        for (MapData map : getTrailMapper().getMaps()) {
            if (text.equals(map.getName())) {
                changeComletion(false);
                name.setError(
                        getString(R.string.msg_same_name),
                        ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_error, Color.WHITE)
                );
                return;
            }
        }

        for (MapData map : getTrailMapper().getOfflineMaps()) {
            if (text.equals(map.getName())) {
                changeComletion(false);
                name.setError(
                        getString(R.string.msg_same_name),
                        ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_error, Color.WHITE)
                );
                return;
            }
        }

        if (getMap() != null) {
            getMap().name = text;
            isNameComplete = true;
            changeComletion(isImageComplete);
        }
    }

    @OnClick(R.id.selectImage)
    void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_image)), REQUEST_SELECT_IMAGE);
    }

    @OnClick(R.id.takePicture)
    void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            File file = new File(getActivity().getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + ".png");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            getActivity().startActivityForResult(intent, REQUEST_TAKE_PICTURE);

            if (getMap() != null)
                getMap().offlineImage = file.getAbsolutePath();
        }
    }

    @Override
    public void onSelect() {
    }

    @Override
    public void onLocationChanged(PositionData position) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onMapChanged(MapData map) {
    }

    @Override
    public void onMapsChanged() {
    }

    @Override
    public void onPreferenceChanged() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    File file = new File(data.getData().getPath());
                    if (getMap() != null) {
                        getMap().offlineImage = file.getAbsolutePath();
                        isImageComplete = true;
                    }
                }
                break;
            case REQUEST_TAKE_PICTURE:
                isImageComplete = resultCode == Activity.RESULT_OK;
                break;
        }

        changeComletion(isNameComplete && isImageComplete);
        if (getMap() != null)
            getMap().getDrawable(getContext()).into(imageView);

        super.onActivityResult(requestCode, resultCode, data);
    }
}