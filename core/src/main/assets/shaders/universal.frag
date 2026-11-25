/*
 * Author: DenySilver (Reforged Pixel Dungeon)
 * License: GPLv3
 */

#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
precision highp int;
#else
precision mediump float;
precision mediump int;
#endif
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_fisheyeAmount;

uniform float u_distortionAmount;
uniform float u_time;

uniform float u_chromaticAmount;

uniform float u_saturation;
uniform vec3 u_tintColor;
uniform float u_tintAmount;

uniform float u_noiseAmount;
uniform float u_seed;

uniform float u_vignetteIntensity;
uniform vec3 u_vignetteColor;

float rand(vec2 co) {
    #ifdef GL_ES
    vec3 coord = floor(gl_FragCoord.xyx / 2.0);
    #else
    vec3 coord = gl_FragCoord.xyx;
    #endif
    vec3 p3 = fract(coord * 0.1031);
    p3 += fract(u_seed * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec2 uv = v_texCoords;

    // fisheye
    if (u_fisheyeAmount > 0.0001) {
        vec2 xy = 2.0 * uv - 1.0;
        float d = length(xy);
        xy *= 1.0 + u_fisheyeAmount * (d * d);
        uv = (xy + 1.0) * 0.5;
    }

    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    // distortion
    if (u_distortionAmount > 0.0001) {
        uv.x += sin(uv.y * 20.0 + u_time * 4.0) * u_distortionAmount;
        uv.y += cos(uv.x * 20.0 + u_time * 4.0) * u_distortionAmount;
    }

    // chromatic aberration
    vec4 color;
    if (u_chromaticAmount > 0.0001) {
        vec2 offset = (uv - vec2(0.5)) * u_chromaticAmount;
        float r = texture2D(u_texture, uv + offset).r;
        float g = texture2D(u_texture, uv).g;
        float b = texture2D(u_texture, uv - offset).b;
        float a = texture2D(u_texture, uv).a;
        color = vec4(r, g, b, a);
    } else {
        color = texture2D(u_texture, uv);
    }

    // saturation
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 satColor = mix(vec3(gray), color.rgb, u_saturation);

    // tint
    vec3 tintedColor = satColor * u_tintColor;
    vec3 finalColor = mix(satColor, tintedColor, u_tintAmount);

    // noise
    if (u_noiseAmount > 0.001) {
        float grain = (rand(uv) - 0.5) * u_noiseAmount;
        finalColor += vec3(grain);
    }

    // vignette
    if (u_vignetteIntensity > 0.001) {
        vec2 vignetteUV = uv - vec2(0.5);
        float dist = length(vignetteUV);
        float vignette = smoothstep(0.2, 0.7, dist * (u_vignetteIntensity + 0.5));
        vec3 baseVignetteColor = mix(u_vignetteColor, vec3(0.0), 1.0 - vignette);
        finalColor = mix(finalColor, baseVignetteColor, vignette * u_vignetteIntensity);
    }

    gl_FragColor = vec4(finalColor, color.a);
}