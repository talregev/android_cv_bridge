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

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.ros.internal.message.MessageBuffers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import sensor_msgs.Image;
import sensor_msgs.imageEncodings;
import std_msgs.Header;

/**
 * @author Tal Regev
 */
public class CvImage
{
    protected static final String TAG = "cv_bridge::CvImage";
    public Header header;
    public Mat image = new Mat();
    public String encoding = "";
    protected ChannelBufferOutputStream stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

    CvImage(){}

    public CvImage(final Header header, final String encoding)
    {
        this.header = header;
        this.encoding = encoding.toUpperCase();
        this.image = new Mat();
    }

    public CvImage(final Header header, final String encoding,
                   final Mat image)
    {
        this.header = header;
        this.encoding = encoding.toUpperCase();
        this.image = image;
    }

//    @SuppressWarnings("unused")
//    public final Image toImageMsg() throws IOException {
//        //TODO create new blank message and not get it from the user. (i don't know how to do it)
//        Image newBlankMessage = null; //new Image()
//        //noinspection ConstantConditions
//        return toImageMsg(newBlankMessage);
//    }

    @SuppressWarnings("unused")
    public final Image toImageMsg(final Image ros_image) throws IOException {
        ros_image.setHeader(header);
        ros_image.setEncoding(encoding.toLowerCase());
        int totalByteFrame = (ImEncode.safeLongToInt(image.total()));
        ros_image.setWidth(image.width());
        ros_image.setHeight(image.height());
        ros_image.setStep(totalByteFrame / image.height());

        byte[] imageInBytes = new byte[totalByteFrame * image.channels()];
        image.get(0, 0, imageInBytes);
        stream.write(imageInBytes);

        //noinspection UnusedAssignment
        imageInBytes = null;

        ros_image.setData(stream.buffer().copy());
        return ros_image;
    }

    @SuppressWarnings("unused")
    static public CvImage toCvCopy(final Image source) throws Exception {
        return CvImage.toCvCopyImpl(matFromImage(source), source.getHeader(), source.getEncoding().toUpperCase(), "");
    }

    @SuppressWarnings("unused")
    static public CvImage toCvCopy(final Image source, final String encoding) throws Exception {
        return CvImage.toCvCopyImpl(matFromImage(source), source.getHeader(), source.getEncoding().toUpperCase(), encoding.toLowerCase());
    }

    @SuppressWarnings("unused")
    static public CvImage cvtColor(final CvImage source, String encoding) throws Exception {
        return toCvCopyImpl(source.image, source.header, source.encoding, encoding);
    }



    protected static CvImage toCvCopyImpl(final Mat source,
                            final Header src_header,
                            final String src_encoding,
                            final String dst_encoding) throws Exception
    {
        /// @todo Handle endianness - e.g. 16-bit dc1394 camera images are big-endian

        // Copy metadata
        CvImage cvImage = new CvImage();
        cvImage.header = src_header;

        // Copy to new buffer if same encoding requested
        if (dst_encoding.isEmpty() || dst_encoding.equals(src_encoding))
        {
            cvImage.encoding = src_encoding;
            source.copyTo(cvImage.image);
        }
        else
        {
            // Convert the source data to the desired encoding
            final Vector<Integer> conversion_codes = ImEncode.getConversionCode(src_encoding, dst_encoding);
            Mat image1 = source;
            Mat image2 = new Mat();

            for(int i=0; i < conversion_codes.size(); ++i)
            {
                int conversion_code = conversion_codes.get(i);
                if (conversion_code == ImEncode.SAME_FORMAT) {
                    //double alpha = 1.0;
                    int src_depth = imageEncodings.bitDepth(src_encoding);
                    int dst_depth = imageEncodings.bitDepth(dst_encoding);
                    // Do scaling between CV_8U [0,255] and CV_16U [0,65535] images.
                    if (src_depth == 8 && dst_depth == 16)
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding), 65535. / 255.);
                    else if (src_depth == 16 && dst_depth == 8)
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding), 255. / 65535.);
                    else
                        image1.convertTo(image2, ImEncode.getCvType(dst_encoding));

                }
                else
                {
                    // Perform color conversion
                    Imgproc.cvtColor(image1, image2, conversion_codes.get(0));
                }
                image1 = image2;
            }
            cvImage.image = image2;
            cvImage.encoding = dst_encoding;
        }
        return cvImage;
    }

    protected static Mat matFromImage(final Image source) throws Exception {
        byte[] imageInBytes = source.getData().array();
        imageInBytes = Arrays.copyOfRange(imageInBytes,source.getData().arrayOffset(),imageInBytes.length);
        String encoding = source.getEncoding().toUpperCase();
        Mat cvImage = new Mat(source.getHeight(),source.getWidth(),ImEncode.getCvType(encoding));
        cvImage.put(0,0,imageInBytes);
        return cvImage;
    }
}
