/*
 * Copyright (c) 2015, Tal Regev
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Android Sensors Driver nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package cv_bridge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.ros.internal.message.MessageBuffers;

import java.io.IOException;
import java.nio.ByteBuffer;

import sensor_msgs.CompressedImage;
import std_msgs.Header;

/**
 * @author Tal Regev
 */
public class CvCompressImage
{
    public Header header;
    public Mat image = new Mat();
    public String format = "";
    protected ChannelBufferOutputStream stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

    @SuppressWarnings("unused")
    public CvCompressImage(){}

    @SuppressWarnings("unused")
    public CvCompressImage(final Header header, final String format)
    {
        this.header = header;
        this.format = format.toLowerCase();
        this.image = new Mat();
    }
    @SuppressWarnings("unused")
    public CvCompressImage(final Header header, final String format,
                           final Mat image)
    {
        this.header = header;
        this.format = format.toLowerCase();
        this.image = image;
    }

    @SuppressWarnings("unused")
    public final CompressedImage toImageMsg() throws IOException {
        //TODO create new blank message and not get it from the user. (i don't know how to do it)
        CompressedImage newBlankMessage = null; //new CompressedImage()
        //noinspection ConstantConditions
        return toImageMsg(newBlankMessage);
    }

    @SuppressWarnings("unused")
    //TODO add a compression parameter.
    public final CompressedImage toImageMsg(final CompressedImage ros_image) throws IOException {
        ros_image.setHeader(header);
        MatOfByte   buf         = new MatOfByte();
        if (format.isEmpty() || format.equals("jpg"))
        {
            ros_image.setFormat("jpg");
            Highgui.imencode(".jpg", image, buf);
        }

        if(format.equals("png"))
        {
            ros_image.setFormat("png");
            Highgui.imencode(".png", image, buf);
        }

        //TODO: check this formats (on rviz) and add more formats
        //from http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#Mat imread(const string& filename, int flags)
        if(format.equals("jp2"))
        {
            ros_image.setFormat("jp2");
            Highgui.imencode(".jp2", image, buf);
        }

        if(format.equals("bmp"))
        {
            ros_image.setFormat("bmp");
            Highgui.imencode(".bmp", image, buf);
        }
        if(format.equals("tif"))
        {
            ros_image.setFormat("tif");
            Highgui.imencode(".tif", image, buf);
        }

        stream.write(buf.toArray());

        ros_image.setData(stream.buffer().copy());
        return ros_image;
    }

    @SuppressWarnings("unused")
    static public CvCompressImage toCvCopy(final CompressedImage source) throws Exception {
        return CvCompressImage.toCvCopyImpl(matFromImage(source), source.getHeader(), "");
    }

    @SuppressWarnings("unused")
    static public CvCompressImage toCvCopy(final CompressedImage source, final String format) throws Exception {
        return CvCompressImage.toCvCopyImpl(matFromImage(source), source.getHeader(), format.toLowerCase());
    }

    @SuppressWarnings("unused")
    static public CvCompressImage cvtColor(final CvCompressImage source ) throws Exception {
        return toCvCopyImpl(source.image, source.header, source.format);
    }

    protected static CvCompressImage toCvCopyImpl(final Mat source,
                            final Header src_header,
                            final String dst_format) throws Exception {

        // Copy metadata
        CvCompressImage cvCompressImage = new CvCompressImage();
        cvCompressImage.header = src_header;
        cvCompressImage.format = dst_format;
        return cvCompressImage;
    }

    protected static Mat matFromImage(final CompressedImage source) throws Exception
    {
        ChannelBuffer data = source.getData();
        byte[] imageInBytes = data.array();
        int offset = data.arrayOffset();
        Bitmap image = BitmapFactory.decodeByteArray(imageInBytes, offset, imageInBytes.length);
        ByteBuffer bb = ByteBuffer.allocate(image.getRowBytes() * image.getHeight());
        image.copyPixelsToBuffer(bb);
        //TODO: check which cv type the encoded image.
        Mat cvImage = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC3);
        cvImage.put(image.getHeight(),image.getWidth(),bb.array());
        return cvImage;
    }
}
