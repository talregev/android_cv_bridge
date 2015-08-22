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

import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


//from http://stackoverflow.com/questions/11047756/getting-enum-associated-with-int-value
public enum Encode { INVALID(-1), GRAY(0), RGB(1), BGR(2), RGBA(3), BGRA(4), YUV422(5), BAYER_RGGB(6), BAYER_BGGR(7), BAYER_GBRG(8), BAYER_GRBG(9);
    protected int formatNumbr;
    private static Map<Integer, Encode> map = new HashMap<Integer, Encode>();
    static {
        for (Encode legEnum : Encode.values()) {
            map.put(legEnum.formatNumbr, legEnum);
        }
    }

    Encode(final int formatNumbr) { this.formatNumbr = formatNumbr; }

    public static Encode valueOf(int formatNumbr) {
        return map.get(formatNumbr);
    }
}

class ImEncode
{
    protected static final int SAME_FORMAT = -1;

    static int getCvType(final String encoding) throws Exception {

        String lEncoding = encoding.toUpperCase();

        if (lEncoding.equals("BGR8"))   return CvType.CV_8UC3;
        if (lEncoding.equals("MONO8"))  return CvType.CV_8UC1;
        if (lEncoding.equals("RGB8"))   return CvType.CV_8UC3;
        if (lEncoding.equals("MONO16")) return CvType.CV_16UC1;
        if (lEncoding.equals("BGR16"))  return CvType.CV_16UC3;
        if (lEncoding.equals("RGB16"))  return CvType.CV_16UC3;
        if (lEncoding.equals("BGRA8"))  return CvType.CV_8UC4;
        if (lEncoding.equals("RGBA8"))  return CvType.CV_8UC4;
        if (lEncoding.equals("BGRA16")) return CvType.CV_16UC4;
        if (lEncoding.equals("RGBA16")) return CvType.CV_16UC4;

        // For bayer, return one-channel
        if (lEncoding.equals("BAYER_RGGB8")) return CvType.CV_8UC1;
        if (lEncoding.equals("BAYER_BGGR8")) return CvType.CV_8UC1;
        if (lEncoding.equals("BAYER_GBRG8")) return CvType.CV_8UC1;
        if (lEncoding.equals("BAYER_GRBG8")) return CvType.CV_8UC1;
        if (lEncoding.equals("BAYER_RGGB16")) return CvType.CV_16UC1;
        if (lEncoding.equals("BAYER_BGGR16")) return CvType.CV_16UC1;
        if (lEncoding.equals("BAYER_GBRG16")) return CvType.CV_16UC1;
        if (lEncoding.equals("BAYER_GRBG16")) return CvType.CV_16UC1;

        // Miscellaneous
        if (lEncoding.equals("YUV422")) return CvType.CV_8UC2;

        //macro code
        if (lEncoding.equals("TYPE_8UC1")) return CvType.CV_8UC1;
        if (lEncoding.equals("TYPE_8UC2")) return CvType.CV_8UC2;
        if (lEncoding.equals("TYPE_8UC3")) return CvType.CV_8UC3;
        if (lEncoding.equals("TYPE_8UC4")) return CvType.CV_8UC4;
        if (lEncoding.equals("TYPE_8SC1")) return CvType.CV_8SC1;
        if (lEncoding.equals("TYPE_8SC2")) return CvType.CV_8SC2;
        if (lEncoding.equals("TYPE_8SC3")) return CvType.CV_8SC3;
        if (lEncoding.equals("TYPE_8SC4")) return CvType.CV_8SC4;
        if (lEncoding.equals("TYPE_16UC1")) return CvType.CV_16UC1;
        if (lEncoding.equals("TYPE_16UC2")) return CvType.CV_16UC2;
        if (lEncoding.equals("TYPE_16UC3")) return CvType.CV_16UC3;
        if (lEncoding.equals("TYPE_16UC4")) return CvType.CV_16UC4;
        if (lEncoding.equals("TYPE_16SC1")) return CvType.CV_16SC1;
        if (lEncoding.equals("TYPE_16SC2")) return CvType.CV_16SC2;
        if (lEncoding.equals("TYPE_16SC3")) return CvType.CV_16SC3;
        if (lEncoding.equals("TYPE_16SC4")) return CvType.CV_16SC4;
        if (lEncoding.equals("TYPE_32SC1")) return CvType.CV_32SC1;
        if (lEncoding.equals("TYPE_32SC2")) return CvType.CV_32SC2;
        if (lEncoding.equals("TYPE_32SC3")) return CvType.CV_32SC3;
        if (lEncoding.equals("TYPE_32SC4")) return CvType.CV_32SC4;
        if (lEncoding.equals("TYPE_32FC1")) return CvType.CV_32FC1;
        if (lEncoding.equals("TYPE_32FC2")) return CvType.CV_32FC2;
        if (lEncoding.equals("TYPE_32FC3")) return CvType.CV_32FC3;
        if (lEncoding.equals("TYPE_32FC4")) return CvType.CV_32FC4;
        if (lEncoding.equals("TYPE_64FC1")) return CvType.CV_64FC1;
        if (lEncoding.equals("TYPE_64FC2")) return CvType.CV_64FC2;
        if (lEncoding.equals("TYPE_64FC3")) return CvType.CV_64FC3;
        if (lEncoding.equals("TYPE_64FC4")) return CvType.CV_64FC4;

        throw new Exception("Unrecognized image encoding [" + encoding + "]");
    }

    protected static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    @SuppressWarnings("Convert2Diamond")
    protected static Map<Pair<Encode, Encode>, Vector<Integer>> getConversionCodes() {
        Map<Pair<Encode, Encode>, Vector<Integer>> res = new HashMap<Pair<Encode, Encode>, Vector<Integer>>();

        for(int i=0; i<=5; ++i) {
            res.put(new Pair<Encode, Encode>(Encode.valueOf(i), Encode.valueOf(i)),
                    new Vector<Integer>(Arrays.asList(new Integer[]{SAME_FORMAT})));
        }

        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_GRAY2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_GRAY2BGR})));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.RGBA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_GRAY2RGBA})));
        res.put(new Pair<Encode, Encode>(Encode.GRAY, Encode.BGRA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_GRAY2BGRA})));

        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGB2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGB2BGR})));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.RGBA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGB2RGBA})));
        res.put(new Pair<Encode, Encode>(Encode.RGB, Encode.BGRA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGB2BGRA})));

        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGR2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGR2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.RGBA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGR2RGBA})));
        res.put(new Pair<Encode, Encode>(Encode.BGR, Encode.BGRA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGR2BGRA})));

        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGBA2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGBA2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGBA2BGR})));
        res.put(new Pair<Encode, Encode>(Encode.RGBA, Encode.BGRA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_RGBA2BGRA})));

        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGRA2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGRA2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGRA2BGR})));
        res.put(new Pair<Encode, Encode>(Encode.BGRA, Encode.RGBA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BGRA2RGBA})));

        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_YUV2GRAY_UYVY})));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_YUV2RGB_UYVY})));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_YUV2BGR_UYVY})));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.RGBA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_YUV2RGBA_UYVY})));
        res.put(new Pair<Encode, Encode>(Encode.YUV422, Encode.BGRA),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_YUV2BGRA_UYVY})));

        // Deal with Bayer
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerBG2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerBG2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_RGGB, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerBG2BGR})));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerRG2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerRG2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_BGGR, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerRG2BGR})));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGR2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGR2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GBRG, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGR2BGR})));

        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.GRAY),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGB2GRAY})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.RGB),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGB2RGB})));
        res.put(new Pair<Encode, Encode>(Encode.BAYER_GRBG, Encode.BGR),
                new Vector<Integer>(Arrays.asList(new Integer[]{Imgproc.COLOR_BayerGB2BGR})));

        return res;
    }

    protected static Encode getFormat(final String encoding)
    {
        String lEncoding = encoding.toUpperCase();

        if ((lEncoding.equals("MONO8")  || (lEncoding.equals("MONO8")))) return Encode.GRAY;
        if ((lEncoding.equals("BGR8")   || (lEncoding.equals("BGR8")))) return Encode.BGR;
        if ((lEncoding.equals("RGB8")   || (lEncoding.equals("RGB8")))) return Encode.RGB;
        if ((lEncoding.equals("BGRA8")  || (lEncoding.equals("BGRA8")))) return Encode.BGRA;
        if ((lEncoding.equals("RGBA8")  || (lEncoding.equals("RGBA8")))) return Encode.RGBA;
        if (lEncoding.equals("YUV422")) return Encode.YUV422;

        if ((lEncoding.equals("BAYER_RGGB8") || (lEncoding.equals("BAYER_RGGB8")))) return Encode.BAYER_RGGB;
        if ((lEncoding.equals("BAYER_BGGR8") || (lEncoding.equals("BAYER_BGGR8")))) return Encode.BAYER_BGGR;
        if ((lEncoding.equals("BAYER_GBRG8") || (lEncoding.equals("BAYER_GBRG8")))) return Encode.BAYER_GBRG;
        if ((lEncoding.equals("BAYER_GRBG8") || (lEncoding.equals("BAYER_GRBG8")))) return Encode.BAYER_GRBG;

        // We don't support conversions to/from other types
        return Encode.INVALID;
    }

    protected static Vector<Integer> getConversionCode(String src_encoding, String dst_encoding) throws Exception {
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
}