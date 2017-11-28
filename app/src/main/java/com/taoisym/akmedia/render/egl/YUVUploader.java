package com.taoisym.akmedia.render.egl;


public class YUVUploader {
//    static final int LayoutSemiplanarTiled = 0;
//    static final int LayoutSemiPlanar = 1;
//    static final int ColorspaceYUV = 0;
//    static final int LayoutPlanar = 1;
//    GLTexture texture_y;
//    GLTexture texture_uv;
//    int yHorizontalStride;
//    int yVerticalSpan;
//    int uvHorizontalStride;
//    int uvVerticalSpan;
//    int yBufferSize;
//    int uvBufferSize;
//    int yBufferOffset;
//    int uvBufferOffset;
//    int expectedFileLength;
//    int orderVU;
//    int layout;
//    int colorspace;
//
//    int numTilesX;
//    int numTilesY_Y;
//    int numTilesY_UV;
//    int width;
//    int height;
//    GLFbo fbo;
//
//    public YUVUploader(MediaFormat mediaFormat) throws IOException {
//        super(new File("file:///android_assets/yuvtexture.fs"), new File("file:///android_assets/mapping.vs_shader"));
//
//        int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
//        int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
//
//        if (mediaFormat != null) {
//            int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
//
//            switch (colorFormat) {
//
//                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
//                    planar(this);
//                    break;
//
//                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
//                    semiPlanarAligned(this, 1, 1);
//                    break;
//
////                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PlanarVU:
////                    planar(this);
////                    orderVU = 0;
////                    break;
////
////                case MediaCodecInfo.CodecCapabilities.OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka:
////                    semiPlanarTiled(this, 64, 32);
////                    break;
////
////                case MediaCodecInfo.CodecCapabilities.OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar32m:
////                    semiPlanarAligned(this, 64, 32);
////                    break;
//
//                default:
//                    planar(this);
//                    break;
//            }
//            //texture_y=new GLTexture(false,width,height);
//            //texture_uv=new GLTexture(false,width,height);
//        } else {
//            planar(this);
//        }
//    }
//
//    private int roundUp(int x, int factor) {
//        return (((x - 1) / factor) + 1) * factor;
//    }
//
//    private void semiPlanarTiled(YUVUploader in, int horizontalAlignment, int verticalAlignment) {
//        int blockSize = horizontalAlignment * verticalAlignment;
//        int blockGroupSize = blockSize * 4;
//
//        in.yBufferOffset = 0;
//
//        int absoluteTilesX = (in.width - 1) / horizontalAlignment + 1;
//        in.numTilesX = roundUp(absoluteTilesX, 2);
//        in.numTilesY_Y = (in.height - 1) / verticalAlignment + 1;
//        in.numTilesY_UV = (in.height / 2 - 1) / verticalAlignment + 1;
//
//        in.yBufferSize = numTilesY_Y * numTilesX * blockSize;
//        if (in.yBufferSize % blockGroupSize != 0) {
//            in.yBufferSize = ((in.yBufferSize - 1) / blockGroupSize + 1) * blockGroupSize;
//        }
//        in.yHorizontalStride = numTilesX * horizontalAlignment;
//        in.yVerticalSpan = in.yBufferSize / in.yHorizontalStride;
//        in.uvVerticalSpan = roundUp(in.height / 2, verticalAlignment);
//
//        in.uvBufferSize = numTilesX * numTilesY_UV * blockSize;
//        in.uvBufferOffset = in.yBufferSize;
//
//        in.expectedFileLength = in.yBufferSize + in.uvBufferSize;
//        in.orderVU = 0;
//        in.layout = LayoutSemiplanarTiled;
//        in.colorspace = ColorspaceYUV;
//    }
//
//    private void semiPlanarAligned(YUVUploader in, int horizontalAlignment, int verticalAlignment) {
//        in.yHorizontalStride = roundUp(in.width, horizontalAlignment);
//        in.yVerticalSpan = roundUp(in.height, verticalAlignment);
//        in.uvHorizontalStride = in.yHorizontalStride;
//        in.uvVerticalSpan = in.yVerticalSpan / 2;
//        in.yBufferSize = in.yHorizontalStride * in.yVerticalSpan;
//        in.uvBufferSize = in.uvHorizontalStride * in.uvVerticalSpan;
//        in.yBufferOffset = 0;
//        in.uvBufferOffset = in.yBufferSize;
//        in.expectedFileLength = in.yBufferSize + in.uvBufferSize;
//        in.orderVU = 0;
//        in.layout = LayoutSemiPlanar;
//        in.colorspace = ColorspaceYUV;
//    }
//
//    private void planar(YUVUploader in) {
//        in.yHorizontalStride = in.width;
//        in.yVerticalSpan = in.height;
//        in.yBufferSize = in.yHorizontalStride * in.yVerticalSpan;
//        in.yVerticalSpan = in.height;
//        in.uvHorizontalStride = in.width / 2;
//        in.uvVerticalSpan = in.height;
//        in.yBufferOffset = 0;
//        in.uvBufferSize = in.uvHorizontalStride * in.uvVerticalSpan;
//        in.uvBufferOffset = in.yBufferSize;
//        in.expectedFileLength = in.yBufferSize + in.uvBufferSize;
//        in.orderVU = 0;
//        in.layout = LayoutPlanar;
//        in.colorspace = ColorspaceYUV;
//    }
//
//    public void upload(ByteBuffer image) {
//        GLES20.glUseProgram(this.id);
//        YUVUploader descriptor = this;
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_y.id);
//        image.rewind();
//
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
//                descriptor.yHorizontalStride, descriptor.yVerticalSpan, 0,
//                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, image);
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_uv.id);
//
//        image.position(descriptor.uvBufferOffset);
//
//        if (descriptor.layout == LayoutSemiPlanar
//                || descriptor.layout == LayoutSemiplanarTiled) {
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA,
//                    descriptor.uvHorizontalStride / 2, descriptor.uvVerticalSpan, 0,
//                    GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, image);
//        } else {//planar
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
//                    descriptor.uvHorizontalStride, descriptor.uvVerticalSpan, 0,
//                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, image);
//        }
//
//    }
}
