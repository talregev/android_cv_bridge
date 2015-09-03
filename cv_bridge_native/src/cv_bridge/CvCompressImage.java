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

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgproc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;

import java.util.Arrays;
import java.util.Vector;

import sensor_msgs.CompressedImage;
import sensor_msgs.imageEncodings;
import std_msgs.Header;

/**
 * @author Tal Regev
 */
public class CvCompressImage
{
    public Header header;
    public Mat image        = new Mat();
    public String encoding  = imageEncodings.BGR8;
    protected ChannelBufferOutputStream stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

    @SuppressWarnings("unused")
    public CvCompressImage(){}

    @SuppressWarnings("unused")
    public CvCompressImage(final Header header, final String encoding)
    {
        this.header     = header;
        this.encoding   = encoding;
        this.image      = new Mat();
    }
    @SuppressWarnings("unused")
    public CvCompressImage(final Header header, final String encoding,
                           final Mat image)
    {
        this.header     = header;
        this.encoding   = encoding;
        this.image      = image;
    }

//    @SuppressWarnings("unused")
//    public final CompressedImage toImageMsg() throws IOException {
//        //TODO create new blank message and not get it from the user. (i don't know how to do it)
//        CompressedImage newBlankMessage = null; //new CompressedImage()
//        //noinspection ConstantConditions
//        return toImageMsg(newBlankMessage);
//    }

    //TODO add a compression parameter.
    public final CompressedImage toImageMsg(final CompressedImage ros_image, Format dst_format) throws Exception {
        ros_image.setHeader(header);
        if(!encoding.equals(imageEncodings.BGR8))
        {
            CvCompressImage temp = CvCompressImage.cvtColor(this,imageEncodings.BGR8);
            this.image      = temp.image;
        }
        //from https://github.com/bytedeco/javacpp-presets/issues/29#issuecomment-6408082977
        BytePointer buf = new BytePointer();
        if (Format.JPG == dst_format)
        {
            ros_image.setFormat("jpg");
            opencv_highgui.imencode(".jpg", image, buf);
        }

        if(Format.PNG == dst_format)
        {
            ros_image.setFormat("png");
            opencv_highgui.imencode(".png", image, buf);
        }

        //TODO: check this formats (on rviz) and add more formats
        //from http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#Mat imread(const string& filename, int flags)
        if(Format.JP2 == dst_format)
        {
            ros_image.setFormat("jp2");
            opencv_highgui.imencode(".jp2", image, buf);
        }

        if(Format.BMP == dst_format)
        {
            ros_image.setFormat("bmp");
            opencv_highgui.imencode(".bmp", image, buf);
        }
        if(Format.TIF == dst_format)
        {
            ros_image.setFormat("tif");
            opencv_highgui.imencode(".tif", image, buf);
        }
        //from https://github.com/bytedeco/javacpp-presets/issues/29#issuecomment-6408082977
        byte[] outputBuffer = new byte[buf.limit()];
        buf.get(outputBuffer);
        stream.write(outputBuffer);

        ros_image.setData(stream.buffer().copy());
        return ros_image;
    }


    static public CvCompressImage toCvCopy(final CompressedImage source) throws Exception {
        return CvCompressImage.toCvCopyImpl(matFromImage(source), source.getHeader(), "","");
    }

    static public CvCompressImage toCvCopy(final CompressedImage source,final String dst_encoding) throws Exception {
        return CvCompressImage.toCvCopyImpl(matFromImage(source), source.getHeader(), "", dst_encoding.toUpperCase());
    }

    static public CvCompressImage cvtColor(final CvCompressImage source,final String dst_encoding) throws Exception {
        return toCvCopyImpl(source.image, source.header,source.encoding,dst_encoding);
    }

    protected static CvCompressImage toCvCopyImpl(final Mat source,
                                    final Header src_header,
                                    final String src_encoding,
                                    final String dst_encoding) throws Exception
    {
        /// @todo Handle endianness - e.g. 16-bit dc1394 camera images are big-endian

        // Copy metadata
        CvCompressImage cvCompressImage = new CvCompressImage();
        cvCompressImage.header = src_header;


        // Copy to new buffer if same encoding requested
        if (dst_encoding.isEmpty() || dst_encoding.equals(src_encoding))
        {
            if(!src_encoding.isEmpty()) {
                cvCompressImage.encoding = src_encoding;
            }
            source.copyTo(cvCompressImage.image);
        }
        else
        {
            // Convert the source data to the desired encoding
            String encoding = src_encoding;
            if (src_encoding.isEmpty())
            {
                encoding = "bgr8";
            }
            final Vector<Integer> conversion_codes = ImEncode.getConversionCode(encoding, dst_encoding);

            Mat image1 = source;
            Mat image2 = new Mat();


            for(int i=0; i < conversion_codes.size(); ++i)
            {
                int conversion_code = conversion_codes.get(i);
                if (conversion_code == ImEncode.SAME_FORMAT) {
                    //convert from Same number of channels, but different bit depth

                    //double alpha = 1.0;
                    int src_depth = imageEncodings.bitDepth(src_encoding);
                    int dst_depth = imageEncodings.bitDepth(dst_encoding);
                    // Do scaling between CV_8U [0,255] and CV_16U [0,65535] images.
                    //TODO: check which value default for beta is ok.
                    //from http://www.rubydoc.info/github/ruby-opencv/ruby-opencv/OpenCV/CvMat
                    //from http://docs.opencv.org/modules/core/doc/basic_structures.html
                    int beta = 0;
                    if (src_depth == 8 && dst_depth == 16)
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding), 65535. / 255.,beta);
                    else if (src_depth == 16 && dst_depth == 8)
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding), 255. / 65535.,beta);
                    else
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding));
                }
                else
                {
                    // Perform color conversion
                    opencv_imgproc.cvtColor(image1, image2, conversion_codes.get(0));
                }
                image1 = image2;
            }
            cvCompressImage.image = image2;
            cvCompressImage.encoding = dst_encoding;
        }
        return cvCompressImage;
    }

    protected static Mat matFromImage(final CompressedImage source) throws Exception
    {
        ChannelBuffer data = source.getData();
        byte[] imageInBytes = data.array();
        imageInBytes = Arrays.copyOfRange(imageInBytes, source.getData().arrayOffset(), imageInBytes.length);
        //from http://stackoverflow.com/questions/23202130/android-convert-byte-array-from-camera-api-to-color-mat-object-opencv
        Mat cvImage = new Mat(1, imageInBytes.length, opencv_core.CV_8UC1);
        BytePointer bytePointer = new BytePointer(imageInBytes);
        cvImage = cvImage.data(bytePointer);

        return opencv_highgui.imdecode(cvImage, opencv_highgui.IMREAD_COLOR);
    }
}
