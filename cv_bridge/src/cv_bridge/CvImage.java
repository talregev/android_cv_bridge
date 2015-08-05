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

import android.util.Pair;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.ros.internal.message.MessageBuffers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sensor_msgs.Image;
import std_msgs.Header;

class CvImage
{
    public Header header;
    public Mat image = new Mat();
    public String encoding = "";
    protected ChannelBufferOutputStream stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
    protected static final int SAME_FORMAT = -1;


    @SuppressWarnings("unused")
    public CvImage(){}

    @SuppressWarnings("unused")
    public CvImage(final Header header, final String encoding)
    {
        this.header = header;
        this.encoding = encoding.toUpperCase();
        this.image = new Mat();
    }
    @SuppressWarnings("unused")
    public CvImage(final Header header, final String encoding,
                   final Mat image)
    {
        this.header = header;
        this.encoding = encoding.toUpperCase();
        this.image = image;
    }

    @SuppressWarnings("unused")
    public final Image toImageMsg() throws IOException {
        //TODO create new blank message and not get it from the user. (i don't know how to do it)
        Image newBlankMessage = null; //new Image()
        //noinspection ConstantConditions
        return toImageMsg(newBlankMessage);
    }

    @SuppressWarnings("unused")
    public final Image toImageMsg(final Image ros_image) throws IOException {
        ros_image.setHeader(header);
        ros_image.setEncoding(encoding.toLowerCase());
        int totalByteFrame = (safeLongToInt(image.total()));
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

    static int getCvType(final String encoding) throws Exception {

        if (encoding.equals("BGR8"))   return CvType.CV_8UC3;
        if (encoding.equals("MONO8"))  return CvType.CV_8UC1;
        if (encoding.equals("RGB8"))   return CvType.CV_8UC3;
        if (encoding.equals("MONO16")) return CvType.CV_16UC1;
        if (encoding.equals("BGR16"))  return CvType.CV_16UC3;
        if (encoding.equals("RGB16"))  return CvType.CV_16UC3;
        if (encoding.equals("BGRA8"))  return CvType.CV_8UC4;
        if (encoding.equals("RGBA8"))  return CvType.CV_8UC4;
        if (encoding.equals("BGRA16")) return CvType.CV_16UC4;
        if (encoding.equals("RGBA16")) return CvType.CV_16UC4;

        // For bayer, return one-channel
        if (encoding.equals("BAYER_RGGB8")) return CvType.CV_8UC1;
        if (encoding.equals("BAYER_BGGR8")) return CvType.CV_8UC1;
        if (encoding.equals("BAYER_GBRG8")) return CvType.CV_8UC1;
        if (encoding.equals("BAYER_GRBG8")) return CvType.CV_8UC1;
        if (encoding.equals("BAYER_RGGB16")) return CvType.CV_16UC1;
        if (encoding.equals("BAYER_BGGR16")) return CvType.CV_16UC1;
        if (encoding.equals("BAYER_GBRG16")) return CvType.CV_16UC1;
        if (encoding.equals("BAYER_GRBG16")) return CvType.CV_16UC1;

        // Miscellaneous
        if (encoding.equals("YUV422")) return CvType.CV_8UC2;
        throw new Exception("Unrecognized image encoding [" + encoding + "]");
    }

    protected int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    protected static Map<Pair<Encode, Encode>, Vector<Integer>> getConversionCodes() {
        Map<Pair<Encode, Encode>, Vector<Integer>> res = new HashMap<Pair<Encode, Encode>, Vector<Integer>>();

        for(int i=0; i<=5; ++i)
            res.put(new Pair<Encode, Encode>(Encode.valueOf(i), Encode.valueOf(i)),new Vector<Integer>(SAME_FORMAT));

        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.RGB), new Vector<Integer>(Imgproc.COLOR_GRAY2RGB));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_GRAY2BGR));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.RGBA),new Vector<Integer>(Imgproc.COLOR_GRAY2RGBA));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.BGRA),new Vector<Integer>(Imgproc.COLOR_GRAY2BGRA));

        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_RGB2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_RGB2BGR));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.RGBA),new Vector<Integer>(Imgproc.COLOR_RGB2RGBA));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.BGRA),new Vector<Integer>(Imgproc.COLOR_RGB2BGRA));

        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BGR2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BGR2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.RGBA),new Vector<Integer>(Imgproc.COLOR_BGR2RGBA));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.BGRA),new Vector<Integer>(Imgproc.COLOR_BGR2BGRA));

        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_RGBA2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_RGBA2RGB));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_RGBA2BGR));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.BGRA),new Vector<Integer>(Imgproc.COLOR_RGBA2BGRA));

        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BGRA2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BGRA2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_BGRA2BGR));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.RGBA),new Vector<Integer>(Imgproc.COLOR_BGRA2RGBA));

        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_YUV2GRAY_UYVY));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_YUV2RGB_UYVY));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_YUV2BGR_UYVY));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.RGBA),new Vector<Integer>(Imgproc.COLOR_YUV2RGBA_UYVY));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.BGRA),new Vector<Integer>(Imgproc.COLOR_YUV2BGRA_UYVY));

        // Deal with Bayer
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BayerBG2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BayerBG2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_BayerBG2BGR));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BayerRG2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BayerRG2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_BayerRG2BGR));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BayerGR2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BayerGR2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_BayerGR2BGR));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.GRAY),new Vector<Integer>(Imgproc.COLOR_BayerGB2GRAY));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.RGB),new Vector<Integer>(Imgproc.COLOR_BayerGB2RGB));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.BGR),new Vector<Integer>(Imgproc.COLOR_BayerGB2BGR));

        return res;
    }

    protected static Encode getFormat(final String encoding)
    {
        if ((encoding.equals("MONO8") || (encoding.equals("MONO8")))) return Encode.GRAY;
        if ((encoding.equals("BGR8") || (encoding.equals("BGR8")))) return Encode.BGR;
        if ((encoding.equals("RGB8") || (encoding.equals("RGB8")))) return Encode.RGB;
        if ((encoding.equals("BGRA8") || (encoding.equals("BGRA8")))) return Encode.BGRA;
        if ((encoding.equals("RGBA8") || (encoding.equals("RGBA8")))) return Encode.RGBA;
        if (encoding.equals("YUV422")) return Encode.YUV422;

        if ((encoding.equals("BAYER_RGGB8") || (encoding.equals("BAYER_RGGB8")))) return Encode.BAYER_RGGB;
        if ((encoding.equals("BAYER_BGGR8") || (encoding.equals("BAYER_BGGR8")))) return Encode.BAYER_BGGR;
        if ((encoding.equals("BAYER_GBRG8") || (encoding.equals("BAYER_GBRG8")))) return Encode.BAYER_GBRG;
        if ((encoding.equals("BAYER_GRBG8") || (encoding.equals("BAYER_GRBG8")))) return Encode.BAYER_GRBG;

        // We don't support conversions to/from other types
        return Encode.INVALID;
    }

    protected static final Vector<Integer> getConversionCode(String src_encoding, String dst_encoding) throws Exception {
        Encode src_encode = getFormat(src_encoding);
        Encode dst_encode = getFormat(dst_encoding);
        //TODO: check if src is color format
        boolean is_src_color_format = true; /*sensor_msgs::image_encodings::isColor(src_encoding) ||
            sensor_msgs::image_encodings::isMono(src_encoding) ||
            sensor_msgs::image_encodings::isBayer(src_encoding) ||
            (src_encoding == sensor_msgs::image_encodings::YUV422);
        */
        //TODO: check if dst is color format
        boolean is_dst_color_format = true; /*sensor_msgs::image_encodings::isColor(dst_encoding) ||
            sensor_msgs::image_encodings::isMono(dst_encoding) ||
            sensor_msgs::image_encodings::isBayer(dst_encoding) ||
            (dst_encoding == sensor_msgs::image_encodings::YUV422);
        */
        //TODO: check if number of channels are the same in src and dst
        boolean is_num_channels_the_same = true; /*(sensor_msgs::image_encodings::numChannels(src_encoding) == sensor_msgs::image_encodings::numChannels(dst_encoding));

        */
        // If we have no color info in the source, we can only convert to the same format which
        // was resolved in the previous condition. Otherwise, fail
        if (!is_src_color_format) {
            if (is_dst_color_format)
                throw new Exception("[" + src_encoding + "] is not a color format. but [" + dst_encoding +
                        "] is. The conversion does not make sense");
            if (!is_num_channels_the_same)
                throw new Exception("[" + src_encoding + "] and [" + dst_encoding + "] do not have the same number of channel");
            return new Vector<Integer>(1, SAME_FORMAT);
        }

        // If we are converting from a color type to a non color type, we can only do so if we stick
        // to the number of channels
        if (!is_dst_color_format) {
            if (!is_num_channels_the_same)
                throw new Exception("[" + src_encoding + "] is a color format but [" + dst_encoding + "] " +
                        "is not so they must have the same OpenCV type, CV_8UC3, CV16UC1 ....");
            return new Vector<Integer>(1, SAME_FORMAT);
        }

        // If we are converting from a color type to another type, then everything is fine
        final Map<Pair<Encode, Encode>, Vector<Integer>> CONVERSION_CODES = getConversionCodes();

        Pair<Encode, Encode> key = new Pair<Encode, Encode>(src_encode, dst_encode);
        Vector<Integer> val = CONVERSION_CODES.get(key);

        if (val == null)
            throw new Exception("Unsupported conversion from [" + src_encoding +
                    "] to [" + dst_encoding + "]");

        // And deal with depth differences
        //TODO check if it had depth differences
        //if (enc::bitDepth(src_encoding) != enc::bitDepth(dst_encoding))
        //    val.add(SAME_FORMAT);

        return val;
    }

    protected static CvImage toCvCopyImpl(final Mat source,
                            final Header src_header,
                            final String src_encoding,
                            final String dst_encoding) throws Exception {
        /// @todo Handle endianness - e.g. 16-bit dc1394 camera images are big-endian

        // Copy metadata
        CvImage cvCompressImage = new CvImage();
        cvCompressImage.header = src_header;

        // Copy to new buffer if same encoding requested
        if (dst_encoding.isEmpty() || dst_encoding.equals(src_encoding))
        {
            cvCompressImage.encoding = src_encoding;
            source.copyTo(cvCompressImage.image);
        }
        else
        {
            // Convert the source data to the desired encoding
            final Vector<Integer> conversion_codes = getConversionCode(src_encoding, dst_encoding);
            Mat image1 = source;
            Mat image2 = new Mat();


            for(int i=0; i < conversion_codes.size(); ++i)
            {
                int conversion_code = conversion_codes.get(i);
                if (conversion_code == SAME_FORMAT) {
                    //TODO: convert from Same number of channels, but different bit depth
                    /*
                    double alpha = 1.0;
                    int src_depth = enc::bitDepth(src_encoding);
                    int dst_depth = enc::bitDepth(dst_encoding);
                    // Do scaling between CV_8U [0,255] and CV_16U [0,65535] images.
                    if (src_depth == 8 && dst_depth == 16)
                        image1.convertTo(image2, getCvType(dst_encoding), 65535. / 255.);
                    else if (src_depth == 16 && dst_depth == 8)
                        image1.convertTo(image2, getCvType(dst_encoding), 255. / 65535.);
                    else
                        image1.convertTo(image2, getCvType(dst_encoding));
                     */
                }
                else
                {
                    // Perform color conversion
                    Imgproc.cvtColor(image1, image2, conversion_codes.get(0));
                }
                image1 = image2;
            }
            cvCompressImage.image = image2;
            cvCompressImage.encoding = dst_encoding;
        }
        return cvCompressImage;
    }

    protected static Mat matFromImage(final Image source) throws Exception {
        byte[] imageInBytes = source.getData().array();
        imageInBytes = Arrays.copyOfRange(imageInBytes,source.getData().arrayOffset(),imageInBytes.length);
        Mat cvImage = new Mat(source.getHeight(),source.getWidth(),getCvType(source.getEncoding()));
        cvImage.put(source.getHeight(),source.getWidth(),imageInBytes);
        return cvImage;
    }
}
