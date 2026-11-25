package com.watabou.noosa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.watabou.utils.Random;

public class PostProcessingManager {

    public static PostProcessingManager instance = new PostProcessingManager();

    private ShaderProgram fisheyeShader;
    private ShaderProgram universalShader;

    public boolean enabled = true;
    public float baseSaturation = 1.0f;

    public float poisonIntensity = 0f;
    public float poisonDistortion = 0f;

    public float saturation = 1f;
    public float vignetteIntensity = 0f;
    public float vignetteRed = 0f;
    public float tintAmount = 0f;
    public float tintR = 1f, tintG = 1f, tintB = 1f;

    public float starvingFlash = 0f;
    public float teleportFlash = 0f;

    public float chromaticDistortion = 0f;
    public float fisheyeAmount = 0f;
    public float corrosionNoise = 0f;

    public int dynamicLighting = 2;
    public boolean waterReflections = true;
    public boolean bloomEnabled = true;

    public boolean testMode = true;

    public static void triggerStarvingFlash() {
        if (!instance.enabled) return;
        instance.starvingFlash = 0.85f;
    }

    public static void triggerTeleport() {
        if (!instance.enabled) return;
        instance.teleportFlash = 0.16f;
    }

    public void resetStatusEffects() {
        poisonIntensity = 0f;
        poisonDistortion = 0f;
        saturation = baseSaturation;
        vignetteIntensity = 0f;
        vignetteRed = 0f;
        tintAmount = 0f;
        starvingFlash = 0f;
        teleportFlash = 0f;
        chromaticDistortion = 0f;
        fisheyeAmount = 0f;
        corrosionNoise = 0f;
    }

    public void update(float delta) {
        if (!enabled) {
            starvingFlash = 0f;
            teleportFlash = 0f;
            return;
        }

        if (starvingFlash > 0f) {
            starvingFlash -= delta * 2.0f;
            if (starvingFlash < 0f) starvingFlash = 0f;
        }

        if (teleportFlash > 0f) {
            teleportFlash -= delta * 0.18f;
            if (teleportFlash < 0f) teleportFlash = 0f;
        }
    }

    public void init(int width, int height) {
        if (universalShader != null) universalShader.dispose();
        if (fisheyeShader != null) fisheyeShader.dispose();

        universalShader = loadShader("shaders/blur.vert", "shaders/universal.frag");
        fisheyeShader = loadShader("shaders/blur.vert", "shaders/fisheye.frag");
    }

    private ShaderProgram loadShader(String vertPath, String fragPath) {
        ShaderProgram shader = new ShaderProgram(Gdx.files.internal(vertPath), Gdx.files.internal(fragPath));
        if (!shader.isCompiled()) {
            Gdx.app.error("ShaderError", shader.getLog());
        }
        return shader;
    }

    public ShaderProgram getActiveShader() {
        float activeSaturation = enabled ? saturation : baseSaturation;
        boolean hasCustomSaturation = Math.abs(activeSaturation - 1.0f) > 0.001f;

        if (!enabled) {
            return hasCustomSaturation ? universalShader : null;
        }

        float activeChromatic = chromaticDistortion + teleportFlash;

        if (hasCustomSaturation || tintAmount > 0.001f || fisheyeAmount > 0.001f ||
                corrosionNoise > 0.001f || activeChromatic > 0.0001f ||
                poisonDistortion > 0.0001f || vignetteIntensity > 0.001f || starvingFlash > 0.001f) {
            return universalShader;
        }
        return null;
    }

    public void bindUniforms(ShaderProgram shader) {
        if (shader == universalShader) {
            if (!enabled) {
                setUniformIfHas(shader, "u_fisheyeAmount", 0f);
                setUniformIfHas(shader, "u_saturation", baseSaturation);
                setUniformIfHas(shader, "u_vignetteIntensity", 0f);
                setUniformIfHas(shader, "u_tintAmount", 0f);
                setUniformIfHas(shader, "u_noiseAmount", 0f);
                setUniformIfHas(shader, "u_chromaticAmount", 0f);
                setUniformIfHas(shader, "u_distortionAmount", 0f);
                return;
            }

            setUniformIfHas(shader, "u_fisheyeAmount", fisheyeAmount);
            setUniformIfHas(shader, "u_saturation", saturation);

            float currentVignette = Math.max(vignetteIntensity, starvingFlash);
            setUniformIfHas(shader, "u_vignetteIntensity", currentVignette);

            if (shader.hasUniform("u_vignetteColor")) {
                float r = (vignetteIntensity > 0f) ? vignetteRed : 0f;
                shader.setUniformf("u_vignetteColor", r, 0f, 0f);
            }

            if (shader.hasUniform("u_tintColor")) {
                shader.setUniformf("u_tintColor", tintR, tintG, tintB);
            }
            setUniformIfHas(shader, "u_tintAmount", tintAmount);

            setUniformIfHas(shader, "u_noiseAmount", corrosionNoise);
            setUniformIfHas(shader, "u_seed", Random.Float(100f));

            setUniformIfHas(shader, "u_chromaticAmount", chromaticDistortion + teleportFlash);
            setUniformIfHas(shader, "u_distortionAmount", poisonDistortion);
            setUniformIfHas(shader, "u_time", Game.timeTotal % 62.831853f);
        }
    }

    private void setUniformIfHas(ShaderProgram shader, String name, float value) {
        if (shader.hasUniform(name)) {
            shader.setUniformf(name, value);
        }
    }
}