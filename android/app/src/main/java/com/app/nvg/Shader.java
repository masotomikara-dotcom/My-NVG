package com.app.nvg;

public class Shader {
    
    public static final String VERTEX_SHADER =
        "attribute vec4 position;\n" +
        "attribute vec4 inputTextureCoordinate;\n" +
        "varying vec2 textureCoordinate;\n" +
        "uniform mat4 uSTMatrix;\n" +
        "void main() {\n" +
        "    gl_Position = position;\n" +
        "    textureCoordinate = (uSTMatrix * inputTextureCoordinate).xy;\n" +
        "}\n";

    public static final String FRAGMENT_SHADER =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;\n" +
        "varying vec2 textureCoordinate;\n" +
        "uniform samplerExternalOES videoTex;\n" +
        "uniform int uMode;\n" +
        "uniform float uExposure;\n" +
        
        "void main() {\n" +
        "    vec4 color = texture2D(videoTex, textureCoordinate);\n" +
        "    \n" +
        "    if (uMode > 0) {\n" +
        "        float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));\n" +
        "        \n" +
        "        float exp_value = uExposure;\n" +
        "        if (exp_value < 1.0) { exp_value = 1.0; }\n" +
        "        \n" +
        "        float final_brightness = pow(brightness, 1.0 / (exp_value * 0.35));\n" +
        "        \n" +
        "        if (brightness < 0.08) {\n" +
        "            final_brightness += 0.15 * (exp_value / 4.0);\n" +
        "        }\n" +
        "        \n" +
        "        if (final_brightness > 0.95) {\n" +
        "            final_brightness = 1.0;\n" +
        "        }\n" +
        "        \n" +
        "        if (uMode == 1) {\n" +
        "            float red_blue = final_brightness - 0.3;\n" +
        "            if (red_blue < 0.0) { red_blue = 0.0; }\n" +
        "            \n" +
        "            float green = final_brightness * 1.3;\n" +
        "            color.rgb = vec3(red_blue, green, red_blue);\n" +
        "            \n" +
        "        } else if (uMode == 2) {\n" +
        "            float red = final_brightness - 0.2;\n" +
        "            if (red < 0.0) { red = 0.0; }\n" +
        "            \n" +
        "            float green_blue = final_brightness * 1.2;\n" +
        "            color.rgb = vec3(red, green_blue * 0.9, green_blue);\n" +
        "        }\n" +
        "    }\n" +
        "    gl_FragColor = color;\n" +
        "}\n";
}