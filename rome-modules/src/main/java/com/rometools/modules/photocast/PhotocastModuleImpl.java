/*
 * Copyright 2006 Robert Cooper, Temple of the Screaming Penguin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rometools.modules.photocast;

import java.net.URL;
import java.time.ZonedDateTime;

import com.rometools.modules.photocast.types.Metadata;
import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;

public class PhotocastModuleImpl implements PhotocastModule {

    private static final long serialVersionUID = 1L;
    private ZonedDateTime photoDate;
    private ZonedDateTime cropDate;
    private URL imageUrl;
    private URL thumbnailUrl;
    private Metadata metadata;

    public PhotocastModuleImpl() {
    }

    @Override
    public ZonedDateTime getPhotoDate() {
        return photoDate;
    }

    @Override
    public void setPhotoDate(final ZonedDateTime photoDate) {
        this.photoDate = photoDate;
    }

    @Override
    public ZonedDateTime getCropDate() {
        return cropDate;
    }

    @Override
    public void setCropDate(final ZonedDateTime cropDate) {
        this.cropDate = cropDate;
    }

    @Override
    public URL getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(final URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public URL getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public void setThumbnailUrl(final URL thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void copyFrom(final CopyFrom obj) {
        final PhotocastModule pm = (PhotocastModule) obj;
        setPhotoDate(pm.getPhotoDate() == null ? null : (ZonedDateTime) pm.getPhotoDate());
        setCropDate(pm.getCropDate() == null ? null : (ZonedDateTime) pm.getCropDate());
        setImageUrl(pm.getImageUrl());
        setThumbnailUrl(pm.getThumbnailUrl());
        setMetadata(pm.getMetadata());
    }

    @Override
    public Object clone() {
        final PhotocastModuleImpl pm = new PhotocastModuleImpl();
        pm.setPhotoDate(getPhotoDate() == null ? null : (ZonedDateTime) getPhotoDate());
        pm.setCropDate(getCropDate() == null ? null : (ZonedDateTime) getCropDate());
        pm.setImageUrl(getThumbnailUrl());
        pm.setThumbnailUrl(getThumbnailUrl());
        pm.setMetadata(getMetadata());

        return pm;
    }

    @Override
    public String getUri() {
        return PhotocastModule.URI;
    }

    @Override
    public Class<PhotocastModule> getInterface() {
        return PhotocastModule.class;
    }

    @Override
    public String toString() {
        return ToStringBean.toString(PhotocastModuleImpl.class, this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBean.beanEquals(PhotocastModuleImpl.class, this, obj);
    }

    @Override
    public int hashCode() {
        return EqualsBean.beanHashCode(this);
    }
}
