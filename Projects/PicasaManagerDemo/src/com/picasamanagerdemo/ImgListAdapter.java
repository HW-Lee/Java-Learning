package com.picasamanagerdemo;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImgListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<PicasaPhotoInfo> PhotoArray = new ArrayList<PicasaPhotoInfo>();

    private class ViewTag {
        ImageView img = null;
        TextView albumName = null;
        TextView imageSrc = null;
        TextView imageSize = null;
        TextView imageSizeDetail = null;

        private ViewTag(ImageView img, TextView albumName, TextView imageSrc, TextView imageSize,
                TextView imageSizeDetail) {
            this.img = img;
            this.albumName = albumName;
            this.imageSrc = imageSrc;
            this.imageSize = imageSize;
            this.imageSizeDetail = imageSizeDetail;
        }
    }

    public ImgListAdapter(Context context, ArrayList<PicasaPhotoInfo> PhotoArray) {
        mInflater = LayoutInflater.from(context);
        this.PhotoArray = PhotoArray;
    }

    @Override
    public int getCount() {
        return PhotoArray.size();
    }

    @Override
    public Object getItem(int position) {
        return PhotoArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewTag tag;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_imglist, null);
            tag = new ViewTag((ImageView) convertView.findViewById(R.id.img),
                    (TextView) convertView.findViewById(R.id.albumName),
                    (TextView) convertView.findViewById(R.id.imageSrc),
                    (TextView) convertView.findViewById(R.id.imageSize),
                    (TextView) convertView.findViewById(R.id.imageSizeDetail));
            convertView.setTag(tag);
        } else
            tag = (ViewTag) convertView.getTag();
        tag.img.setImageBitmap(PhotoArray.get(position).getImageBitmap());
        tag.albumName.setText(PhotoArray.get(position).getAlbumNameToString());
        tag.imageSrc.setText(PhotoArray.get(position).getImageSrcToString());
        tag.imageSize.setText(PhotoArray.get(position).getImageSizeToString());
        tag.imageSizeDetail.setVisibility(View.GONE);
        return convertView;
    }

}
