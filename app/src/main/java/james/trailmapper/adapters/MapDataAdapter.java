package james.trailmapper.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import james.trailmapper.R;
import james.trailmapper.TrailMapper;
import james.trailmapper.activities.MapActivity;
import james.trailmapper.data.MapData;

public class MapDataAdapter extends RecyclerView.Adapter<MapDataAdapter.ViewHolder> {

    private Activity activity;
    private TrailMapper trailMapper;
    private List<MapData> maps;

    public MapDataAdapter(Activity activity, List<MapData> maps) {
        this.activity = activity;
        trailMapper = (TrailMapper) activity.getApplicationContext();
        this.maps = maps;
    }

    public void setMaps(List<MapData> maps) {
        this.maps = maps;
        notifyDataSetChanged();
    }

    public List<MapData> getMaps() {
        return new ArrayList<>(maps);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MapData map = maps.get(position);

        ((TextView) holder.v.findViewById(R.id.title)).setText(map.getName());
        ((TextView) holder.v.findViewById(R.id.offlineText)).setText(map.isOffline() ? R.string.action_delete : R.string.action_save);

        ImageView offlineImage = (ImageView) holder.v.findViewById(R.id.offlineImage);
        offlineImage.setVisibility(View.VISIBLE);
        offlineImage.setImageResource(map.isOffline() ? R.drawable.ic_delete : R.drawable.ic_download);

        holder.v.findViewById(R.id.offlineProgress).setVisibility(View.GONE);

        RequestBuilder<Bitmap> request = map.getDrawable(activity);
        if (request != null)
            request.into(((ImageView) holder.v.findViewById(R.id.image)));

        holder.v.findViewById(R.id.action_directions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position < 0 || position >= maps.size()) return;

                LatLng latLng = maps.get(position).getLatLng();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude)));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(activity.getPackageManager()) == null) //might not have google maps, use standard geo uri
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude)));

                activity.startActivity(intent);
            }
        });

        holder.v.findViewById(R.id.action_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position >= 0 && position < maps.size()) {
                    MapData mapData = maps.get(position);
                    if (!mapData.isOffline()) {
                        trailMapper.addOfflineMap(mapData);

                        ((TextView) holder.v.findViewById(R.id.offlineText)).setText(R.string.title_downloading);
                        holder.v.findViewById(R.id.offlineProgress).setVisibility(View.VISIBLE);
                        holder.v.findViewById(R.id.offlineImage).setVisibility(View.GONE);
                    } else trailMapper.removeOfflineMap(mapData);
                }
            }
        });

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position < 0 || position >= maps.size()) return;

                Intent intent = new Intent(activity, MapActivity.class);
                intent.putExtra(MapActivity.EXTRA_MAP, maps.get(position));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle());
                else activity.startActivity(intent);
            }
        });

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return maps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
